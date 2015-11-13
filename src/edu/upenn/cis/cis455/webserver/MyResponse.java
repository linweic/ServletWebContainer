package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyResponse implements HttpServletResponse {
	static final Logger logger = Logger.getLogger(MyResponse.class);
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	private Map<String, List<String>> responseHeaders;
	private PrintWriter pw; 
	private int statusCode;
	private int bufferSize;
	private StringBuffer buffer;
	private boolean committed;
	
	public MyResponse(){
		committed = false;
		responseHeaders = new HashMap<String, List<String>>();
		pw = WorkerThread.outstandingResponse;
	}
	private String transferDate(long arg0){
		Date date = new Date(arg0);
		SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("EEE, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = sdf.format(date);
        return dateString;
	}
	
	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub
		if(isCommitted() == true) return;
		StringBuffer values = new StringBuffer(arg0.getName());
		//StringBuffer nameValue = new StringBuffer(arg0.getName());
		values.append("=").append(arg0.getValue());
		values.append("; Max-Age=").append(arg0.getMaxAge());
		values.append("; Secure=").append(arg0.getSecure());
		values.append("; Version=").append(arg0.getVersion());
		if(arg0.getComment() != null){
			values.append("; Comment=").append(arg0.getComment());
		}
		if(arg0.getDomain() != null){
			values.append("; Domain=").append(arg0.getDomain());
		}
		if(arg0.getPath() != null){
			values.append("; Path=").append(arg0.getPath());
		}
		//buffer.append("\r\nSet-Cookie: ").append(values);
		//logger.info(buffer.toString());
		List<String> value = new ArrayList<String>();
		value.add(values.toString());
		responseHeaders.put("Set-Cookie".toUpperCase(), value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		if (responseHeaders.containsKey(arg0.toUpperCase())) return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted() == true) {
			throw new IllegalStateException(); 
		}
		else{
			PrintWriter out = WorkerThread.outstandingResponse;
			StringBuffer statusLine = new StringBuffer(WorkerThread.httpVersion);
			statusLine = statusLine.append(" ").append(arg0).append(" ").append(arg1);
			logger.info(statusLine);
			out.println(statusLine);
			setContentType("text/html");
			logger.info("Content-Type: text/html");
			out.println("Content-Type: text/html");
			StringBuffer msgBody = new StringBuffer();
			msgBody.append("<html><body><h2>Error:").append(arg0).append("</h2>").append(arg1).append("</body></html>");
			StringBuffer contentLength = new StringBuffer("Content-Length: ");
			logger.info(contentLength.append(msgBody.length()));
			out.println(contentLength.append(msgBody.length()));
			out.println("\r\n");
			logger.info(msgBody);
			out.println(msgBody);
			out.flush();
			committed = true;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted() == true){
			throw new IllegalStateException();
		}
		else{
			PrintWriter out = WorkerThread.outstandingResponse;
			StringBuffer statusLine = new StringBuffer(WorkerThread.httpVersion);
			statusLine = statusLine.append(" ").append(arg0);
			out.println(statusLine);
			StringBuffer msgBody = new StringBuffer();
			msgBody.append("<html><body><h2>Error:").append(arg0).append("</h2>").append("</body></html>");
			StringBuffer contentLength = new StringBuffer("Content-Length: ");
			logger.info(contentLength.append(msgBody.length()));
			out.println(contentLength.append(msgBody.length()));
			out.println("\r\n");
			logger.info(msgBody);
			out.println(msgBody);
			out.flush();
			committed = true;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		if(isCommitted() == true){
			throw new IllegalStateException();
		}
		else {
			logger.debug("[DEBUG] redirect to " + arg0 + " requested");
			logger.debug("[DEBUG] stack trace: ");
			
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		String dateString = transferDate(arg1);
		setHeader(arg0,dateString);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
        String dateString = transferDate(arg1);
        addHeader(arg0,dateString);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		String header = arg0.toUpperCase();
		List<String> value = new ArrayList<String>();
		value.add(arg1);
		responseHeaders.put(header, value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		String header = arg0.toUpperCase();
		List<String> value = new ArrayList<String>();
		if(responseHeaders.containsKey(header)){
			value = responseHeaders.get(header);
			value.add(arg1);
		}
		else value.add(arg1);
		responseHeaders.put(arg0.toUpperCase(), value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		String intValue = String.valueOf(arg1);
		setHeader(arg0,intValue);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
		String intValue = String.valueOf(arg1);
		addHeader(arg0,intValue);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub
		statusCode = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		if(responseHeaders.containsKey("Content-Type".toUpperCase())){
			String type = responseHeaders.get("Content-Type".toUpperCase()).get(0);
			return type;
		}
		else return "text/html";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		if(isCommitted() == true) {
			logger.info("response has already been committed.");
			return null;
		}
		else{
			//PrintWriter pw = WorkerThread.outstandingResponse;
			StringBuffer statusLine = new StringBuffer(WorkerThread.httpVersion);
			statusLine = statusLine.append(" 200 OK");
			pw.println(statusLine);
			if(!responseHeaders.containsKey("Date".toUpperCase())){
				long time = new Date().getTime();
				setDateHeader("Date",time);
			}
			for(String headerName:responseHeaders.keySet()){
				StringBuffer headerLine = new StringBuffer(headerName);
				headerLine.append(": ");
				List<String> headerField = responseHeaders.get(headerName);
				for(String value:headerField){
					headerLine.append(value).append(", ");
				}
				String line = headerLine.substring(0,headerLine.length()-2);
				logger.debug(line); 
				pw.println(line);
			}
			pw.print("\r\n");
			//committed = true;
			//flushBuffer();
			//committed = false;
			pw.flush();
			return pw;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		if(isCommitted() == true) {
			logger.info("Response has been committed, this method has no effect.");
			return;
		}
		if(containsHeader("Content-Type".toUpperCase()) == false){
			System.err.println("No content-type has been set");
			return;
		}
		String values = responseHeaders.get("Content-Type".toUpperCase()).get(0);
		StringBuffer sb = new StringBuffer();
		if(values.contains(";")){
			String[] strings = values.split(";");
			sb.append(strings[0]);
			sb.append("; charset=").append(arg0);
		}
		else{
			sb.append(values).append("; charset=").append(arg0);
		}
		logger.debug("content-type value is "+ sb.toString());
		List<String> value = new ArrayList<String>();
		value.add(sb.toString());
		responseHeaders.put("Content-Type".toUpperCase(), value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		setHeader("Content-Length", String.valueOf(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		setHeader("Content-Type", arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		if(isCommitted() == true){
			throw new IllegalStateException();
		}
		else {
			bufferSize = arg0;
			buffer = new StringBuffer(arg0);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return buffer.capacity();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted() == true){
			logger.info("response has already been committed.");
			throw new IllegalStateException();
		}
		/*
		else if(!responseHeaders.containsKey("Content-Length".toUpperCase())&&!responseHeaders.containsKey("Date".toUpperCase())){
			logger.info("response does not written content-length and date");
			throw new IllegalStateException();
		}
		*/
		else{
			pw.flush();
			committed = true;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// TODO Auto-generated method stub
		buffer = new StringBuffer(0);  
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return committed;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub
		if(isCommitted()) throw new IllegalStateException();
		else{
			buffer.delete(0,buffer.length());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		List<String> value = new ArrayList<String>();
		value.add(arg0.getLanguage());
		responseHeaders.put("Content-Language", value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		if(responseHeaders.containsKey("Content-Language".toUpperCase()) == false) return null;
		String language = responseHeaders.get("Content-Language".toUpperCase()).get(0);
		Locale value = new Locale(language); 
		return value;
	}

}
