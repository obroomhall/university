import java.io.File;
import java.util.HashMap;

public class PlayGame {
	
	protected GameLogic logic;
	
	public PlayGame(){
		logic = new GameLogic();
	}
	
	public void selectMap(String mapName){
		logic.setMap(new File("maps", mapName));
	}
	
	public boolean addPlayer(int pno){
		return logic.addPlayer(pno);
	}
	
	public String removePlayer(int pno){
		return logic.removePlayer(pno);
	}
	
	public boolean gameRunning(){
		return logic.gameRunning();
	}
	
	public boolean playerActive(int pno){
		if (logic.playerActive(pno)){
			return true;
		}
		return false;
	}
	
	/**
	 * Parsing and Evaluating the User Input.
	 * @param readUserInput input the user generates
	 * @return answer of GameLogic
	 */
	protected synchronized String parseCommand(String readUserInput) {
		
		int pno = readUserInput.charAt(0) - 48;
		readUserInput = readUserInput.substring(1, readUserInput.length());
		
		String [] command = readUserInput.trim().split(" ");
		String answer = "FAIL";
		
		switch (command[0].toUpperCase()){
		case "HELLO":
			answer = hello(pno);
			break;
		case "MOVE":
			if (command.length == 2)
			answer = move(command[1].charAt(0), pno);
		break;
		case "PICKUP":
			answer = pickup(pno);
			break;
		case "LOOK":
			answer = look(pno);
			break;
		case "QUIT":
			answer = removePlayer(pno);
			break;
		default:
			answer = "FAIL";
		}
		
		return answer;
	}

	public int playersConnected(){
		return logic.playersConnected();
	}
	
	private String hello(int pno) {
		return logic.hello(pno);
	}

	private String move(char direction, int pno) {
		return logic.move(direction, pno);
	}

	private String pickup(int pno) {
		return logic.pickup(pno);
	}

	private String look(int pno) {
		return logic.look(pno);
	}
	
	protected void quitGame(){
		logic.quitGame();
	}
	
	public HashMap<Integer, int[]> getPositions(){
		return logic.getPositions();
	}
	
	public int[] getPosition(int pno){
		return logic.getPosition(pno);
	}
}
