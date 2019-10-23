package com.leyou.search.listener;

import com.leyou.common.constants.MQConstants;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.search.dao.GoodsRepository;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private GoodsRepository repository;

    /**
     * 上架
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= MQConstants.Queue.SEARCH_ITEM_UP,durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_UP_KEY
    ))
    public void itemUp(Long spuId){
        SpuDTO spuDTO = itemClient.findSpuById(spuId);
        Goods goods = searchService.createGoods(spuDTO);
        repository.save(goods);
    }

    /**
     * 下架
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name= MQConstants.Queue.SEARCH_ITEM_DOWN,durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.ITEM_DOWN_KEY
    ))
    public void itemDown(Long spuId){
        System.out.println("删除spuid=="+spuId);
        repository.deleteById(spuId);
    }
}
