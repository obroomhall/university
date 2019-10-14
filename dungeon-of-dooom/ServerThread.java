/**
* ServerThread is created every time a new client connects to the server.
*	Instantiates I/O readers and writers to communicate with the client.
*	Passes commands to the PlayGame object.
*
* @author Oliver Broomhall
* @version 1.0
* @release 07/03/2016
* @see Server.java, Runnable.java
*/

import java.net.Socket;
import java.net.ServerSocket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;

public class ServerThread implements Runnable{
	
	private Socket socket;
	private ServerSocket serverSoc;
	private PlayGame game;
	private BufferedReader fromClient;
	protected PrintWriter toClient;
	private int playerNumber;
	private ServerUpdate update;
	private Server server;
	
	/**
	* Constructor. Gets the socket, PlayGame instance and player ID.
	*/
	public ServerThread(Socket socket, PlayGame game, int playerNumber, ServerSocket serverSoc, ServerUpdate update, Server server){
		this.socket = socket;
		this.game = game;
		this.playerNumber = playerNumber;
		this.serverSoc = serverSoc;
		this.update = update;
		this.server = server;
	}
	
	/**
	* Method. Runs when the thread starts. Instantiates readers and writers. Adds
	*	the player to the game and sends useful information to the client.
	*/
	public void run(){
		try{
			fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			toClient = new PrintWriter(socket.getOutputStream(), true);
			
			// If the player was unable to be added to the map, exit.
			if (!game.addPlayer(playerNumber)){
				toClient.println("Could not initiate player on map!");
				return;
			}
			
			server.addOutput(playerNumber, toClient);
			update.playerMoved(playerNumber);
			
			System.out.println("Client " + playerNumber + " joined the game");
			toClient.println("You may now use MOVE, LOOK, QUIT and any other legal commands");
			update();
		}
		catch (IOException e){}
	}
	
	/**
	* Method. Repeats whilst the game is active. Gets commands from the client
	*	and passes them to the PlayGame instance.
	*/
	private void update() throws IOException{
		String userInput;
		int[] tempPos = new int[2];
		while (game.playerActive(playerNumber) && (userInput = fromClient.readLine()) != null){
			// Gets the command from the client and passes it to the game.
			if (userInput.startsWith("QUIT")){
				tempPos = game.getPosition(playerNumber);
			}
			String answer = game.parseCommand(playerNumber + userInput);
			if (userInput.startsWith("MOVE")){
				update.playerMoved(playerNumber);
			}
			// Sends the response to the client.
			toClient.println(answer);
		}
		
		// If the player disconnected without using the QUIT command, they need to be manually removed.
		if (game.playerActive(playerNumber)){
			game.removePlayer(playerNumber);
		}
		
		System.out.println("Client " + playerNumber + " left the game");
		server.removeOutput(playerNumber);
		update.playerLeft(tempPos);
		
		// Server will end if all players disconnect.
		if (game.playersConnected() == 0){
			game.quitGame();
			serverSoc.close();
		}
		
		socket.close();
	}
}