package com.leyou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.order.DTO.OrderDTO;
import com.leyou.order.DTO.OrderVO;
import com.leyou.order.entity.TbOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
public interface TbOrderService extends IService<TbOrder> {

    Long saveOrder(OrderDTO orderDTO);

    OrderVO findOrderById(Long orderId);

    String getUrl(Long orderId);

    void handleNotify(Map<String, String> result);

    Integer getStatus(Long orderId);

    List<Long> getOverTimeOrderId(Date overTime);


    /**
     * 关闭过期订单
     * @param overDate
     */
    void closeOverTimeOrder(Date overDate);
}
