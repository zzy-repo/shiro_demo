package com.zzy.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
/**
 * (Account)表实体类
 *
 * @author makejava
 * @since 2024-09-01 16:51:30
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("account")
public class Account  {

    @TableId
    private Integer id;

    private String username;

    private String password;

    private String perms;

    private String role;
    
}
