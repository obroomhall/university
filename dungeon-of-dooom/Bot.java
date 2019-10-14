/**
* Same functions as Client, but inputs its own commands.
*
* @author Oliver Broomhall
* @version 1.0
* @release 07/03/2016
* @see Player.java
*/

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.IOException;

public class Bot extends Player{
	protected Random random;
	protected static final char [] DIRECTIONS = {'N','S','E','W'};
	
	protected ArrayBlockingQueue<String> queue;
	
	protected char[][] map;
	protected int[] destination;
	protected int line;
	
	protected int goldLeft;
	
	public static void main(String [] args) throws IOException{
		new Bot(args);
	}
	
	public Bot(String[] args) throws IOException{
		random = new Random();
		queue = new ArrayBlockingQueue<String>(1024);
		map = new char[5][5];
		destination = new int[2];
		line = -1;
		
		
		// Connects to the server at port
		connectToServer(getPort(args));
		// Instantiates readers and writers connected to the server
		instantiateIO();
		checkForGUI(false);
		// Creates the thread that listens to the server
		startListenThread();
		// Creates the thread that sends commands to the server
		startTalkThread();
	}

	/**
	* Method. Decides what command to send the pass to the server.
	*/
	protected void botAction(String lastAnswer){
		IdentifyResponse ir = new IdentifyResponse();
		int n = ir.getInt(lastAnswer);
		
		switch (n){
			case 1:
				responseFail();
				break;
			case 2:
				responseSuccess();
				break;
			case 3:
				responseNothingToPickUp();
				break;
			case 4:
				responseSuccessPickUp();
				break;
			case 5:
				responseGold(lastAnswer);
				break;
			case 6:
				responseWelcome();
				break;
			case 7:
				responseEndGame();
				break;
			case 8:
				responseMap(lastAnswer);
				break;
		}
	}
	
	protected void responseFail(){
		// If the bot hits a wall or player, have another look at the map and try again
		queue.clear();
	}
	
	protected void responseGold(String gold){
		// Gets the number of gold needed to win
		goldLeft = Integer.parseInt(gold.split(" ")[1]);
	}
	
	protected void responseEndGame(){
		System.exit(0);
	}
	
	protected void responseMap(String lastAnswer){
		// If the bot still needs to collect gold, look for the gold and pick it up
		if (goldLeft != 0){
			storeMap(lastAnswer, 'G');
			if (destination != null){
				moveToDestination();
				queue.add("PICKUP");
				queue.add("HELLO");
			}
		}
		// If all the gold is collected, look for the exit.
		else{
			storeMap(lastAnswer, 'E');
			if (destination != null){
				moveToDestination();
			}
		}
		// Move 5 times in a random direction
		char dir = DIRECTIONS[random.nextInt(4)];
		int rand = random.nextInt(5);
		for (int i = 0; i < rand; i++){
			queue.add("MOVE " + dir);
		}
	}
	
	protected void responseSuccess(){}
	protected void responseNothingToPickUp(){}
	protected void responseSuccessPickUp(){}
	protected void responseWelcome(){}
	
	/**
	* Method. Store the look window in a 5 by 5 array.
	*/
	protected void storeMap(String mapString, char search){
		boolean mapContainsDestination = false;
		destination = new int[2];
		
		if (line == -1){
			line = 4;
		}
		
		// Iterate through the map and store each character
		for (int i = 0; i < 5; i++){
			map[4-line][i] = mapString.charAt(i);
			// If the current coordinate equals the search char, make that the destination
			if (map[4-line][i] == search){
				mapContainsDestination = true;
				destination[0] = 2 - (4-line);
				destination[1] = 2 - i;
			}
		}
		line--;
		if (!mapContainsDestination){
			destination = null;
		}
	}
	
	/**
	* Method. Moves the bot to the destination.
	*/
	protected void moveToDestination(){
		queue.clear();
		while (destination[0] < 0){
			queue.add("MOVE S");
			destination[0] += 1;
		}
		while (destination[0] > 0){
			queue.add("MOVE N");
			destination[0] -= 1;
		}
		while (destination[1] < 0){
			queue.add("MOVE E");
			destination[1] += 1;
		}
		while (destination[1] > 0){
			queue.add("MOVE W");
			destination[1] -= 1;
		}
		destination = null;
	}
	
	/**
	* Method. Prints the next command to the server whilst the game is running.
	*/
	public void talk() throws IOException, InterruptedException{
		toServer.println("PICKUP");
		toServer.println("HELLO");
		toServer.println("LOOK");
		while (!gameOver){
			Thread.sleep(500);
			toServer.println(queue.take());
			toServer.println("LOOK");
		}
	}
	
	/**
	* Method. While the socket is still open, read from the server.
	*/
	public void listen() throws IOException, InterruptedException{
		String response;
		while (!serverSoc.isClosed() && (response = fromServer.readLine()) != null){
			if (gui != null){
				gui.giveResponse(response);
			}
			System.out.println(response);
			botAction(response);
		}
		queue.add("");
		gameOver = true;
	}

}