package cn.gxx.bookstore.user.web.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cn.gxx.bookstore.user.domain.User;
import cn.gxx.bookstore.user.service.UserService;
import cn.gxx.bookstore.user.service.exception.UserException;
import cn.itcast.commons.CommonUtils;
import cn.itcast.servlet.BaseServlet;
/**
 * 用户模块WEB层
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
public class UserServlet extends BaseServlet {
	private UserService  userService= new UserService();
	
	/**
	 * ajax用户名是否注册校验
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String ajaxValidateLoginname(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//1、获取用户名
		String loginname = request.getParameter("loginname");
		
		//2、通过service得到校验结果
		boolean b = userService.ajaxValidateLoginname(loginname);
		
		//3、发给客户端
		response.getWriter().print(b);
		
		return null;
	}

	/**
	 * email是否注册校验
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String ajaxValidateEmail(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//1、获取用户名
				String email = request.getParameter("email");
				
				//2、通过service得到校验结果
				boolean b = userService.ajaxValidateEmail(email);
				
				//3、发给客户端
				response.getWriter().print(b);
		return null;
	}
	
	/**
	 * 验证码是否注册校验
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String ajaxValidateVerifyCode(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//1、获取输入框中的校验码
		String verifyCode = request.getParameter("verifyCode");
		//2、获取图片上的真是的验证码
		String vcode = (String) request.getSession().getAttribute("vCode");
		//3、进行忽略大小写比较，得到结果
		boolean b = verifyCode.equalsIgnoreCase(vcode);  
		//4、发送给客户端
		response.getWriter().print(b);
		return null;
	}
	
	/**
	 * 注册功能
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String regist(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		//1、封装表单到User对象中
		User  formUser = CommonUtils.toBean(request.getParameterMap(), User.class);
		
		//2、校验
		Map<String,String> errors = this.validateRegist(formUser ,request.getSession());
		if(errors.size()>0){
			request.setAttribute("form", formUser);
			request.setAttribute("errors", errors);
			return "f:/jsps/user/regist.jsp";
		}
		
		//3、使用service完成业务
		userService.regist(formUser);
		
		//4、保存成功信息，转发到msg.jsp显示！
		request.setAttribute("code", "success");
		request.setAttribute("msg", "注册成功，请马上到邮箱激活");
		return "f:/jsps/msg.jsp";
	}
	
	/**
	 * 注册校验
	 * @param formUser
	 * @param session
	 * @return
	 */
	private Map<String,String> validateRegist(User formUser , HttpSession session){
		Map<String,String> errors = new HashMap<String, String>();
		//1、校验登录名
		String loginname = formUser.getLoginname();
		if(loginname==null || loginname.trim().isEmpty()){
			errors.put("loginname", "用户名不能为空！");
		}else if(loginname.length()<3 || loginname.length()>20){
			errors.put("loginname", "用户名长度必须在3-20之间！");
		}else if(!userService.ajaxValidateLoginname(loginname)){
			errors.put("loginname", "用户名已被注册！");
		}
		
		//2、校验登录密码
		String loginpass = formUser.getLoginpass();
		if(loginpass==null || loginpass.trim().isEmpty()){
			errors.put("loginpass", "密码不能为空！");
		}else if(loginpass.length()<3 || loginpass.length()>20){
			errors.put("loginpass", "密码长度必须在3-20之间！");
		}
		
		//3、校验确认登录密码
				String reloginpass = formUser.getReloginpass();
				if(reloginpass==null || reloginpass.trim().isEmpty()){
					errors.put("reloginpass", "密码不能为空！");
				}else if(!reloginpass.equals(loginpass)){
					errors.put("reloginpass", "两次输入不一致！");
				}
		
		//4、校验email
		String email = formUser.getEmail();
		if(email==null || email.trim().isEmpty()){
			errors.put("email", "Email不能为空！");
		}else if(!email.matches("^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,3}){1,2})$")){
			errors.put("email", "Email格式错误！");
		}else if(!userService.ajaxValidateEmail(email)){
			errors.put("email", "该邮箱已被注册！");
		}		
		
		//5、校验验证码
				String verifyCode = formUser.getVerifyCode();
				String vCode = (String) session.getAttribute("vCode");
				if(verifyCode==null || verifyCode.trim().isEmpty()){
					errors.put("verifyCode", "验证码不能为空！");
				}else if(!verifyCode.equalsIgnoreCase(vCode)){
					errors.put("verifyCode", "验证码错误！");
				}
				
		return errors;		
	}
	
	/**
	 * 登录校验
	 * @param formUser
	 * @param session
	 * @return
	 */
	private Map<String,String> validateLogin(User formUser , HttpSession session){
		Map<String,String> errors = new HashMap<String, String>();
		//1、校验登录名
				String loginname = formUser.getLoginname();
				if(loginname==null || loginname.trim().isEmpty()){
					errors.put("loginname", "用户名不能为空！");
				}else if(loginname.length()<2 || loginname.length()>20){
					errors.put("loginname", "用户名长度必须在2-20之间！");
				}
				
				//2、校验登录密码
				String loginpass = formUser.getLoginpass();
				if(loginpass==null || loginpass.trim().isEmpty()){
					errors.put("loginpass", "密码不能为空！");
				}else if(loginpass.length()<3 || loginpass.length()>20){
					errors.put("loginpass", "密码长度必须在3-20之间！");
				}
				
				//3、校验验证码
				String verifyCode = formUser.getVerifyCode();
				String vCode = (String) session.getAttribute("vCode");
				if(verifyCode==null || verifyCode.trim().isEmpty()){
					errors.put("verifyCode", "验证码不能为空！");
				}else if(!verifyCode.equalsIgnoreCase(vCode)){
					errors.put("verifyCode", "验证码错误！");
				}
						
		return errors;		
	}
	
	
	/**
	 * 激活功能
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	public String activation(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/**
		 * 1、获取参数激活码
		 * 2、用激活码调用service方法完成激活
		 * 	》service方法有可能抛出异常,把异常信息保存到request中，转发到msg.jsp。
		 * 3、保存成功信息到request，转发到msg.jsp
		 */
		String code = (String) request.getParameter("activationCode");
		try {
			userService.activation(code);
			request.setAttribute("code", "success");//通知msg.jsp显示正确页面
			request.setAttribute("msg", "恭喜激活成功！");
		} catch (UserException e) {
			//抛出异常
			request.setAttribute("msg",e.getMessage());
			request.setAttribute("code", "error");//通知msg.jsp显示错误页面
		}
		return "f:/jsps/msg.jsp";
	}
	
