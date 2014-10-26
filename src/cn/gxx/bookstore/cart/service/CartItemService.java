package cn.gxx.bookstore.cart.service;

import java.sql.SQLException;
import java.util.List;

import cn.gxx.bookstore.cart.dao.CartItemDao;
import cn.gxx.bookstore.cart.domain.CartItem;
import cn.itcast.commons.CommonUtils;

public class CartItemService {
	private CartItemDao cartItemDao = new CartItemDao();
	
	/**
	 * 我的购物车功能
	 * @param uid
	 * @return
	 */
	public List<CartItem> myCart(String uid){
		try {
			return cartItemDao.findByUser(uid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 添加到购物车
	 * @param cartItem
	 */
	public void add(CartItem cartItem){
		try {
			/*
			 * 使用uid和bid去数据库中查询这个条目是否存在
			 */
			//数据库中存在的条目
			CartItem _cartItem = cartItemDao.findByUidAndBid(cartItem.getUser().getUid(), cartItem.getBook().getBid());
			if(_cartItem==null){//如果原来没有这个条目
				cartItem.setCartItemId(CommonUtils.uuid());
				cartItemDao.addCartItem(cartItem);
			}else{//如果原来有这个条目，修改数量
				int quantity  = cartItem.getQuantity()+_cartItem.getQuantity();
				cartItemDao.updateQuentity(_cartItem.getCartItemId(), quantity);
			}
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		
	}
	
	/**
	 * 批量删除
	 * @param cartItemIds
	 */
	public void batchDelete(String cartItemIds){
		try {
			cartItemDao.batchDelete(cartItemIds);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 修改条目数量
	 * @param cartItemId
	 * @param quantity
	 * @return
	 */
	public CartItem updateQuantity(String cartItemId,int quantity){
		try {
			cartItemDao.updateQuentity(cartItemId, quantity);
			return cartItemDao.findByCartItemId(cartItemId);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * 装载多个购物车条目
	 * @param cartItemIds
	 * @return
	 */
	public List<CartItem> loadCartItems(String cartItemIds){
		try {
			return cartItemDao.loadCartItems(cartItemIds);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
