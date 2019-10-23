package com.leyou.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.order.entity.TbOrder;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author HM
 * @since 2019-08-12
 */
public interface TbOrderMapper extends BaseMapper<TbOrder> {

    @Select("select order_id from tb_order where status = 1 and create_time <= #{overTime}")
    List<Long> selectOrderIdListOverTime(Date overTime);
}
