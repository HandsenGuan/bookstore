package cn.gxx.bookstore.user.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.mail.Session;

import cn.gxx.bookstore.user.dao.UserDao;
import cn.gxx.bookstore.user.domain.User;
import cn.gxx.bookstore.user.service.exception.UserException;
import cn.itcast.commons.CommonUtils;
import cn.itcast.mail.Mail;
import cn.itcast.mail.MailUtils;

/**
 * 用户模块业务层
 * @author Administrator
 *
 */
public class UserService {
	private  UserDao userDao = new  UserDao();
	
	public boolean ajaxValidateLoginname(String loginname) {
		try {
			return userDao.ajaxValidateLoginname(loginname);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public boolean ajaxValidateEmail(String email){
		try {
			return userDao.ajaxValidateEmail(email);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 激活功能
	 * @param code
	 * @throws UserException 
	 */
	public void activation(String code) throws UserException{
		/**
		 * 1、通过激活码查询用户
		 * 2、如果User是null，说明是无效激活码，抛出异常，发出异常信息。(无效激活码)
		 * 3、查看用户激活状态是否为true，如果为true，抛出异常，发出异常信息。(二次激活)
		 * 4、修改用户状态为true
		 */
		try {
			User	user = userDao.findByCode(code);
			if(user==null) throw new UserException("无效的激活码！");
			if(user.isStatus())throw  new UserException("您已激活，不要二次激活！");
			userDao.updateStatus(user.getUid(), true);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		
		
	}
	
	
	/**
	 * 注册功能
	 * @param user
	 */
	public void regist(User user){
		//1、数据补全
		user.setUid(CommonUtils.uuid());
		user.setStatus(false);
		user.setActivationCode(CommonUtils.uuid()+CommonUtils.uuid());
		
		//2、向数据库插入
		try {
			userDao.add(user);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		//3、发邮件
		
		//把配置文件加载到prop中
		Properties prop = new Properties();
		try {
			prop.load(this.getClass().getClassLoader().getResourceAsStream("email_template.properties"));
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		
		
		//登录邮件服务器得到session
		String host = prop.getProperty("host");//服务器主机名
		String name = prop.getProperty("username");//登录名
		String pass = prop.getProperty("password");//登录密码
		Session session = MailUtils.createSession(host, name, pass);
		
		//创建mail对象
		String from = prop.getProperty("from");
		String to = user.getEmail();
		String subject =prop.getProperty("subject");
		//Message	.format方法会把第一个参数中的{0}，使用第二个参数来替换。
		//例如Message.format("你好{0}，{1}"，"张三"，"拜拜");返回“你好张三，拜拜”
		String content = MessageFormat.format(prop.getProperty("content"),user.getActivationCode());
		Mail mail = new Mail(from, to,subject,content);
		
		//发送邮件
		try {
			MailUtils.send(session, mail);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
	}

	/**
	 * 登录功能
	 * @param user
	 */
	public User login(User user){
		try {
			return 	userDao.findByLoginnameAndLoginpass(user.getLoginname(), user.getLoginpass());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
	}

/**
 * 修改密码
 * @param uid
 * @param newPass
 * @param oldPass
 * @throws UserException
 */
	public void updatePassword(String uid,String newPass,String oldPass) throws UserException{
		try {
			//1、校验老密码
			boolean bool = userDao.findByUidAndPassword(uid, oldPass);
			if(!bool){
				throw new UserException("原密码错误！");
			}
			
			//2、修改新密码
			userDao.updatePassword(uid, newPass);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
	}

}
