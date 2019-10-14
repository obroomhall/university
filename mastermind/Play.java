import java.util.Random;
import java.util.Arrays;

/**
* Holds the values for the game and handles test case input.
*
* @author Oliver Broomhall
* @version 1.0
* @release 05/01/2016
* @see Setup.java, Output.java
*/
public class Play{
	
	private String hiddenPegs;
	private int attempts;
	private int colours;
	private int pegs;
	private int mode;
	private boolean won;
	
	/**
	* Constructor.
	*
	* @param int colours
	*			Number of colours to be used.
	* @param int pegs
	*			Number of pegs to be used.
	* @param int mode
	*			Game mode to play.
	* @param Setup s
	* 			For access to hidden pegs.
	*/
	public Play(int colours, int pegs, int mode, String hiddenPegs){
		
		this.colours = colours;
		this.pegs = pegs;
		this.mode = mode;
		this.hiddenPegs = hiddenPegs;
		
	}
	
	/**
	* @return Hidden pegs string.
	*/
	public String getHiddenPegs(){
		return hiddenPegs;
	}
	
	/**
	* @return Number of pegs.
	*/
	public int getPegs(){
		return pegs;
	}
	
	/**
	* @return Number of colours.
	*/
	public int getColours(){
		return colours;
	}
	
	/**
	* @return True if won, else false.
	*/
	public boolean checkWin(){
		if (won){
			return true;
		}
		return false;
	}
	
	/**
	* @return True if codebreaker has had 12 attempts, else false.
	*/
	public boolean checkLoss(){
		if (attempts >= 12){
			return true;
		}
		return false;
	}
	
	/**
	* @return True if either win or loss, else false.
	*/
	public boolean end(){
		if (checkWin() || checkLoss()){
			return true;
		}
		return false;
	}
	
	/**
	* Method. Handles test case input.
	*
	* @param String in
	*			Test case code for input, already guarded against errors
	*
	* @return String split[1]
	*			Key pegs string
	*
	* @exception ArrayIndexOutOfBoundsException
	*/
	public String input(String in) throws ArrayIndexOutOfBoundsException{
		
		// Increment attempts
		attempts++;
		
		// If attempts are greater than 12, exit
		if (attempts > 12){
			return null;
		}
		
		// If input string and hidden pegs are equal, game is won
		if (in.equals(hiddenPegs)){
			won = true;
		}
		
		// Array for storing key pegs. True = same colour, same position (black), false = same colour, wrong position (white).
		int[] keyPegs = new int[in.length()];
		Arrays.fill(keyPegs, 0);
		
		// Loop to determine number of black and white key pegs to be awarded
		for (int i = 0; i < in.length(); i++){
			
			// If colours are equal and in the same position, award a black peg
			if (in.charAt(i) == hiddenPegs.charAt(i)){
				
				// If the current peg has already been awarded a white peg, look for the next same colour
				// If the same colour is found, award a white peg in that position
				if (keyPegs[i] == 2){
					for (int j = i + 1; j < in.length(); j++){
						if (in.charAt(i) == hiddenPegs.charAt(j)){
							keyPegs[j] = 2;
						}
					}
				}
				
				// Award a black peg
				keyPegs[i] = 1;
			}
			
			// If the colours are not equal in the same position, look for the same colour in a different position
			else{
				
				// Loop checks the whole array again
				for (int j = 0; j < hiddenPegs.length(); j++){
					
					// If the element is unused, and the colours are the same in different positions, award a white peg
					if (keyPegs[j] == 0 && hiddenPegs.charAt(j) == in.charAt(i)){
						keyPegs[j] = 2;
						// Leave loop to prevent awarding multiple white pegs
						break;
					}
				}
			}
		}
		
		// Creates a new StringBuilder with input string and colon to separate the input from the key pegs
		StringBuilder sb = new StringBuilder(in + ":");
		
		// For each true value in keyPegs, append B to StringBuilder
		for (int a : keyPegs){
			if (a == 1){
				sb.append("B");
			}
		}
		
		// For each false value in keyPegs, append W to StringBuilder
		for (int a : keyPegs){
			if (a == 2){
				sb.append("W");
			}
		}
		
		// Insert the code to the display
		Output.insertPattern(sb.toString(), 31-(2*attempts), 28);
		
		// Updates number of attempts
		Output.insertAttempts(attempts);
		
		// Split the string via the colon
		String[] split = sb.toString().split(":");
		try{
			// Returns a String if key pegs were awarded
			return split[1];
		}
		catch (ArrayIndexOutOfBoundsException e){
			// Returns null if split only has one element, the input code
			return null;
		}
	}
	
}