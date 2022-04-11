package com.infuq.mulsource.mapper.second;

import com.infuq.mulsource.entity.Address;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface SecondUserMapper {

    @Select("select * from test_1.t_2")
    List<Address> getList();



    @Select("select * from test_1.t_3")
    List<Address> getAllAddress();

	@Insert("insert into test_1.t_2(id,address) values(13,'chengdu')")
	int insert();


}
