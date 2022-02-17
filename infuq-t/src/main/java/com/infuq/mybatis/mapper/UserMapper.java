package com.infuq.mybatis.mapper;

import com.infuq.mybatis.entity.Address;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface UserMapper {

	@Options(flushCache = Options.FlushCachePolicy.TRUE)
    @Select("select * from test_0.t_0")
    List<Address> getList();



    @Select("select * from test_1.t_2")
    List<Address> getAllAddress();

	@Update("update test_0.t_0 set address=\"zhengjiang-yuhang\" where id=12")
	int update();


}
