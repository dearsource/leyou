package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.item.entity.TbSku;
import com.leyou.item.mapper.TbSkuMapper;
import com.leyou.item.service.TbSkuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * <p>
 * sku表,该表表示具体的商品实体,如黑色的 64g的iphone 8 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
@Service
public class TbSkuServiceImpl extends ServiceImpl<TbSkuMapper, TbSku> implements TbSkuService {

    @Override
    @Transactional
    public void minusStock(Map<Long, Integer> skuIdNumMap) {
        for (Long skuId : skuIdNumMap.keySet()) {
            Integer num = skuIdNumMap.get(skuId);
            int count = this.getBaseMapper().minusStock(skuId,num);
            if(count != 1){
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH_ERROR);
            }
        }

    }

    @Override
    @Transactional
    public void plusStock(Map<Long, Integer> skuMap) {
        for (Long skuId : skuMap.keySet()) {
            Integer num = skuMap.get(skuId);
            int count = this.getBaseMapper().plusStock(skuId, num);
            if(count != 1){
                throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
            }
        }
    }
}
