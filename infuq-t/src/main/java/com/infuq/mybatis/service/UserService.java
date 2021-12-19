package com.infuq.mybatis.service;

import com.infuq.mybatis.entity.User;

import java.util.List;

public interface UserService {

    List<User> getList();

    List<User> getAllAddress();

}
