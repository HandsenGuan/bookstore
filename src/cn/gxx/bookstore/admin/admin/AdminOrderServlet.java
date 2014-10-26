package cn.gxx.bookstore.admin.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.order.domain.Order;
import cn.gxx.bookstore.order.service.OrderService;
import cn.gxx.bookstore.pager.PageBean;
import cn.gxx.bookstore.user.domain.User;
import cn.itcast.servlet.BaseServlet;

public class AdminOrderServlet extends BaseServlet {
	private OrderService orderService = new OrderService();

	/**
	 * 查询所有订单
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findAll(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/**
		 * 1、得到pc（当前页），如果页面传，使用页面的。如果没传pc=1. 2、得到url 3、获取查询条件，本方法就是uid，及登录用户uid
		 * 4、使用pc和uid调用service的myOrder得到pageBean
		 * 5、给pageBean设置url，保存pageBean，转发到/jsps/book/list.jsp
		 */

		int pc = getPc(request);

		String url = getUrl(request);

		User user = (User) request.getSession().getAttribute("sessionUser");

		PageBean<Order> pageBean = orderService.findAll(pc);

		pageBean.setUrl(url);

		request.setAttribute("pb", pageBean);

		return "f:/adminjsps/admin/order/list.jsp";
	}
	
	/**
	 * 按状态查询
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String findByStatus(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/**
		 * 1、得到pc（当前页），如果页面传，使用页面的。如果没传pc=1. 
		 * 2、得到url 
		 * 3、获取查询条件，本方法就是uid，及登录用户uid
		 * 4、使用pc和uid调用service的myOrder得到pageBean
		 * 5、给pageBean设置url，保存pageBean，转发到/jsps/book/list.jsp
		 */

		int pc = getPc(request);

		String url = getUrl(request);

		User user = (User) request.getSession().getAttribute("sessionUser");

		int status = Integer.parseInt(request.getParameter("status"));
		
		PageBean<Order> pageBean = orderService.findByStatus(status,pc);

		pageBean.setUrl(url);

		request.setAttribute("pb", pageBean);

		return "f:/adminjsps/admin/order/list.jsp";
	}
	
	/**
	 * 查看订单详细信息
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String load(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String oid = request.getParameter("oid");

		Order order = orderService.load(oid);

		request.setAttribute("order", order);

		String btn = request.getParameter("btn");// btn说明用户点哪个超链接来访问本方法的
		request.setAttribute("btn", btn);

		return "f:/adminjsps/admin/order/desc.jsp";

	}
	
	
	/**
	 * 返回当前页
	 * 
	 * @param request
	 * @return
	 */
	private int getPc(HttpServletRequest request) {
		int pc = 1;
		String param = request.getParameter("pc");
		if (param != null && !param.trim().isEmpty()) {
			try {
				pc = Integer.valueOf(param);
			} catch (RuntimeException e) {
			}
		}
		return pc;
	}

	/**
	 * 截取url，页面中的分页导航需要使用它作为超链接目标！
	 * http://localhost:8080/bookstore/BookServlet?method=findByCategory&cid=xxx
	 * getRequestURL()-->/bookstore/BookServlet
	 * getQueryString()-->method=findByCategory&cid=xxx
	 * 
	 * @param request
	 * @return
	 */
	private String getUrl(HttpServletRequest request) {
		String url = request.getRequestURL() + "?" + request.getQueryString();
		/**
		 * 如果url存在pc参数，截取掉，如果不存在那就不用截取。
		 */
		int index = url.lastIndexOf("&pc=");
		if (index != -1) {
			url = url.substring(0, index);
		}
		return url;
	}
	
	/**
	 * 取消订单
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String cancel(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String oid = request.getParameter("oid");

		int status = orderService.findStatus(oid);
		if (status != 1) {// 不是未付款状态
			request.setAttribute("code", "error");
			request.setAttribute("msg", "状态不对，不能取消!");
			return "f:/jsps/msg.jsp";
		}

		orderService.updateStatus(oid, 5);// 设置状态为取消
		request.setAttribute("code", "success");
		request.setAttribute("msg", "您的订单已取消!");
		return "f:/adminjsps/msg.jsp";

	}
	
	/**
	 * 发货
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String deliver(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String oid = request.getParameter("oid");

		int status = orderService.findStatus(oid);
		if (status != 2) {// 不是未付款状态
			request.setAttribute("code", "error");
			request.setAttribute("msg", "状态不对，不能发货!");
			return "f:/jsps/msg.jsp";
		}

		orderService.updateStatus(oid, 3);// 设置状态为发货
		request.setAttribute("code", "success");
		request.setAttribute("msg", "您的订单已发货!");
		return "f:/adminjsps/msg.jsp";

	}

}
