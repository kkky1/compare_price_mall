package com.yk.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2025-07-25 14:55:42
 */
@Data
public class LoginForm implements Serializable {
    private static final long serialVersionUID = -86067992232960979L;

    private String username;

    private String password;

}

