package com.infuq.mybatis.service;

import com.infuq.mybatis.entity.Address;

import java.util.List;

public interface UserService {

    List<Address> getList();

    List<Address> getAllAddress();

}
