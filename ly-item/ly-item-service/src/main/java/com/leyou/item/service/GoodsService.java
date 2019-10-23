package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.Exception.LyException;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.*;
import com.leyou.item.pojo.SkuDTO;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.item.pojo.SpuDetailDTO;
import net.bytebuddy.asm.Advice;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private TbSpuService spuService;
    @Autowired
    private TbCategoryService categoryService;
    @Autowired
    private TbBrandService brandService;
    public PageResult<SpuDTO> findSpu(Integer page, Integer rows, String key, Boolean saleable) {
        Page<TbSpu> page1 = new Page<>(page, rows);
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            queryWrapper.lambda().like(TbSpu::getName,key);
        }
        if(saleable != null){
            queryWrapper.lambda().eq(TbSpu::getSaleable,saleable);
        }

        IPage<TbSpu> tbSpuIPage = spuService.page(page1, queryWrapper);
        if(tbSpuIPage ==null || CollectionUtils.isEmpty(tbSpuIPage.getRecords())){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(tbSpuIPage.getRecords(), SpuDTO.class);
        handlerCategoryAndBrandName(spuDTOList);
        return new PageResult<>(tbSpuIPage.getTotal(),tbSpuIPage.getPages(),spuDTOList);
    }

    private void handlerCategoryAndBrandName(List<SpuDTO> spuDTOList) {
        for (SpuDTO spuDTO : spuDTOList) {
            List<Long> categoryIds = spuDTO.getCategoryIds();
            Collection<TbCategory> tbCategories = categoryService.listByIds(categoryIds);
            String categoryName = tbCategories.stream().map(TbCategory::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryName);
            //处理品牌名称
            TbBrand tbBrand = brandService.getById(spuDTO.getBrandId());
            spuDTO.setBrandName(tbBrand.getName());
        }
    }

    @Autowired
    private TbSpuDetailService spuDetailService;
    @Autowired
    private TbSkuService skuService;
    /**
     * 保存商品
     * @param spuDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveGoods(SpuDTO spuDTO) {
        //保存spu表
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        boolean bSpu = spuService.save(tbSpu);
        if(!bSpu){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        Long spuId = tbSpu.getId();
        //保存detail
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDetailDTO, TbSpuDetail.class);
        tbSpuDetail.setSpuId(spuId);
//        保存spuDetail
        boolean bDetail = spuDetailService.save(tbSpuDetail);
        if(!bDetail){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//        保存sku
        List<SkuDTO> skus = spuDTO.getSkus();

        List<TbSku> tbSkuList = skus.stream().map(skuDTO -> {
            skuDTO.setSpuId(spuId);
            return BeanHelper.copyProperties(skuDTO, TbSku.class);
        }).collect(Collectors.toList());

//        List<TbSku> tbSkus = BeanHelper.copyWithCollection(skus, TbSku.class);
//        for (TbSku tbSku : tbSkus) {
//            tbSku.setSpuId(spuId);
//        }
        boolean bSku = skuService.saveBatch(tbSkuList);
        if(!bSku){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * 更新上下架
     * @param spuId
     * @param saleable
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleable(Long spuId, Boolean saleable) {
        //跟新spu
        TbSpu tbSpu = new TbSpu().setId(spuId).setSaleable(saleable);
        boolean bSpu = spuService.updateById(tbSpu);
        if(!bSpu){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //更新sku
        UpdateWrapper<TbSku> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbSku::getSpuId,spuId);
        updateWrapper.lambda().set(TbSku::getEnable,saleable);
        boolean bSku = skuService.update(updateWrapper);
        if(!bSku){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //发送消息到中间件
        String itemSaleable = MQConstants.RoutingKey.ITEM_DOWN_KEY;
        if(saleable){
            itemSaleable = MQConstants.RoutingKey.ITEM_UP_KEY;
        }
        amqpTemplate.convertAndSend(MQConstants.Exchange.ITEM_EXCHANGE_NAME, itemSaleable,spuId);
    }

    public SpuDetailDTO findSpuDetail(Long spuId) {
        TbSpuDetail tbSpuDetail = spuDetailService.getById(spuId);
        if(tbSpuDetail == null){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(tbSpuDetail,SpuDetailDTO.class);
    }

    public List<SkuDTO> findSkuListBySpuId(Long spuId) {
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        List<TbSku> tbSkuList = skuService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbSkuList)){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSkuList,SkuDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(SpuDTO spuDTO) {
        //修改spu表
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        boolean bSpu = spuService.updateById(tbSpu);
        if(!bSpu){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//        修改spudetail表
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDetailDTO, TbSpuDetail.class);
        boolean bDetail = spuDetailService.updateById(tbSpuDetail);
        if(!bDetail){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        Long spuId = spuDTO.getId();
//        删除sku
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        boolean bRemoveSku = skuService.remove(queryWrapper);
        if(!bRemoveSku){
            throw  new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
//        新增sku
        List<SkuDTO> skus = spuDTO.getSkus();
        List<TbSku> tbSkuList = skus.stream().map(skuDTO -> {
            skuDTO.setSpuId(spuId);
            return BeanHelper.copyProperties(skuDTO, TbSku.class);
        }).collect(Collectors.toList());
        boolean bSaveSku = skuService.saveBatch(tbSkuList);
        if(!bSaveSku){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    public SpuDTO findSpuById(Long spuId) {
        TbSpu tbSpu = spuService.getById(spuId);
        SpuDTO spuDTO = BeanHelper.copyProperties(tbSpu, SpuDTO.class);
        return spuDTO;
    }

    public List<SkuDTO> findSkuListByIds(List<Long> ids) {

        Collection<TbSku> tbSkuCollection = skuService.listByIds(ids);
        if(CollectionUtils.isEmpty(tbSkuCollection)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SkuDTO> skuDTOList = tbSkuCollection.stream().map(tbSku -> {
            return BeanHelper.copyProperties(tbSku, SkuDTO.class);
        }).collect(Collectors.toList());
        return skuDTOList;
    }

    /**
     * 减库存
     * @param skuIdNumMap
     */
    public void minusStock(Map<Long, Integer> skuIdNumMap) {
        skuService.minusStock(skuIdNumMap);
    }

    /**
     * 增加库存
     * @param skuMap
     */
    public void plusStock(Map<Long, Integer> skuMap) {
        skuService.plusStock(skuMap);
    }
}
