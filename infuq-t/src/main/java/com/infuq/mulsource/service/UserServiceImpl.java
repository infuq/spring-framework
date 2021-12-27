package com.infuq.mulsource.service;


import com.infuq.mulsource.mapper.primary.PrimaryUserMapper;
import com.infuq.mulsource.mapper.second.SecondUserMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

public class UserServiceImpl implements UserService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private PrimaryUserMapper primaryUserMapper;

	@Autowired
	private SecondUserMapper secondUserMapper;

//    @Transactional(transactionManager = "primaryTransactionManager")
    @Transactional
    @Override
    public void getList() {

		System.out.println("结果2[" + secondUserMapper.insert() + "]");
        System.out.println("结果1[" + primaryUserMapper.insert() + "]");

//		System.out.println("结果2[" + secondUserMapper.getList() + "]");
//		System.out.println("结果1[" + primaryUserMapper.getList() + "]");



    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
