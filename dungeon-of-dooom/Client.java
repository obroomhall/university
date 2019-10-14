/**
* Client class. Extends Player. Connects to the server.
*
* @author Oliver Broomhall
* @version 1.0
* @release 07/03/2016
* @see Player.java
*/

import java.util.Scanner;
import java.io.IOException;

public class Client extends Player{
	
	private Scanner in;
	
	public static void main(String[] args) throws IOException, ArrayIndexOutOfBoundsException{
		new Client(args);
	}
	
	/**
	* Constructor. Connects to the server and identifies I/O streams.
	* 	Also starts two threads, one for sending commands to the server,
	* 	and one thread for listening to responses.
	*
	* @exception IOException
	*	Signals that an I/O exception of some sort has occurred.
	*/
	public Client(String[] args) throws IOException{
		
		
		
		// Connects to the server at port
		connectToServer(getPort(args));
		
		// Instantiates readers and writers connected to the server
		instantiateIO();
		checkForGUI(true);
		in = new Scanner(System.in);
		
		// Creates the thread that listens to the server
		startListenThread();
		
		// Creates the thread that sends commands to the server
		startTalkThread();
		
	}
	
	/**
	* Method. Listens for responses from the server, then prints the
	*	responses out to the terminal.
	*
	* @exception IOException
	*	Signals that an I/O exception of some sort has occurred.
	*/
	public void listen() throws IOException{
		String response;
		while (!serverSoc.isClosed() && (response = fromServer.readLine()) != null){
			if (gui != null){
				gui.giveResponse(response);
			}
			System.out.println(response);
		}
		System.out.println("Press enter to close the client.");
		gameOver = true;
	}
	
	/**
	* Method. Waits for user input from command line, then sends the
	*	commands to the server.
	*
	* @exception IOException
	*	Signals that an I/O exception of some sort has occurred.
	*/
	public void talk() throws IOException{
		String userInput = "";
		while (!gameOver && (userInput = in.nextLine()) != null){
			if (userInput.equals("gui")){
				if (gui == null){
					gui = new GameGUI(this, true);
					toServer.println("LOOK");
					toServer.println("HELLO");
				}
				else{
					gui.displayGUI();
				}
			}
			else{
				toServer.println(userInput);
			}
		}
	}
}