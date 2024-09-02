package com.zzy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzy.entity.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * (Account)表数据库访问层
 *
 * @author makejava
 * @since 2024-09-01 16:51:30
 */
@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}
