package cn.gxx.bookstore.admin.admin.service;

import java.sql.SQLException;

import cn.gxx.bookstore.admin.admin.dao.AdminDao;
import cn.gxx.bookstore.admin.admin.domain.Admin;

public class AdminService {
	
	private AdminDao adminDao = new AdminDao();
	
	public Admin login(Admin admin) {
		 try {
			return	adminDao.find(admin.getAdminname(), admin.getAdminpwd());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
