package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;



public class HttpServer {
	static final Logger logger = Logger.getLogger(HttpServer.class);
	private static int portNumber;
	private static String rootPath;
	private static String xmlPath;
	
	static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = (m_state == 7) ? 1 : 8;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			} else if (qName.compareTo("servlet-mapping") == 0){
				m_state = 5;
			} else if (qName.compareTo("url-pattern") == 0){
				m_state = 6;
			} else if (qName.compareTo("servlet") == 0){
				m_state = 7;
			} else if (qName.compareTo("web-app") == 0) {
				m_state = 22;
			} 
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1 || m_state == 8) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 6) {
				if (m_servletName == null){
					logger.error("url pattern '" + value + "' without servlet name");
					System.exit(-1);
				}
				m_servletsMapping.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 22){
				m_contextParams.put("display-name", value);
			}
		}
		private int m_state = 0;
		private String m_servletName;
		private String m_paramName;
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,String> m_servletsMapping = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>(); //stores init-parameter, one set of k-v pair per servlet
	}
		
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		logger.debug(file.getAbsolutePath());
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		return h;
	}
	
	private static MyContext createContext(Handler h) {
		MyContext fc = new MyContext(portNumber);
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}
	
	private static HashMap<String,HttpServlet> createServlets(Handler h, MyContext fc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			MyConfig config = new MyConfig(servletName, fc);
			configs.put(servletName, config);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

	private static void usage() {
		System.err.println("usage: java TestHarness <path to web.xml> " 
				+ "[<GET|POST> <servlet?params> ...]");
	}
	
	public static Handler h;
	public static MyContext context;
	public static MyConfig config;
	public static HashMap<String, MyConfig> configs;
	public static MyResponse response;
	public static MyRequest request;
	public static MySession session;
	public static HashMap<String, HttpServlet> servlets;
	public static HashMap<String, HttpSession> sessions;
	
	public static void main(String args[]) throws Exception {
		/* your code here */
		//parse arguments from command line
		portNumber = Integer.parseInt(args[0]);//get portnumber from first parameter
		rootPath = args[1]; //get root directory from second parameter
		xmlPath = args[2];
	    logger.info("portNumber:"+portNumber);
	    logger.info("root path:"+rootPath);
	    logger.info("web.xml route:"+xmlPath);

	    h = parseWebdotxml(xmlPath);
	    
	    for(String k:h.m_servlets.keySet()){
	    	logger.info(k+":"+h.m_servlets.get(k));
	    	HashMap<String,String> servletParams = h.m_servletParams.get(k);
	    	if(servletParams!=null){
	    		for(String name: servletParams.keySet()){
	    			logger.info("\t"+name+":"+servletParams.get(name));
	    		}
	    	}
	    }
	    logger.info("--------");
	    for(String k: h.m_contextParams.keySet()){
	    	logger.info(k + ":" + h.m_contextParams.get(k));
	    }
	    logger.info("--------");
	    for(String k: h.m_servletsMapping.keySet()){
	    	logger.info(k + ":" + h.m_servletsMapping.get(k));
	    }
	    
	    configs = new HashMap<String, MyConfig>();
	    context = createContext(h);
	    servlets = createServlets(h, context);
		sessions = new HashMap<String, HttpSession>();
		
		//Create a client socket as daemon thread listening on port number
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(portNumber);
		}catch (IOException e){
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Connecting...");
		LinkedList<Socket> deque = new LinkedList<Socket>();
		int sizeOfQueue = 10;
		int sizeOfPool = 10;
		/*start the thread pool*/
		ThreadPool tp = new ThreadPool(deque, sizeOfPool, rootPath);
		tp.startThreads();
		while(ThreadPool.isrunning ==true){
			Socket clientSocket = null;
			System.out.println("Listening to client...");
			try{
				clientSocket = serverSocket.accept();
			}catch (IOException e){
				System.err.println("Accept failed.");
			}
			System.out.println("Successfully connected.");
			while(deque.size() == sizeOfQueue){
				synchronized(deque){
					System.out.println("Queue is full right now!");
					deque.wait();
				}
			}
			synchronized(deque){
				deque.add(clientSocket);
				System.out.println("socket added");
				deque.notifyAll();
			}
		}
		
		System.out.println("isrunning:"+ThreadPool.isrunning);
		try{
			tp.terminateThreads();
			serverSocket.close();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		
		System.out.println("All threads terminated.");
	}
  }
