package com.wms.modules.location.policy;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.modules.location.dto.LocationRecommendationResponse;
import com.wms.modules.location.dto.PolicyDecision;
import com.wms.modules.location.entity.WarehouseLocation;
import com.wms.modules.location.enums.PolicyType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OnnxPolicyEngine implements LocationPolicyEngine {

    @Autowired
    private FeatureEncoder featureEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile OrtEnvironment environment;
    private volatile OrtSession session;

    private volatile String loadedModelPath;
    private volatile String loadedModelVersion = "none";
    private volatile String loadedMessage = "model not loaded";
    private volatile LocalDateTime loadedAt;

    @Override
    public PolicyType getPolicyType() {
        return PolicyType.RL;
    }

    @Override
    public PolicyDecision decide(LocationPolicyContext context) {
        OrtSession localSession = session;
        OrtEnvironment localEnvironment = environment;
        if (localSession == null || localEnvironment == null) {
            return null;
        }

        List<WarehouseLocation> candidates = selectCandidates(context);
        if (candidates.isEmpty()) {
            return null;
        }

        String inputName = localSession.getInputNames().iterator().next();
        List<ScoredCandidate> scored = new ArrayList<ScoredCandidate>();

        for (WarehouseLocation candidate : candidates) {
            float[] features = featureEncoder.encode(context, candidate);
            float score = runInference(localEnvironment, localSession, inputName, features);
            if (Float.isNaN(score) || Float.isInfinite(score)) {
                continue;
            }
            scored.add(new ScoredCandidate(candidate, score));
        }

        if (scored.isEmpty()) {
            return null;
        }

        scored.sort(Comparator.comparing(ScoredCandidate::getScore).reversed());
        ScoredCandidate best = scored.get(0);

        PolicyDecision decision = new PolicyDecision();
        decision.setLocationCode(best.getLocation().getCode());
        decision.setCapacity(safeInt(best.getLocation().getCapacity()));
        decision.setCurrentLoad(safeInt(best.getLocation().getCurrentLoad()));
        decision.setAvailableCapacity(safeInt(best.getLocation().getCapacity()) - safeInt(best.getLocation().getCurrentLoad()));
        decision.setScore(round(best.getScore()));
        decision.setConfidence(round(calculateConfidence(scored)));
        decision.setReason("ONNX DQN 评分选择，候选集合内得分最高");
        decision.setPolicyType(PolicyType.RL);
        decision.setPolicyVersion(loadedModelVersion == null ? "onnx-unknown" : loadedModelVersion);
        decision.setCandidates(toCandidates(scored));
        return decision;
    }

    public synchronized boolean reload(String modelPath, String modelVersion) {
        String finalPath = normalize(modelPath);
        if (finalPath == null) {
            loadedMessage = "model path is empty";
            return false;
        }

        File modelFile = new File(finalPath);
        if (!modelFile.exists() || !modelFile.isFile()) {
            loadedMessage = "model file not found: " + finalPath;
            return false;
        }

        OrtSession oldSession = session;

        try {
            OrtEnvironment newEnvironment = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            OrtSession newSession = newEnvironment.createSession(finalPath, options);

            String metadataVersion = loadVersionFromMetadata(modelFile);
            session = newSession;
            environment = newEnvironment;
            loadedModelPath = finalPath;
            loadedModelVersion = normalize(modelVersion);
            if (loadedModelVersion == null) {
                loadedModelVersion = metadataVersion == null ? "onnx-v1" : metadataVersion;
            }
            loadedAt = LocalDateTime.now();
            loadedMessage = "ok";

            if (oldSession != null) {
                oldSession.close();
            }
            return true;
        } catch (Exception e) {
            loadedMessage = "reload failed: " + e.getMessage();
            return false;
        }
    }

    public synchronized void clear() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception ignored) {
            }
        }
        session = null;
        environment = null;
        loadedModelPath = null;
        loadedModelVersion = "none";
        loadedMessage = "model cleared";
        loadedAt = LocalDateTime.now();
    }

    public OnnxModelStatus getStatus() {
        OnnxModelStatus status = new OnnxModelStatus();
        status.setLoaded(session != null);
        status.setModelPath(loadedModelPath);
        status.setModelVersion(loadedModelVersion);
        status.setMessage(loadedMessage);
        status.setLastLoadTime(loadedAt);
        return status;
    }

    private List<WarehouseLocation> selectCandidates(LocationPolicyContext context) {
        List<WarehouseLocation> active = context.getActiveLocations();
        if (active == null || active.isEmpty()) {
            return Collections.emptyList();
        }

        List<WarehouseLocation> tempMatched = active.stream()
                .filter(location -> matchTemperature(location, context.getTemperatureLevel()))
                .collect(Collectors.toList());

        if (tempMatched.isEmpty()) {
            tempMatched = active;
        }

        int inboundQty = Math.max(1, context.getInboundQuantity());
        List<WarehouseLocation> capacityMatched = tempMatched.stream()
                .filter(location -> getAvailableCapacity(location) >= inboundQty)
                .collect(Collectors.toList());

        if (capacityMatched.isEmpty()) {
            return tempMatched;
        }

        return capacityMatched;
    }

    private float runInference(OrtEnvironment localEnvironment,
                               OrtSession localSession,
                               String inputName,
                               float[] features) {
        try (OnnxTensor tensor = OnnxTensor.createTensor(localEnvironment, new float[][]{features});
             OrtSession.Result result = localSession.run(Collections.singletonMap(inputName, tensor))) {

            if (!result.iterator().hasNext()) {
                return Float.NaN;
            }

            Object output = result.iterator().next().getValue();
            return extractScore(output);
        } catch (Exception e) {
            loadedMessage = "inference failed: " + e.getMessage();
            return Float.NaN;
        }
    }

    private float extractScore(Object output) throws OrtException {
        if (output == null) {
            return Float.NaN;
        }
        if (output instanceof float[]) {
            float[] values = (float[]) output;
            return values.length == 0 ? Float.NaN : values[0];
        }
        if (output instanceof float[][]) {
            float[][] values = (float[][]) output;
            return values.length == 0 || values[0].length == 0 ? Float.NaN : values[0][0];
        }
        if (output instanceof double[]) {
            double[] values = (double[]) output;
            return values.length == 0 ? Float.NaN : (float) values[0];
        }
        if (output instanceof double[][]) {
            double[][] values = (double[][]) output;
            return values.length == 0 || values[0].length == 0 ? Float.NaN : (float) values[0][0];
        }
        if (output instanceof long[]) {
            long[] values = (long[]) output;
            return values.length == 0 ? Float.NaN : (float) values[0];
        }
        if (output instanceof long[][]) {
            long[][] values = (long[][]) output;
            return values.length == 0 || values[0].length == 0 ? Float.NaN : (float) values[0][0];
        }
        return Float.NaN;
    }

    private List<LocationRecommendationResponse.Candidate> toCandidates(List<ScoredCandidate> scored) {
        List<LocationRecommendationResponse.Candidate> candidates = new ArrayList<LocationRecommendationResponse.Candidate>();
        for (ScoredCandidate item : scored.stream().limit(3).collect(Collectors.toList())) {
            WarehouseLocation location = item.getLocation();
            LocationRecommendationResponse.Candidate candidate = new LocationRecommendationResponse.Candidate();
            candidate.setLocationCode(location.getCode());
            candidate.setCapacity(safeInt(location.getCapacity()));
            candidate.setCurrentLoad(safeInt(location.getCurrentLoad()));
            candidate.setAvailableCapacity(getAvailableCapacity(location));
            candidate.setScore(round(item.getScore()));
            candidate.setReason("ONNX评分=" + round(item.getScore()));
            candidates.add(candidate);
        }
        return candidates;
    }

    private double calculateConfidence(List<ScoredCandidate> scored) {
        if (scored.size() == 1) {
            return 0.8;
        }
        double first = scored.get(0).getScore();
        double second = scored.get(1).getScore();
        double max = Math.max(first, second);
        double firstExp = Math.exp(first - max);
        double secondExp = Math.exp(second - max);
        double total = firstExp + secondExp;
        if (total <= 0.0) {
            return 0.5;
        }
        return firstExp / total;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int getAvailableCapacity(WarehouseLocation location) {
        return safeInt(location.getCapacity()) - safeInt(location.getCurrentLoad());
    }

    private boolean matchTemperature(WarehouseLocation location, String requestTemperature) {
        if (requestTemperature == null || requestTemperature.trim().isEmpty()) {
            return true;
        }
        String level = normalize(location.getTemperatureLevel());
        if (level == null) {
            return true;
        }
        return level.equalsIgnoreCase(requestTemperature.trim());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String loadVersionFromMetadata(File modelFile) {
        File metadataFile = new File(modelFile.getParentFile(), "metadata.json");
        if (!metadataFile.exists() || !metadataFile.isFile()) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(metadataFile.toPath());
            JsonNode root = objectMapper.readTree(bytes);
            JsonNode versionNode = root.get("version");
            if (versionNode != null && !versionNode.isNull()) {
                return versionNode.asText();
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private static class ScoredCandidate {
        private final WarehouseLocation location;
        private final double score;

        private ScoredCandidate(WarehouseLocation location, double score) {
            this.location = location;
            this.score = score;
        }

        public WarehouseLocation getLocation() {
            return location;
        }

        public double getScore() {
            return score;
        }
    }

    public static class OnnxModelStatus {
        private boolean loaded;
        private String modelPath;
        private String modelVersion;
        private String message;
        private LocalDateTime lastLoadTime;

        public boolean isLoaded() {
            return loaded;
        }

        public void setLoaded(boolean loaded) {
            this.loaded = loaded;
        }

        public String getModelPath() {
            return modelPath;
        }

        public void setModelPath(String modelPath) {
            this.modelPath = modelPath;
        }

        public String getModelVersion() {
            return modelVersion;
        }

        public void setModelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getLastLoadTime() {
            return lastLoadTime;
        }

        public void setLastLoadTime(LocalDateTime lastLoadTime) {
            this.lastLoadTime = lastLoadTime;
        }
    }
}
