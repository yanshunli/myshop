package com.qf.service;

import com.qf.entity.Goods;

import java.util.List;

public interface ISearchService {

    int insertSolr(Goods goods);

    List<Goods> querySolr(String keyword);

}
