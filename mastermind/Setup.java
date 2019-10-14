import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
* Defines all the varibles needed to play the game with and setup the display.
*
* @author Oliver Broomhall
* @version 1.0
* @release 05/01/2016
* @see Output.java, Terminal.java, Input.java
*/
public class Setup{
	
	public String hiddenPegs;
	
	/**
	* Method. Gets user input for the settings of the game.
	*
	* @return int[] parameters
	*			Int array of size 3. Contains number of colours and number of pegs to use, and the game mode.
	*
	* @exception IOException
	*/
	public int[] getParameters() throws IOException{
		
		// Asks whether the user wishes to read the README.txt file
		Output.welcomeMessage();		
		readMe();
		
		// Asks whether the user wishes to load a previously saved game
		Output.loadGameMessage();
		loadSavedGame();
		
		// Asks for the number of colours, pegs, and the game mode
		int colours = colours();
		int pegs = pegs();
		int mode = mode();
		
		// Adds the settings to an array
		int[] parameters = new int[3];
		parameters[0] = colours;
		parameters[1] = pegs;
		parameters[2] = mode;
		
		// Mode 1 is Human vs Human. This mode requires the user to input a hidden code. In other modes, the hidden code is computer generated
		if (mode == 1){
			// Asks for the hidden pegs
			Output.codeSetMessage(colours, pegs);
			hiddenPegs = Input.getPegs(colours, pegs);
		}
		// If the mode is 2 or 3, generate hidden pegs
		else{
			
			generateRandomCode(colours, pegs);
			
		}
		
		return parameters;
	}
	
	/**
	* Method. Generates random hidden code.
	*/
	private void generateRandomCode(int colours, int pegs){
		Random r = new Random();
		hiddenPegs = "";
		// Create a string of length pegs, with random colours from the colourNames array in Output
		for (int i = 0; i < pegs; i++){
			String colour = Output.colourNames[r.nextInt(colours)];
			hiddenPegs += colour.substring(0, 1).toLowerCase();
		}
	}
	
	/**
	* Method. Finishes setting up the display before asking the codebreaker to attempt a test case.
	*
	* @param Play game
	*			Data type which stores all of the game settings.
	* @param (optional) int colours
	*			Sets the number of colours used in the game.
	* @param (optional) int pegs
	*			Sets the number of pegs used in the game.
	*/
	public void finishSetup(int colours, int pegs){
		Output.gameDisplay(pegs);
		Terminal.moveCursorUp(1);
		Output.codeAttemptMessage(colours, pegs);
		Terminal.unsaveCursor();
		Output.insertPattern(hiddenPegs, 30, -12);
		Terminal.unsaveCursor();
	}
	
	/**
	* Method. Tests whether the parameters given will work to setup the game.
	*
	* @param String[] s
	* 			Array of strings of size 5.
	*			 1. y/n to display README.txt
	*			 2. Number of colours
	*			 3. Number of pegs
	*			 4. Mode
	*			 5. (optional) Hidden code
	*
	* @return boolean
	* 			True if the parameters setup the game correctly, false otherwise
	*
	* @exception IOException
	* @exception ArrayIndexOutOfBoundsException
	*
	* @see Input.java
	*/
	public boolean giveParameters(String[] s) throws IOException, ArrayIndexOutOfBoundsException{
		
		// If the array has the incorrect number of Strings, return false
		try{
			
			// The array cannot be less than 4 elements long
			if (s.length < 4){
				return false;
			}
			
			// Asks whether the user wishes to read the README.txt file
			Output.welcomeMessage();
			
			// If the input is a char, check for 'y' or 'n'
			if (Input.charCheck(s[0])){
				// If yes, print y and the README.txt file
				if (s[0].charAt(0) == 'y'){
					System.out.println("y");
					Output.readMe();
				}
				// If no, print n for cosmetic purposes
				else if (s[0].charAt(0) == 'n'){
					System.out.println("n");
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}
			
			// Asks for the number of colours
			Output.requestColours();
			
			// Checks the input for between 3 and 8 exclusive
			if (Input.numCheck(3, 8, s[1])){
				System.out.println(s[1]);
			}
			else{
				return false;
			}
			
			// Asks for the number of pegs
			Output.requestPegs();
			
			// Checks the input for between 3 and 8 exclusive
			if (Input.numCheck(3, 8, s[2])){
				System.out.println(s[2]);
			}
			else{
				return false;
			}
			
			// Asks for the mode
			Output.requestMode();
			
			// Checks the input is between 0 and 4 exclusive
			if (Input.numCheck(0, 4, s[3])){
				System.out.println(s[3]);
			}
			else{
				return false;
			}
			
			int colours = Integer.parseInt(s[1]);
			int pegs = Integer.parseInt(s[2]);
			int mode = Integer.parseInt(s[3]);
			
			// If the mode = 1, ask for the hidden code
			if (mode == 1){
				Output.codeSetMessage(colours, pegs);
				
				// If the hidden code input satifies the check, set hiddenPegs
				if (Input.pegsCheck(colours, pegs, s[4].toCharArray())){
					System.out.println(s[4]);
					hiddenPegs = s[4];
				}
				else{
					return false;
				}
			}
			else{
				generateRandomCode(colours, pegs);
			}
		}
		catch (ArrayIndexOutOfBoundsException e){
			return false;
		}
		
		return true;
	}
	
	/**
	* Method. Loads a previously saved game.
	* UNFINSISHED
	*
	* @exception IOException
	*/
	private void loadSavedGame() throws IOException{
		char c = Input.getCharacter();
		if (c == 'y'){
			MMFiles.load();
		}
		else if (c != 'n'){
			Output.fix();
			loadSavedGame();
		}
	}
	
	/**
	* Method. Gets char input and tests for y to show README.txt.
	*
	* @exception IOException
	*/
	private void readMe() throws IOException{
		char c = Input.getCharacter();
		
		// If input is y, show README.txt
		if (c == 'y'){
			Output.readMe();
		}
		// If input is anything else but n, fix the interface and recurse
		else if (c != 'n'){
			Output.fix();
			readMe();
		}
		// If n, do nothing
	}
	
	/**
	* @return int Number of colours between 3 and 8 exclusive
	*/
	private int colours() throws IOException{
		Output.requestColours();
		return Input.getNumber(3, 8);
	}
	
	/**
	* @return int Number of pegs between 3 and 8 exclusive
	*/
	private int pegs() throws IOException{
		Output.requestPegs();
		return Input.getNumber(3, 8);
	}
	
	/**
	* @return int Mode between 0 and 4 exclusive
	*/
	private int mode() throws IOException{
		Output.requestMode();
		return Input.getNumber(0, 4);
	}
}