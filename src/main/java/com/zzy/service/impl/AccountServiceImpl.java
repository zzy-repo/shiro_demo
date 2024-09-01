package com.zzy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzy.mapper.AccountMapper;
import com.zzy.entity.Account;
import com.zzy.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * (Account)表服务实现类
 *
 * @author makejava
 * @since 2024-09-01 16:51:30
 */
@Service("accountService")
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Autowired
    AccountMapper accountMapper;

    @Override
    public Account getAccountByUsername(String username){
        QueryWrapper<Account> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return accountMapper.selectOne(wrapper);
    }
}
