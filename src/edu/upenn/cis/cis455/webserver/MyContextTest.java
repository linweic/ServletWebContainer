package edu.upenn.cis.cis455.webserver;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MyContextTest {
	MyContext context = new MyContext(8080);
	@Before
	public void setUp() throws Exception{
		context.setAttribute("Attr1", "Object1");
		context.setAttribute("Attr1", "Object2");
		context.setAttribute("Attr2", null);
		context.setInitParam("param1","value1");
	}
	@Test
	public void testMyContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttribute() {
		assertTrue(context.getAttribute("Attr1").equals("Object2"));
		assertTrue(context.getAttribute("Attr2") == null);
	}

	@Test
	public void testGetAttributeNames() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetContext() {
		assertEquals(context.getContext("string"), null);
	}

	@Test
	public void testGetInitParameter() {
		assertEquals(context.getInitParameter("param2"),null);
		assertTrue(context.getInitParameter("param1").equals("value1"));
	}

	@Test
	public void testGetInitParameterNames() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMajorVersion() {
		assertEquals(context.getMajorVersion(),2);
	}

	@Test
	public void testGetMimeType() {
		assertEquals(context.getMimeType("foo"),null);
	}

	@Test
	public void testGetMinorVersion() {
		assertEquals(context.getMinorVersion(),4);
	}

	@Test
	public void testGetNamedDispatcher() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRealPath() {
		assertTrue("http://localhost:8080/index.html".equals("/index.html"));
		assertTrue("http://localhost:8080/".equals("/"));
		assertTrue("http://localhost:8080".equals(""));
	}

	@Test
	public void testGetRequestDispatcher() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResourceAsStream() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResourcePaths() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServerInfo() {
		assertTrue("HttpServer/1.0".equals(context.getServerInfo()));
	}

	@Test
	public void testGetServlet() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServletContextName() {
		assertEquals(context.getServletContextName(),null);
		context.setInitParam("display-name", null);
		assertEquals(context.getServletContextName(),null);
		context.setInitParam("display-name", "bar");
		assertTrue(context.getServletContextName().equals("bar"));
	}

	@Test
	public void testGetServletNames() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServlets() {
		fail("Not yet implemented");
	}

	@Test
	public void testLogExceptionString() {
		fail("Not yet implemented");
	}

	@Test
	public void testLogString() {
		fail("Not yet implemented");
	}

	@Test
	public void testLogStringThrowable() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveAttribute() {
		context.removeAttribute("Attr1");
		context.setAttribute("Attr3", "value3");
		context.removeAttribute("Attr4");
		assertEquals(context.getAttribute("Atrr1"),null);
		assertTrue(context.getAttribute("Attr3").equals("value3"));
	}

	@Test
	public void testSetAttribute() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetInitParam() {
		fail("Not yet implemented");
	}

}
