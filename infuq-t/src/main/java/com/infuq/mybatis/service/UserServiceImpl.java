package com.infuq.mybatis.service;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;


public class UserServiceImpl implements UserService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private BookService bookService;


    @Override
    public void getList() throws Exception {

		bookService.findList();


    }


    @Override
    public void getAllAddress() {


    }

	@Override
	public int update() {
		return bookService.update();
	}

	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
