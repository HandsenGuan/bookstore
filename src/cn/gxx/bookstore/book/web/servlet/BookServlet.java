package cn.gxx.bookstore.book.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.book.service.BookService;
import cn.gxx.bookstore.pager.PageBean;
import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;

public class BookServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	private BookService bookService = new BookService();
	
	/**
	 * 通过bid查询
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String load(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String bid = request.getParameter("bid");
		Book book = bookService.load(bid);
		request.setAttribute("book", book);
		return "f:/jsps/book/desc.jsp";
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
		
		return "f:/jsps/book/list.jsp";
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
		
		return "f:/jsps/book/list.jsp";
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
		
		return "f:/jsps/book/list.jsp";
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
		
		return "f:/jsps/book/list.jsp";
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
		
		return "f:/jsps/book/list.jsp";
		
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
	
}
