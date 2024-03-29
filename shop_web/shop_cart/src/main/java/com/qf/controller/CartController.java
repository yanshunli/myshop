package com.qf.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.qf.aop.IsLogin;
import com.qf.aop.UserHolder;
import com.qf.entity.ShopCart;
import com.qf.entity.User;
import com.qf.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Reference
    private ICartService cartService;

    @Autowired
    private RedisTemplate redisTemplate;

    @IsLogin
    @RequestMapping("/insert")
    public String insert(Integer gid, Integer gnumber,
                         @CookieValue(name = "cartToken", required = false) String cartToken,
                         HttpServletResponse response){
        System.out.println("加入购物车：" + gid + "---" + gnumber);
        User user = UserHolder.getUser();
        System.out.println("当前的登录信息：" + user);
        ShopCart cart = new ShopCart().setGid(gid).setNumber(gnumber);
        cartToken = cartService.insertCart(cart, user, cartToken);
        Cookie cookie = new Cookie("cartToken", cartToken);
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "insertok";
    }

    @RequestMapping("/list")
    @ResponseBody
    @IsLogin
    public String list(@CookieValue(name = "cartToken", required = false) String cartToken,
                       String callback){
        User user = UserHolder.getUser();
        List<ShopCart> shopCarts = cartService.listCarts(cartToken, user);
        return callback != null ? callback + "(" + JSON.toJSONString(shopCarts) + ")" : JSON.toJSONString(shopCarts);
    }

    @RequestMapping("/merge")
    @IsLogin(mustLogin = true)
    public String cartMerge(@CookieValue(name = "cartToken", required = false) String cartToken, HttpServletResponse response, String returnUrl){
        if(cartToken != null){
            Long size = redisTemplate.opsForList().size(cartToken);
            List<ShopCart> carts = redisTemplate.opsForList().range(cartToken, 0, size);
            User user = UserHolder.getUser();
            for (ShopCart cart : carts) {
                cartService.insertCart(cart, user, cartToken);
            }
            redisTemplate.delete(cartToken);
            Cookie cookie = new Cookie("cartToken", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return "redirect:" + returnUrl;
    }

    @RequestMapping("/showlist")
    @IsLogin
    public String showList(@CookieValue(name = "cartToken", required = false) String cartToken, Model model){
        User user = UserHolder.getUser();
        List<ShopCart> shopCarts = cartService.listCarts(cartToken, user);
        model.addAttribute("carts", shopCarts);
        return "cartlist";
    }
}
