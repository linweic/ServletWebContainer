package servlets;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis.cis455.webserver.MyConfig;

public class InitParamServlet extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		response.setContentType("text/html");
		StringBuffer sb = new StringBuffer();
	    sb.append("<html><head><title>Test</title></head><body>");
	    /*
	    while(request.getParameterNames().hasMoreElements()){
	    	String name = (String) request.getParameterNames().nextElement();
	    	out.println("Init name: ["+ name+"] Init value: ["+request.getParameter(name)+"]");
	    }
	    */
	    MyConfig config = (MyConfig) getServletConfig();
	    sb.append("Init name: [TestParam] Init value: ["+config.getInitParameter("TestParam")+"]");
	    sb.append("</body></html>");
	    response.setContentLength(sb.length());
	    PrintWriter out = response.getWriter();
	    out.println(sb);
	    out.flush();
	}
}
