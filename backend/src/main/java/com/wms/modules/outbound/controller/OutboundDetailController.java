package com.wms.modules.outbound.controller;

import com.wms.common.result.Result;
import com.wms.modules.outbound.entity.OutboundDetail;
import com.wms.modules.outbound.service.OutboundDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound/detail")
public class OutboundDetailController {

    @Autowired
    private OutboundDetailService outboundDetailService;

    @GetMapping("/list/{orderId}")
    public Result<List<OutboundDetail>> listByOrderId(@PathVariable Long orderId) {
        List<OutboundDetail> list = outboundDetailService.getDetailsByOrderId(orderId);
        return Result.success(list);
    }

    @GetMapping("/info/{id}")
    public Result<OutboundDetail> info(@PathVariable Long id) {
        OutboundDetail detail = outboundDetailService.getDetailById(id);
        return Result.success(detail);
    }

    @PostMapping("/add")
    public Result<OutboundDetail> add(@RequestBody OutboundDetail detail) {
        OutboundDetail createdDetail = outboundDetailService.createDetail(detail);
        return Result.success(createdDetail);
    }

    @PutMapping("/update")
    public Result<OutboundDetail> update(@RequestBody OutboundDetail detail) {
        OutboundDetail updatedDetail = outboundDetailService.updateDetail(detail);
        return Result.success(updatedDetail);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = outboundDetailService.deleteDetail(id);
        if (deleted) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }
}
