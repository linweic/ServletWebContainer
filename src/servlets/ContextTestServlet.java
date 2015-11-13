package servlets;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContextTestServlet extends HttpServlet{
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    out.println("<html><head><title>Test</title></head><body>");
	    out.println(getServletContext());
	    out.println("</body></html>");
	    out.flush();
	}

}
