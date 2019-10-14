import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

/**
* Handles all the input Strings
*
* @author Oliver Broomhall
* @version 1.0
* @release 05/01/2016
* @see Output.java
*/
public class Input{
	
	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	private static String s;
	
	/**
	* @return Lowercase String input from keyboard.
	*/
	public static String getInput() throws IOException{
		s = br.readLine().toLowerCase();
		return s;
	}
	
	/**
	* @return Lowercase char from keyboard.
	*/
	public static char getCharacter() throws IOException{
		getInput();
		// Checks the String is only 1 character long
		if (charCheck(s)){
			return s.charAt(0);
		}
		Output.fix();
		return getCharacter();
	}
	
	/**
	* @return True for Strings of length 1, false otherwise.
	*/
	public static boolean charCheck(String s){
		if (s.length() == 1){
			return true;
		}
		return false;
	}
	
	/**
	* Method. Gets number input from keyboard between the bound arguments.
	*
	* @param int a
	*			Lower bound.
	* @param int b
	*			Upper bound.
	*
	* @return int Number between the bounds.
	*/
	public static int getNumber(int a, int b) throws IOException, NumberFormatException{
		getInput();
		if (numCheck(a, b, s)){
			return Integer.parseInt(s);
		}
		Output.fix();
		return getNumber(a, b);
	}
	
	/**
	* @return True if String can be parsed as an int and is between the bounds.
	*/
	public static boolean numCheck(int a, int b, String s){
		try{
			int i = Integer.parseInt(s);
			if (checkRange(i, a, b)){
				return true;
			}
		}
		catch (NumberFormatException e){}
		return false;
	}
	
	/**
	* @return True if int i is between the bounds.
	*/
	public static boolean checkRange(int i, int a, int b){
		if (i > a && i < b){
			return true;
		}
		return false;
	}
	
	public static char[] getCharArray(int pegs) throws IOException{
		getInput();
		char[] c = s.toCharArray();
		if (charArrayCheck(pegs, c)){
			return c;
		}
		Output.fix();
		return getCharArray(pegs);
	}
	
	public static boolean charArrayCheck(int pegs, char[] c){
		if (c.length == pegs){
			return true;
		}
		return false;
	}
	
	public static String getPegs(int colours, int pegs) throws IOException{
		char[] inputColours = getCharArray(pegs);
		if (pegsCheck(colours, pegs, inputColours)){
			return new String(inputColours);
		}
		Output.fix();
		return getPegs(colours, pegs);
	}
	
	public static boolean pegsCheck(int colours, int pegs, char[] inputColours){
		if (!charArrayCheck(pegs, inputColours)){
			return false;
		}
		
		char[] availableColours = new char[colours];
		for (int i = 0; i < colours; i++){
			availableColours[i] = Output.colourLetters[i];
		}
		
		for (char inputColour : inputColours){
			if (Arrays.binarySearch(availableColours, inputColour) < 0){
				return false;
			}
		}
		return true;
	}
	
	public static String getFileName(String[] fileNames) throws IOException{
		s = br.readLine();
		for (String name : fileNames){
			if (s.equals(name)){
				return s;
			}
		}
		return null;
	}
}