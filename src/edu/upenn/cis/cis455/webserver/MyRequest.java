package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 * @author Todd J. Green
 */
class MyRequest implements HttpServletRequest {
	static final Logger logger = Logger.getLogger(MyRequest.class);
	MyRequest() {
	}
	
	MyRequest(MySession session) {
		m_session = session;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		// TODO Auto-generated method stub
		return BASIC_AUTH;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		String cookie = "Cookie".toUpperCase();
		@SuppressWarnings("unchecked")
		ArrayList<String> values = (ArrayList<String>) getAttribute(cookie);
		Cookie[] cookies = new Cookie[values.size()];
		int i = 0;
		for(String s: values){
			String[] strings = s.split("=");
			cookies[i] = new Cookie(strings[0],strings[1]);
			i++;
		}
		return cookies;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		//arg0 = arg0.toUpperCase();
		if(getAttribute(arg0) == null) return -1;
		else {
			String value = String.valueOf(getAttribute(arg0));//WorkerThread.headerLines.get(arg0).get(0);
			logger.info(value);
			SimpleDateFormat sdf1 = new SimpleDateFormat();
			sdf1.applyPattern("EEE, dd MMM yyyy HH:mm:ss z");
			sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
			//sdf1.setLenient(false);
			Date dateOfHeader = null;
            try {
				dateOfHeader = sdf1.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				throw new IllegalArgumentException();
			}
            return dateOfHeader.getTime();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		//arg0 = arg0.toUpperCase();
		if(getAttribute(arg0)!=null) {
			ArrayList<String> valueList = (ArrayList<String>) getAttribute(arg0);
			StringBuffer sb = new StringBuffer();
			for(String value: valueList){
				sb.append(value).append(", ");
			}
			String headerField = sb.substring(0,sb.length()-2).toString();
			return headerField;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String arg0) {
		// TODO Auto-generated method stub
		arg0 = arg0.toUpperCase();
		if (WorkerThread.headerLines.containsKey(arg0)){
			ArrayList<String> values = WorkerThread.headerLines.get(arg0);
			Vector<String> atts = new Vector<String>(values);
			return atts.elements();
		}
		Vector<String> atts = new Vector<String>();
		return atts.elements();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		// TODO Auto-generated method stub
		Set<String> keys = WorkerThread.headerLines.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) throws NumberFormatException{
		// TODO Auto-generated method stub
		//arg0 = arg0.toUpperCase();
		if(getAttribute(arg0)!=null){
			int intValue = Integer.parseInt((String) getAttribute(arg0));
			return intValue;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		// TODO Auto-generated method stub
		String servletPath = getServletPath();
		HashMap<String, String> mapping = (HashMap<String, String>) getAttribute("ServletMapping");
		Collection<String> patterns = mapping.values();
		String pathInfo = null;
		for(String pattern:patterns){
			if(pattern.charAt(0)!='/'){
				pattern = "/".concat(pattern);
			}
			if(pattern.startsWith(servletPath)){
				//logger.debug("matched pattern is: "+ pattern);
				pathInfo = pattern.substring(servletPath.length());
				if(pathInfo.equals("/*")) pathInfo = "/random";
			}
		}
		//String pathInfo = ((String)getAttribute("requestURL")).substring(servletPath.length());
		return pathInfo;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// TODO Auto-generated method stub
		//Only one webapp in this case, just return ""
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		// TODO Auto-generated method stub
		String[] questionMark = ((String) getAttribute("initialLine")).split("\\?");
		if(questionMark.length == 1) return null;
		String[] whiteSpace = questionMark[1].split("\\s");
		return whiteSpace[0];
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		
		//String cookie = "Cookie".toUpperCase();
		if(getAttribute("Cookie")!=null){
			@SuppressWarnings("unchecked")
			ArrayList<String> cookieValue = (ArrayList<String>) getAttribute("Cookie");
			for(String s: cookieValue){
				if(s.contains("JSESSIONID")){
					String[] str = s.split("=");
					return str[1];
				}
			}
		}
		return null;
		/*
		if(m_session != null) return m_session.cookie.getValue();
		else return null;
		*/
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		String url = (String)getAttribute("requestURL"); //after protocol name, before query string
		String uri = null;
		if (url.contains("http://")){
			//full url pathname
			int index1 = url.indexOf("//");
			int index2 = url.indexOf("/",index1+2);
			uri = url.substring(index2);
		}
		else uri = url;
		return uri;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		String method = getMethod();
		@SuppressWarnings("unchecked")
		String server = ((ArrayList<String>)getAttribute("Host")).get(0);
		logger.debug("[getRequestURL] server is "+ server);
		String serverPath = getRequestURI();
		logger.debug("[getRequestURL] serverpath is "+ server);
		sb = sb.append(method).append(" ").append(server).append(serverPath);
		return sb;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		String url = (String) getAttribute("requestURL");
		@SuppressWarnings("unchecked")
		HashMap<String, String> mapping = (HashMap<String, String>) getAttribute("ServletMapping");//HttpServer.h.m_servletsMapping;
		Collection<String> patterns = mapping.values();
		int maxLength = 0;
		String servletPath = null;
		for (String str: patterns){
			if(str.charAt(0)!='/') str = "/" + str;
			if(str.contains("/*")){
				str = str.substring(0, str.length()-2);
				if(url.contains(str)){
					if(str.length()>maxLength){
						maxLength = str.length();
						servletPath = str;
					}
				}
			}
			else{
				if(str.equals(url)) servletPath = str;
			}
		}
		return servletPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				logger.info("request does not have a session");
				m_session = new MySession();
				m_session.setAttribute("Request", this);
				m_session.setAttribute("ServletContext", getAttribute("ServletContext"));				
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return m_session.isValid();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		//String cookie = "Cookie".toUpperCase();
		/*
		if(getAttribute("Cookie")!=null){
			ArrayList<String> values = (ArrayList<String>) getAttribute("Cookie");
			for(String s: values){
				if(s.contains("JSESSIONID")) return true;
			}
		}
		return false;
		*/
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		//String query = getQueryString();
		//if (query.contains("JSESSIONID")) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		if(m_props.containsKey(arg0.toUpperCase()) == false) return null;
		return m_props.get(arg0.toUpperCase());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		if(getAttribute("Accept-Charset")!=null) return (String)getAttribute("Accept-Charset");
		else return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		ArrayList<String> encoding = new ArrayList<String>();
		encoding.add(arg0);
		setAttribute("Accept-Charset",encoding);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	@SuppressWarnings("unchecked")
	public int getContentLength() {
		// TODO Auto-generated method stub
		if(getAttribute("Content-Length")!=null){
			String strValue = ((ArrayList<String>)getAttribute("Content-Length")).get(0);
			int intValue = Integer.valueOf(strValue).intValue();
			return intValue;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")
		ArrayList<String> result = (ArrayList<String>) getAttribute("Content-Type");
		if(result!=null){
			return result.get(0);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		return m_params.getProperty(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return m_params.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		String value = m_params.getProperty(arg0);
		String[] values = value.split("\\s");
		return values;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		return m_params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return (String)getAttribute("protocol");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		String hostValue = WorkerThread.headerLines.get("Host".toUpperCase()).get(0);
		String[] values = hostValue.split(":");
		return values[0];
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		// TODO Auto-generated method stub
		String hostValue = WorkerThread.headerLines.get("Host".toUpperCase()).get(0);
		String[] values = hostValue.split(":");
		int port = Integer.valueOf(values[1]).intValue();
		return port;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return WorkerThread.incomingRequest;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return WorkerThread.clientSocket.getLocalAddress().getHostAddress();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return WorkerThread.clientSocket.getLocalAddress().getHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0.toUpperCase(), arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		if(m_props.containsKey("Accepted-Language")) {
			return (Locale) m_props.get("Accepted-Language");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return WorkerThread.clientSocket.getLocalPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return getServerName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		if(m_params.containsKey(key)){
			String strValue = m_params.getProperty(key);
			StringBuffer values = new StringBuffer(strValue);
			values.append(" ").append(value);
			m_params.setProperty(key,values.toString());
		}
		else m_params.setProperty(key, value);
	}
	
	void clearParameters() {
		m_params.clear();
	}
	
	boolean hasSession() {
		return (m_session != null);
	}
		
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private MySession m_session = null;
	private String m_method;
}
