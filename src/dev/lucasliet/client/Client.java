package dev.lucasliet.client;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		var props = new Properties();
		props.load(new FileReader("conf.ini"));
		
		var socket = new Socket(
				"localhost",
				Integer.parseInt(props.getProperty("port"))
		);

		System.out.println("Connection Stablished");

		var send = new Thread(() -> {
			try {
				System.out.println("You can send commands!");

				var saida = new PrintStream(
						socket.getOutputStream());

				var teclado = new Scanner(System.in);

				while (teclado.hasNextLine()) {

					String linha = teclado.nextLine();

					if (linha.trim().equals("")) {
						break;
					}

					saida.println(linha);
				}

				saida.close();
				teclado.close();
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		var receive = new Thread(() -> {
			try {
				System.out.println("Receiving data from server");
				var respostaServidor = new Scanner(
						socket.getInputStream());

				while (respostaServidor.hasNextLine()) {

					var linha = respostaServidor.nextLine();
					System.out.println(linha);
				}

				respostaServidor.close();
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		send.start();
		receive.start();
		
		send.join();
		
		System.out.println("Closing client socket");

		socket.close();
	}

}
