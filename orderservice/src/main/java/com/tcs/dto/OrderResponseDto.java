package com.tcs.dto;

public class OrderResponseDto {

	private int id;
	private String item;
	private double price;
	private int userId;
	private String username;
	private String email;
	public OrderResponseDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public OrderResponseDto(int id, String item, double price, int userId, String username, String email) {
		super();
		this.id = id;
		this.item = item;
		this.price = price;
		this.userId = userId;
		this.username = username;
		this.email = email;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	
	
	
	
	
}
