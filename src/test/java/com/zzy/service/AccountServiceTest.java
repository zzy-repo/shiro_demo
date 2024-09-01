package com.zzy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @org.junit.jupiter.api.Test
    void getAccountByUsername() {
        System.out.println(accountService.getAccountByUsername("user1"));
    }
}