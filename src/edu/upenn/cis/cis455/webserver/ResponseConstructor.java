package edu.upenn.cis.cis455.webserver;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class ResponseConstructor {
	public ResponseConstructor(){}
	public StringBuffer controlHandler(String httpVersion){
		StringBuffer content = new StringBuffer();
		StringBuffer msgBody = new StringBuffer();
		msgBody.append("<html><head><title>Control Panel</title></head>\n");
		msgBody.append("<body><h1>This is the control panel.</h1><br>\n");
		msgBody.append("<p>Name: Linwei Chen, Penn Login: linweic</p><br>\n");
		msgBody.append("<form action=\"shutdown\" method=\"get\">\n");
		msgBody.append("<input type=\"submit\" value=\"Shutdown\">\n");
		msgBody.append("</form></body></html>");
		content.append(httpVersion).append(" 200 OK\r\n");
		content.append("Date: ").append(DateHeader()).append("\r\n");
		content.append("Content-Type: text/html\r\n");
		content.append("Content-Length: ").append(msgBody.length()).append("\r\n\r\n");
		content.append(msgBody);
		return content;
	}
	public String DateHeader(){
		Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("EEE, dd MMM yyyy HH:mm:ss z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(date);
	}
	public StringBuffer errHandler(String httpVersion, String errorType, String headline, String body){
		StringBuffer content = new StringBuffer();
		StringBuffer msgBody = new StringBuffer();
		/*construct message body first*/
		msgBody.append("<html><body>\n<h2>");
		msgBody.append(headline);
		msgBody.append("</h2>\n");
		msgBody.append(body);
		msgBody.append("\n</body></html>\n");
		/*get the content-length value*/
		int length = msgBody.length();
		content.append(httpVersion).append(" ").append(errorType).append("\r\n");//construct status line
		content.append("Content-Type: text/html\r\n");
		content.append("Content-Length: ").append(length).append("\r\n");
		content.append("Date: ").append(DateHeader()).append("\r\n\r\n");
		content.append(msgBody);
		return content;
	}
	public StringBuffer firstLineHandler(){
		StringBuffer content = new StringBuffer("HTTP/1.1 100 Continue\r\n\r\n");
		return content;
	}
	public StringBuffer dirRequestHandler(String httpVersion, String method, String[] list){
		StringBuffer content = new StringBuffer(httpVersion);
		content.append(" 200 OK\r\n");
		content.append("Date: ").append(DateHeader()).append("\r\n");
		//System.out.println("dir response:"+ content);
		if(method.equals("GET")){
			StringBuffer msgBody = new StringBuffer();
			/*construct message body*/
			msgBody.append("<html><body>\n");
			msgBody.append("<ul style=\"list-style-type:none\">\n");
			for (String element:list){
				msgBody.append("<li>").append(element).append("</li>\n");
			}
			msgBody.append("</ul>\n");
			msgBody.append("</body></html>\n");
			//System.out.println("message body:"+ msgBody);
			int length = msgBody.length();
			
			content.append("Content-Type: text/html\r\n");
			content.append("Content-Length: ").append(length).append("\r\n\r\n");
			content.append(msgBody);
			//System.out.println("entire response:\n"+ content);
		}
		else content.append("\r\n");
		return content;
	}
	
	public void fileHandler(String httpVersion, String method, String fullpath, String extension, PrintWriter out) throws IOException{
		StringBuffer content = new StringBuffer();
		content.append(httpVersion).append(" 200 OK\r\n");
		content.append("Date: ").append(DateHeader()).append("\r\n");
		//out.print(content);
		System.out.println("enter file handler");
		if(method.equals("GET")){
			if(extension.equals("jpg")||extension.equals("gif")||extension.equals("png")){
				System.out.println("parse image");
				Path file = Paths.get(fullpath);
				byte[] bytes = Files.readAllBytes(file);
				StringBuffer binaryFile = new StringBuffer();
				for(byte b: bytes){
					String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
					binaryFile.append(s1);
				}
				content.append("Content-Type: image/").append(extension).append("\r\n");
				content.append("Content-Length: ").append(bytes.length).append("\r\n");
				out.print(content);
				out.print("\r\n");
				out.print(binaryFile);
				//out.flush();
			}
			else if (extension.equals("html")||extension.equals("txt")){
				StringBuffer msgBody = new StringBuffer();
				try {
					
			        BufferedReader in = new BufferedReader(new FileReader(new File(fullpath)));
			        String str;
			        while ((str = in.readLine()) != null) {
			        	System.out.println(str);
			            msgBody.append(str);
			        }
			        in.close();
			    } catch (IOException e) {
			    	e.printStackTrace();
			    }
				content.append("Content-Type: text/").append(extension).append("\r\n");
				content.append("Content-Length: ").append(msgBody.length()).append("\r\n\r\n");
				out.print(content);
				out.print(msgBody);
				//out.flush();
			}
		}
		out.print("\r\n");
		out.flush();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
