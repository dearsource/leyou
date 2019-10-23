package com.leyou.sms.listener;

import com.leyou.common.Exception.LyException;
import com.leyou.common.constants.MQConstants;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.RegexUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class SmsListener {

    @Autowired
    private SmsHelper smsHelper;
    @Autowired
    private SmsProperties prop;

    /**
     * 发送短信的方法
     * map的结构
     * key -phone  value -电话号码
     * key-code   value-验证码
     * @param map
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.Queue.SMS_VERIFY_CODE_QUEUE,durable = "true"),
            exchange = @Exchange(name = MQConstants.Exchange.SMS_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = MQConstants.RoutingKey.VERIFY_CODE_KEY
    ))
    public void listenVerifyCode(Map<String,String> map){
        //获取电话号码
        String phone = map.remove("phone");
        //判断手机号是否符合要求
        if(!RegexUtils.isPhone(phone)){
            return ;
        }
        log.info("电话号码={}",phone);
        try {
            smsHelper.sendMessage(phone, prop.getSignName(), prop.getVerifyCodeTemplate(), JsonUtils.toString(map));
        } catch (LyException e) {
            // 短信验证码失败后不重发，所以需要捕获异常。
            log.error("【SMS服务】短信验证码发送失败", e);
        }
    }
}
