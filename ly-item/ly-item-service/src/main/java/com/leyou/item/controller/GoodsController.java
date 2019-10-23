package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.SkuDTO;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.item.pojo.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(@RequestParam(name = "page",defaultValue = "1") Integer page,
                                                            @RequestParam(name = "rows",defaultValue = "5") Integer rows,
                                                            @RequestParam(name = "key",required = false) String key,
                                                            @RequestParam(name = "saleable",required = false) Boolean saleable){
        return ResponseEntity.ok(goodsService.findSpu(page,rows,key,saleable));
    }

    /**
     * 保存商品
     * @param spuDTO
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
            goodsService.saveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/goodss")
    public void saveGood(@RequestBody JSONObject jsom){
        System.out.println(jsom);
    }
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 商品的上下架
     * @param spuId
     * @param saleable
     * @return
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSaleable(@RequestParam(name = "id") Long spuId,
                                               @RequestParam(name = "saleable") Boolean saleable){
        goodsService.updateSaleable(spuId,saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> findSpuDetail(@RequestParam(name = "id") Long spuId){
        return ResponseEntity.ok(goodsService.findSpuDetail(spuId));
    }

    /**
     * 根据spuid 查询sku的集合
     * @param spuId
     * @return
     */
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> findSkuListBySpuId(@RequestParam(name = "id") Long spuId){
        return ResponseEntity.ok(goodsService.findSkuListBySpuId(spuId));
    }

    /**
     * 根据id 查询spu
     * @param spuId
     * @return
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuDTO> findSpuById(@PathVariable(name = "id") Long spuId){
        return ResponseEntity.ok(goodsService.findSpuById(spuId));
    }

    /**
     * 根据ids 查询sku集合
     * @param ids
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<SkuDTO>> findSkuListByIds(@RequestParam(name = "ids") List<Long> ids){
        return ResponseEntity.ok(goodsService.findSkuListByIds(ids));
    }

    /**
     * 减库存
     * @param skuIdNumMap
     */
    @PutMapping("/stock/minus")
    public ResponseEntity<Void> minusStock(@RequestBody Map<Long, Integer> skuIdNumMap){
        goodsService.minusStock(skuIdNumMap);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 增加库存
     * key  -   sku的id
     * value -  增加库存的数量
     * @param skuMap
     */
    @PutMapping("/stock/plus")
    public ResponseEntity<Void> plusStock(@RequestBody Map<Long, Integer> skuMap){
        goodsService.plusStock(skuMap);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
