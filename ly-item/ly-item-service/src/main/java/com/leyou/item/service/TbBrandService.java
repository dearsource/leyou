package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.pojo.BrandDTO;

import java.util.List;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
public interface TbBrandService extends IService<TbBrand> {

    PageResult<BrandDTO> findBrandList(String key, Integer page, Integer rows, String sortBy, Boolean desc);

    void saveBrand(TbBrand tbBrand, List<Long> cids);

    List<BrandDTO> findByCategoryId(Long cid);
}
