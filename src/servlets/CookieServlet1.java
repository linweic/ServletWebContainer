package servlets;

import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieServlet1 extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		response.setContentType("text/html");
		StringBuffer sb = new StringBuffer();
	    sb.append("<html><head><title>Test</title></head><body>");
	    Cookie[] cookies = request.getCookies();
	    sb.append("</body></html>");
	    response.setContentLength(sb.length());
	    PrintWriter out = response.getWriter();
	    out.println(out);
	    out.flush();
	}

}
