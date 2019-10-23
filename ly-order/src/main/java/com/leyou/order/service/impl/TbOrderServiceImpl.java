package com.leyou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.wxpay.sdk.WXPayConstants;
import com.leyou.common.Exception.LyException;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.threadlocatls.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.SkuDTO;
import com.leyou.order.DTO.*;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.entity.TbOrderDetail;
import com.leyou.order.entity.TbOrderLogistics;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.mapper.TbOrderMapper;
import com.leyou.order.service.TbOrderDetailService;
import com.leyou.order.service.TbOrderLogisticsService;
import com.leyou.order.service.TbOrderService;
import com.leyou.order.utils.PayHelper;
import com.leyou.user.DTO.AddressDTO;
import com.leyou.user.client.UserClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
@Service
public class TbOrderServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder> implements TbOrderService {

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private TbOrderDetailService orderDetailService;
    @Autowired
    private TbOrderLogisticsService orderLogisticsService;
    @Autowired
    private UserClient userClient;


    @Override
    @Transactional
    public Long saveOrder(OrderDTO orderDTO) {
//        1、保存order信息
//        1.1、生成orderid
        long orderId = idWorker.nextId();
        //获取userid
        Long userId = UserHolder.getUser();
//        1.2、计算金额
        //获取 sku的集合信息
        List<CartDTO> cartDTOList = orderDTO.getCarts();
        //生成skuid的集合
        List<Long> skuIdList = cartDTOList.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //创建skuid 和 num的map
        Map<Long, Integer> skuIdNumMap = cartDTOList.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //获取sku的集合
        List<SkuDTO> skuDTOList = itemClient.findSkuListByIds(skuIdList);
        //循环用户要购买的商品，获取skuid，和num
        //总价
        long totalFee = 0;
        List<TbOrderDetail> orderDetailList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Long skuId = skuDTO.getId();
            Long price = skuDTO.getPrice();
            Integer num = skuIdNumMap.get(skuId);
            totalFee = price * num;
            //构造orderdetail
            TbOrderDetail tbOrderDetail = new TbOrderDetail();
            tbOrderDetail.setOrderId(orderId);
            tbOrderDetail.setNum(num);
            tbOrderDetail.setTitle(skuDTO.getTitle());
            tbOrderDetail.setOwnSpec(skuDTO.getOwnSpec());
            tbOrderDetail.setImage(StringUtils.substringBefore(skuDTO.getImages(),","));
            tbOrderDetail.setPrice(price);
            tbOrderDetail.setSkuId(skuId);
            orderDetailList.add(tbOrderDetail);
        }
        //运费  包邮
        long postFee = 0;
        //实付金额 = 总金额 + 运费 - 优惠金额 :目前没有，默认是0
        long actualFee = totalFee + postFee - 0;
        TbOrder tbOrder = new TbOrder();
        tbOrder.setOrderId(orderId);
        tbOrder.setSourceType(2);
        tbOrder.setPostFee(postFee);
        tbOrder.setStatus(OrderStatusEnum.INIT.value());
        tbOrder.setActualFee(actualFee);
        tbOrder.setTotalFee(totalFee);
        tbOrder.setUserId(userId);
        tbOrder.setPaymentType(1);
        boolean b = this.save(tbOrder);
        if(!b){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//        2、保存orderdetail
        boolean bdetail = orderDetailService.saveBatch(orderDetailList);
        if(!b){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//        3、保存物流信息
        AddressDTO addressDTO = userClient.findAddressByUserIdAndId(userId, orderDTO.getAddressId());
        TbOrderLogistics tbOrderLogistics = BeanHelper.copyProperties(addressDTO, TbOrderLogistics.class);
        tbOrderLogistics.setOrderId(orderId);

        boolean bLogistic = orderLogisticsService.save(tbOrderLogistics);
        if(!b){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //4、减库存
        itemClient.minusStock(skuIdNumMap);

        return orderId;
    }

    @Override
    public OrderVO findOrderById(Long orderId) {
//        1、查询order
        TbOrder tbOrder = this.getById(orderId);
        if(tbOrder == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        Long userId = UserHolder.getUser();
        if(userId.longValue() != tbOrder.getUserId().longValue()){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
//        2、查询orderDetail
        QueryWrapper<TbOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbOrderDetail::getOrderId,orderId);
        List<TbOrderDetail> orderDetialList = orderDetailService.list(queryWrapper);

//        3、查询orderLogistics
        TbOrderLogistics tbOrderLogistics = orderLogisticsService.getById(orderId);
        OrderVO orderVO = BeanHelper.copyProperties(tbOrder, OrderVO.class);
        orderVO.setDetailList(BeanHelper.copyWithCollection(orderDetialList, OrderDetailVO.class));
        orderVO.setLogistics(BeanHelper.copyProperties(tbOrderLogistics, OrderLogisticsVO.class));
        return orderVO;
    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayHelper payHelper;
    /**
     * 获取 支付链接
     * @param orderId
     * @return
     */
    private String payUrlKey = "ly:pay:orderid:";
    @Override
    public String getUrl(Long orderId) {

        String redisKey = payUrlKey + orderId;
        //1、从redis中获取 支付url key--orderId value = url
        String url = redisTemplate.opsForValue().get(redisKey);
        //2、如果url不是null，直接返回url
        if(!org.springframework.util.StringUtils.isEmpty(url)){
            return url;
        }
//        redis中不存在order对应的 url
        TbOrder tbOrder = this.getById(orderId);
        if(tbOrder == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
//        订单状态不是 1 的时候不能支付
        if(tbOrder.getStatus().intValue() != OrderStatusEnum.INIT.value().intValue()){
            throw new LyException(ExceptionEnum.INVALID_ORDER_STATUS);
        }
        //调用wxpay ，获取url
        Long totalFee = 1L;//tbOrder.getActualFee();
        String desc = "乐优商城商品支付";
        String payUrl = payHelper.createOrder(orderId, totalFee, desc);
        //payUrl 有2小时的 有效期，放入redis中
        redisTemplate.opsForValue().set(redisKey,payUrl,2, TimeUnit.HOURS);
        return payUrl;
    }

    @Override
    public void handleNotify(Map<String, String> result) {
//        验证消息的真实性
        if(result.get("result_code") == null || !result.get("result_code").equals(WXPayConstants.SUCCESS)){
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        try {
            payHelper.isValidSign(result);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
//        获取订单号
        String orderId = result.get("out_trade_no");
        if(StringUtils.isEmpty(orderId)){
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
        Long orderIdLong = Long.valueOf(orderId);
        //微信支付的总金额
        String totalFee = result.get("total_fee");
        if(StringUtils.isEmpty(totalFee)){
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }

//        获取支付金额
        //用户支付的总金额
        long payTotalFee = Long.valueOf(totalFee).longValue();

//        获取这个订单的信息
        //查询订单数据
        TbOrder tbOrder = this.getById(orderIdLong);
        Long actualFee = 1L;//tbOrder.getActualFee();
//        比对支付的金额
        if(actualFee != payTotalFee){
            //订单金额和 支付的金额不符
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_PARAM);
        }
//        检查订单的状态
        if(tbOrder.getStatus().intValue() != OrderStatusEnum.INIT.value().intValue()){
            //如果不是未支付状态，就不修改订单的状态
            return ;
        }
//        修改订单的状态, 保证 接口的 幂等
//        update tb_order set status = 2 where order_id=? and  status =1
        UpdateWrapper<TbOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbOrder::getOrderId,orderIdLong);
        updateWrapper.lambda().eq(TbOrder::getStatus,OrderStatusEnum.INIT.value());
        updateWrapper.lambda().set(TbOrder::getStatus,OrderStatusEnum.PAY_UP.value());
        boolean bOrder = this.update(updateWrapper);
        if(!bOrder){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    @Override
    public Integer getStatus(Long orderId) {
        TbOrder tbOrder = this.getById(orderId);
        if(tbOrder == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return tbOrder.getStatus();
    }

    @Override
    public List<Long> getOverTimeOrderId(Date overTime) {
        List<Long> orderIdList = this.getBaseMapper().selectOrderIdListOverTime(overTime);

        return orderIdList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeOverTimeOrder(Date overDate) {
//        1、查询已经过期的订单
        List<Long> orderIdList = this.getOverTimeOrderId(overDate);
        if(CollectionUtils.isEmpty(orderIdList)){
            System.out.println("没有过期的订单需要处理");
            return ;
        }
//        1.1、修改订单的状态
        UpdateWrapper<TbOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().lt(TbOrder::getCreateTime,overDate);
        updateWrapper.lambda().eq(TbOrder::getStatus,OrderStatusEnum.INIT.value());
        updateWrapper.lambda().set(TbOrder::getStatus,OrderStatusEnum.CLOSED.value());
//        updateWrapper.lambda().in(TbOrder::getOrderId,orderIdList);
        boolean orderUpdate = this.update(updateWrapper);
        if(!orderUpdate){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//        2、查询对应的 订单信 息中的商品数量
//        select * from tb_order_detail where order_id in ();
        QueryWrapper<TbOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(TbOrderDetail::getOrderId,orderIdList);
        List<TbOrderDetail> tbOrderDetailList = orderDetailService.list(queryWrapper);
        if(CollectionUtils.isEmpty(tbOrderDetailList)){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        Map<Long, Integer> skuMap = tbOrderDetailList.stream().
                collect(Collectors.groupingBy(
                        TbOrderDetail::getSkuId,
                        Collectors.summingInt(TbOrderDetail::getNum)));
        System.out.println("skumap=="+skuMap);
//        Map<Long,Integer> skuMap = new HashMap<>();
//        for(TbOrderDetail orderDetail:tbOrderDetailList){
//            Long skuId = orderDetail.getSkuId();
//            Integer num = orderDetail.getNum();
//            Integer integer = skuMap.get(skuId);
//            if(integer == null){
//                //Skumap中 不存在当前的商品
//                skuMap.put(skuId,num);
//            }else{
//                //skumap中存在当前的商品
//                skuMap.put(skuId,num+integer);
//            }
//        }
//        3、把订单的数量 恢复
        itemClient.plusStock(skuMap);
    }


}
