package dev.lucasliet.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket("localhost", 3333);

		System.out.println("Connection Stablished");

		Thread send = new Thread(() -> {
			try {
				System.out.println("Pode enviar comandos!");

				PrintStream saida = new PrintStream(
						socket.getOutputStream());

				Scanner teclado = new Scanner(System.in);

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

		Thread receive = new Thread(() -> {
			try {
				System.out.println("Recebendo dados do servidor");
				Scanner respostaServidor = new Scanner(
						socket.getInputStream());

				while (respostaServidor.hasNextLine()) {

					String linha = respostaServidor.nextLine();
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
