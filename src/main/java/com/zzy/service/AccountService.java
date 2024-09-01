package com.zzy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzy.entity.Account;
import org.springframework.stereotype.Service;


/**
 * (Account)表服务接口
 *
 * @author makejava
 * @since 2024-09-01 16:51:30
 */
@Service
public interface AccountService extends IService<Account> {
    Account getAccountByUsername(String username);
}
