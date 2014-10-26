package cn.gxx.bookstore.caregory.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import cn.gxx.bookstore.caregory.domain.Category;
import cn.itcast.commons.CommonUtils;
import cn.itcast.jdbc.TxQueryRunner;

/**
 * 分类的持久层
 *
 */
public class CategoryDao {
	private QueryRunner qr = new TxQueryRunner();
	
	/**
	 * 查找所有的分类
	 * @return
	 * @throws SQLException
	 */
	public List<Category> findAll() throws SQLException{
		String sql = "select * from t_category where pid is null order by orderBy";
		List<Map<String,Object>> mapList = qr.query(sql, new MapListHandler());//将表中所有的数据封装到map链表中
		
		List<Category> parents = toCategorieList(mapList);
		for(Category parent:parents){
			//查询出当前父分类的所有子分类
			List<Category> children = findByParent(parent.getCid());
			//设置给父分类
			parent.setChildren(children);
		}
		return parents;
	}
	
	/**
	 * 通过父分类id查询二级分类
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	public List<Category> findByParent(String pid) throws SQLException{
		String sql = "select * from t_category where pid=? order by orderBy";
		 List<Map<String, Object>> mapList =  qr.query(sql, new MapListHandler(), pid);
		return toCategorieList(mapList);
	}
	
	/**
	 * 把多个map映射成多个category
	 * @param mapList
	 * @return
	 */
	private List<Category> toCategorieList(List<Map<String,Object>> mapList){
		List<Category> categoryList = new ArrayList<Category>();
		for(Map<String,Object> map:mapList){
			Category c = toCategory(map);
			categoryList.add(c);
		}
		return categoryList;
	}
	
	/**
	 * 把一个map中的数据映射到category中
	 * @param map
	 * @return
	 */
	private Category toCategory(Map<String ,Object> map){
		/**
		 * map{cid	cname	pid	desc	orderBy}
		 * category{cid	cname	parent	children		desc 	orderBy}
		 */
		Category category = CommonUtils.toBean(map, Category.class);
		
		String pid = (String) map.get("pid");
		if(pid!=null){//没有父分类，该分类为二级分类
			/*
			 * 如果父分类id不为空
			 * 使用一个父分类对象装载pid
			 * 再把父分类设置给category
			 */
			Category parent = new Category();
			parent.setCid(pid);
			category.setParent(parent);
		}
		return category;
	}
	
	/**
	 * 添加分类
	 * @param category
	 * @throws SQLException 
	 */
	public void add(Category category) throws SQLException{
		String sql ="insert into t_category(cid,cname,pid,`desc`) value(?,?,?,?)";//desc为mysql中的关键字，所有必须用反单引号包裹
		/**
		 * 因为一级分类没有parent，而二级分类有！
		 */
		String pid = null;//一级分类
		if(category.getParent()!=null){
			pid = category.getParent().getCid();
		}
		Object[] params = {category.getCid(),category.getCname(),pid,category.getDesc()};
		qr.update(sql, params);
		
	}
	
	/**
	 * 查找所有的一级分类
	 * @return
	 * @throws SQLException
	 */
	public List<Category> findParents() throws SQLException{
		String sql = "select * from t_category where pid is null order by orderBy";
		List<Map<String,Object>> mapList = qr.query(sql, new MapListHandler());//将表中所有的数据封装到map链表中
		List<Category> parents = toCategorieList(mapList);
		return parents;
	}

	/**
	 * 加载分类
	 * 既可加载一级分类，也可加载而家分类
	 * @param cid
	 * @return
	 * @throws SQLException 
	 */
	public Category load(String cid) throws SQLException{
		String sql = "select * from t_category where cid=?";
		return toCategory(qr.query(sql, new MapHandler(), cid));
	}

	/**
	 * 修改分类
	 * 既可修改一级分类也可修改二级分类
	 * @param category
	 * @throws SQLException 
	 */
	public void edit(Category category) throws SQLException{
		String sql = "update t_category set cname=?,pid=?,`desc`=? where cid=?";
		String pid = null;
		if(category.getParent()!=null){
			pid = category.getParent().getCid();
		}
		Object[] params = {category.getCname(),pid,category.getDesc(),category.getCid()};
		qr.update(sql, params);
	}
	
	
	/**
	 * 查询指定一级分类下的二级分类个数
	 * @param pid
	 * @return
	 * @throws SQLException 
	 */
	public int  findChildrenCountByParent(String pid) throws SQLException{
		String sql = "select count(*) from t_category where pid=?";
		Number number = (Number) qr.query(sql, new ScalarHandler(), pid);
		
		return number == null ? 0 : number.intValue();
	}
	
	/**
	 * 删除分类
	 * @param cid
	 * @throws SQLException
	 */
	public void delete(String cid) throws SQLException{
		String sql = "delete from t_category where cid=?";
		qr.update(sql, cid);
	}
}
