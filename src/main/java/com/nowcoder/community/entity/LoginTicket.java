package com.nowcoder.community.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Date: create in 12:31 2023/3/10
 * @describe:   用户登录凭证
 */
@Data
public class LoginTicket {
    private Integer id;

    private int userId;

    private String ticket;

    private int status;

    private Date expired;

}
