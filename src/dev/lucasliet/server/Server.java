package dev.lucasliet.server;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
	private String[] args;
	private AtomicBoolean running;
	private ServerSocket server;
	private ExecutorService threadPool;
	
	public Server(String[] args, int port) throws IOException {
		this.args = args;
		log("--- Starting Server ---");
		log(new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm:ss z").format(new Date(System.currentTimeMillis())));
		this.running = new AtomicBoolean(true);
		this.server = new ServerSocket(port);
		this.threadPool = Executors.newFixedThreadPool(4);		
	}
	
	public void run(){
		while(this.running.get()) {
			try {
				var socket = this.server.accept();
				log("New Client on port: " + socket.getPort());

				var tasks = new TaskDeliver(threadPool, socket, this);

				this.threadPool.execute(tasks);
			} catch (Exception e) {
				if (!this.running.get()) 
					log("Server isn't running");
			}
		}
	}
	
	public void stop() throws IOException {
		this.running.set(false);
		this.threadPool.shutdown();
		this.server.close();
		log("--- Stopping Server ---");		
	}
	
	public void log(String msg) {
		for (String arg : this.args) {
			if(arg.equalsIgnoreCase("--debug") || arg.equalsIgnoreCase("-d")) {
				System.out.println(msg);
				break;
			}
		}
		try {
			var filewriter = new FileWriter("serverlog.txt", true);
			filewriter.write(System.lineSeparator());
			filewriter.write(System.lineSeparator());
			filewriter.write(msg);
			filewriter.close();
		} catch (IOException e) {
			System.out.println("File log save ERROR: " + e.getMessage());
		}
	}

	public static void main(String[] args) throws IOException {
		var props = new Properties();
		props.load(new FileReader("conf.ini"));
		
		var server = new Server(
				args,
				Integer.parseInt(props.getProperty("port"))
		);
		server.run();
	}

}
