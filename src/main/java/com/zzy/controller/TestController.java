package com.zzy.controller;

import com.zzy.entity.Account;
import com.zzy.mapper.AccountMapper;
import com.zzy.service.impl.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {

    @Autowired
    private AccountMapper accountMapper;

    @GetMapping("/test")
    public List<Account> test(){
        List<Account> list = accountMapper.selectList(null);
        System.out.println(list);
        return list;
    }
}
