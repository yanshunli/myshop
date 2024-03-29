package com.qf.serviceimpl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qf.dao.GoodsImagesMapper;
import com.qf.dao.GoodsMapper;
import com.qf.entity.Goods;
import com.qf.entity.GoodsImages;
import com.qf.service.IGoodsService;
import com.qf.service.ISearchService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoodsService implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Reference
    private ISearchService searchService;

    @Autowired
    private GoodsImagesMapper goodsImagesMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<Goods> list() {
        return goodsMapper.queryList();
    }

    @Override
    public IPage<Goods> listPage(Page<Goods> page) {
        return goodsMapper.queryListPage(page);
    }

    @Override
    @Transactional
    public int insert(Goods goods) {
        goodsMapper.insert(goods);
        GoodsImages goodsImages = new GoodsImages()
                .setGid(goods.getId())
                .setIsfengmian(1)
                .setUrl(goods.getFmurl());
        goodsImagesMapper.insert(goodsImages);
        for (String otherurl : goods.getOtherurls()) {
            GoodsImages gi = new GoodsImages()
                    .setGid(goods.getId())
                    .setIsfengmian(0)
                    .setUrl(otherurl);
            goodsImagesMapper.insert(gi);
        }
        rabbitTemplate.convertAndSend("goods_exchange", "", goods);
        return 1;
    }

    @Override
    public Goods queryById(Integer id) {
        return goodsMapper.queryById(id);
    }
}
