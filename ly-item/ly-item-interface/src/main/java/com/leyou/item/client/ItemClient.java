package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient("item-service")
public interface ItemClient {
    /**
     * 根据分类id集合，查询分类信息
     * @param cids
     * @return
     */
    @GetMapping("/category/list")
    String findCateogryListByCids(List<Long> cids);

    /**
     * 根据id 查询品牌信息
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    BrandDTO findById(@PathVariable(name = "id") Long id);

    /**
     * 分页获取spu
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    PageResult<SpuDTO> findSpuByPage(@RequestParam(name = "page",defaultValue = "1") Integer page,
                                     @RequestParam(name = "rows",defaultValue = "5") Integer rows,
                                     @RequestParam(name = "key",required = false) String key,
                                     @RequestParam(name = "saleable",required = false) Boolean saleable);

    /**
     * 查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail")
    SpuDetailDTO findSpuDetail(@RequestParam(name = "id") Long spuId);

    /**
     * 根据spuid 查询sku的集合
     * @param spuId
     * @return
     */
    @GetMapping("/sku/of/spu")
    List<SkuDTO> findSkuListBySpuId(@RequestParam(name = "id") Long spuId);

    /**
     * 查询 规格参数
     * @param gid
     * @return
     */
    @GetMapping("/spec/params")
    List<SpecParamDTO> findParamList(@RequestParam(name = "gid",required = false) Long gid,
                                                            @RequestParam(name = "cid",required = false) Long cid,
                                                            @RequestParam(name = "searching",required = false) Boolean searching);

    /**
     * 查询分类集合
     * @param cids
     * @return
     */
    @GetMapping("/category/categoryList")
    List<CategoryDTO> findCateogrySByCids(@RequestParam(name = "cids") List<Long> cids);

    /**
     * 查询品牌的集合信息
     * @param brandIds
     * @return
     */
    @GetMapping("/brand/list")
    List<BrandDTO> findBrandListByIds(@RequestParam(name = "ids") List<Long> brandIds);

    /**
     * 根据id 查询spu
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{id}")
    SpuDTO findSpuById(@PathVariable(name = "id") Long spuId);

    /**
     * 根据cid 查询 规格分组信息
     * @param cid
     * @return
     */
    @GetMapping("/spec/of/category")
    List<SpecGroupDTO> findGroupByCid(@RequestParam(name = "id")Long cid);

    /**
     * 根据skuid的集合 获取sku集合
     * @param ids
     * @return
     */
    @GetMapping("/sku/list")
    List<SkuDTO> findSkuListByIds(@RequestParam(name = "ids") List<Long> ids);

    /**
     * 减库存
     * @param skuIdNumMap
     */
    @PutMapping("/stock/minus")
    void minusStock(@RequestBody  Map<Long, Integer> skuIdNumMap);

    /**
     * 增加库存
     * key  -   sku的id
     * value -  增加库存的数量
     * @param skuMap
     */
    @PutMapping("/stock/plus")
    void plusStock(@RequestBody Map<Long, Integer> skuMap);
}
