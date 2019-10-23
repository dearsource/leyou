package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.pojo.CategoryDTO;
import com.leyou.item.service.TbCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    @Override
    public List<CategoryDTO> findCategoryListByParentId(Long pid) {

        //设置查询条件
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<>();
        //设置查询条件
        //queryWrapper.eq("parent_id",pid);
        queryWrapper.lambda().eq(TbCategory::getParentId,pid);
        List<TbCategory> tbCategoryList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbCategoryList)){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbCategoryList,CategoryDTO.class);
    }

    @Override
    public String findCateogryListByCids(List<Long> cids) {
        Collection<TbCategory> listByIds = this.listByIds(cids);

        String categoryNames = listByIds.stream().map(TbCategory::getName).collect(Collectors.joining(","));

        return categoryNames;
    }

    @Override
    public List<CategoryDTO> findCateogrySByCids(List<Long> cids) {
        Collection<TbCategory> listByIds = this.listByIds(cids);
        if(CollectionUtils.isEmpty(listByIds)){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<CategoryDTO> collect = listByIds.stream().map(tbCategory -> {
            return BeanHelper.copyProperties(tbCategory, CategoryDTO.class);
        }).collect(Collectors.toList());
        return collect;
    }
}
