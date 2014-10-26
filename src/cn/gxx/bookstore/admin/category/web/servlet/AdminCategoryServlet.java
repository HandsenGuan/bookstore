package cn.gxx.bookstore.admin.category.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.book.service.BookService;
import cn.gxx.bookstore.caregory.domain.Category;
import cn.gxx.bookstore.caregory.service.CategoryService;
import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;

public class AdminCategoryServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	private CategoryService categoryService = new CategoryService();
	private BookService bookService = new BookService();
	
	/**
	 * 查询所有分类
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findAll(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("parents", categoryService.findAll());
		return "f:/adminjsps/admin/category/list.jsp";
	}
	
	/**
	 * 添加一级分类
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String  addParent(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException { 
		/**
		 * 1、封装表单数据到Category
		 * 2、调用service的add()方法完成添加
		 * 3、调用findAll(),返回list.jsp显示分类
		 */
		Category category = CommonUtils.toBean(request.getParameterMap(), Category.class);
		category.setCid(CommonUtils.uuid());
		categoryService.add(category);
		return findAll(request, response);
	}

	/**
	 * 添加二级分类：第一步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String addChildPre(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pid = request.getParameter("pid");//当前点击的父分类的id
		List<Category> list = categoryService.findParent();//获取所有的一级分类
		request.setAttribute("pid", pid);
		request.setAttribute("parents", list);
		return "f:/adminjsps/admin/category/add2.jsp";
	}
	
	/**
	 * 添加二级分类：第二步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String addChild(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/**
		 * 1、封装表单数据到Category
		 * 2、需要手动的把pid映射到child中
		 * 3、调用service的add()方法完成添加
		 * 4、调用findAll(),返回list.jsp显示分类
		 */
		Category child = CommonUtils.toBean(request.getParameterMap(), Category.class);
		child.setCid(CommonUtils.uuid());
		
		//手动映射pid
		String pid = request.getParameter("pid");
		Category parent = new Category();
		parent.setCid(pid);
		
		child.setParent(parent);
		
		categoryService.add(child);
		return findAll(request, response);
	}
	
	/**
	 * 修改一级分类：第一步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String editParentPre(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 *1、获取链接中的cid
		 *2、使用cid加载category
		 *3、保存category
		 *4、转发到edit.jsp页面显示category 
		 */
		String cid = request.getParameter("cid");
		Category parent = categoryService.load(cid);
		request.setAttribute("parent", parent);
		return "f:/adminjsps/admin/category/edit.jsp";
	}
	
	/**
	 * 修改一级分类：第二步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String editParent(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、封装表单数据到category中
		 * 2、调用service完成修改
		 * 3、转发到list.jsp显示所有分类
		 */
		Category category  = CommonUtils.toBean(request.getParameterMap(), Category.class);
		categoryService.edit(category);
		return findAll(request, response);
	}
	
	/**
	 * 修改二级分类：第一步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String editChildPre(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、获取链接参数cid，通过cid加载category，保存
		 * 2、查询出所有的一级分类，保存
		 * 3、转发到edit2.jsp
		 */
		String cid = request.getParameter("cid");
		Category child = categoryService.load(cid);
		List<Category> parents = categoryService.findParent();
		request.setAttribute("child", child);
		request.setAttribute("parents", parents);
		return "/adminjsps/admin/category/edit2.jsp";
	}
	
	/**
	 * 修改二级分类：第二步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String editChild(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、封装表单参数到category
		 * 2、把表单中的pid也封装到category中
		 * 3、调用service的edit（）方法完成修改
		 * 4、转发到list.jsp
		 */
		Category child = CommonUtils.toBean(request.getParameterMap(), Category.class);
		String pid = request.getParameter("pid");
		Category parent = new Category();
		parent.setCid(pid);
		child.setParent(parent);
		categoryService.edit(child);
		
		return findAll(request, response);
	}
	
	/**
	 * 删除一级分类
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String deleteParent(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、通过链接获取参数cid，它是一个一级分类
		 * 2、通过cid，查看该父分类下子分类的个数
		 * 3、如果大于0，说明还有子分类，不能删除。保存错误信息，转发到msg.jsp
		 * 4、如果等于0，说明没有子分类，删除，返回list.jsp	
		 */
		String cid = request.getParameter("cid");
		
		int count = categoryService.findChildrenCountByParent(cid);
		
		if(count==0){
			categoryService.delete(cid);
			return findAll(request, response);
		}else{
			request.setAttribute("msg", "该分类下还有子分类，不能删除！");
			return  "f:/adminjsps/msg.jsp";
		}
	}
	
	/**
	 * 删除二级分类
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String deleteChild(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、获取cid,即2级分类cid
		 * 2、查询该分类下的图书个数
		 * 3、如果大于0，保存错误信息，转发到msg.jsp
		 * 4、如果等于0，删除，转发到list.jsp
		 */
		String cid = request.getParameter("cid");
		int count = bookService.findBookCountByCategory(cid);
		
		if(count>0){
			request.setAttribute("msg", "该分类下存在图书，不能删除！");
			return "/adminjsps/msg.jsp";
		}else{
			categoryService.delete(cid);
			return findAll(request, response);
		}

	}
	
}
