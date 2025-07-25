package com.yk.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yk.domain.Post;
import com.yk.mapper.PostMapper;
import com.yk.service.PostService;
import org.springframework.stereotype.Service;

@Service("postService")
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
}
