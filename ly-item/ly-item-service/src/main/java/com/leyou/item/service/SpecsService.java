package com.leyou.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.pojo.SpecGroupDTO;
import com.leyou.item.pojo.SpecParamDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecsService {

    @Autowired
    private TbSpecGroupService groupService;

    public List<SpecGroupDTO> findGroupByCategoruId(Long cid) {
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,cid);
        List<TbSpecGroup> tbSpecGroupList = groupService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbSpecGroupList)){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSpecGroupList,SpecGroupDTO.class);
    }

    public void saveGroup(TbSpecGroup specGroup) {
        boolean b = groupService.save(specGroup);
        if(!b){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Autowired
    private TbSpecParamService paramService;

    public List<SpecParamDTO> findParamList(Long gid,Long cid,Boolean searching) {
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        if(gid != null && gid!= 0){
            queryWrapper.lambda().eq(TbSpecParam::getGroupId,gid);
        }
        if(cid != null && cid!= 0){
            queryWrapper.lambda().eq(TbSpecParam::getCid,cid);
        }
        if(searching != null ){
            queryWrapper.lambda().eq(TbSpecParam::getSearching,searching);
        }
        List<TbSpecParam> tbSpecParamList = paramService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbSpecParamList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSpecParamList,SpecParamDTO.class);
    }
}
