package com.leyou.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.entity.TbSku;

import java.util.Map;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 服务类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
public interface TbSkuService extends IService<TbSku> {

    void minusStock(Map<Long, Integer> skuIdNumMap);

    void plusStock(Map<Long, Integer> skuMap);
}
