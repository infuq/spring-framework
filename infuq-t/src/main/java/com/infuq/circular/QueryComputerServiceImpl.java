package com.infuq.circular;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QueryComputerServiceImpl implements QueryComputerService {

    @Autowired
    private QueryMemoryService memoryService;


    @Override
    public int queryComputerCount() {
        return 100;
    }

}
