package servlets;

import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.*;

public class SessionServlet1 extends HttpServlet{
	static final Logger logger = Logger.getLogger(SessionServlet1.class);
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException{
		response.setContentType("text/html");
		StringBuffer sb = new StringBuffer();
	    sb.append("<html><head><title>Test</title></head><body>");
	    HttpSession session = request.getSession();
	    logger.info(session.getId());
	    sb.append("</body></html>");
	    response.setContentLength(sb.length());
	    PrintWriter out = response.getWriter();
	    out.println(out);
	    out.flush();
	}

}
