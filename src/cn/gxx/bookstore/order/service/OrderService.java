package cn.gxx.bookstore.order.service;

import java.sql.SQLException;

import cn.gxx.bookstore.order.dao.OrderDao;
import cn.gxx.bookstore.order.domain.Order;
import cn.gxx.bookstore.pager.PageBean;
import cn.itcast.jdbc.JdbcUtils;

public class OrderService {
	private OrderDao orderDao = new OrderDao();

	/**
	 * 根据用户查询订单
	 * 
	 * @param uid
	 * @param pc
	 * @return
	 */
	public PageBean<Order> myOrder(String uid, int pc) {
		try {

			JdbcUtils.beginTransaction();
			PageBean<Order> pageBean = orderDao.findByUser(uid, pc);
			JdbcUtils.commitTransaction();

			return pageBean;

		} catch (SQLException e) {

			try {
				JdbcUtils.rollbackTransaction();
			} catch (SQLException e1) {
			}

			throw new RuntimeException(e);
		}
	}

	/**
	 * 查询所有
	 * 
	 * @param pc
	 * @return
	 */
	public PageBean<Order> findAll(int pc) {
		try {

			JdbcUtils.beginTransaction();
			PageBean<Order> pageBean = orderDao.findAll(pc);
			JdbcUtils.commitTransaction();

			return pageBean;

		} catch (SQLException e) {
			try {
				JdbcUtils.rollbackTransaction();
			} catch (SQLException e1) {
			}
			throw new RuntimeException(e);
		}
	}

	/**
	 * 按状态查询
	 * @param status
	 * @return
	 */
	public PageBean<Order> findByStatus(int status,int pc) {
		try {

			JdbcUtils.beginTransaction();
			PageBean<Order> pageBean = orderDao.findByStatus(status,pc);
			JdbcUtils.commitTransaction();

			return pageBean;

		} catch (SQLException e) {
			try {
				JdbcUtils.rollbackTransaction();
			} catch (SQLException e1) {
			}
			throw new RuntimeException(e);
		}

	}

	/**
	 * 生成订单
	 * 
	 * @param uid
	 * @param pc
	 * @return
	 */
	public void createOrder(Order order) {
		try {

			JdbcUtils.beginTransaction();
			orderDao.add(order);
			JdbcUtils.commitTransaction();

		} catch (SQLException e) {

			try {
				JdbcUtils.rollbackTransaction();
			} catch (SQLException e1) {
			}

			throw new RuntimeException(e);
		}
	}

	/**
	 * 加载订单
	 * 
	 * @param oid
	 * @return
	 */
	public Order load(String oid) {
		try {

			JdbcUtils.beginTransaction();
			Order order = orderDao.load(oid);
			JdbcUtils.commitTransaction();
			return order;
		} catch (SQLException e) {

			try {
				JdbcUtils.rollbackTransaction();
			} catch (SQLException e1) {
			}

			throw new RuntimeException(e);
		}
	}

	/**
	 * 查询订单状态
	 * 
	 * @param oid
	 * @return
	 */
	public int findStatus(String oid) {
		try {
			return orderDao.findStatus(oid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 修改订单状态
	 * 
	 * @param oid
	 * @param status
	 */
	public void updateStatus(String oid, int status) {
		try {
			orderDao.updateStatus(oid, status);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
