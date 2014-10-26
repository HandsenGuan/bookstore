package cn.gxx.bookstore.caregory.service;

import java.sql.SQLException;
import java.util.List;

import cn.gxx.bookstore.caregory.dao.CategoryDao;
import cn.gxx.bookstore.caregory.domain.Category;

/**
 * 分类模块业务层
 *
 */
public class CategoryService {
	private CategoryDao categoryDao = new CategoryDao();
	
	/**
	 * 查询所有分类
	 * @return
	 */
	public List<Category> findAll(){
		try {
			return categoryDao.findAll();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 添加分类
	 * @param category
	 */
	public void add(Category category){
		try {
			categoryDao.add(category);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 查询所有的一级分类
	 * @return
	 */
	public List<Category> findParent(){
		try {
			return categoryDao.findParents();
		} catch (SQLException e) {
		throw new RuntimeException(e);
		}
	}

	/**
	 * 加载分类
	 * @param cid
	 * @return
	 */
	public Category load(String cid){
		try {
			return categoryDao.load(cid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 修改分类
	 * @param category
	 */
	public void edit(Category category){
		try {
			categoryDao.edit(category);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 查询指定父分类下的子分类个数
	 * @param pid
	 * @return
	 */
	public int  findChildrenCountByParent(String pid){
		try {
			return categoryDao.findChildrenCountByParent(pid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 删除分类
	 * @param cid
	 */
	public void delete(String cid){
		try {
			categoryDao.delete(cid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 查询指定父分类下的所有子分类
	 * @param pid
	 * @return
	 */
	public List<Category> findByParent(String pid){
		try {
			return categoryDao.findByParent(pid);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
}
