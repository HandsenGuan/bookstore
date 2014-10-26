package cn.gxx.bookstore.book.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.caregory.domain.Category;
import cn.gxx.bookstore.pager.Expression;
import cn.gxx.bookstore.pager.PageBean;
import cn.gxx.bookstore.pager.PageConstants;
import cn.itcast.commons.CommonUtils;
import cn.itcast.jdbc.TxQueryRunner;

public class BookDao {
	private QueryRunner qr = new TxQueryRunner();
	
	/**
	 * 根据bid查询
	 * @param bid
	 * @return
	 * @throws SQLException
	 */
	public Book findByBid(String bid) throws SQLException{
		String sql = "select * from t_book b,t_category c where b.cid=c.cid and b.bid=?";
		//一行记录中包含了很多book属性，还有一个cid属性
		Map<String ,Object> map = qr.query(sql, new MapHandler(), bid);
		//把map中除了cid以外的所有属性映射到book中
		Book book = CommonUtils.toBean(map, Book.class);
		//把map中cid映射到Category中，即这个Category只有cid属性
		Category category = CommonUtils.toBean(map, Category.class);
		//两者建立关系
		book.setCategory(category);
		
		//把pid获取出来，创建一个category parent，把pid赋给它，然后再把parent赋给category。
		if(map.get("pid")!=null){
			Category parent = new Category();
			parent.setCid((String)map.get("pid"));
			category.setParent(parent);
		}
		return book;
	}
	
	/**
	 * 多条件组合模糊查询
	 * @param criteria
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Book> findByCombination(Book criteria ,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("bname", "like","%"+criteria.getBname()+"%"));
		exprList.add(new Expression("author", "like","%"+criteria.getAuthor()+"%"));
		exprList.add(new Expression("press", "like","%"+criteria.getPress()+"%"));
		return findByCriteria(exprList, pc);
	}
	
	/**
	 * 按出版社模糊查询
	 * @param press
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Book> findByPress(String press ,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("press", "like","%"+press+"%"));
		return findByCriteria(exprList, pc);
	}
	
	/**
	 * 按作者模糊查询
	 * @param author
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Book> findByAuthor(String author ,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("author", "like","%"+author+"%"));
		return findByCriteria(exprList, pc);
	}
	
	/**
	 * 按书名模糊查询
	 * @param bname
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Book> findByBname(String bname ,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("bname", "like","%"+bname+"%"));
		return findByCriteria(exprList, pc);
	}
	
	/**
	 * 按分类查询
	 * @param cid
	 * @param pc
	 * @return
	 * @throws SQLException
	 */
	public PageBean<Book> findByCategory(String cid ,int pc) throws SQLException{
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("cid", "=",cid));
		return findByCriteria(exprList, pc);
	}
	
	
	/**
	 * 通用的查询方法
	 * 通过条件查询图书
	 * @param exprList
	 * @param pc
	 * @return	
	 * @throws SQLException
	 */
	private PageBean<Book> findByCriteria(List<Expression> exprList,int pc) throws SQLException{
		/**
		 * 1、得到ps（每页记录数）
		 * 2、得到tr（总记录数）
		 * 3、得到beanList
		 * 4、创建pageBean返回
		 */
		
		//得到每页记录数
		int ps =PageConstants.BOOK_PAGE_SIZE;
		
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
		String sql = "select count(*) from t_book"+whereSql;
		
		Number number = (Number) qr.query(sql, new ScalarHandler(), params.toArray());
		
		int tr = number.intValue();
		
		//当前记录（beanList）
		sql = "select * from t_book "+whereSql+" order by orderBy limit ?,?";
		
		params.add( (pc-1) * ps );//第一个问号,当前页首行记录下标
		params.add(ps);//第二个问号，每页记录数
		
		List<Book> beanList =   qr.query(sql, new BeanListHandler<Book>(Book.class), params.toArray());
		
		//创建pageBean，设置参数
		PageBean<Book> pb = new PageBean<Book>();
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
	 * 查询指定分类下图书的个数
	 * @param cid
	 * @return
	 * @throws SQLException
	 */
	public int findBookCountByCategory(String cid) throws SQLException{
		String sql = "select count(*) from t_book where cid=?";
		Number num = (Number) qr.query(sql, new ScalarHandler(), cid);
		return num == null ? 0 :num.intValue();
	}

	/**
	 * 添加图书
	 * @param book
	 * @throws SQLException 
	 */
	public void add(Book book) throws SQLException {
		String sql = "insert into t_book(bid,bname,author,price,currPrice,discount,press,publishtime,edition,pageNum,wordNum,printtime,booksize,paper,cid,image_w,image_b) "+
						"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] params = {book.getBid(),book.getBname(),book.getAuthor(),book.getPrice(),book.getCurrPrice(),book.getDiscount(),book.getPress(),book.getPublishtime(),book.getEdition(),book.getPageNum(),book.getWordNum(),book.getPrinttime(),book.getBooksize(),book.getPaper(),book.getCategory().getCid(),book.getImage_w(),book.getImage_b()};
		qr.update(sql,params);
	}
	
	
	/**
	 * 修改图书
	 * @param book
	 * @throws SQLException 
	 */
	public void edit(Book book) throws SQLException{
		String sql = "update t_book set bname=?,author=?,price=?,currPrice=?,discount=?,press=?,publishtime=?,edition=?,pageNum=?,wordNum=?,printtime=?,booksize=?,paper=?,cid=? where bid=?";
		Object[] params = {book.getBname(),book.getAuthor(),book.getPrice(),
						   book.getCurrPrice(),book.getDiscount(),book.getPress(),
						   book.getPublishtime(),book.getEdition(),book.getPageNum(),
						   book.getWordNum(),book.getPrinttime(),book.getBooksize(),
						   book.getPaper(),book.getCategory().getCid(),book.getBid()};
		qr.update(sql, params);
		
	}
	
	/**
	 * 删除图书
	 * @param bid
	 * @throws SQLException 
	 */
	public void delete(String bid) throws SQLException{
		String sql = "delete from t_book where bid=?";
		qr.update(sql, bid);
	}
	
}
