package com.wms.modules.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.inventory.service.InventoryService;
import com.wms.modules.outbound.entity.OutboundDetail;
import com.wms.modules.outbound.entity.OutboundOrder;
import com.wms.modules.outbound.mapper.OutboundOrderMapper;
import com.wms.modules.outbound.service.OutboundDetailService;
import com.wms.modules.outbound.service.OutboundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder> implements OutboundOrderService {

    @Autowired
    private OutboundDetailService outboundDetailService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @Transactional
    public OutboundOrder createOrder(OutboundOrder order) {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
            order.setOrderNo(generateOrderNo());
        }
        if (order.getStatus() == null) {
            order.setStatus(0);
        }
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        
        // 计算总数量和总金额
        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            int totalQuantity = order.getDetails().stream()
                    .mapToInt(OutboundDetail::getQuantity)
                    .sum();
            order.setTotalQuantity(totalQuantity);
        }
        
        save(order);
        
        // 保存明细
        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            for (OutboundDetail detail : order.getDetails()) {
                detail.setOrderId(order.getId());
                outboundDetailService.save(detail);
            }
        }
        
        return order;
    }

    @Override
    @Transactional
    public OutboundOrder updateOrder(OutboundOrder order) {
        order.setUpdateTime(LocalDateTime.now());
        
        // 计算总数量和总金额
        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            int totalQuantity = order.getDetails().stream()
                    .mapToInt(OutboundDetail::getQuantity)
                    .sum();
            order.setTotalQuantity(totalQuantity);
        }
        
        updateById(order);
        
        // 更新明细：先删除旧明细，再保存新明细
        if (order.getDetails() != null) {
            // 删除旧明细
            outboundDetailService.remove(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OutboundDetail>()
                    .eq(OutboundDetail::getOrderId, order.getId())
            );
            
            // 保存新明细
            for (OutboundDetail detail : order.getDetails()) {
                detail.setOrderId(order.getId());
                outboundDetailService.save(detail);
            }
        }
        
        return order;
    }

    @Override
    @Transactional
    public boolean deleteOrder(Long id) {
        OutboundOrder order = getOrderById(id);
        if (order == null) {
            return false;
        }
        
        // 如果出库单已批准，需要恢复库存
        if (order.getStatus() == 1 && order.getDetails() != null && !order.getDetails().isEmpty()) {
            for (OutboundDetail detail : order.getDetails()) {
                if (detail.getMaterialId() != null && detail.getQuantity() != null) {
                    inventoryService.addStock(detail.getMaterialId(), detail.getQuantity(), order.getRecipientName());
                }
            }
        }
        
        // 删除出库明细
        outboundDetailService.remove(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OutboundDetail>()
                .eq(OutboundDetail::getOrderId, id)
        );
        
        // 删除出库单
        return removeById(id);
    }

    @Override
    public OutboundOrder getOrderById(Long id) {
        OutboundOrder order = getById(id);
        if (order != null) {
            // 加载出库明细
            List<OutboundDetail> details = outboundDetailService.getDetailsByOrderId(id);
            order.setDetails(details);
        }
        return order;
    }

    @Override
    public List<OutboundOrder> getOrderList(String keyword, Integer status) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(OutboundOrder::getOrderNo, keyword)
                    .or()
                    .like(OutboundOrder::getRecipientName, keyword));
        }
        
        if (status != null) {
            wrapper.eq(OutboundOrder::getStatus, status);
        }
        
        wrapper.orderByDesc(OutboundOrder::getCreateTime);
        return list(wrapper);
    }

    private String generateOrderNo() {
        String prefix = "OUT";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + timestamp;
    }

    @Override
    @Transactional
    public OutboundOrder approveOrder(Long id) {
        OutboundOrder order = getOrderById(id);
        if (order == null) {
            return null;
        }
        
        if (order.getStatus() != 0) {
            return null;
        }
        
        // 获取出库明细，减少库存
        if (order.getDetails() != null && !order.getDetails().isEmpty()) {
            System.out.println("出库明细数量: " + order.getDetails().size());
            for (OutboundDetail detail : order.getDetails()) {
                System.out.println("物料ID: " + detail.getMaterialId() + ", 数量: " + detail.getQuantity());
                if (detail.getMaterialId() != null && detail.getQuantity() != null) {
                    boolean result = inventoryService.reduceStock(detail.getMaterialId(), detail.getQuantity(), order.getRecipientName());
                    System.out.println("减少库存结果: " + result);
                }
            }
        } else {
            System.out.println("出库明细为空");
        }
        
        order.setStatus(1);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        
        return order;
    }
}
