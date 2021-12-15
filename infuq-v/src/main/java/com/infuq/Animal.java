package com.infuq;

public class Animal {

	private String name = "cat";

	private String color = "black";

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

	@Override
	public String toString() {
		return "Animal{" +
				"name='" + name + '\'' +
				", color='" + color + '\'' +
				'}';
	}
}
