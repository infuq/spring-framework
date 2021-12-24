package com.infuq.mybatis.entity;


public class Address {

    private int id;
    private String address;

	@Override
	public String toString() {
		return "Address{" +
				"id=" + id +
				", address='" + address + '\'' +
				'}';
	}
}
