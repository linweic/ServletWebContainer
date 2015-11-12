package edu.upenn.cis.cis455.webserver;

import static org.junit.Assert.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

public class MyConfigTest {
	HashMap<String,String> initParams;
	MyContext context;
	MyConfig config;
	@Before
	public void setUp() throws Exception{
		initParams = new HashMap<String, String>();
		context = new MyContext(8080);
		config = new MyConfig("servletName", context);
		config.setInitParam("a", "bc");
		config.setInitParam("d", "ef");
	}

	@Test
	public void testGetInitParameter() {
		assertTrue(config.getInitParameter("a").equals("bc"));
		assertTrue(config.getInitParameter("d").equals("ef"));
	}

	@Test
	public void testGetInitParameterNames() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServletContext() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetServletName() {
		assertTrue("servletName".equals(config.getServletName()));
	}

	@Test
	public void testSetInitParam() {
		fail("Not yet implemented");
	}

}
