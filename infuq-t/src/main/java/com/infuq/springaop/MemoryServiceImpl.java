package com.infuq.springaop;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemoryServiceImpl implements MemoryService {


	@Autowired
	private QueryComputerService queryComputerService;

    @Override
    public int findMemoryCount() {
        return 100;
    }

}
