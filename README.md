### mysql生成表格account

```mysql
create table account
(
    id       int auto_increment
        primary key,
    username varchar(50)  not null,
    password varchar(50)  not null,
    perms    varchar(100) null,
    role     varchar(50)  null
)
    charset = utf8mb3;
```

### shiro配置类

```java

@Configuration
@Component
public class ShiroConfig {

    @Bean
    public AccountRealm accountRealm() {
        return new AccountRealm();
    }

    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(@Qualifier("accountRealm") AccountRealm accountRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(accountRealm);
        return securityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("defaultWebSecurityManager") DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String,String> map = new HashMap<>();

        // set filter
        map.put("/main","authc");
        map.put("/manage","perms[manage]");
        map.put("/administrator","roles[administrator]");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(map);

        // set url
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/index");
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized");

        return shiroFilterFactoryBean;
    }

    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }
}

```

### 控制器

```java
package com.zzy.controller;

import com.zzy.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class AccountController {

    @PostMapping("/login")
    public String login(String username, String password, Model model) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            subject.login(token);
            Account account = (Account) subject.getPrincipal();

            // obtain the Session and set attributes
            Session session = subject.getSession();
            session.setAttribute("account", account);

            // Set the session timeout for 30 minutes
            int timeout = 30 * 60 * 1000;
            session.setTimeout(timeout);

            return "redirect:/index";
        } catch (UnknownAccountException e) {
            log.error(e.getMessage(), e);
            model.addAttribute("msg", "wrong username!");
            return "login";
        } catch (IncorrectCredentialsException e) {
            log.error(e.getMessage(), e);
            model.addAttribute("msg", "wrong password!");
            return "login";
        }
    }

    @GetMapping("/unauthorized")
    @ResponseBody
    public String unauthorized() {
        return "unauthorized";
    }

    @GetMapping("/logout")
    public String logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        return "redirect:/index";
    }

}
```

### realm

```java
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
```