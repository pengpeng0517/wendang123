package com.wms.modules.outbound.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.modules.outbound.entity.OutboundOrder;

import java.util.List;

public interface OutboundOrderService extends IService<OutboundOrder> {

    OutboundOrder createOrder(OutboundOrder order);

    OutboundOrder updateOrder(OutboundOrder order);

    boolean deleteOrder(Long id);

    OutboundOrder getOrderById(Long id);

    List<OutboundOrder> getOrderList(String keyword, Integer status);

    OutboundOrder approveOrder(Long id);
}
