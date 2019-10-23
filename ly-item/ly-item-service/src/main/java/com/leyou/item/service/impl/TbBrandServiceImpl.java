package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.pojo.BrandDTO;
import com.leyou.item.service.TbBrandService;
import com.leyou.item.service.TbCategoryBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Autowired
    private TbCategoryBrandService categoryBrandService;
    @Override
    public PageResult<BrandDTO> findBrandList(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        Page<TbBrand> page1 = new Page<>(page, rows);
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(key)){
            queryWrapper.lambda().like(TbBrand::getLetter,key);
        }
        if(!StringUtils.isEmpty(sortBy)){
            if(desc){
                queryWrapper.orderByDesc(sortBy);
            }else{
                queryWrapper.orderByAsc(sortBy);
            }
        }
        IPage<TbBrand> brandIPage = this.page(page1, queryWrapper);
        if(brandIPage == null || CollectionUtils.isEmpty(brandIPage.getRecords())){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        List<BrandDTO> brandDTOList = BeanHelper.copyWithCollection(brandIPage.getRecords(), BrandDTO.class);
        return new PageResult<BrandDTO>(page1.getTotal(),page1.getPages(),brandDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBrand(TbBrand tbBrand, List<Long> cids) {
        //保存品牌信息
        boolean b = this.save(tbBrand);
        if(!b){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        Long brandId = tbBrand.getId();
        //保存中间表,批量
//        List<TbCategoryBrand> list = new ArrayList();
//        for(Long cid:cids){
//            TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
//            tbCategoryBrand.setBrandId(brandId);
//            tbCategoryBrand.setCategoryId(cid);
//            list.add(tbCategoryBrand);
//
//        }

        List<TbCategoryBrand> list =  cids.stream().map( cid->{
            TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
            tbCategoryBrand.setCategoryId(cid);
            tbCategoryBrand.setBrandId(brandId);
            return tbCategoryBrand;
        }).collect(Collectors.toList());

        boolean bBatch = categoryBrandService.saveBatch(list);
        if(!bBatch){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }


    /**
     * 根据cid 查询品牌列表
     * @param cid
     * @return
     */
    @Override
    public List<BrandDTO> findByCategoryId(Long cid) {
        List<TbBrand> brandList = this.getBaseMapper().selectBrandListJoinCategoryId(cid);
        if(CollectionUtils.isEmpty(brandList)){
            throw  new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brandList,BrandDTO.class);
    }
}
