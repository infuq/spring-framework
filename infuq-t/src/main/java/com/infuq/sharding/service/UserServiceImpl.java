package com.infuq.sharding.service;


import com.infuq.sharding.mapper.UserMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;


public class UserServiceImpl implements UserService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private UserMapper userMapper;

//    @Transactional(transactionManager = "druidTransactionManager")
    @Override
    public void getList() {

        System.out.println(userMapper.getList());
        System.out.println(userMapper.getListv2());

//		getAllAddress();

//        UserService userService = applicationContext.getBean(UserService.class);
//        userService.getAllAddress();


    }




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
