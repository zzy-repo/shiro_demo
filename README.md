# springboot集成Shiro的安全认证程序
> 技术栈：Springboot+Shiro+Thymeleaf+Mybatisplus

![image-20240904131813728](https://gitee.com/zzy2401/picbed/raw/master/images/image-20240904131813728.png)

### 时序图

![20240906152403](https://gitee.com/zzy2401/picbed/raw/master/images/20240906152403.png)


### 数据库设计

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

### pom配置

```xml
<dependencies>
    <!-- spring-boot-dependencies -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.7</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/com.auth0/java-jwt -->
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>java-jwt</artifactId>
        <version>4.4.0</version>
    </dependency>
    <!-- https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api &ndash;&gt;-->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>
    <!-- https://mvnrepository.com/artifact/com.github.theborakompanioni/thymeleaf-extras-shiro -->
    <dependency>
        <groupId>com.github.theborakompanioni</groupId>
        <artifactId>thymeleaf-extras-shiro</artifactId>
        <version>2.1.0</version>
    </dependency>

    <!--
        直接引入shiro会发生报错:java.lang.ClassNotFoundException: javax.servlet.Filter.
        这是由于Spring Boot 3.0 使用了Servlet 5.0，而javax.servlet此时已经迁移到了jakarta.servlet中.
        Shiro已经提供了适配Servlet 5.0 的依赖包，使用<classifier>标签即可选取适配版本.
        不过部分Shiro包中仍嵌套依赖了一些没有适配jakarta的依赖包.
        所以我们需要使用<exclude>将其排除，再引入同版本的jakarta适配包.
        参考链接:https://blog.csdn.net/weixin_43492211/article/details/131217344
    -->
    <dependency>
        <groupId>org.apache.shiro</groupId>
        <artifactId>shiro-spring</artifactId>
        <version>2.0.1</version>
        <classifier>jakarta</classifier> <!-- 使用classifier标签选择适配版本 -->
        <exclusions> <!-- 排除未适配的依赖 -->
            <exclusion>
                <groupId>org.apache.shiro</groupId>
                <artifactId>shiro-web</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shiro</groupId>
                <artifactId>shiro-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <!-- 引入适配jakarta的依赖包 -->
    <dependency>
        <groupId>org.apache.shiro</groupId>
        <artifactId>shiro-core</artifactId>
        <classifier>jakarta</classifier>
        <version>2.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shiro</groupId>
        <artifactId>shiro-web</artifactId>
        <classifier>jakarta</classifier>
        <version>2.0.1</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.shiro</groupId>
                <artifactId>shiro-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

</dependencies>
```
