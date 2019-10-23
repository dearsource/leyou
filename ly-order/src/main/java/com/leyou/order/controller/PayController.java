package com.leyou.order.controller;

import com.leyou.order.service.TbOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private TbOrderService orderService;
    /**
     * 微信支付的回调
     * @param result
     */
    @PostMapping(value = "/wx/notify",produces = {"application/xml"})
    public Map<String, String> wxNotify(@RequestBody  Map<String,String> result){
// 处理回调
        log.info("[支付回调] 接收微信支付回调, 结果:{}", result);
        orderService.handleNotify(result);

        // 返回成功
        Map<String, String> msg = new HashMap<>();
        msg.put("return_code", "SUCCESS");
        msg.put("return_msg", "OK");
        return msg;
    }
}
