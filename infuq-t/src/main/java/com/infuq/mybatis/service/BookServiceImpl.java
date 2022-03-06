package com.infuq.mybatis.service;


import com.infuq.mybatis.mapper.UserMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class BookServiceImpl implements BookService, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private UserMapper userMapper;

    @Transactional(transactionManager = "druidTransactionManager")
    @Override
    public void findList() throws Exception {

//		System.out.println("休眠");
//		Thread.sleep(30000);
		System.out.println(Thread.currentThread().getName() + "第一次查询");
        System.out.println(userMapper.getList());
//        System.out.println(userMapper.update());
//        System.out.println(userMapper.tmp());

//		Thread.sleep(30000);

//		System.out.println(Thread.currentThread().getName() + "第二次查询");
//		System.out.println(userMapper.getList());


//		getAllAddress();

//        UserService userService = applicationContext.getBean(UserService.class);
//        userService.getAllAddress();


    }


    @Transactional(transactionManager = "transactionManager")
    @Override
    public void getAllAddress() {

        System.out.println(userMapper.getAllAddress());

    }

	@Transactional(transactionManager = "druidTransactionManager")
	@Override
	public int update() {

		System.out.println(userMapper.getList());
		System.out.println(userMapper.update());
		System.out.println(userMapper.getList());

		return 1;
	}

	@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
