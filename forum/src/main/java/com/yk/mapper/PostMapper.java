package com.yk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yk.domain.Post;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * (Post)表数据库访问层
 *
 * @author makejava
 * @since 2025-07-25 15:42:37
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

}

