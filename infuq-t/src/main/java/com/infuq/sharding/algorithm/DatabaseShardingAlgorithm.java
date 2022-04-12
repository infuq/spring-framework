package com.infuq.sharding.algorithm;


import com.alibaba.fastjson.JSON;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.util.Collection;


public class DatabaseShardingAlgorithm implements PreciseShardingAlgorithm<Integer> {

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Integer> shardingValue) {

        System.out.println("[数据库分片算法]" + JSON.toJSONString(availableTargetNames) + " , " + JSON.toJSONString(shardingValue));

        Integer value = shardingValue.getValue();

        String index = availableTargetNames.toArray(new String[0])[ value % 4 ];
        System.out.println("[数据库分片算法] 使用的数据库索引=" + index);
        return index;
    }


}