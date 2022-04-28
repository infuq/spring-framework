package com.infuq.circular;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryMemoryServiceImpl implements QueryMemoryService {

    @Autowired
    private QueryComputerService computerService;

    @Override
    public int queryMemoryCount() {
        return 64;
    }

}
