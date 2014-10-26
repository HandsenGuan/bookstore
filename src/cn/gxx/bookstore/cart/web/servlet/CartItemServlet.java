package cn.gxx.bookstore.cart.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.cart.domain.CartItem;
import cn.gxx.bookstore.cart.service.CartItemService;
import cn.gxx.bookstore.user.domain.User;
import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;

public class CartItemServlet extends BaseServlet {
	
	private static final long serialVersionUID = 1L;
	private CartItemService cartItemService = new CartItemService();
	
	/**
	 * 通过用户id查询购物车条目
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String myCart(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//从session中取出当前登录的用户得到uid
		User user = (User) request.getSession().getAttribute("sessionUser");
		String uid = user.getUid();
		//通过CartItemService获取所有购物车条目
		List<CartItem> cartItemList = cartItemService.myCart(uid);
		//保存到request中，转发到/jsps/cart/list.jsp
		request.setAttribute("cartItemList", cartItemList);
		
		return "f:/jsps/cart/list.jsp";
	}
	
	/**
	 * 添加购物车条目
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String add(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//封装表单数据到cartitem
		CartItem cartItem = CommonUtils.toBean(request.getParameterMap(), CartItem.class);//将表单中quantity封装到cartitem
		Book book = CommonUtils.toBean(request.getParameterMap(), Book.class);//将bid封装到book中
		User user = (User) request.getSession().getAttribute("sessionUser");//从session中取出当前登录的用户得到uid
		cartItem.setBook(book);
		cartItem.setUser(user);
		
		//调用service完成添加
		cartItemService.add(cartItem);
		
		//查询出当前用户的所有条目，转发给list.jsp
		return myCart(request, response);
	}
	
	/**
	 * 批量删除功能
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String batchDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//获取cartItemIds参数
		String cartItemIds =  request.getParameter("cartItemIds");
		//调用service方法批量删除
		cartItemService.batchDelete(cartItemIds);
		//返回到list.jsp
		return myCart(request, response);
	}

	/**
	 * 修改购物车条目数量
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String updateQuantity(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//从客户端获取购物车条目id和数量
		String cartItemId = request.getParameter("cartItemId");
		int quantity = Integer.parseInt(request.getParameter("quantity"));
		
		//修改购物车条目数量并从数据库中返回该条目数量
		CartItem cartItem = cartItemService.updateQuantity(cartItemId, quantity);
		
		//给客户端返回一个json对象（包含该条目的数量和小计）
		StringBuilder sb = new StringBuilder("{");
		sb.append("\"quantity\"").append(":").append(cartItem.getQuantity());
		sb.append(",");
		
		sb.append("\"subtotal\"").append(":").append(cartItem.getSubtotal());

		sb.append("}");
		
		response.getWriter().print(sb);
		
		return null;
	}

	/**
	 * 加载多个cartItem
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String loadCartItems(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//获取cartItemIds参数
		String cartItemIds = request.getParameter("cartItemIds");
		
		//获取总计值
		double total = Double.parseDouble(request.getParameter("total"));
		
		//得到多个cartItem
		List<CartItem> list = cartItemService.loadCartItems(cartItemIds);
		//保存，转发给/cart/showitem.jsp
		request.setAttribute("cartItemList", list);
		
		request.setAttribute("total", total);
		
		//保存cartItemIds，转发给showitem.jsp
		request.setAttribute("cartItemIds", cartItemIds);
		
		return "f:/jsps/cart/showitem.jsp";
		
	}
	
}
