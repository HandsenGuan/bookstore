package cn.gxx.bookstore.caregory.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.caregory.domain.Category;
import cn.gxx.bookstore.caregory.service.CategoryService;
import cn.itcast.servlet.BaseServlet;
/**
 * 分类模块WEB层
 *
 */
public class CategoryServlet extends BaseServlet{


	private static final long serialVersionUID = 1L;
	private CategoryService categoryService = new CategoryService();
	
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
		/**
		 * 1、通过service得到所有的分类
		 * 2、保存到request中，转发到left.jsp
		 */
		List<Category> parents = categoryService.findAll();
		request.setAttribute("parents", parents);
		return "f:/jsps/left.jsp";
	}
	
	
	
}
