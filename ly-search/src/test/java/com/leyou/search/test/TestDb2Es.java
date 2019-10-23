package com.leyou.search.test;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.search.dao.GoodsRepository;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestDb2Es {
    @Autowired
    private SearchService searchService;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private GoodsRepository repository;
    @Test
    public void db2es(){

        int page = 1;
        int rows = 100;
        while(true){
            PageResult<SpuDTO> spuByPage = itemClient.findSpuByPage(page, rows, null, true);
            if(spuByPage == null || CollectionUtils.isEmpty(spuByPage.getItems())){
                break;
            }
            List<SpuDTO> spuDTOList = spuByPage.getItems();
//            List<Goods> goodsList = new ArrayList<>();
//            for(SpuDTO spuDTO:spuDTOList){
//                Goods goods = searchService.createGoods(spuDTO);
//                goodsList.add(goods);
//            }
            List<Goods> goodsList = spuDTOList.stream().map(searchService::createGoods).collect(Collectors.toList());
            //把goods的集合写入es
            repository.saveAll(goodsList);
            if(spuDTOList.size()<rows){
                break;
            }
            page++;
        }

    }
}
