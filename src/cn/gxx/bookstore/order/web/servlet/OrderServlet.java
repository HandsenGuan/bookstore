package cn.gxx.bookstore.order.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.cart.domain.CartItem;
import cn.gxx.bookstore.cart.service.CartItemService;
import cn.gxx.bookstore.order.domain.Order;
import cn.gxx.bookstore.order.domain.OrderItem;
import cn.gxx.bookstore.order.service.OrderService;
import cn.gxx.bookstore.pager.PageBean;
import cn.gxx.bookstore.user.domain.User;
import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;

public class OrderServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private OrderService orderService = new OrderService();
	private CartItemService cartItemService = new CartItemService();

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
	 * 我的订单
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String myOrders(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/**
		 * 1、得到pc（当前页），如果页面传，使用页面的。如果没传pc=1. 2、得到url 3、获取查询条件，本方法就是uid，及登录用户uid
		 * 4、使用pc和uid调用service的myOrder得到pageBean
		 * 5、给pageBean设置url，保存pageBean，转发到/jsps/book/list.jsp
		 */

		int pc = getPc(request);

		String url = getUrl(request);

		User user = (User) request.getSession().getAttribute("sessionUser");

		PageBean<Order> pageBean = orderService.myOrder(user.getUid(), pc);

		pageBean.setUrl(url);

		request.setAttribute("pb", pageBean);

		return "f:/jsps/order/list.jsp";
	}

	/**
	 * 生成订单
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String createOrder(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * 1、获取所有购物车条目id，查询之
		 */
		String cartItemIds = request.getParameter("cartItemIds");
		List<CartItem> cartItemList = cartItemService
				.loadCartItems(cartItemIds);

		/*
		 * 2、创建order
		 */
		Order order = new Order();
		order.setOid(CommonUtils.uuid());// 设置主键
		order.setOrdertime(String.format("%tF %<tT", new Date()));// 设置下单时间
		order.setStatus(1);// 设置状态为1，未付款
		order.setAddress(request.getParameter("address"));// 设置地址

		User user = (User) request.getSession().getAttribute("sessionUser");
		order.setOwner(user);// 设置订单所有者

		BigDecimal total = new BigDecimal("0");
		for (CartItem cartItem : cartItemList) {
			total = total.add(new BigDecimal(cartItem.getSubtotal() + ""));
		}
		order.setTotal(total.doubleValue());// 设置总计

		/*
		 * 3、创建orderItem 一个CartItem对应一个OrderItem
		 */
		List<OrderItem> orderItemList = new ArrayList<OrderItem>();
		for (CartItem cartItem : cartItemList) {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrderItemId(CommonUtils.uuid());// 设置主键
			orderItem.setQuantity(cartItem.getQuantity());// 设置数量
			orderItem.setSubtotal(cartItem.getSubtotal());// 设置小计
			orderItem.setBook(cartItem.getBook());// 设置图书
			orderItem.setOrder(order);// 设置订单

			orderItemList.add(orderItem);
		}

		order.setOrderItemList(orderItemList);

		/*
		 * 4、调用service完成订单创建
		 */
		orderService.createOrder(order);

		// 删除购物车条目
		cartItemService.batchDelete(cartItemIds);

		/*
		 * 5、保存订单，转发到ordersucc.jsp
		 */
		request.setAttribute("order", order);
		return "f:/jsps/order/ordersucc.jsp";

	}

	/**
	 * 加载订单
	 * 
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

		return "f:/jsps/order/desc.jsp";

	}

	/**
	 * 取消订单
	 * 
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
		return "f:/jsps/msg.jsp";

	}

	/**
	 * 确认收货
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String confirm(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String oid = request.getParameter("oid");

		int status = orderService.findStatus(oid);
		if (status != 3) {// 不是付款未发货状态
			request.setAttribute("code", "error");
			request.setAttribute("msg", "状态不对，不能确认收货!");
			return "f:/jsps/msg.jsp";
		}

		orderService.updateStatus(oid, 4);// 设置状态为交易成功
		request.setAttribute("code", "success");
		request.setAttribute("msg", "交易成功!");
		return "f:/jsps/msg.jsp";
	}

	/**
	 * 支付准备工作
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String paymentPre(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String oid = request.getParameter("oid");

		Order order = orderService.load(oid);

		request.setAttribute("order", order);
		return "f:/jsps/order/pay.jsp";
	}

	/**
	 * 支付方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String payment(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader()
				.getResourceAsStream("payment.properties"));
		/*
		 * 1、准备13个参数
		 */
		String p0_Cmd = "Buy";// 业务类型，固定值Buy
		String p1_MerId = properties.getProperty("p1_MerId");// 商号编码，在易宝的唯一标识
		String p2_Order = request.getParameter("oid");// 订单编码
		String p3_Amt = "0.01";// 支付金额
		String p4_Cur = "CNY";// 交易币种，固定值
		String p5_Pid = "";// 商品名称
		String p6_Pcat = "";// 商品种类
		String p7_Pdesc = "";// 商品描述
		String p8_Url = properties.getProperty("p8_Url");// 在支付成功后，易宝会访问这个地址
		String p9_SAF = "";// 送货地址
		String pa_MP = "";// 商户扩展信息
		String pd_FrpId = request.getParameter("yh");// 支付通道编码
		String pr_NeedResponse = "1";// 应答机制，固定值

		/*
		 * 2、计算hmac 需要13个参数 需要keyValue 需要加密算法
		 */
		String keyValue = properties.getProperty("keyValue");
		String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
				p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
				pd_FrpId, pr_NeedResponse, keyValue);

		/*
		 * 3、重定向到易宝的支付网关 https://www.yeepay.com/app-merchant-proxy/node
		 */
		StringBuilder sb = new StringBuilder(
				"https://www.yeepay.com/app-merchant-proxy/node");

		sb.append("?").append("p0_Cmd=").append(p0_Cmd);
		sb.append("&").append("p1_MerId=").append(p1_MerId);
		sb.append("&").append("p2_Order=").append(p2_Order);
		sb.append("&").append("p3_Amt=").append(p3_Amt);
		sb.append("&").append("p4_Cur=").append(p4_Cur);
		sb.append("&").append("p5_Pid=").append(p5_Pid);
		sb.append("&").append("p6_Pcat=").append(p6_Pcat);
		sb.append("&").append("p7_Pdesc=").append(p7_Pdesc);
		sb.append("&").append("p8_Url=").append(p8_Url);
		sb.append("&").append("p9_SAF=").append(p9_SAF);
		sb.append("&").append("pa_MP=").append(pa_MP);
		sb.append("&").append("pd_FrpId=").append(pd_FrpId);
		sb.append("&").append("pr_NeedResponse=").append(pr_NeedResponse);
		sb.append("&").append("hmac=").append(hmac);

		response.sendRedirect(sb.toString());
		return null;
	}

	/**
	 * 回馈方法 当支付成功时，易宝会访问这里 用两种方法访问： 1、引导用户的浏览器重定向（如果关闭了浏览器，就不能访问这里了）
	 * 2、易宝的服务器会使用点对点通讯的方法访问这个方法。（必须回馈success，不然易宝服务器会一直调用这个方法）
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String back(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1、获取12个参数
		 */
		String p1_MerId = request.getParameter("p1_MerId");
		String r0_Cmd = request.getParameter("r0_Cmd");
		String r1_Code = request.getParameter("r1_Code");
		String r2_TrxId = request.getParameter("r2_TrxId");
		String r3_Amt = request.getParameter("r3_Amt");
		String r4_Cur = request.getParameter("r4_Cur");
		String r5_Pid = request.getParameter("r5_Pid");
		String r6_Order = request.getParameter("r6_Order");
		String r7_Uid = request.getParameter("r7_Uid");
		String r8_MP = request.getParameter("r8_MP");
		String r9_BType = request.getParameter("r9_BType");
		String hmac = request.getParameter("hmac");

		/*
		 * 2、获取keyValue
		 */
		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader()
				.getResourceAsStream("payment.properties")); 
		String keyValue = properties.getProperty("keyValue");

		/*
		 * 3、调用PaymentUtil的校验方法来校验调用者的身份 
		 * 	》如果校验失败：保存错误信息，转发到msg.jsp 
		 * 	》如果校验通过：
		 * 		*判断是重定向还是点对点 *如果是重定向：修改订单状态，保存成功信息，转发到msg.jsp
		 * 		*如果是点对点：修改订单状态，返回success
		 */
		boolean bool = PaymentUtil.verifyCallback(hmac, p1_MerId, r0_Cmd,
				r1_Code, r2_TrxId, r3_Amt, r4_Cur, r5_Pid, r6_Order, r7_Uid,
				r8_MP, r9_BType, keyValue);
		
		if( !bool ){
			
			request.setAttribute("code", "error");
			request.setAttribute("msg", "无效的签名，支付失败！");
			return "f:/jsps/msg.jsp";
		}
		if(r1_Code.equals("1")){	//支付成功
				
			orderService.updateStatus(r6_Order, 2);//修改订单状态
			if(r9_BType.equals("1")){ //交易结果返回类型, 为“1”: 浏览器重定向;
				request.setAttribute("code", "success"); 
				request.setAttribute("msg", "恭喜，支付成功！");
				return "f:/jsps/msg.jsp";
			}else if(r9_BType.equals("2")){ //交易结果返回类型,为“2”: 服务器点对点通讯.
				response.getWriter().print("success");
			}
		}
		return null;
	}

}
