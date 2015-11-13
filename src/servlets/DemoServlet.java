package servlets;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DemoServlet extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		response.setContentType("text/html");
		StringBuffer sb = new StringBuffer("<html><head><title>Test</title></head><body>");
		sb.append("<form method=\"post\">First name<br><input type = \"text\" name=\"firstname\"><br>");
		sb.append("Last name<br><input type = \"text\" name=\"lastname\"><br>");
		sb.append("<input type=\"submit\" value=\"Submit\"></form>");
		sb.append("</body></html>");
		response.setHeader("Content-Length", String.valueOf(sb.length()));
	    PrintWriter out = response.getWriter();
	    out.println(sb);
	    out.flush();
	    //out.flush();
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		Map<String, String> map = request.getParameterMap();
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><title>Test</title></head><body>");
		for(String key: map.keySet()){
			sb.append(key+":"+request.getParameter(key)+"<br>");
		}
		sb.append("</body></html>");
		response.setContentType("text/html");
		response.setContentLength(sb.length());
		PrintWriter out = response.getWriter();
		out.println(sb);
		out.flush();
	}
}
