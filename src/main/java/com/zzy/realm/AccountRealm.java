package com.zzy.realm;

import com.zzy.entity.Account;
import com.zzy.service.AccountService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    private AccountService accountService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        // read account
        Subject subject = SecurityUtils.getSubject();
        Account account = (Account) subject.getPrincipal();

        // set roles
        Set<String> roles = new HashSet<>();
        roles.add(account.getRole());

        // set perms
        Set<String> permissions = new HashSet<>();
        permissions.add(account.getPerms());

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addRoles(roles);
        info.addStringPermissions(permissions);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        Account account = accountService.getAccountByUsername(token.getUsername());

        if (account == null) {
            throw new UnknownAccountException("账户不存在！");
        }
        else return new SimpleAuthenticationInfo(account, account.getPassword(), getName());
    }
}
