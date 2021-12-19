package com.infuq.mybatis.mapper;

import com.infuq.mybatis.entity.User;
import com.infuq.mybatis.entity.Address;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserMapper {

    @Select("select * from mysql.user")
    List<User> getList();



    @Select("select * from infuq_example.t1")
    List<Address> getAllAddress();


}
