package com.leyou.order.controller;

import com.leyou.order.DTO.OrderDTO;
import com.leyou.order.DTO.OrderVO;
import com.leyou.order.service.TbOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private TbOrderService orderService;
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.saveOrder(orderDTO));
    }

    /**
     * 订单查询
     * @param orderId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderVO> findOrderById(@PathVariable(name = "id") Long orderId){
        return ResponseEntity.ok(orderService.findOrderById(orderId));
    }

    /**
     * 获取 支付链接
     * @param orderId
     * @return
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> getUrl(@PathVariable(name = "id")Long orderId){
        return ResponseEntity.ok(orderService.getUrl(orderId));
    }

    /**
     * 查询订单的状态
     * @param orderId
     * @return
     */
    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> getStatus(@PathVariable(name = "id")Long orderId){
        return ResponseEntity.ok(orderService.getStatus(orderId));
    }
}
