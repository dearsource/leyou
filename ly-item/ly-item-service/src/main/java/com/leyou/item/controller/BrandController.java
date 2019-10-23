package com.leyou.item.controller;

import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.pojo.BrandDTO;
import com.leyou.item.service.TbBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//@RefreshScope
@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private TbBrandService brandService;

//    @Value("${ly.msg}")
//    private String lymsg;
//
//    @GetMapping("/testbus")
//    public ResponseEntity<String> testBus(){
//        return ResponseEntity.ok(lymsg);
//    }

    /**
     * 根据条件查询 brand列表 分页
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<BrandDTO>> findBrandList(@RequestParam(name = "key",required = false) String key,
                                                    @RequestParam(name = "page",defaultValue = "1") Integer page,
                                                    @RequestParam(name = "rows",defaultValue = "10") Integer rows,
                                                    @RequestParam(name = "sortBy" ,required = false) String sortBy,
                                                    @RequestParam(name = "desc",defaultValue = "false") Boolean desc){
        return ResponseEntity.ok(brandService.findBrandList(key,page,rows,sortBy,desc));
    }

    /**
     * 保存品牌信息
     * @param tbBrand
     * @param cids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(TbBrand tbBrand, @RequestParam(name = "cids") List<Long> cids){
        brandService.saveBrand(tbBrand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid查询品牌列表
     * @param cid
     * @return
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> findByCategoryId(@RequestParam(name = "id") Long cid){
        return ResponseEntity.ok(brandService.findByCategoryId(cid));
    }

    /**
     * 根据id 查询品牌信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> findById(@PathVariable(name = "id") Long id){
        TbBrand tbBrand = brandService.getById(id);
        BrandDTO brandDTO = BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        return ResponseEntity.ok(brandDTO);
    }

    /**
     * 根据id集合 ，查询品牌集合
     * @param brandIds
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<BrandDTO>> findBrandListByIds(@RequestParam(name = "ids") List<Long> brandIds){
        Collection<TbBrand> tbBrands = brandService.listByIds(brandIds);
        if(CollectionUtils.isEmpty(tbBrands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        List<BrandDTO> brandDTOList = tbBrands.stream().map(tbBrand -> {
            return BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(brandDTOList);
    }
}
