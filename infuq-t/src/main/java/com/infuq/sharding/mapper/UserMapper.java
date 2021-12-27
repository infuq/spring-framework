package com.infuq.sharding.mapper;

import com.infuq.sharding.entity.Address;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserMapper {

    @Select("select * from test_0.t_0")
    List<Address> getList();



    @Select("select * from test_1.t_2")
    List<Address> getAllAddress();


}
