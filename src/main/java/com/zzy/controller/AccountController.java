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
