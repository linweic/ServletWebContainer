package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger; 

public class WorkerThread extends Thread{
	static final Logger logger = Logger.getLogger(WorkerThread.class);
	private final LinkedList<Socket> sharedQueue;
	//private final int queueSize;
	private final StringBuffer rootPath;
	public static BufferedReader incomingRequest;
	public static PrintWriter outstandingResponse;
	public static HashMap<String,ArrayList<String>> headerLines;
	public static String initialLine;
	public static String url;
	public static String httpVersion;
	public static Socket clientSocket;
	
	public WorkerThread(LinkedList<Socket> sharedQueue, String rootPath){
		this.sharedQueue = sharedQueue;
		//this.queueSize = queueSize;
		this.rootPath = new StringBuffer(rootPath);
	}
	
	public Socket readFromQueue() throws InterruptedException{
		if(ThreadPool.isrunning == false) Thread.currentThread().interrupt();
		while(sharedQueue.isEmpty()){
			synchronized(sharedQueue){
				System.out.println("Queue is empty right now.");
				sharedQueue.wait();
			}
		}
		synchronized(sharedQueue){
			sharedQueue.notifyAll();
			if(ThreadPool.isrunning == false) Thread.currentThread().interrupt();
			clientSocket = sharedQueue.removeFirst();
			return clientSocket;
		}
	}
	
	public boolean doesExist(StringBuffer s){
		Path resourceFile = Paths.get(s.toString());
		//File requestFile = new File(s.toString());
		if(Files.exists(resourceFile) == true) return true;
		else return false;
	}
	
	public boolean isAccessible(StringBuffer s){
		Path resource = Paths.get(s.toString());
		return Files.isReadable(resource);
	}
	
	public Matcher regexMatcher(String pattern, String text){
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(text);
		return m;
	}
	
