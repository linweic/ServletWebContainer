package edu.upenn.cis.cis455.webserver;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.junit.Test;
import org.junit.Before;

public class MyRequestTest {
	MyRequest request;
	MySession session;
	ArrayList<String> cookies;
	HashMap<String,ArrayList<String>> headerLines;
	String url;
	@Before
	public void setUp() throws Exception{
		request = new MyRequest();
		session = new MySession();
		cookies = new ArrayList<String>();
		cookies.add("cookie1=value1");
		cookies.add("cookie2=value2");
		headerLines = new HashMap<String,ArrayList<String>>();
		headerLines.put("COOKIE", cookies);
	}
	
	@Test
	public void testMyRequest() {
	}

	@Test
	public void testMyRequestMySession() {
	}

	@Test
	public void testGetAuthType() {
		assertTrue("BASIC".equals(request.getAuthType()));
	}

	@Test
	public void testGetCookies() {
		request.setAttribute("COOKIE",cookies);
		Cookie[] c = request.getCookies();
		assertTrue(c[0].getName().equals("cookie1"));
		assertTrue(c[0].getValue().equals("value1"));
		assertTrue(c[1].getName().equals("cookie2"));
		assertTrue(c[1].getValue().equals("value2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetDateHeader() {
		assertEquals(request.getDateHeader("Date"),-1);
		assertEquals(request.getDateHeader("DATE"),-1);
		//ArrayList<String> date = new ArrayList<String>();
		request.setAttribute("Date","Fri, 31 Dec 1995 23:59:59 GMT");
		//assertTrue("Fri, 31 Dec 1995 23:59:59 GMT".equals(request.getAttribute("Date")));
		long expect = 820454399000L;
		assertEquals(expect,request.getDateHeader("Date"));
		assertEquals(expect,request.getDateHeader("DATE"));
		request.setAttribute("Date", "wrong");
		request.getDateHeader("Date");
	}

	@Test
	public void testGetHeader() {
		request.setAttribute("Header1","Value1");
		assertTrue(request.getHeader("Header1").equals("Value1"));
		assertTrue(request.getHeader("HEAder1").equals("Value1"));
		assertEquals(null,request.getHeader("Header2"));
	}

	@Test
	public void testGetHeaders() {
	}

	@Test
	public void testGetHeaderNames() {
	}

	@Test//(expected=NumberFormatException.class)
	public void testGetIntHeader() {
		request.setAttribute("intHeader1","1");
		request.setAttribute("intHeader2","a");
		assertEquals(1,request.getIntHeader("INTheaDer1"));
		assertEquals(1,request.getIntHeader("intheader1"));
		assertEquals(-1,request.getIntHeader("intHeaderrr"));
		request.getHeader("intHeader2");
	}

	@Test
	public void testGetMethod() {
		request.setMethod("GET");
		assertTrue("GET".equals(request.getMethod()));
	}

	@Test
	public void testGetPathInfo() {
		HashMap<String,String> mapping = new HashMap<String, String>();
		mapping.put("servlet1", "/calculator");
		mapping.put("servlet2", "/calculator/test");
		request.setAttribute("ServletMapping", mapping);
		url = new String("/calculator/test");
		request.setAttribute("requestURL",url);
		assertTrue("".equals(request.getPathInfo()));
		mapping.put("servlet3", "/calculator/*");
		mapping.put("servlet4", "calculator/a/*");
		request.setAttribute("requestURL","/calculator/a/b/test");
		assertTrue("/b/test".equals(request.getPathInfo()));
	}

	@Test
	public void testGetPathTranslated() {
	}

	@Test
	public void testGetContextPath() {
		assertTrue("".equals(request.getContextPath()));
	}

	@Test
	public void testGetQueryString() {
		request.setAttribute("initialLine","GET /foo/bar HTTP/1.1");
		assertEquals(request.getQueryString(),null);
		request.setAttribute("initialLine","GET /foo/bar?a=b&c=d HTTP/1.1");
		assertTrue("a=b&c=d".equals(request.getQueryString()));
	}

	@Test
	public void testGetRemoteUser() {
		//fail("Not yet implemented");
	}

	@Test
	public void testIsUserInRole() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetUserPrincipal() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRequestedSessionId() {
		//fail("Not yet implemented");
		assertNull(request.getRequestedSessionId());
		cookies.add("JSESSIONID=id");
		request.setAttribute("COOKIE",cookies);
		assertTrue("id".equals(request.getRequestedSessionId()));
	}

	@Test
	public void testGetRequestURI() {
		request.setAttribute("requestURL", "/some/path.html");
		assertTrue("/some/path.html".equals(request.getRequestURI()));
		request.setAttribute("requestURL", "http://foo.bar/a.html");
		assertTrue("/a.html".equals(request.getRequestURI()));
	}

	@Test
	public void testGetRequestURL() {
		request.setAttribute("initialLine","GET /some/path?a=b&c=d HTTP/1.1");
		ArrayList<String> host = new ArrayList<String>();
		host.add("http://foo.bar:8080");
		request.setAttribute("Host",host);
		request.setAttribute("requestURL", "/some/path.html");
		request.setMethod("GET");
		assertTrue("GET http://foo.bar:8080/some/path.html".equals(request.getRequestURL().toString()));
		assertFalse("GET http://foo.bar:8080/some/path.html?a=b&c=d".equals(request.getRequestURL().toString()));
	}

	@Test
	public void testGetServletPath() {
		HashMap<String,String> mapping = new HashMap<String, String>();
		mapping.put("servlet1", "/calculator");
		mapping.put("servlet2", "/calculator/test");
		request.setAttribute("ServletMapping", mapping);
		url = new String("/calculator/test");
		request.setAttribute("requestURL",url);
		assertTrue("/calculator/test".equals(request.getServletPath()));
		mapping.put("servlet3", "/calculator/*");
		mapping.put("servlet4", "calculator/a/*");
		request.setAttribute("requestURL","/calculator/a/b/test");
		assertTrue("/calculator/a".equals(request.getServletPath()));
	}

	@Test
	public void testGetSessionBoolean() {
		MyContext context = new MyContext(8000);
		request = new MyRequest();
		request.setAttribute("ServletContext", context);
		assertEquals(null, request.getSession(false));
		assertNotNull(request.getSession(true));
		request = new MyRequest(session);
		assertNotNull(request.getSession(false));
	}

	@Test
	public void testGetSession() {
		MyContext context = new MyContext(8000);
		request.setAttribute("ServletContext", context);
		assertNotNull(request.getSession());
		request = new MyRequest(session);
		assertNotNull(request.getSession());
	}

	@Test
	public void testIsRequestedSessionIdValid() {
	}

	@Test
	public void testIsRequestedSessionIdFromCookie() {
		assertTrue(request.isRequestedSessionIdFromCookie());
	}

	@Test
	public void testIsRequestedSessionIdFromURL() {
		assertFalse(request.isRequestedSessionIdFromURL());
	}

	@Test
	public void testIsRequestedSessionIdFromUrl() {
	}

	@Test
	public void testGetAttribute() {
	}

	@Test
	public void testGetAttributeNames() {
	}

	@Test
	public void testGetCharacterEncoding() {
		assertTrue("ISO-8859-1".equals(request.getCharacterEncoding()));
		ArrayList<String> encoding = new ArrayList<String>();
		encoding.add("unicode");
		request.setAttribute("Accept-Charset", encoding);
		assertTrue("unicode".equals(request.getCharacterEncoding()));
	}

	@Test
	public void testSetCharacterEncoding() throws UnsupportedEncodingException {
		request.setCharacterEncoding("sss");
		assertTrue("sss".equals(request.getCharacterEncoding()));
	}

	@Test
	public void testGetContentLength() {
		assertEquals(0,request.getContentLength());
		ArrayList<String> length = new ArrayList<String>();
		length.add("50");
		request.setAttribute("Content-Length",length);
		assertEquals(50,request.getContentLength());
	}

	@Test
	public void testGetContentType() {
		assertNull(request.getContentType());
		ArrayList<String> type = new ArrayList<String>();
		type.add("css");
		request.setAttribute("Content-Type",type);
		assertEquals("css",request.getContentType());
	}

	@Test
	public void testGetInputStream() {
	}

	@Test
	public void testGetParameter() {
		assertNull(request.getParameter("key"));
		request.setParameter("key1", "value1");
		assertTrue("value1".equals(request.getParameter("key1")));
		request.setParameter("key1","value2");
		assertFalse("value2".equals(request.getParameter("key1")));
	}

	@Test
	public void testGetParameterNames() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetParameterValues() {
		assertNull(request.getParameter("key"));
		request.setParameter("key1", "value1");
		assertTrue("value1".equals((request.getParameterValues("key1"))[0]));
		request.setParameter("key1","value2");
		assertTrue("value1".equals((request.getParameterValues("key1"))[0]));
		assertTrue("value2".equals((request.getParameterValues("key1"))[1]));
	}

	@Test
	public void testGetParameterMap() {
	}

	@Test
	public void testGetProtocol() {
		request.setAttribute("protocol", "HTTP/1.1");
		assertTrue("HTTP/1.1".equals(request.getProtocol()));
	}

	@Test
	public void testGetScheme() {
		assertTrue("http".equals(request.getScheme()));
	}

	@Test
	public void testGetServerName() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetServerPort() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetReader() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRemoteAddr() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRemoteHost() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetAttribute() {
		//fail("Not yet implemented");
	}

	@Test
	public void testRemoveAttribute() {
		request.setAttribute("Header", "Value");
		assertTrue("Value".equals(request.getAttribute("Header")));
		request.removeAttribute("Header");
		System.out.println(request.getAttribute("Header"));
		assertTrue("Value".equals(request.getAttribute("Header")));
	}

	@Test
	public void testGetLocale() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetLocales() {
		//fail("Not yet implemented");
	}

	@Test
	public void testIsSecure() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRequestDispatcher() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetRealPath() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetLocalName() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetLocalAddr() {
		//fail("Not yet implemented");
	}

	@Test
	public void testGetLocalPort() {
		//fail("Not yet implemented");
	}

	@Test
	public void testSetMethod() {
		request.setMethod("Method");
		assertTrue("Method".equals(request.getMethod()));
	}

	@Test
	public void testSetParameter() {
		//fail("Not yet implemented");
	}

	@Test
	public void testClearParameters() {
		request.setParameter("Key", "Value");
		assertTrue("Value".equals(request.getParameter("Key")));
		request.clearParameters();
		assertNull(request.getParameter("Key"));
	}

	@Test
	public void testHasSession() {
		//fail("Not yet implemented");
	}

}
