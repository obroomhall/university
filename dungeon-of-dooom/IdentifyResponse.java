public class IdentifyResponse{
	
	public int getInt(String response){
		
		if (response.equals("FAIL")){
			return 1;
		}
		else if (response.equals("SUCCESS")){
			return 2;
		}
		else if (response.equals("There is nothing to pick up...")){
			return 3;
		}
		else if (response.contains("SUCCESS, GOLD COINS:")){
			return 4;
		}
		else if (response.contains("GOLD")){
			return 5;
		}
		else if (response.equals("You may now use MOVE, LOOK, QUIT and any other legal commands")){
			return 6;
		}
		else if (response.equals("You have escaped the Dungeon of Dooom!!!!!") || response.equals("You have left the game!")){
			return 7;
		}
		else{
			// Is a map
			return 8;
		}
	}
	
}