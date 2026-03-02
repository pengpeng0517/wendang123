package com.wms.modules.outbound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.outbound.entity.OutboundDetail;

import java.util.List;

public interface OutboundDetailService extends IService<OutboundDetail> {

    OutboundDetail createDetail(OutboundDetail detail);

    OutboundDetail updateDetail(OutboundDetail detail);

    boolean deleteDetail(Long id);

    OutboundDetail getDetailById(Long id);

    List<OutboundDetail> getDetailsByOrderId(Long orderId);
}
