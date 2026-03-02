package com.wms.modules.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.modules.material.entity.Material;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialMapper extends BaseMapper<Material> {
}
