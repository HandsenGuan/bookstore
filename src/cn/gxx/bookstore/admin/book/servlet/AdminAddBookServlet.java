package cn.gxx.bookstore.admin.book.servlet;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import cn.gxx.bookstore.book.domain.Book;
import cn.gxx.bookstore.book.service.BookService;
import cn.gxx.bookstore.caregory.domain.Category;
import cn.itcast.commons.CommonUtils;

public class AdminAddBookServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			/*
			 *1、commons-fileupdate的上传三步 
			 */
			//创建工具
			FileItemFactory factory = new DiskFileItemFactory();
			
			//创建解析器对象
			ServletFileUpload  sfu = new ServletFileUpload(factory);
			sfu.setFileSizeMax(80 * 1024); //设置单个上传文件上限为80kb
			
			//解析request得到List<FileItem>
			List<FileItem> fileItemList = null;
			try {
				fileItemList = sfu.parseRequest(request);
			} catch (FileUploadException e) {
				//如果出现这个异常，说明单个文件超过80kb
				error("上传的文件超过了80kb", request, response);
				return;
			}
			
			//把List<FileItem>封装到Book对象中
			//首先把普通表单字段封装到map中，在把map转换成Book和Category中，然后建立两者联系
			Map<String,Object> map = new HashMap<String,Object>();
			for(FileItem fileItem : fileItemList){
				if(fileItem.isFormField()){//如果是普通表单字段
					map.put(fileItem.getFieldName(), fileItem.getString("UTF-8"));
				}
			}
			
			Book book = CommonUtils.toBean(map, Book.class);//把大部分数据封装在book对象中，没有cid
			Category category = CommonUtils.toBean(map, Category.class);//把cid封装到category中
			book.setCategory(category);
			
			//把上传的图片保存起来
			//	>获取文件名，截取
			//	>给文件添加前缀：用uuid前缀，避免文件同名现象
			//	>检验文件扩展名：只能是jpg
			//	>校验图片尺寸
			//	>指定图片的保存路径，这需要使用servletContext#getRealPath()
			//	>保存
			//	>把图片的路径设置给Book对象
			FileItem fileItem = fileItemList.get(1);//获取大图
			String filename = fileItem.getName();
			//截取文件名，因为部分浏览器上传的绝对路径
			int index = filename.lastIndexOf("\\");
			if(index!=-1){
				filename = filename.substring(index+1);
			}
			filename = CommonUtils.uuid()+"_"+filename;//添加前缀
			//校验扩展名
			if(!filename.toLowerCase().endsWith(".jpg")){
				
				System.out.println("图片一");
				
				error("必须上传jpg类型图片", request, response);
				return;
			}
			
			//校验图片尺寸
			//	1、保存上传图片,把图片new成一个图片对象Image	 
			String savepath = this.getServletContext().getRealPath("/book_img");//获取真实路径，斜杠表示WebRoot
			File destFile = new File(savepath,filename);//创建目标文件
			try {
				fileItem.write(destFile);//保存文件（它会把临时文件重定向到指定的路径，再删除临时文件）
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			//校验图片尺寸
			ImageIcon imageIcon = new ImageIcon(destFile.getAbsolutePath());//使用文件路径创建ImageIcon
			Image image = imageIcon.getImage();
			if(image.getWidth(null)>350 || image.getHeight(null)>350){
				
				System.out.println("图片二");
				
				error("您上传的图片尺寸超出了350*350", request, response);
				destFile.delete();//删除非法文件
				return;
			}
			
			//把图片路径设置给Book对象
			book.setImage_w("book_img/"+filename);
			
			
			
			
			 fileItem = fileItemList.get(2);//获取小图
			 filename = fileItem.getName();
			//截取文件名，因为部分浏览器上传的绝对路径
			 index = filename.lastIndexOf("\\");
			if(index!=-1){
				filename = filename.substring(index+1);
			}
			filename = CommonUtils.uuid()+"_"+filename;//添加前缀
			//校验扩展名
			if(!filename.toLowerCase().endsWith(".jpg")){
				error("必须上传jpg类型图片", request, response);
				return;
			}
			
			//校验图片尺寸
			//	1、保存上传图片,把图片new成一个图片对象Image	 
			 savepath = this.getServletContext().getRealPath("/book_img");//获取真实路径，斜杠表示WebRoot
			 destFile = new File(savepath,filename);//创建目标文件
			try {
				fileItem.write(destFile);//保存文件（它会把临时文件重定向到指定的路径，再删除临时文件）
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			//校验图片尺寸
			 imageIcon = new ImageIcon(destFile.getAbsolutePath());//使用文件路径创建ImageIcon
			 image = imageIcon.getImage();
			if(image.getWidth(null)>350 || image.getHeight(null)>350){
				error("您上传的图片尺寸超出了350*350", request, response);
				destFile.delete();//删除非法文件
				return;
			}
			
			//把图片路径设置给Book对象
			book.setImage_b("book_img/"+filename);
			
			
			//调用service完成保存
			book.setBid(CommonUtils.uuid());
			BookService bookService = new BookService();
			bookService.add(book);
			
			//保存成功信息转发到msg.jsp
			request.setAttribute("msg", "添加图书成功！");
			request.getRequestDispatcher("/adminjsps/msg.jsp").forward(request, response);
			
	}
	

	/**
	 * 保存错误信息,转发到add.jsp
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void error(String msg,HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
			request.setAttribute("msg", msg);
			request.getRequestDispatcher("/adminjsps/admin/book/add.jsp").forward(request, response);
	
	}
}
