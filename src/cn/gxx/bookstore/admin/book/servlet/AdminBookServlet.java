package cn.gxx.bookstore.admin.book.servlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.book.service.BookService;
import cn.gxx.bookstore.caregory.domain.Category;
import cn.gxx.bookstore.caregory.service.CategoryService;
import cn.gxx.bookstore.pager.PageBean;
import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;

public class AdminBookServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	
	private BookService bookService = new BookService();
	
	private CategoryService categoryService = new CategoryService();
	
	/**
	 * 删除图书
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String delete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String bid = request.getParameter("bid");
		//删除图片
		Book book = bookService.load(bid);
		String savepath = this.getServletContext().getRealPath("/");//获取真实路径
		new File(savepath, book.getImage_b()).delete();//删除图片
		new File(savepath, book.getImage_w()).delete();//删除图片
		
		//删除数据库记录
		bookService.delete(bid);
		request.setAttribute("msg", "删除成功！");
		return "f:/adminjsps/msg.jsp";
	}
	
	/**
	 * 修改图书
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String edit(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、把表单中的数据封装到book对象中
		 * 2、封装cid到Category中
		 * 3、把Category赋给book
		 * 4、调用service完成修改
		 * 5、保存信息，转发到msg.jsp
		 */
		Map map = request.getParameterMap();
		Book book = CommonUtils.toBean(map, Book.class);
		Category category = CommonUtils.toBean(map, Category.class);
		book.setCategory(category);
		
		bookService.edit(book);
		
		request.setAttribute("msg", "修改图书成功！");
		
		return "f:/adminjsps/msg.jsp";
	}
	
	/**
	 * 加载图书
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String load(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 *1、获取bid得到Book对象，保存 
		 */
		String bid = request.getParameter("bid");
		Book book = bookService.load(bid);
		request.setAttribute("book", book);
		
		/*
		 * 2、获取所有的一级分类，保存
		 */
		List<Category> parent = categoryService.findParent();
		request.setAttribute("parents", parent);
		
		/*
		 * 3、获取当前图书所属的一级分类下的所有二级分类
		 */
		String pid = book.getCategory().getParent().getCid();
		List<Category> children = categoryService.findByParent(pid);
		request.setAttribute("children", children);		
		
		/*
		 * 转发到desc.jsp
		 */
		return "f:/adminjsps/admin/book/desc.jsp";
	}
	
	
	/**
	 * 查询所有分类
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findCategoryAll(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/**
		 * 1、通过service得到所有的分类
		 * 2、保存到request中，转发到left.jsp
		 */
		List<Category> parents = categoryService.findAll();
		request.setAttribute("parents", parents);
		return "f:/adminjsps/admin/book/left.jsp";
	}
	
	
	/**
	 * 多条件组合查询
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findByCombination(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int pc = getPc(request);
		
		String url = getUrl(request);
		
		Book criteria = CommonUtils.toBean(request.getParameterMap(), Book.class);
		
		PageBean<Book> pageBean = bookService.findByCombination(criteria, pc);
		
		pageBean.setUrl(url);
		
		request.setAttribute("pb", pageBean);
		
		return "f:/adminjsps/admin/book/list.jsp";
	}
	
	/**
	 * 按作者查
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findByAuthor(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int pc = getPc(request);
		
		String url = getUrl(request);
		
		String author = request.getParameter("author");
		
		PageBean<Book> pageBean = bookService.findByAuthor(author, pc);
		
		pageBean.setUrl(url);
		
		
		request.setAttribute("pb", pageBean);
		
		return "f:/adminjsps/admin/book/list.jsp";
	}
	
	/**
	 * 按出版社查询
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findByPress(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int pc = getPc(request);
		
		String url = getUrl(request);
		
		String press = request.getParameter("press");
		
		PageBean<Book> pageBean = bookService.findByPress(press, pc);
		
		pageBean.setUrl(url);
		
		request.setAttribute("pb", pageBean);
		
		return "f:/adminjsps/admin/book/list.jsp";
	}
	
	/**
	 * 按书名查
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findByBname(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int pc = getPc(request);
		
		String url = getUrl(request);
		
		String bname = request.getParameter("bname");
		
		PageBean<Book> pageBean = bookService.findByBname(bname, pc);
		
		pageBean.setUrl(url);
		
		request.setAttribute("pb", pageBean);
		
		return "f:/adminjsps/admin/book/list.jsp";
	}
	
	/**
	 * 根据分类查询
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findByCategory(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/**
		 * 1、得到pc（当前页），如果页面传，使用页面的。如果没传pc=1.
		 * 2、得到url
		 * 3、获取查询条件，本方法就是cid，及分类cid
		 * 4、使用pc和cid调用service的findByCategory得到pageBean
		 * 5、给pageBean设置url，保存pageBean，转发到/jsps/book/list.jsp
		 */
		
		int pc = getPc(request);
		
		String url = getUrl(request);
		
		String cid = request.getParameter("cid");
		
		PageBean<Book> pageBean = bookService.findByCategory(cid, pc);
		
		pageBean.setUrl(url);
		
		
		request.setAttribute("pb", pageBean);
		
		return "f:/adminjsps/admin/book/list.jsp";
		
	}
	
	/**
	 * 添加图书：第一步
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String addPre(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<Category> parents = categoryService.findParent();
		request.setAttribute("parents",parents);
		return "/adminjsps/admin/book/add.jsp";
	}
	
	public String ajaxFindChildren(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pid = request.getParameter("pid");
		List<Category> children = categoryService.findByParent(pid);
		String json = toJson(children);
		response.getWriter().print(json);
		return null;
	}
	
	public String add(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		return "";
	}
	
	/**
	 * 截取url，页面中的分页导航需要使用它作为超链接目标！
	 *  http://localhost:8080/bookstore/BookServlet?method=findByCategory&cid=xxx
	 *  getRequestURL()-->/bookstore/BookServlet
	 * getQueryString()-->method=findByCategory&cid=xxx
	 * @param request
	 * @return
	 */
	private String getUrl(HttpServletRequest request){
		String url = request.getRequestURL()+"?"+request.getQueryString();
		/**
		 * 如果url存在pc参数，截取掉，如果不存在那就不用截取。
		 */
		int index = url.lastIndexOf("&pc=");
		if(index != -1){
			url = url.substring(0, index);
		}
		return url;
	}
	
	/**
	 * 返回当前页
	 * @param request
	 * @return
	 */
	private int getPc(HttpServletRequest request){
		int pc = 1;
		String param = request.getParameter("pc");
		if(param!=null && !param.trim().isEmpty()){
			try {
				pc = Integer.valueOf(param);
			} catch (RuntimeException e) {}
		}
		return pc;
	}

	//一个json对象  {"cid":"xxx","cname":"xxx"}
	private String toJson(Category category){
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"cid\"").append(":").append("\"").append(category.getCid()).append("\"");
		sb.append(",");
		sb.append("\"cname\"").append(":").append("\"").append(category.getCname()).append("\"");
		sb.append("}");
		return sb.toString();
	}
	
	//json数组  [{"cid":"xxx","cname":"xxx"},{},{}]
	private String toJson(List<Category> categoryList) {
		StringBuilder sb = new StringBuilder("[");
		for(int i=0;i<categoryList.size();i++){
			sb.append(toJson(categoryList.get(i)));
			if(i<categoryList.size()-1){
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
