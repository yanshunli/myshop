package com.qf.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.qf.entity.Orders;
import com.qf.service.IOrderService;
import com.qf.util.AlipayUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pay")
public class PayController {

    @Reference
    private IOrderService orderService;

    @RequestMapping("/alipay")
    @ResponseBody
    public void alipay(String orderid, HttpServletResponse response) throws IOException {
        Orders orders = orderService.QueryByOid(orderid);
        AlipayClient alipayClient = AlipayUtil.getAlipayClient();
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl("http://localhost:8083/orders/list");
        alipayRequest.setNotifyUrl("http://verygoodwlk.xicp.net/pay/payCallBack");
        alipayRequest.setBizContent("{" +
                "    \"out_trade_no\":\"" + orders.getOrderid() + "\"," +
                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "    \"total_amount\":" + orders.getAllprice().doubleValue() + "," +
                "    \"subject\":\"" + orders.getOrderDetils().get(0).getSubject() + "..\"," +
                "    \"extend_params\":{" +
                "    \"sys_service_provider_id\":\"2088511833207846\"" +
                "    }"+
                "  }");
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();
    }


    @RequestMapping("/payCallBack")
    @ResponseBody
    public String payCallBack(HttpServletRequest request, String out_trade_no, String trade_status) throws AlipayApiException {
        Map<String, String> signParams = new HashMap<>();
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            signParams.put(entry.getKey(), entry.getValue()[0]);
        }
        boolean signVerified = AlipaySignature.rsaCheckV1(signParams, AlipayUtil.ALIPAY_PUBLICK_KEY, signParams.get("charset"), signParams.get("sign_type")); //调用SDK验证签名
        if(signVerified){
            if(trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")){
                orderService.updateOrderStatus(out_trade_no, 1);
                return "success";
            }
        }else{
            System.out.println("支付验证失败！");
        }
        return "failure";
    }
}
