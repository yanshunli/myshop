package com.qf.exception;

import com.qf.entity.ResultData;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;


@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest request, Exception e){
        System.out.println("项目出现异常！" + e.getMessage());

        String header = request.getHeader("X-Requested-With");
        if(header != null && header.equals("XMLHttpRequest")){
            return new ResultData<>().setCode(ResultData.ResultCodeList.ERROR).setMsg("服务器繁忙，请稍后再试！");
        } else {
            return new ModelAndView("myerror");
        }
    }
}
