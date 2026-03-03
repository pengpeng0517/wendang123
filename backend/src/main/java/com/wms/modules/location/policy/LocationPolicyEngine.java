package com.wms.modules.location.policy;

import com.wms.modules.location.dto.PolicyDecision;
import com.wms.modules.location.enums.PolicyType;

public interface LocationPolicyEngine {

    PolicyType getPolicyType();

    PolicyDecision decide(LocationPolicyContext context);
}
