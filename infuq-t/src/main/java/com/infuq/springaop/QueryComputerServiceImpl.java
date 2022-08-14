package com.infuq.springaop;


import org.springframework.stereotype.Component;

@Component
public class QueryComputerServiceImpl implements QueryComputerService {

    @Override
    public int queryComputerCount() {
        return 100;
    }

}
