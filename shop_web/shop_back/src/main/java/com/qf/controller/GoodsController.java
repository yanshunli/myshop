package com.qf.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.qf.entity.Goods;
import com.qf.entity.ResultData;
import com.qf.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    private String uploadPath = "C:/worker/imgs";

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Reference
    private IGoodsService goodsService;

    @RequestMapping("/list")
    public String list(Model model){
        List<Goods> goods = goodsService.list();
        model.addAttribute("goodsList" , goods);
        return "goodslist";
    }

    @RequestMapping("/uploader")
    @ResponseBody
    public ResultData<String> uploader(MultipartFile file){
        String path = null;
        try {
            StorePath resultPath = fastFileStorageClient.uploadImageAndCrtThumbImage(
                    file.getInputStream(),
                    file.getSize(),
                    "JPG",
                    null
            );
            path = resultPath.getFullPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResultData<String>().setCode(ResultData.ResultCodeList.OK).setData("http://www.img.com:8080/" + path);
    }


    @RequestMapping("/insert")
    public String insert(Goods goods){
        goodsService.insert(goods);
        return "redirect:/goods/list";
    }

}
