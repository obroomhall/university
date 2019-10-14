import java.util.concurrent.ArrayBlockingQueue;
import java.util.HashMap;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ServerUpdate implements Runnable{
	
	private PlayGame game;
	private Server server;
	private HashMap<Integer, int[]> positions;
	private Thread thread;
	private int playerMoved;
	
	public ServerUpdate(PlayGame game, Server server){
		thread = new Thread(ServerUpdate.this);
		this.game = game;
		this.server = server;
		positions = game.getPositions();
	}
	
	public void playerMoved(int pno){
		playerMoved = pno;
	}
	
	public void run(){
		try{
		while (true){
			Thread.sleep(10);
			update(playerMoved);
			playerMoved = 0;
		}
		}
		catch (InterruptedException e){}
	}
	
	public void start(){
		thread.start();
	}
	
	// If the player is in the game, get his position
	public void update(int pno){
		
		if (positions.containsKey(pno)){
			ArrayList<Integer> playersToUpdate = new ArrayList<Integer>();
			int xFocus = positions.get(pno)[0];
			int yFocus = positions.get(pno)[1];
			calculate(xFocus, yFocus);
		}
	}
	
	// Then calculate if another player is occupying a tile in a 3x3 radius
	// Only the players in this radius will have a LOOK command sent
	public void calculate(int xFocus, int yFocus){
		ArrayList<Integer> playersToUpdate = new ArrayList<Integer>();
		for (int key : positions.keySet()){
			int x = positions.get(key)[0];
			int y = positions.get(key)[1];
			
			// Get the difference between the player and another
			int xDiff = xFocus - x;
			int yDiff = yFocus - y;
			
			if (xDiff < 0){
				xDiff = xDiff * -1;
			}
			if (yDiff < 0){
				yDiff = yDiff * -1;
			}
			
			int distance = xDiff + yDiff;
			
			// The distance cannot be 0 because that is the same player
			// A LOOK command is already sent after moving for that player
			// Sending a second LOOK would be pointless
			if (xDiff < 4 && yDiff < 4 && distance != 0){
				playersToUpdate.add(key);
			}
		}
		for (int player : playersToUpdate){
			server.getOutputs().get(player).println(game.parseCommand(player + "LOOK"));
		}
	}
	
	public void playerLeft(int[] pos){
		calculate(pos[0], pos[1]);
	}
}