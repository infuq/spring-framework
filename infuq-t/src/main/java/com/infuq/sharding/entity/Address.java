package com.infuq.sharding.entity;


public class Address {

    private int id;
	private String name;
    private String address;

	@Override
	public String toString() {
		return "Address{" +
				"id=" + id +
				", name=" + name +
				", address='" + address + '\'' +
				'}';
	}
}
