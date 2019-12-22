package com.qf.util;

import com.qf.entity.ShopCart;

import java.math.BigDecimal;
import java.util.List;

public class PriceUtil {

    public static double allPrice(List<ShopCart> carts){
        BigDecimal allPirce = BigDecimal.valueOf(0);
        if(carts != null){
            for (ShopCart cart : carts) {
                allPirce = allPirce.add(cart.getCartPrice());
            }
        }
        return allPirce.doubleValue();
    }
}
