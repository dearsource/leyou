package com.leyou.item.mapper;

import com.leyou.item.entity.TbCategory;
import com.leyou.item.entity.TbCategoryBrand;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 商品分类和品牌的中间表，两者是多对多关系 Mapper 接口
 * </p>
 *
 * @author HeiMa
 * @since 2019-07-12
 */
public interface TbCategoryBrandMapper extends BaseMapper<TbCategoryBrand> {


}
