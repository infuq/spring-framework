package com.infuq;


public class Computer {

	private int year = 2020;

	private String address = "Hangzhou";

	public int getYear() {
		return this.year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Computer{" +
				"year=" + year +
				", address='" + address + '\'' +
				'}';
	}
}
