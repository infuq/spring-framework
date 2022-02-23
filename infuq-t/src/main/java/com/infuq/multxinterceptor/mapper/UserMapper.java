package com.infuq.multxinterceptor.mapper;

import com.infuq.multxinterceptor.entity.Address;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface UserMapper {

	@Options(flushCache = Options.FlushCachePolicy.TRUE)
    @Select("select * from test_0.t_0 where id=12")
    List<Address> getList();



    @Select("select * from test_1.t_2")
    List<Address> getAllAddress();

	@Update("update test_0.t_0 set address=\"北京-上海2\" where id=12")
	int update();

	@Options(flushCache = Options.FlushCachePolicy.TRUE)
	@Select("select * from test_0.t_0 where id=12")
	List<Address> tmp();


}
