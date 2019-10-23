package com.leyou.item.controller;

import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.pojo.SpecGroupDTO;
import com.leyou.item.pojo.SpecParamDTO;
import com.leyou.item.service.SpecsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecsController {

    @Autowired
    private SpecsService specsService;


    /**
     * 查询分组 根据cid
     * @param cid
     * @return
     */
    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findGroupByCategoryId(@RequestParam(name = "id") Long cid){
        return ResponseEntity.ok(specsService.findGroupByCategoruId(cid));
    }

    /**
     * 保存分组
     * @param specGroup
     * @return
     */
    @PostMapping("/group")
    public ResponseEntity<Void> saveGorup(@RequestBody TbSpecGroup specGroup){
        specsService.saveGroup(specGroup);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 查询 规格参数
     * @param gid
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> findParamList(@RequestParam(name = "gid",required = false) Long gid,
                                                            @RequestParam(name = "cid",required = false) Long cid,
                                                            @RequestParam(name = "searching",required = false) Boolean searching){
        return ResponseEntity.ok(specsService.findParamList(gid,cid,searching));
    }


    /**
     * 根据cid 查询 规格分组信息
     * @param cid
     * @return
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findGroupByCid(@RequestParam(name = "id")Long cid){
        return ResponseEntity.ok(specsService.findGroupByCategoruId(cid));
    }

}
