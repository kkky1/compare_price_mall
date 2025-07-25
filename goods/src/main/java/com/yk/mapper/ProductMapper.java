package com.yk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yk.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (Product)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-25 16:34:17
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM product WHERE item_name LIKE CONCAT('%', #{name}, '%')")
    List<Product> selectListByName(@Param("name") String name);



}

