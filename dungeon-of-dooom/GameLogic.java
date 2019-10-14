/**
* Game logic class. Connects the map to the game and handles players and their gold.
*
* @author Oliver Broomhall
* @version 1.0
* @release 07/03/2016
* @see IGameLogic.java, Map.java
*/

import java.io.File;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;

public class GameLogic implements IGameLogic{

	private Map map = null;
	private boolean active;
	
	private HashMap<Integer, int[]> playerPosition;
	private HashMap<Integer, Integer> collectedGold;
	
	/**
	* Constructor. Instantiates the HashMaps and creates the map.
	*/
	public GameLogic(){
		playerPosition = new HashMap<Integer, int[]>();
		collectedGold = new HashMap<Integer, Integer>();
		map = new Map();
	}
	
	/**
	* Method. Adds players to the game.
	*
	* @param int pno
	*	Player number (Player NO.)
	*
	* @return True for if the player was created successfully
	*/
	public boolean addPlayer(int pno){
		
		// Gets the coordinates of a non-wall and non-occupied space
		int[] pos = initiatePlayer(pno);
		
		// If a free space could not be found, return false and the client will be force quit
		if (pos == null){
			return false;
		}
		
		// Put the player into the HashMaps
		playerPosition.put(pno, pos);
		collectedGold.put(pno, 0);
		
		return true;
	}

	protected Map getMap(){
		return map;
	}

	public void setMap(File file) {
		map.readMap(file);
		active = true;
	}

	/**
	 * Prints how much gold is still required to win!
	 */
	public String hello(int pno) {
		return "GOLD: " + (map.getWin() - collectedGold.get(pno));
	}

	/**
	 * By proving a character direction from the set of {N,S,E,W} the gamelogic 
	 * checks if this location can be visited by the player. 
	 * If it is true, the player is moved to the new location.
	 * @return If the move was executed Success is returned. If the move could not execute Fail is returned.
	 */
	public String move(char direction, int pno) {
	
		int[] newPosition = playerPosition.get(pno).clone();
		
		switch (direction){
		case 'N':
			newPosition[0] -=1;
			break;
		case 'E':
			newPosition[1] +=1;
			break;
		case 'S':
			newPosition[0] +=1;
			break;
		case 'W':
			newPosition[1] -=1;
			break;
		default:
			return "FAIL";
		}
		
		// If the tile is occupied by a player, return fail and do not move.
		if (tileOccupied(newPosition[0], newPosition[1], pno)){
			return "FAIL";
		}
		
		if(map.lookAtTile(newPosition[0], newPosition[1]) != '#'){
			playerPosition.put(pno, newPosition);
			
			if (checkWin(pno)){
				return "You have escaped the Dungeon of Dooom!!!!!";
			}
			else{
				return "SUCCESS";
			}
		} else {
			return "FAIL";
		}
	}
	
	/**
	* Method. Iterates through the HashMap to look for players on a set of coordinates.
	*
	* @return True if the space is occupied by a player.
	*/
	public boolean tileOccupied(int x, int y, int pno){
		for (int key : playerPosition.keySet()){
			if (key != pno){
				int a = playerPosition.get(key)[0];
				int b = playerPosition.get(key)[1];
				if (a == x && b == y){
					return true;
				}
			}
		}
		return false;
	}

	/**
	* Method. Attempts to pick up gold.
	*
	* @return String
	*	"SUCCESS, GOLD COINS: [gold]" if the tile contained gold
	*	"FAIL \n There is nothing to pick up..." else
	*/
	public String pickup(int pno) {
		
		int[] playerPosition = this.playerPosition.get(pno);
		
		if (map.lookAtTile(playerPosition[0], playerPosition[1]) == 'G') {
			
			// Increments the collected gold of the player
			int gold = collectedGold.get(pno);
			gold++;
			collectedGold.put(pno, gold);
			
			map.replaceTile(playerPosition[0], playerPosition[1], '.');
			return "SUCCESS, GOLD COINS: " + gold;
		}

		return "FAIL" + "\n" + "There is nothing to pick up...";
	}
	
	/**
	* Method. Tests whether the player has been properly initialised.
	*/
	public boolean playerActive(int pno){
		if (playerPosition.get(pno) == null || collectedGold.get(pno) == null){
			return false;
		}
		else{return true;}
	}

	
	/**
	 * The method shows the dungeon around the player location
	 */
	public String look(int pno) {
		int[] pos = new int[2];
		pos = playerPosition.get(pno);
		String output = "";
		char [][] lookReply = map.lookWindow(pos[0], pos[1], 5);
		
		int yDiff = 2 - pos[0];
		int xDiff = 2 - pos[1];
		
		// Replaces tiles with 'P' if they are occupied
		for (int key : playerPosition.keySet()){
			if (key != pno){
				int x = playerPosition.get(key)[0] + yDiff;
				int y = playerPosition.get(key)[1] + xDiff;
				if (x < 5 && x >= 0 && y < 5 && y >= 0){
					lookReply[y][x] = 'P';
				}
			}
		}
		
		lookReply[2][2] = 'P';
		lookReply[0][0] = 'X';
		lookReply[4][4] = 'X';
		lookReply[0][4] = 'X';
		lookReply[4][0] = 'X';

		for (int i=0;i<lookReply.length;i++){
			for (int j=0;j<lookReply[0].length;j++){
				output += lookReply[j][i];
			}
			output += "\n";
		}
		
		return output.substring(0, output.length() - 1);
	}

	/*
	 * Prints the whole map directly to Standard out.
	 */
	public void printMap() {
		map.printMap();
	}

	/**
	 * finds a random position for the player in the map.
	 * @return Return null; if no position is found or a position vector [y,x]
	 */
	private int[] initiatePlayer(int pno) {
		int[] pos = new int[2];
		Random rand = new Random();

		pos[0]=rand.nextInt(map.getMapHeight());
		pos[1]=rand.nextInt(map.getMapWidth());
		int counter = 1;
		
		// Repeats whilst the tile is not a wall or occupied
		while (map.lookAtTile(pos[0], pos[1]) == '#' || tileOccupied(pos[0], pos[1], pno)) {
			if (counter < map.getMapHeight() * map.getMapWidth()){
				pos[1]= (int) ( counter * Math.cos(counter));
				pos[0]=(int) ( counter * Math.sin(counter));
				counter++;
			}
		}
		return (map.lookAtTile(pos[0], pos[1]) == '#') ? null : pos;
	}

	/**
	 * checks if the player collected all GOLD and is on the exit tile
	 * @return True if all conditions are met, false otherwise
	 */
	protected boolean checkWin(int pno) {
		int[] pos = playerPosition.get(pno);
		if (collectedGold.get(pno) >= map.getWin() && 
				map.lookAtTile(pos[0], pos[1]) == 'E') {
			removePlayer(pno);
			System.out.println("Client " + pno + " has escaped!");
			return true;
		}
		return false;
	}

	/**
	 * Quits the game when called
	 */
	public void quitGame() {
		active = false;
	}
	
	/**
	* Method. Removes the player pno from the game.
	*/
	public String removePlayer(int pno){
		playerPosition.remove(pno);
		collectedGold.remove(pno);
		return "You have left the game!";
	}
	
	public boolean gameRunning(){
		return active;
	}
	
	public int playersConnected(){
		return playerPosition.size();
	}
	
	public HashMap<Integer, int[]> getPositions(){
		return playerPosition;
	}
	
	public int[] getPosition(int pno){
		return playerPosition.get(pno);
	}
}
