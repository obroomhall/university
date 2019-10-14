/**
* Abstract Player class. Gives the Client and Bot classes the methods they need to connect
*	to the server and play the game.
*
* @author Oliver Broomhall
* @version 1.0
* @release 07/03/2016
* @see Client.java, Bot.java
*/

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.net.ConnectException;
import java.net.UnknownHostException;

public abstract class Player{
	
	protected BufferedReader fromServer;
	protected PrintWriter toServer;
	protected Socket serverSoc;
	protected boolean gameOver;
	protected GameGUI gui;
	
	/**
	* Method. Gets the port number from command line arguments.
	*/
	protected int getPort(String[] args){
		try{
			if (args.length > 0){
				return Integer.parseInt(args[0]);
			}
		}
		catch (NumberFormatException e){
			System.out.println("Port number given is not an integer!");
			System.exit(-1);
		}
		return 40004;
	}
	
	protected void checkForGUI(boolean enableButtons) throws IOException{
		System.out.println("Would you like to run a GameGUI? (y/n)");
		Scanner in = new Scanner(System.in);
		if (in.nextLine().toLowerCase().equals("y")){
			gui = new GameGUI(this, enableButtons);
		}
		else{
			if (enableButtons){
				System.out.println("You can start a GUI at any time by typing 'gui'.");
			}
		}
	}
	
	/**
	* Method. Creates a new socket connected to the server.
	*/
	protected void connectToServer(int port) throws IOException{
		// Connects to the server at port
		try{
			serverSoc = new Socket("localhost", port);
		}
		catch (ConnectException e){
			System.out.println(e);
			return;
		}
		catch (UnknownHostException e){
			System.out.println(e);
			return;
		}
	}
	
	/**
	* Creates the IO.
	*/
	protected void instantiateIO() throws IOException{
		// Instantiates readers and writers connected to the server
		fromServer = new BufferedReader(new InputStreamReader(serverSoc.getInputStream()));
		toServer = new PrintWriter(serverSoc.getOutputStream(), true);
	}
	
	/**
	* Thread method. Starts the thread for listening to the server.
	*/
	protected void startListenThread(){
		Thread listenThread = new Thread(){
			public void run(){
				try{
					listen();
				}
				catch (IOException e){}
				catch (InterruptedException e){}
			}
		};
		listenThread.start();
	}
	
	/**
	* Thread method. Starts the thread for talking to the server.
	*/
	protected void startTalkThread(){
		// Creates the thread that sends commands to the server
		Thread talkThread = new Thread(){
			public void run(){
				try{
					talk();
				}
				catch (IOException e){}
				catch (InterruptedException e){}
			}
		};
		// Starts the threads
		talkThread.start();
	}
	
	public abstract void listen() throws IOException, InterruptedException;	
	public abstract void talk() throws IOException, InterruptedException;
	
}