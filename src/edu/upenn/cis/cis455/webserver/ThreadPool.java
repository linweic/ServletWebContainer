package edu.upenn.cis.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class ThreadPool {
	private ArrayList<Thread> pool;
	private int initialCapacity;
	private final LinkedList<Socket> deque;
	//private String rootPath;
	public static boolean isrunning;
	public ThreadPool(LinkedList<Socket> deque, int size, String rootPath){
		this.pool = new ArrayList<Thread>(size);
		for(int i = 0; i< size; i++){
			WorkerThread wt = new WorkerThread(deque,rootPath);
			Thread t = new Thread(wt);
			this.pool.add(t); 
		}
		this.initialCapacity = size;
		this.deque = deque;
		//this.rootPath = rootPath;
		ThreadPool.isrunning = true;
	}
	
	public void startThreads(){
		for (int i = 0; i < initialCapacity;i++ ){
			pool.get(i).start();
		}
	}
	
	public void terminateThreads() throws InterruptedException{
		for (int i = 0; i < initialCapacity; i++) {
			pool.get(i).interrupt();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