	public String checkMapping(HttpServer.Handler h, String line){
		logger.info("Requested url is: " + line);
		for (String k: h.m_servletsMapping.keySet()){
			String urlPattern = h.m_servletsMapping.get(k);
			if(urlPattern.charAt(0) != '*' && urlPattern.charAt(0) != '/'){
				urlPattern = "/" + urlPattern;
			}
			logger.info("url pattern is: " + urlPattern);
			/*exact match*/
			if(line.equals(urlPattern)) return k;
			/*prefix match*/
			String wildcard = urlPattern.substring(urlPattern.length()-2);
			if(wildcard.equals("/*")){
				String servletPath = urlPattern.substring(0,urlPattern.length()-2);
				if (line.length() >= servletPath.length() && line.substring(0,servletPath.length()).equals(servletPath)) return k;
			}
			/*suffix match*/
			if(urlPattern.charAt(0) == '*'){
				int suffixLength = urlPattern.length();
				String suffix = urlPattern.substring(1, suffixLength);
				if (line.length() >= suffix.length() && line.endsWith(suffix)) return k;
			}
		}		
		return null;
	}
	public HttpSession isSessionExist() {
		removeInvalidSession();
		logger.debug("check existant sessions...");
		/*
		if(headerLines.containsKey("Cookie".toUpperCase())){
			ArrayList<String> values = headerLines.get("Cookie".toUpperCase());
			ArrayList<String> new_values = new ArrayList<String>();
			HttpSession session = null;
			for(String s : values){
				if(s.contains("JSESSIONID")){
					String[] strings = s.split("=");
					session = HttpServer.sessions.get(strings[1]);
					if(session != null){
						new_values.add(s);
					}
				}
				else new_values.add(s);
			}
			headerLines.put("COOKIE", new_values);
			return session;
		}
		*/
		HttpSession session = null;
		if(HttpServer.sessions == null) return null;
		else{
			for(String key: HttpServer.sessions.keySet()){
				session = HttpServer.sessions.get(key);
				StringBuffer sb = new StringBuffer("JSESSIONID");
				sb.append("=").append(session.getId());
				ArrayList<String> value = new ArrayList<String>();
				value.add(sb.toString());
				headerLines.put("COOKIE", value);
				break;
			}
			return session;
		}
	}
	private void removeInvalidSession(){
		logger.info("removing invalid session....");
		for(String key:HttpServer.sessions.keySet()){
			Date current = new Date();
			HttpSession hs = HttpServer.sessions.get(key);
			if(hs.getMaxInactiveInterval()==-1) return;
			if(current.getTime()- hs.getCreationTime()>(hs.getMaxInactiveInterval()*1000)){
				logger.debug(key+" session is invalid");
				hs.invalidate();
				HttpServer.sessions.remove(key);
				logger.debug(key+" has been removed");
			}
		}
	}
	public void startServlet(String[] strings, String servletName, StringBuffer message) throws ServletException, IOException {
		/*find JSESSIONID*/
		MySession currentSession = (MySession) isSessionExist();
		MyRequest request;
		if(currentSession != null){
			request = new MyRequest(currentSession);
		}
		else{
			request = new MyRequest();
		}
		for(String k : headerLines.keySet()){
			ArrayList<String> values = headerLines.get(k);
			request.setAttribute(k, values);
		}
		request.setAttribute("ServletContext", HttpServer.context);
		request.setAttribute("ServletConfig", HttpServer.configs.get(servletName));
		request.setAttribute("ServletMapping", HttpServer.h.m_servletsMapping);
		request.setAttribute("requestURL", url);
		request.setAttribute("initialLine",initialLine);
		request.setAttribute("protocol",httpVersion);
		
		request.getQueryString();
		
		HttpSession hs = request.getSession(true);
		Date current = new Date();
		hs.setAttribute("Last-Accessed", current.getTime());
		hs.setAttribute("Request", request);
		logger.info(hs.getId());
		if(hs.isNew() == true) HttpServer.sessions.put(hs.getId(), hs);
		
		MyResponse response = new MyResponse();
		
		Cookie cookie = new Cookie("JSESSIONID",hs.getId());
		response.addCookie(cookie);
		
		HttpServlet servlet = HttpServer.servlets.get(servletName);
		logger.info("strings[0] is "+ strings[0]);
		if(strings[0].equals("GET")){
			for(int i = 2; i<strings.length-2; i+=2){
				request.setParameter(strings[i],strings[i+1]);
			}
		}
		else if(strings[0].equals("POST")){
			logger.info("POST suport...");
			String[] querys = message.toString().split("\\?|&|=|;|\\s");
			for(int i = 0; i<querys.length-1; i+=2){
				logger.debug(querys[i]+"\t"+querys[i+1]);
				request.setParameter(querys[i],querys[i+1]);
			}
		}
		if(strings[0].equals("GET")||strings[0].equals("POST")){
			request.setMethod(strings[0]);
			servlet.service(request,response);
			logger.debug("if committed: "+ response.isCommitted());
			//if(response.isCommitted()==false) response.flushBuffer();
			//logger.debug("if committed: "+ response.isCommitted());
			response.flushBuffer();
			logger.debug("if committed: "+ response.isCommitted());
		}
	}
	
