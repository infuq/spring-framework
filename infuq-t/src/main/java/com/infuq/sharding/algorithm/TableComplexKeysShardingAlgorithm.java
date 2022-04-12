package com.infuq.sharding.algorithm;


import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;

import java.util.*;


public class TableComplexKeysShardingAlgorithm implements ComplexKeysShardingAlgorithm<Integer> {


    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<Integer> shardingValue) {
        System.out.println("---");
        return null;
    }
}