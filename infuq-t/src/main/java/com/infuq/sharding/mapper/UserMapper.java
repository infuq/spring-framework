package com.infuq.sharding.mapper;

import com.infuq.sharding.entity.Address;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface UserMapper {

    @Select("select * from t where u_id = 76564354")
    List<Address> getList();

    @Select("select * from t where u_id = 54325432")
    List<Address> getListv2();

}
