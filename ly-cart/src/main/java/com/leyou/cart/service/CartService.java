package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.threadlocatls.UserHolder;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "ly:cart:uid:";
    /**
     * 保存 购物车数据
     * @param cart
     */
    public void addCart(Cart cart) {
        //redis的key
        String key = KEY_PREFIX + UserHolder.getUser();
        //hash 的key
        String hashKey = cart.getSkuId().toString();

        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(key);

        //本次用户选择的数量
        Integer num = cart.getNum();
        Boolean b = boundHashOps.hasKey(hashKey);
        if(b != null && b){
            //合并数量
            String cartJson = boundHashOps.get(hashKey);
            //redis中存储的cart
            cart = JsonUtils.toBean(cartJson, Cart.class);
            cart.setNum(num+cart.getNum());
        }
        //把数据放入redis中
        redisTemplate.opsForHash().put(key,hashKey, JsonUtils.toString(cart));

    }


    /**
     * 获取购物车
     * @return
     */
    public List<Cart> getUserCart() {
//        获取用户信息
        String key = KEY_PREFIX + UserHolder.getUser();
//        从redis中获取user的cart信息
        Boolean b = redisTemplate.hasKey(key);
        if(b == null || !b){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(key);
        List<String> jsonList = boundHashOps.values();
        if(CollectionUtils.isEmpty(jsonList)){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        List<Cart> cartList = jsonList.stream().map(json -> {
            return JsonUtils.toBean(json, Cart.class);
        }).collect(Collectors.toList());
        return cartList;
    }

    /**
     * 修改购物车 商品数量
     * @param skuId
     * @param num
     */
    public void update(Long skuId, Integer num) {
        String key = KEY_PREFIX + UserHolder.getUser();
        Boolean b = redisTemplate.hasKey(key);
        if(b == null || !b){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(key);
        String json = boundHashOps.get(skuId.toString());
        if(StringUtils.isEmpty(json)){
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        Cart cart = JsonUtils.toBean(json, Cart.class);
        cart.setNum(num);
        boundHashOps.put(skuId.toString(),JsonUtils.toString(cart));
    }

    /**
     * 删除购物车
     * @param skuId
     */
    public void delete(Long skuId) {
        String key = KEY_PREFIX + UserHolder.getUser();
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(key);
        boundHashOps.delete(skuId.toString());
    }

    public void addBatchCart(List<Cart> cartList) {
        String key = KEY_PREFIX + UserHolder.getUser();
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(key);
        for (Cart cart : cartList) {
            Integer num = cart.getNum();
            String hashKey = cart.getSkuId().toString();
            Boolean aBoolean = boundHashOps.hasKey(hashKey);
            if(aBoolean != null && aBoolean){
                //这个商品在redis中存在
                String json = boundHashOps.get(hashKey);
                cart = JsonUtils.toBean(json, Cart.class);
                cart.setNum(num + cart.getNum());
            }
            boundHashOps.put(hashKey,JsonUtils.toString(cart));
        }
    }
}
