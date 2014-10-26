package cn.gxx.bookstore.cart.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.cart.domain.CartItem;
import cn.gxx.bookstore.user.domain.User;
import cn.itcast.commons.CommonUtils;
import cn.itcast.jdbc.TxQueryRunner;

public class CartItemDao {
	private QueryRunner qr = new TxQueryRunner();
	
	/**
	 * 把一个map映射成一个cartitem
	 * @return
	 */
	private CartItem toCartItem(Map<String , Object> map){
		if(map==null || map.size() == 0) return null;
		CartItem cartItem = CommonUtils.toBean(map, CartItem.class);
		Book book = CommonUtils.toBean(map,Book.class);
		User user = CommonUtils.toBean(map, User.class);
		cartItem.setBook(book);
		cartItem.setUser(user);
		return cartItem;
	}
	
	/**
	 * 把多个Map（List<Map<String , Object>>）映射成多个CartItem（List<CartItem>）
	 * @param mapList
	 * @return
	 */
	private List<CartItem> toCartItemList(List<Map<String , Object>> mapList){
		List<CartItem> cartItemList = new ArrayList<CartItem>();
		for(Map<String , Object> map:mapList){
			cartItemList.add(toCartItem(map));
		}
		return cartItemList;	
	}
	
	/**
	 * 根据用户id查询
	 * @param uid
	 * @return
	 * @throws SQLException
	 */
	public List<CartItem> findByUser(String uid) throws SQLException{
		String sql = "select * from t_book b,t_cartitem c where c.bid=b.bid and uid=? order by c.orderBy";
		List<Map<String , Object>> mapList = qr.query(sql, new MapListHandler(), uid);
		return toCartItemList(mapList);
	}
	
	/**
	 * 查询某个用户的某本图书的购物车条目是否存在	
	 * @param uid
	 * @param bid
	 * @return
	 * @throws SQLException
	 */
	public CartItem findByUidAndBid(String uid,String bid) throws SQLException{
		String sql = "select  * from t_cartitem where uid=? and bid=?";
		Map<String ,Object> map = qr.query(sql, new MapHandler(), uid,bid);
		return toCartItem(map);
	}
	
	/**
	 * 修改指定条目的数量
	 * @param cartItemId
	 * @param quantity
	 * @throws SQLException
	 */
	public void updateQuentity(String cartItemId , int quantity) throws SQLException{
		String sql = "update t_cartitem set quantity=? where cartItemId=?";
		qr.update(sql, quantity,cartItemId);
	}
	
	/**
	 * 添加购物车条目
	 * @param cartItem
	 * @throws SQLException
	 */
	public void addCartItem(CartItem cartItem) throws SQLException{
		String sql ="insert into t_cartitem(cartItemId,quantity,bid,uid) values(?,?,?,?)";
		Object[] params = {cartItem.getCartItemId(),cartItem.getQuantity(),cartItem.getBook().getBid(),cartItem.getUser().getUid()};
		qr.update(sql, params);
	}
	
	/**
	 * 生成where子句
	 * @param len
	 * @return
	 */
	private String toWhereSql(int len){
		StringBuilder sb  = new StringBuilder("cartItemId in (");
		for(int i=0;i<len;i++){
			sb.append("?");
			if(i<len-1){
				sb.append(",");
			}
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	/**
	 * 批量删除
	 * @param cartItemIds
	 * @throws SQLException 
	 */
	public void batchDelete(String cartItemIds) throws SQLException{
		/**
		 * 1、需要先把cartItemIds转换成数组
		 * 2、把cartItemIds转换成where子句
		 * 3、与delete from 连接然后执行
		 */
		Object[] cartItemIdArray = cartItemIds.split(",");
		String whereSql = toWhereSql(cartItemIdArray.length);
		String sql = "delete from t_cartitem where "+whereSql;
		qr.update(sql,cartItemIdArray);//其中cartItemIdArray必须是Object类型数组！
		
	}

	/**
	 * 按cartItemId查询购物车条目
	 * @param cartItemId
	 * @return
	 * @throws SQLException 
	 */
	public CartItem findByCartItemId(String cartItemId) throws SQLException{
		String sql = "select * from t_cartitem c,t_book b where c.bid=b.bid and c.cartItemId=? ";
		Map<String, Object> map =  qr.query(sql, new MapHandler(), cartItemId);
		return toCartItem(map);
	}

	/**
	 * 装载多个购物车条目
	 * @param cartItemIds
	 * @return
	 * @throws SQLException
	 */
	public List<CartItem> loadCartItems(String cartItemIds) throws SQLException{
		/*
		 * 把cartItemIds转换成数组
		 */
		Object[] cartItemIdArray = cartItemIds.split(",");
		/*
		 * 生成where子句
		 */
		String whereSql = toWhereSql(cartItemIdArray.length);
		/*
		 * 生成sql
		 */
		String sql = "select * from t_book b,t_cartItem c where c.bid=b.bid and "+whereSql;
		/*
		 * 执行sql
		 */
		List<Map<String, Object>> mapList = qr.query(sql, new MapListHandler(), cartItemIdArray);
		
		return toCartItemList(mapList);
	}
	
}
