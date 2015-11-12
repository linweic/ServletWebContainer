package edu.upenn.cis.cis455.webserver;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DemoServlet extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		       throws java.io.IOException
		  {
		    response.setContentType("text/html");
		    StringBuffer msg = new StringBuffer();
		    msg.append("<html><head><title>Foo</title></head>");
		    msg.append("<body>Hello World</body></html>");
		    int length =msg.length();
		    response.setContentLength(length);
		    PrintWriter out = response.getWriter();
		    out.println(msg);
		    response.flushBuffer();
		    //out.flush();
		    out.close();
		  }
}