	public HashMap<String, ArrayList<String>> parseHeaderLines(BufferedReader bf) throws IOException{
		HashMap<String, ArrayList<String>> headerLines = new HashMap<String,ArrayList<String>>();
		String lastKey = null;
		ArrayList<String> lastValue = new ArrayList<String>();
		String currentLine;
		while(!(currentLine=bf.readLine()).trim().equals("")){ //read header lines before blank line
		//while((currentLine=bf.readLine())!=null){
			logger.debug("current header line:"+currentLine);
			String pattern = "(.*):\\s+(.*)";
			Matcher m = regexMatcher(pattern, currentLine);
			if(m.find()) { //if match a line with "header:value", put the pair into hashmap
				lastKey = m.group(1).toUpperCase();
				if(headerLines.containsKey(lastKey)){
					lastValue = headerLines.get(lastKey);
				}
				else lastValue = new ArrayList<String>();
				if(m.group(2).contains("GMT")){
					if(!m.group(2).trim().contains("GMT\\s+,")){
						//only one date value in the header field
						lastValue.add(m.group(2));
					}
					else{
						String[] values = m.group(2).split("GMT\\s+,");
						for(String value: values){
							if(value.trim().endsWith("GMT")) lastValue.add(value.trim());
							else{
								value = value.trim() +" GMT";
								lastValue.add(value);
							}
						}
					}
					headerLines.put(lastKey, lastValue);
				}
				else{
					String[] values = m.group(2).split(",");
					for(String s: values) lastValue.add(s);
					headerLines.put(lastKey, lastValue);
					//logger.debug("put "+ lastKey+" "+lastValue);
				}
				//logger.debug("put "+ lastKey+" "+lastValue);
			}
			else{ //if match a line starts with space or tab, append the value to the previous value of header line
				pattern = "(\\s+|\\t+)(.*)";
				m = regexMatcher(pattern,currentLine);
				if(m.find()){
					String[] values = m.group(2).split(",");
					for(String s: values ) lastValue.add(s);
					if(lastKey != null)headerLines.put(lastKey, lastValue);
					else return null;
				}
			}
		}
		for(String k: headerLines.keySet()) {
			for(String s: headerLines.get(k)){
				logger.debug(k + ":" + headerLines.get(k));
			}
		}
		logger.info("finished parsing");
		return headerLines;
	}
	public void processRequest(BufferedReader incomingStuff){
		System.out.println("start process");
		ResponseConstructor rc = new ResponseConstructor();
		StringBuffer messageBody = new StringBuffer();
		try{
			if((initialLine=incomingStuff.readLine())==null) {//read the first line of request
				/*bad request, no content in the request*/
				StringBuffer errResponse = rc.errHandler("HTTP/1.1","400 Bad Request","Request received as null.",
						"HTTP requests must include the status line and header:value lines.");
				outstandingResponse.print(errResponse);
				outstandingResponse.flush();
				return;
			}
			logger.info("intial line:"+ initialLine);
			
			headerLines = parseHeaderLines(incomingStuff);
			String[] strings = initialLine.split("\\?|&|=|;|\\s");
			for (String s: strings) logger.info(s);
			if(strings[0].equals("POST")){
				String contentLength = headerLines.get("CONTENT-LENGTH").get(0);
				int length = Integer.valueOf(contentLength);
				logger.debug("content length is "+ length);
				logger.debug("---mesg body----");
				while(incomingStuff.ready()){
					logger.debug("buffered reader is ready.");
					int i=0;
					while(i<length){
						//logger.debug("start");
						char character = (char)incomingStuff.read();
						messageBody.append(character);
						i++;
						//logger.debug("over");
					}
					logger.debug(messageBody);
					logger.debug("------");
				}
			}
			httpVersion = initialLine.substring(initialLine.length()-8, initialLine.length());
			url = strings[1];
			if(url.trim().startsWith("http://")){
				url = url.substring(8);
				int index = url.indexOf("/");
				url = url.substring(index);
			}
			String servletName = checkMapping(HttpServer.h, url);
			logger.info(servletName);
			if(servletName != null){
				logger.info("There is a mapping...");
				startServlet(strings, servletName, messageBody);
				return;
			}
			logger.info("There is not a mapping...");
			/*
			while((currentLine = incomingStuff.readLine())!=null) { //store optional message body into messageBody variable
				//System.out.println("currentline:"+currentLine);
				messageBody.append(currentLine).append("\n");
				//System.out.println("msgbody:"+messageBody);
			}
			*/
		} catch (IOException|ServletException e){
			e.printStackTrace();
		}
		/*handle special cases*/
		if(initialLine.equals("GET /shutdown "+ httpVersion)) {
			logger.info("start processing shutdown");
			/*shutdown server*/
			ThreadPool.isrunning = false;
			System.out.println(ThreadPool.isrunning);
			outstandingResponse.print(httpVersion +" 200 OK\r\n");
			outstandingResponse.print("\r\n");
			outstandingResponse.flush();
			return;
		}
		else if(initialLine.equals("GET /control "+ httpVersion)){
			/*show up control page*/
			StringBuffer controlResponse = rc.controlHandler(httpVersion);
			outstandingResponse.print(controlResponse);
			outstandingResponse.flush();
		}
		else{
			/*process first line of the request*/
			if(initialLine.trim().equals("")) {
				/*bad request, initial line is empty*/
				StringBuffer errResponse = rc.errHandler(httpVersion,"400 Bad Request","No status line received.",
						"HTTP 1.1 requests must include the status line.");
				outstandingResponse.print(errResponse);
				outstandingResponse.flush();
				return;
				};
			String pattern = "(\\w+)\\s(.*)\\sHTTP.+";
		    Matcher m = regexMatcher(pattern, initialLine);
		    String method = null;
		    StringBuffer relativePath = null;
		    if(!m.find()) {
		    	/*bad request, initial line invalid*/
		    	StringBuffer errResponse = rc.errHandler(httpVersion,"400 Bad Request","Invalid status line received.",
						"Please make sure the HTTP request status line includes a supported HTTP method and a valid pathname.");
				outstandingResponse.print(errResponse);
				outstandingResponse.flush();
				return;
		    	}
		    method = m.group(1);
		    System.out.println("method:"+method);
		    relativePath = new StringBuffer(m.group(2));
		    System.out.println("relative path:"+ relativePath);
		    //StringBuffer fullPathname = rootPath.append(relativePath);
		    File rootDir = new File(rootPath.toString());
		    File file = new File(rootDir, relativePath.toString());
		    StringBuffer fullPathname = new StringBuffer(file.getPath());
		    logger.info("full pathname:"+ fullPathname);
		    if((!method.equals("GET")) && (!method.equals("HEAD"))){
		    	/*501 not implemented*/
		    	StringBuffer errResponse = rc.errHandler(httpVersion, "501 Not Implemented", "HTTP method not supported", 
		    			"HTTP only supports GET and HEAD methods right now.");
		    	outstandingResponse.print(errResponse);
		    	outstandingResponse.flush();
		    	return;
		    	}
		    logger.info("if file exists: " + doesExist(fullPathname));
		    if(doesExist(fullPathname)==false){
		    	/*404 not found*/
		    	StringBuffer errResponse = rc.errHandler(httpVersion, "404 Not Found", "The requested resource doesn't exist", 
		    			"");
		    	outstandingResponse.print(errResponse);
		    	outstandingResponse.flush();
		    	return;
		    	}
		    
		    if(!isAccessible(fullPathname)){
		    	//401 Unauthorized
		    	StringBuffer errResponse = rc.errHandler(httpVersion, "401 Unauthorized",
		    			"The client is not authorized to get access to the resource. ", "");
		    	outstandingResponse.print(errResponse);
		    	outstandingResponse.flush();
		    	return;
		    }
		    	
		    /*the initial line is validated*/
		    /*100 Continue*/
		    if(httpVersion.equals("HTTP/1.1") && headerLines.keySet().contains("Expect")){
		    	StringBuffer interimResponse = rc.firstLineHandler();
		    	outstandingResponse.print(interimResponse);
		    	outstandingResponse.flush();
		    }
		    /*check if host header exists*/
		    for(String key: headerLines.keySet()){
		    	ArrayList<String> values = headerLines.get(key);
		    	System.out.print(key+": ");
		    	for(String value: values) System.out.print(value);
		    	System.out.println();
		    }
		    if((!headerLines.keySet().contains("HOST")) && httpVersion.equals("HTTP/1.1")){
		    	/*400 Bad request, no host header received*/
		    	StringBuffer errResponse = rc.errHandler(httpVersion, "400 Bad Request", "No Host: header received",
		    			"HTTP 1.1 requests must include the Host:header.");
		    	outstandingResponse.print(errResponse);
		    	outstandingResponse.flush();
		    	return;
		    	}
		    /*create a Path instance regarding given pathname*/
		    Path resource = null;
		    try{
		    	resource = Paths.get(fullPathname.toString());
		    }catch(InvalidPathException e){
		    	e.printStackTrace();
		    }
		    
		    /*check if the path represents a directory*/
		    try{
		    	if(Files.isDirectory(resource,LinkOption.NOFOLLOW_LINKS)){
		    		File currentDir = new File(fullPathname.toString());
		    		System.out.println(currentDir.toString());
		    		String[] fileList = currentDir.list();
		    		for (String i: fileList) System.out.println(i);
		    		/*return a page with a list of files in the directory*/
		    		StringBuffer dirResponse = rc.dirRequestHandler(httpVersion, method, fileList);
		    		//System.out.println("@@@@@@@@@@@@@@@@@@@@" + dirResponse);
		    		outstandingResponse.print(dirResponse);
		    		outstandingResponse.flush();
		    		outstandingResponse.close();
		    		//rc.dirRequestHandler(httpVersion, method, fileList, outstandingResponse);
		    		return;
		    	}
		    }catch(SecurityException e){
		    	e.printStackTrace();
		    }
			/*if the file is not a directory, determine the extension of requested file*/
			pattern = "/(\\w+)\\.(\\w+)";
			m = regexMatcher(pattern,relativePath.toString());
			String filename = null;
			String fileExtension = null;
			if(m.find()) {
				filename = m.group(1);
				fileExtension = m.group(2);
			}
			else{
				System.out.println("here");
				/*404 not found*/
				StringBuffer errResponse = rc.errHandler(httpVersion,"404 Not Found", "The requested resource doesn't exist", 
		    			"");
		    	outstandingResponse.print(errResponse);
		    	outstandingResponse.flush();
		    	return;
			}			
			try{
				/*handle If-Modified-Since request*/
				if(headerLines.containsKey("If-Modified-Since")){
					FileTime lastModified = Files.getLastModifiedTime(resource,LinkOption.NOFOLLOW_LINKS);
					String lastModified_string = lastModified.toString();
					String GMT_value = headerLines.get("If-Modified-Since").get(0);
					SimpleDateFormat sdf1 = new SimpleDateFormat();
					sdf1.applyPattern("EEE, dd MMM yyyy HH:mm:ss z");
					sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		            Date dateOfHeader = sdf1.parse(GMT_value);
		            SimpleDateFormat sdf2 = new SimpleDateFormat();
		            sdf2.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		            sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
		            Date dateOfFile = sdf2.parse(lastModified_string);
		            if( dateOfHeader.compareTo(dateOfFile) == 1){
		            	/*304 Not Modified*/
		            	StringBuffer errResponse = rc.errHandler(httpVersion,"304 Not Modified", "The requested resource hasn't been modified since given date", 
				    			"The requested resource is required to be modified after given date value.");
				    	outstandingResponse.print(errResponse);
				    	outstandingResponse.flush();
				    	return;
		            	}
				}
				if(headerLines.containsKey("If-Unmodified-Since")){
					FileTime lastModified = Files.getLastModifiedTime(resource,LinkOption.NOFOLLOW_LINKS);
					String lastModified_string = lastModified.toString();
					String GMT_value = headerLines.get("If-Unmodified-Since").get(0);
					SimpleDateFormat sdf1 = new SimpleDateFormat();
					sdf1.applyPattern("EEE, dd MMM yyyy HH:mm:ss z");
					sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		            Date dateOfHeader = sdf1.parse(GMT_value);
		            SimpleDateFormat sdf2 = new SimpleDateFormat();
		            sdf2.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		            sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
		            Date dateOfFile = sdf2.parse(lastModified_string);
		            if( dateOfHeader.compareTo(dateOfFile) == -1){
		            	/*412 Precondition Failed*/
		            	StringBuffer errResponse = rc.errHandler(httpVersion, "412 Precondition Failed", "The requested resource has been modified since given date", 
				    			"The requested resource is required not to be modified after given date value.");
				    	outstandingResponse.print(errResponse);
				    	outstandingResponse.flush();
				    	return;
		            	}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			try {
				rc.fileHandler(httpVersion, method, fullPathname.toString(), fileExtension, outstandingResponse);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void run(){
		while(true){
			Socket current = null;
			/*read the first socket from blocking queue*/
			try{
				current = readFromQueue();
			} catch (InterruptedException e){
				System.out.println("terminated while in waiting status");
				break;
				//continue;
			}
			logger.info("Successfully get socket");
			/*successfully get the socket, now parse the request */
			try{
				incomingRequest = new BufferedReader(new InputStreamReader(current.getInputStream(),StandardCharsets.UTF_8));
			}catch(IOException e){
				logger.error("IOException occurs when getInputStream() method is called");
			}
			logger.info("got input stream");
			try{
				outstandingResponse = new PrintWriter(new OutputStreamWriter(current.getOutputStream(),StandardCharsets.ISO_8859_1),true);
			}catch(IOException e){
				logger.error("IOExcetption occurs when getOutputStream() method is called.");
			}
			logger.info("Prepare to start process request.");
			processRequest(incomingRequest);
			try {
				current.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("current socket is closed.");
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
