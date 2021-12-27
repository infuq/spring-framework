package com.infuq.mulsource.mapper.primary;

import com.infuq.mulsource.entity.Address;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface PrimaryUserMapper {

    @Select("select * from test_0.t_0")
    List<Address> getList();



    @Select("select * from test_0.t_1")
    List<Address> getAllAddress();


}
