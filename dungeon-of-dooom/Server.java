/**
* Server class. Opens port 40004 ready for connections from clients. Also
* 	starts the game and defines the map used. Class runs until the game
*	is no longer running, then closes all server threads cleanly exits.
*
* @author Oliver Broomhall
* @version 1.0
* @release 07/03/2016
*/

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.BindException;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.io.PrintWriter;

public class Server{
	
	private PlayGame game;
	private int players;
	
	private ServerUpdate update;
	private HashMap<Integer, PrintWriter> outputs;
	
	
	/**
	* Main method. Gets the port number and passes it to the constructor.
	*/
	public static void main(String[] args) throws IOException, ArrayIndexOutOfBoundsException{
		int port = 40004;
		try{
			if (args.length > 0){
				port = Integer.parseInt(args[0]);
			}
		}
		catch (NumberFormatException e){
			System.out.println("Port number given is not an integer!");
			System.exit(-1);
		}
		new Server(port);
	}
	
	/**
	* Constructor. Starts the server on port 40004, starts the game, and
	*	finally allow connections to the server socket.
	*
	* @exception IOException
	*	Signals that an I/O exception of some sort has occurred.
	*/
	private Server(int port) throws IOException{

		ServerSocket serverSoc = null;
		
		// Tries to start the server socket on port.
		try{
			serverSoc = new ServerSocket(port);
		}
		// If the port is already in use, aids the user into correcting the error.
		catch (BindException e){
			System.err.println(e);
			System.out.println("To fix: $ lsof -i:[port]");
			System.out.println("Then: $ kill [pid]");
			System.exit(-1);
		}
		
		// Starts the game.
		game = new PlayGame();
		
		// Sets the map.
		askForMap();
		
		update = new ServerUpdate(game, this);
		update.start();
		
		outputs = new HashMap<Integer, PrintWriter>();
		
		// Accepts new clients.
		while (game.gameRunning()){
			try{
				Socket clientSoc = serverSoc.accept();
				players++;
				new Thread(new ServerThread(clientSoc, game, players, serverSoc, update, this)).start();
			}
			catch (SocketException e){
				System.out.println("Server will quit!");
				System.exit(-1);
			}
		}
		System.out.println("Server will quit!");
		serverSoc.close();
	}
	
	public void addOutput(int pno, PrintWriter pw){
		outputs.put(pno, pw);
	}
	
	public void removeOutput(int pno){
		outputs.remove(pno);
	}
	
	public HashMap<Integer, PrintWriter> getOutputs(){
		return outputs;
	}
	
	/**
	* Method. Gets map name from command line.
	*/
	private void askForMap(){
		Scanner in = new Scanner(System.in);
		System.out.println("Do you want to load a specific map?");
		System.out.println("Press enter for default map");
		game.selectMap(in.nextLine());
	}
}