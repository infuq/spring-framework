package com.infuq.mulsource.mapper.second;

import com.infuq.mulsource.entity.Address;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface SecondUserMapper {

    @Select("select * from test_1.t_2")
    List<Address> getList();



    @Select("select * from test_1.t_3")
    List<Address> getAllAddress();


}
