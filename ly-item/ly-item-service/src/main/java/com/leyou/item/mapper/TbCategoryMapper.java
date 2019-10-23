package com.leyou.item.mapper;

import com.leyou.item.entity.TbCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 Mapper 接口
 * </p>
 *
 * @author HeiMa
 * @since 2019-07-12
 */
public interface TbCategoryMapper extends BaseMapper<TbCategory> {

    List<TbCategory> selectCategoryListByBrandId(Long id);

    @Select("SELECT c2.id, c2.name,c2.parent_id,c2.is_parent ,c2.sort " +
            "FROM (  " +
            "    SELECT  " +
            "        @r AS _id,  (SELECT @r := parent_id FROM tb_category WHERE id = _id) AS parent_id,  @l := @l -1 AS lvl  " +
            "    FROM  \n" +
            "        (SELECT @r := #{cid3}, @l := 3) vars,  tb_category c  " +
            "    WHERE @r <> 0 ) c1  " +
            "JOIN tb_category c2  " +
            "ON c1._id = c2.id  " +
            "ORDER BY c1.lvl")
    List<TbCategory> selectCategoryListByCid3(Long cid3);
}
