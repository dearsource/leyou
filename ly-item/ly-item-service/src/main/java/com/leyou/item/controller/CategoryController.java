package com.leyou.item.controller;

import com.leyou.item.pojo.CategoryDTO;
import com.leyou.item.service.TbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private TbCategoryService categoryService;
    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByParentId(@RequestParam(name = "pid") Long pid){
        return ResponseEntity.ok(categoryService.findCategoryListByParentId(pid));
    }

    /**
     * 根据分类id集合，查询分类信息
     * @param cids
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<String> findCateogryListByCids(@RequestParam(name = "cids") List<Long> cids){
        return ResponseEntity.ok(categoryService.findCateogryListByCids(cids));
    }

    @GetMapping("/categoryList")
    public ResponseEntity<List<CategoryDTO>> findCateogrySByCids(@RequestParam(name = "cids") List<Long> cids){
        return ResponseEntity.ok(categoryService.findCateogrySByCids(cids));
    }
}
