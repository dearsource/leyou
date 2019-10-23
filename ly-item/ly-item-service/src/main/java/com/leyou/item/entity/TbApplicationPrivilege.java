package com.leyou.item.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 服务中间表，记录服务id以及服务能访问的目标服务的id
 * </p>
 *
 * @author HeiMa
 * @since 2019-07-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TbApplicationPrivilege extends Model<TbApplicationPrivilege> {

private static final long serialVersionUID=1L;

    /**
     * 服务id
     */
    private Integer serviceId;

    /**
     * 目标服务id
     */
    private Integer targetId;

    /**
     * 创建时间
     */
    private Date createTime;


    @Override
    protected Serializable pkVal() {
        return this.serviceId;
    }

}
