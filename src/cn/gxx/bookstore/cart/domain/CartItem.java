package cn.gxx.bookstore.cart.domain;

import java.math.BigDecimal;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.user.domain.User;

public class CartItem {
	private String cartItemId;//主键
	private int quantity;//数量
	private Book book;//条目对应的图书
	private User user;//所属用户
	
	//添加获取小计的方法
	public double getSubtotal(){
		/*
		 * 使用BigDecimal不会有误差
		 * 要求使用String类型的构造器
		 */
		BigDecimal b1 = new BigDecimal(book.getPrice()+"");
		BigDecimal b2 = new BigDecimal(quantity+"");
		BigDecimal result = b1.multiply(b2);
		return result.doubleValue();
	}
	
	
	public String getCartItemId() {
		return cartItemId;
	}
	public void setCartItemId(String cartItemId) {
		this.cartItemId = cartItemId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Book getBook() {
		return book;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	
	
}
