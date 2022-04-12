package com.infuq.mybatis.mapper;

import com.infuq.mybatis.entity.Address;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


public interface UserMapper {

	@Options(flushCache = Options.FlushCachePolicy.TRUE)
    @Select("select * from db0.t_1 where id=1")
    List<Address> getList();



    @Select("select * from db1.t2")
    List<Address> getAllAddress();

	@Update("update db0.t0 set address=\"北京\" where id=12")
	int update();

	@Options(flushCache = Options.FlushCachePolicy.TRUE)
	@Select("select * from db0.t0 where id=12")
	List<Address> tmp();


}
