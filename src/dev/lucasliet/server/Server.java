package dev.lucasliet.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
	private AtomicBoolean running;
	private ServerSocket server;
	private ExecutorService threadPool;
	
	public Server() throws IOException {
		System.out.println("--- Starting Server ---");
		this.running = new AtomicBoolean(true);
		this.server = new ServerSocket(3333);
		this.threadPool = Executors.newFixedThreadPool(4);		
	}
	
	public void run(){
		while(this.running.get()) {
			try {
				Socket socket = this.server.accept();
				System.out.println("New Client on port: " + socket.getPort());

				TaskDeliver tasks = new TaskDeliver(threadPool, socket, this);

				this.threadPool.execute(tasks);
			} catch (Exception e) {
				if (!this.running.get()) 
					System.out.println("Server isn't running");
			}
		}
	}
	
	public void stop() throws IOException {
		this.running.set(false);
		this.threadPool.shutdown();
		this.server.close();
		System.out.println("--- Stopping Server ---");		
	}
	
	public static void main(String[] args) throws IOException {
		var server = new Server();
		server.run();
	}

}