/**
 * 登录功能
 * @param request
 * @param response
 * @return
 * @throws Exception
 */
	public String login (HttpServletRequest request, HttpServletResponse response) throws Exception{
		/**
		 * 1、封装表单数据到User
		 * 2、校验表单那数据
		 * 3、使用service查询得到User
		 * 4、查看用户是否存在，如果不存在：
		 * 	*保存错误信息：用户名或密码错误
		 * 	*保存用户数据：为了回显
		 * 	*转发到login.jsp
		 * 5、如果存在，查看状态，如果状态为false：
		 * 	*保存错误信息：您没有激活
		 * 	*保存表单数据：为了回显
		 * 	*转发到login.jsp
		 * 6、登录成功
		 * 	*保存当前查询出的user到session中
		 * 	*保存当前用户到cookie中，注意中文需要编码处理
		 */
		User formUser = CommonUtils.toBean(request.getParameterMap(), User.class);
		
		Map<String,String > errors = this.validateLogin(formUser, request.getSession());
		
		if(errors.size()>0){
			request.setAttribute("form", formUser);
			request.setAttribute("errors", errors);
			return "f:/jsps/user/login.jsp";
		}
		
		User user = userService.login(formUser);
		
		if(user==null){
			request.setAttribute("msg", "用户名或密码错误！");
			request.setAttribute("user", formUser);
			return "f:/jsps/user/login.jsp";
		}else{
			if(!user.isStatus()){
				request.setAttribute("msg", "您还没有激活！");
				request.setAttribute("user", formUser);
				return "f:/jsps/user/login.jsp";
			}else{
				//保存用户到session中
				request.getSession().setAttribute("sessionUser", user);
				//将用户名存入cookie中
				String loginname = user.getLoginname();
				loginname = URLEncoder.encode(loginname,"UTF-8");  
				Cookie cookie  = new Cookie("loginname", loginname);
				cookie.setMaxAge(60*60*24*10);
				response.addCookie(cookie);
				return "r:/index.jsp";
			}
		}
		
	}

	/**
	 * 修改密码
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public String updatePassword (HttpServletRequest request, HttpServletResponse response) throws Exception{
		/**
		 * 1、封装表单到user中
		 * 2、从session中获取uid
		 * 3、使用uid和表单中的oldpassword和newpassword来调用service方法
		 * 	》如果出现异常，保存异常信息到request中，装发到pwd.jsp
		 * 4、保存成功信息到request
		 * 5、转发到msg.jsp
		 */
		User formUser = CommonUtils.toBean(request.getParameterMap(), User.class);
		
		User user = (User) request.getSession().getAttribute("sessionUser");
		if(user==null){
			request.setAttribute("msg", "您还没有登录！");
			return "f:/jsps/user/login.jsp";
		}
		try {
			userService.updatePassword(user.getUid(), formUser.getNewloginpass(), formUser.getLoginpass());
			request.setAttribute("code", "success");
			request.setAttribute("msg", "修改密码成功！");
			return "f:/jsps/msg.jsp";
		} catch (Exception e) {
			request.setAttribute("msg", e.getMessage());//将错误信息传给pwd.jsp
			request.setAttribute("user", formUser);//为了回显
			return "f:/jsps/user/pwd.jsp";
		}
				
	}
		
/**
 * 退出功能
 * @param request
 * @param response
 * @return
 * @throws Exception
 */
	public String quit (HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.getSession().invalidate();
		return "r:/jsps/user/login.jsp";
	}


}
