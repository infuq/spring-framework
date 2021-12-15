package com.infuq.springframework;

import org.springframework.stereotype.Component;

@Component
public class ZAnimal {

	private String name = "cat";
	private String color = "black";

	public ZAnimal() {
		System.out.println("实例化Animal");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}



}
