package com.qf.serviceimpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qf.dao.CartMapper;
import com.qf.entity.Goods;
import com.qf.entity.ShopCart;
import com.qf.entity.User;
import com.qf.service.ICartService;
import com.qf.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public String insertCart(ShopCart cart, User user, String cartToken) {
        Goods goods = goodsService.queryById(cart.getGid());
        BigDecimal cartPrice = goods.getPrice().multiply(BigDecimal.valueOf(cart.getNumber()));
        cart.setCartPrice(cartPrice);
        if(user != null){
            cart.setUid(user.getId());
            cartMapper.insert(cart);
        } else {
            cartToken = cartToken != null ? cartToken : UUID.randomUUID().toString();
            redisTemplate.opsForList().leftPush(cartToken, cart);
        }
        return cartToken;
    }

    @Override
    public List<ShopCart> listCarts(String cartToken, User user) {
        List<ShopCart> shopCarts = null;
        if(user != null){
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("uid", user.getId());
            queryWrapper.orderByDesc("create_time");
            shopCarts = cartMapper.selectList(queryWrapper);
        } else {
            if(cartToken != null){
                Long size = redisTemplate.opsForList().size(cartToken);
                shopCarts = redisTemplate.opsForList().range(cartToken,0, size);
            }
        }
        for (ShopCart shopCart : shopCarts) {
            Integer gid = shopCart.getGid();
            Goods goods = goodsService.queryById(gid);
            shopCart.setGoods(goods);
        }
        return shopCarts;
    }

    @Override
    public List<ShopCart> queryCartsByGid(Integer[] gid, User user) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("uid", user.getId());
        queryWrapper.in("gid", gid);
        List<ShopCart> carts = cartMapper.selectList(queryWrapper);
        for (ShopCart shopCart : carts) {
            Integer id = shopCart.getGid();
            Goods goods = goodsService.queryById(id);
            shopCart.setGoods(goods);
        }
        return carts;
    }
    @Override
    public List<ShopCart> queryCartsByCid(Integer[] cids) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.in("id", cids);
        List<ShopCart> carts = cartMapper.selectList(queryWrapper);
        for (ShopCart shopCart : carts) {
            Integer id = shopCart.getGid();
            Goods goods = goodsService.queryById(id);
            shopCart.setGoods(goods);
        }
        return carts;
    }

    @Override
    public int deleteByCids(Integer[] cids) {
        return cartMapper.deleteBatchIds(Arrays.asList(cids));
    }
}
