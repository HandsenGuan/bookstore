package cn.gxx.bookstore.order.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.order.domain.Order;
import cn.gxx.bookstore.order.domain.OrderItem;
import cn.gxx.bookstore.pager.Expression;
import cn.gxx.bookstore.pager.PageBean;
import cn.gxx.bookstore.pager.PageConstants;
import cn.itcast.commons.CommonUtils;
import cn.itcast.jdbc.TxQueryRunner;

public class OrderDao {
	
	private QueryRunner qr = new TxQueryRunner();
	
	/**
	 * 通用的查询方法
	 * 通过条件查询图书
	 * @param exprList
	 * @param pc
	 * @return	
	 * @throws SQLException
	 */
	private PageBean<Order> findByCriteria(List<Expression> exprList,int pc) throws SQLException{
		/**
		 * 1、得到ps（每页记录数）
		 * 2、得到tr（总记录数）
		 * 3、得到beanList
		 * 4、创建pageBean返回
		 */
		
		//得到每页记录数
		int ps =PageConstants.ORDER_PAGE_SIZE;
		
		//通过exprList来生成where子句
		StringBuilder whereSql = new 	 StringBuilder(" where 1=1 ");
		List<Object> params = new ArrayList<Object>();//sql中有问号，它是对应问号的值
		for(Expression expr:exprList){
			/*
			 * 添加一个条件
			 * 	1)以and开头
			 * 	2)条件名称
			 * 	3)条件的运算符，可以是=、！=、>、is null、...，is null没有值
			 * 	4)如果条件不是is null ，再追加问号，然后再向params中添加与问号对应的值
			 */
			whereSql.append("and ").append(expr.getName()+" ").append(expr.getOperator()+" ");
			
			if(!expr.getOperator().equals("is null")){
				whereSql.append("?");
				params.add(expr.getValue());
			}
		}
	
		//总记录数
		String sql = "select count(*) from t_order"+whereSql;
		
		Number number = (Number) qr.query(sql, new ScalarHandler(), params.toArray());
		
		int tr = number.intValue();
		
		//当前记录（beanList）
		sql = "select * from t_order "+whereSql+" order by ordertime desc limit ?,?";
		
		params.add( (pc-1) * ps );//第一个问号,当前页首行记录下标
		params.add(ps);//第二个问号，每页记录数
		
		List<Order> beanList =   qr.query(sql, new BeanListHandler<Order>(Order.class), params.toArray());
		
		//虽然获取了所有的订单，但每个订单中并没有订单条目
		//遍历每个订单，为其加载它所有的条目
		for(Order order:beanList){
			loadOrderItem(order);
		}
		
		
		//创建pageBean，设置参数
		PageBean<Order> pb = new PageBean<Order>();
		/**
		 * 其中pageBean没有url，这个任务有servlet完成
		 */
		pb.setPc(pc);
		pb.setBeanList(beanList);
		pb.setPs(ps);
		pb.setTr(tr);
		
	return pb;
	}
	

	/**
	 * 为指定的order加载指定的orderItem
	 * @param order
	 * @throws SQLException 
	 */
	private void loadOrderItem(Order order) throws SQLException {
		/**
		 * 1、给sql语句
		 * 2、执行，得到List<OrderItem> 
		 * 3、设置给Order
		 */
		String sql = "select * from t_orderitem where oid=?";
		
		List<Map<String,Object>> mapList = qr.query(sql, new MapListHandler(), order.getOid());
		List<OrderItem> orderItemList = toOrderItemList(mapList);
		
		order.setOrderItemList(orderItemList);
		
	}
  
	/**
	 * 将多个map映射为OrderItem
	 * @param mapList
	 * @return
	 */
	private List<OrderItem> toOrderItemList(List<Map<String, Object>> mapList) {
		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
		for(Map<String, Object> map:mapList){
			OrderItem orderItem = toOrderItem(map);
			orderItemList.add(orderItem);
		}
		return orderItemList;
	}

	/**
	 * 将一个map映射为OrderItem
	 * @param map
	 * @return
	 */
	private OrderItem toOrderItem(Map<String, Object> map) {
		OrderItem orderItem = CommonUtils.toBean(map, OrderItem.class);
		Book book = CommonUtils.toBean(map, Book.class);
		orderItem.setBook(book);
		return orderItem;
	}


	/**
	 * 按用户查询订单
	 * @param uid
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Order> findByUser(String uid ,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("uid", "=",uid));
		return findByCriteria(exprList, pc);
	}
	
	/**
	 * 按状态查询
	 * @param status
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Order> findByStatus(int status,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("status","=",status+""));
		return findByCriteria(exprList, pc);
	}
	
	
	/**
	 * 查询所有
	 * @param uid
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Order> findAll(int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		return findByCriteria(exprList, pc);
	}

	/**
	 * 生成订单
	 * @param order
	 * @throws SQLException 
	 */
	public void add(Order order) throws SQLException{
		/*
		 * 插入订单
		 */
		String sql = "insert into t_order values(?,?,?,?,?,?) ";
		Object[] params =  { order.getOid() , order.getOrdertime() , order.getTotal() , 
												order.getStatus()  , order.getAddress() , order.getOwner().getUid()};
		qr.update(sql,params);
		/*
		 * 循环遍历订单的所有条目，让每个条目生成一个Object[]
		 * 多个条目就对应Object[][]
		 * 执行批处理，完成插入订单条目 
		 */
		sql = "insert into t_orderitem values(?,?,?,?,?,?,?,?)";
		int len = order.getOrderItemList().size();
		Object[][] objs = new Object[len][];
		for(int i=0;i<len;i++){
			OrderItem orderItem = order.getOrderItemList().get(i);
			objs[i] = new Object[]{orderItem.getOrderItemId(),orderItem.getQuantity(),orderItem.getSubtotal(),
														orderItem.getBook().getBid(),
														orderItem.getBook().getBname(),
														orderItem.getBook().getCurrPrice(),
														orderItem.getBook().getImage_b(),
														order.getOid()};
		}

		qr.batch(sql, objs);
		
	}
	
	/**
	 * 通过订单oid查询订单
	 * @param oid
	 * @return
	 * @throws SQLException
	 */
	public Order load(String oid ) throws SQLException{
		String sql = "select * from t_order where oid=?";
		Order order = qr.query(sql, new BeanHandler<Order>(Order.class), oid);
		//为当前订单加载所有的订单条目
		loadOrderItem(order);
		return order;	
	}


	/**
	 * 查询订单状态
	 * @param oid
	 * @return
	 * @throws SQLException 
	 */
	public int findStatus(String oid) throws SQLException{
		String sql = "select status from t_order where oid=?";
		Number number = (Number) qr.query(sql, new ScalarHandler(), oid);
		return number.intValue();
	}
	
	/**
	 * 修改订单状态
	 * @param oid
	 * @param status
	 * @throws SQLException 
	 */
	public void updateStatus(String oid,int status) throws SQLException{
		String sql = "update t_order set status=? where oid=?";
		qr.update(sql, status,oid);
	}
	
}
