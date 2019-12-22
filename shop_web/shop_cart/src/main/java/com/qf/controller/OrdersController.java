package com.qf.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qf.aop.IsLogin;
import com.qf.aop.UserHolder;
import com.qf.entity.*;
import com.qf.service.IAddressService;
import com.qf.service.ICartService;
import com.qf.service.IOrderService;
import com.qf.util.PriceUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    @Reference
    private ICartService cartService;
    
    @Reference
    private IAddressService addressService;

    @Reference
    private IOrderService orderService;

    @IsLogin(mustLogin = true)
    @RequestMapping("/toOrdersEdit")
    public String toOrdersEdit(Integer[] gid, Model model){
        User user = UserHolder.getUser();
        List<ShopCart> shopCarts = cartService.queryCartsByGid(gid, user);
        List<Address> addresses = addressService.queryByUid(user.getId());
        double allprice = PriceUtil.allPrice(shopCarts);
        model.addAttribute("carts", shopCarts);
        model.addAttribute("addresses", addresses);
        model.addAttribute("allprice", allprice);
        return "ordersedit";
    }

    @IsLogin(mustLogin = true)
    @PostMapping("/insert")
    @ResponseBody
    public ResultData<Orders> insertOrders(Integer aid, Integer[] cids){
        System.out.println("收货地址的id：" + aid);
        System.out.println("购物清单的id：" + Arrays.toString(cids));
        Orders orders = orderService.insertOrder(cids, aid, UserHolder.getUser());
        return new ResultData<Orders>().setCode(ResultData.ResultCodeList.OK).setMsg("下单成功！").setData(orders);
    }

    @RequestMapping("/list")
    @IsLogin(mustLogin = true)
    public String list(Model model){
        User user = UserHolder.getUser();
        List<Orders> ordersList = orderService.queryByUid(user.getId());
        model.addAttribute("ordersList", ordersList);
        return "orderslist";
    }
}
