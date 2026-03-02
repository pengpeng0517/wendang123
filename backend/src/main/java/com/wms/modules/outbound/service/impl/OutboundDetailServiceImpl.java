package com.wms.modules.outbound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.modules.outbound.entity.OutboundDetail;
import com.wms.modules.outbound.mapper.OutboundDetailMapper;
import com.wms.modules.outbound.service.OutboundDetailService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboundDetailServiceImpl extends ServiceImpl<OutboundDetailMapper, OutboundDetail> implements OutboundDetailService {

    @Override
    public OutboundDetail createDetail(OutboundDetail detail) {
        if (detail.getPrice() != null && detail.getQuantity() != null) {
            detail.setAmount(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
        }
        if (detail.getStatus() == null) {
            detail.setStatus(0);
        }
        detail.setCreateTime(LocalDateTime.now());
        detail.setUpdateTime(LocalDateTime.now());
        save(detail);
        return detail;
    }

    @Override
    public OutboundDetail updateDetail(OutboundDetail detail) {
        if (detail.getPrice() != null && detail.getQuantity() != null) {
            detail.setAmount(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
        }
        detail.setUpdateTime(LocalDateTime.now());
        updateById(detail);
        return detail;
    }

    @Override
    public boolean deleteDetail(Long id) {
        return removeById(id);
    }

    @Override
    public OutboundDetail getDetailById(Long id) {
        return getById(id);
    }

    @Override
    public List<OutboundDetail> getDetailsByOrderId(Long orderId) {
        LambdaQueryWrapper<OutboundDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundDetail::getOrderId, orderId);
        wrapper.orderByAsc(OutboundDetail::getId);
        return list(wrapper);
    }
}
