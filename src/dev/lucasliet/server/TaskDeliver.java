package dev.lucasliet.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskDeliver implements Runnable {

	private ExecutorService threadPool;
	private Socket socket;
	private Server server;

	public TaskDeliver(ExecutorService threadPool, Socket socket, Server server) {
		this.threadPool = threadPool;
		this.socket = socket;
		this.server = server;
	}

	@Override
	public void run() {
		try {
			System.out.printf("=== Delivering tasks to client %s === %n" , socket);
			Scanner clientInput = new Scanner(socket.getInputStream());
			PrintStream clientOutput = new PrintStream(socket.getOutputStream());
			
			while(clientInput.hasNextLine()) {
				String command = clientInput.nextLine();
				
				switch (command) {
				
					case "c1": {						
						clientOutput.println("command c1 is running");
		                this.threadPool.execute( () -> {
		                	System.out.println("running command c1"); 

		            		try {
		            			Thread.sleep(20000); //task time simulation
		            		} catch (InterruptedException e) {
		            			throw new RuntimeException(e);
		            		} 
		            		
		            		clientOutput.println("Server finished c1 command");
		                });
						break;
					}
					
					case "c2": {
						clientOutput.println("command c2 is running");
	
				        Future<String> dbResponse = this.threadPool.submit(() -> {
				        	System.out.println("Server received c2 command - DB");
				    		clientOutput.println("Processing c2 command - DB");

				    		Thread.sleep(15000); //task time simulation

				    		int num = new Random().nextInt(100) + 1; //number from 1 to 100
				    		System.out.println("Server finished c2 command - DB");
				    		return Integer.toString(num);
				        });
				        
				        Future<String> apiResponse = this.threadPool.submit(() -> {
				        	System.out.println("Server received c2 command - API");
				    		clientOutput.println("Processing c2 command - API");

				    		Thread.sleep(10000); //task time simulation

				    		int num = new Random().nextInt(100) + 1; //number from 1 to 100
				    		System.out.println("Server finished c2 command - API");
				    		return Integer.toString(num);
				        });
	
				        this.threadPool.execute(() -> {
				        	System.out.println("Waiting results from API and DB");

				            try {
				                String apiData = apiResponse.get(20, TimeUnit.SECONDS); //Timeout limit
				                String dbData = dbResponse.get(20, TimeUnit.SECONDS);

				                clientOutput.println("c2 Results: "
				                        + apiData + ", " + dbData);

				            } catch (InterruptedException | ExecutionException | TimeoutException e) {

				                System.out.println("Timeout: canceling c2");

				                apiResponse.cancel(true);
				                dbResponse.cancel(true);
				                
				                clientOutput.println("c2 request timeout");
				            }

				            System.out.println("Server finished c2 results");
				        });
				        
						break;
					}
					
					case "stop": {
						clientOutput.println("Shutting down server");
						this.server.stop();
						return;
					}
					
					default: {
						clientOutput.println("Command not found");
					}
				}
				
			}
			
			clientInput.close();
			clientOutput.close();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		

	}

}
