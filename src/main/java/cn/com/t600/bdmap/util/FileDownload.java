package cn.com.t600.bdmap.util;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * 下载文件
 * 创建人：
 * 创建时间：2014年12月23日
 * @version
 */
public class FileDownload {

	/**
	 * @param response 
	 * @param filePath		//文件完整路径(包括文件名和扩展名)
	 * @param fileName		//下载后看到的文件名
	 * @return  文件名
	 */
	public static void fileDownload(final HttpServletResponse response, String filePath, String fileName) throws Exception{  
		   
		byte[] data = FileUtil.toByteArray2(filePath);  
//	    fileName = URLEncoder.encode(fileName, "UTF-8");
		fileName = new String(fileName.getBytes(), "ISO-8859-1");//浏览器下载文件名为中文时  解决中文乱码的代码
		response.reset();
	    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");  
	    response.addHeader("Content-Length", "" + data.length);  
	    response.setContentType("application/octet-stream;charset=UTF-8");  
	    OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());  
	    outputStream.write(data);  
	    outputStream.flush();  
	    outputStream.close();
	    response.flushBuffer();
	}

}
