package com.qf.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.qf.entity.ResultData;
import com.qf.entity.User;
import com.qf.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/sso")
public class SsoController {

    @RequestMapping("/tologin")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/toregister")
    public String toRegister(){
        return "register";
    }

    @Reference
    private IUserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/register")
    public String register(User user, Model model){
        int result = userService.register(user);
        if(result > 0){
            return "login";
        } else if(result == -1){
            model.addAttribute("error", "用户名已经存在！");
        } else {
            model.addAttribute("error", "注册失败！");
        }
        return "register";
    }

    @RequestMapping("/login")
    public String login(String username, String password, String returnUrl, HttpServletResponse response, Model model){
        User user = userService.queryByUserName(username);
        if(user != null && user.getPassword().equals(password)){
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(token, user);
            redisTemplate.expire(token, 7, TimeUnit.DAYS);
            Cookie cookie = new Cookie("loginToken", token);
            cookie.setMaxAge(60 * 60 * 24 * 7);
            cookie.setPath("/");
            cookie.setDomain("localhost");
            response.addCookie(cookie);
            if(returnUrl == null || returnUrl.equals("")){
                returnUrl = "http://localhost";
            }
            try {
                returnUrl = URLEncoder.encode(returnUrl, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "redirect:http://localhost:8083/cart/merge?returnUrl=" + returnUrl;
        }
        model.addAttribute("error", "账号或者密码错误！");
        return "redirect:/sso/tologin";
    }

    @ResponseBody
    @RequestMapping("/islogin")
    public String isLogin(@CookieValue(name = "loginToken", required = false) String loginToken, String callback){
        System.out.println("获得客户端的loginToken：" + loginToken);
        ResultData<User> resultData = new ResultData<User>().setCode(ResultData.ResultCodeList.ERROR);
        if(loginToken != null){
            User user = (User) redisTemplate.opsForValue().get(loginToken);
            if(user != null){
                resultData.setCode(ResultData.ResultCodeList.OK);
                resultData.setData(user);
            }
        }
        return callback != null ? callback + "(" + JSON.toJSONString(resultData) + ")" : JSON.toJSONString(resultData);
    }

    @RequestMapping("/logout")
    public String logout(@CookieValue(name = "loginToken", required = false) String loginToken, HttpServletResponse response){
        System.out.println("注销请求：" + loginToken);
        redisTemplate.delete(loginToken);
        Cookie cookie = new Cookie("loginToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        response.addCookie(cookie);
        return "redirect:/sso/tologin";
    }
}
