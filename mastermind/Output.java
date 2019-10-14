import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

/**
* Handles the output messages to be displayed in the terminal
*
* @author Oliver Broomhall
* @version 1.0
* @release 05/01/2016
* @see Terminal.java, README.txt
*/
public class Output{
	
	// String values for colours
	public static final String reset = "\u001B[0m";
	private static final String red = "\u001B[31m";
	private static final String green = "\u001B[32m";
	private static final String yellow = "\u001B[33m";
	private static final String blue = "\u001B[34m";
	private static final String purple = "\u001B[35m";
	private static final String cyan = "\u001B[36m";
	private static final String white = "\u001B[37m";
	
	// Arrays for incremental access to the colour strings and their corresponding names
	private static final String[] colours = {blue, cyan, green, purple, red, white, yellow};
	public static final char[] colourLetters = {'b', 'c', 'g', 'p', 'r', 'w', 'y'};
	public static final String[] colourNames = {"Blue", "Cyan", "Green", "Purple", "Red", "White", "Yellow"};
	
	/**
	* Method. Prints a star at the end of the current line.
	*/
	private static void starAtEndOfLine(){
		// Ensures cursor is at the beginning of the line
		Terminal.moveCursorRight(-65);
		// Moves cursor to the end of the line
		Terminal.moveCursorRight(64);
		// Prints the star in the default colour
		System.out.println(reset + "*");
	}
	
	/**
	* Method. Prints a full line of 65 multicoloured stars, corresponding to the 7 colours available.
	*/
	private static void lineOfStars(){
		for (int i = 0; i < 65; i++){
			System.out.print(colours[i%7] + "*");
		}
		System.out.println(reset);
	}
	
	/**
	* @param String s
	*			String of letters, each character must correspond to a char in 'colourLetters'.
	*
	* @return String sb.toString()
	* 			Upper case letters separated by spaces in their corresponding colours.
	*/
	public static String getColouredLetters(String s){
		
		// Converts string into char array
		char[] c = s.toLowerCase().toCharArray();
		StringBuilder sb = new StringBuilder();
		
		// Searches char[] colourLetters for input chars, adds the colour string and letter to a StringBuilder
		for (char colour : c){
			sb.append(colours[Arrays.binarySearch(colourLetters, colour)]);
			sb.append(Character.toUpperCase(colour) + " ");
		}
		
		return sb.toString();
	}
	
	/**
	* Same method as above, but returns whole words rather than single letters.
	*
	* @param String s
	* 			String of letters, each character must correspond to a char in 'colourLetters'.
	*
	* @return String sb.toString()
	* 			Words separated by spaces in their corresponding colours.
	*/
	public static String getColouredWords(String s){
		
		char[] c = s.toLowerCase().toCharArray();
		StringBuilder sb = new StringBuilder();
		
		// Finds the correct colour, appends that colour, the word, and a space to the StringBuilder
		for (char colour : c){
			int i = Arrays.binarySearch(colourLetters, colour);
			sb.append(colours[i]);
			sb.append(colourNames[i] + " ");
		}
		
		return sb.toString();
	}
	
	/**
	* Method. Updates the number of attempts the codebreaker has made.
	*
	* @param int attempts
	*			Number of attempts made.
	*/
	public static void insertAttempts(int attempts){
		
		// Moves the cursor to the attempt number
		Terminal.unsaveCursor();
		Terminal.moveCursorRight(-3);
		
		// Prints a yellow attempt number and the cyan colon
		System.out.println(yellow + (attempts+1) + cyan + ":");
		
		// See Output.fix()
		fix();
		
		// If there have been 9 attempts, then the codebreaker is on their 10th attempt
		// 10 adds a digit to the message the cursor position must be corrected accordingly
		if (attempts >= 9){
			Terminal.moveCursorRight(1);
			Terminal.saveCursor();
		}
	}
	
	/**
	* Method. Inserts the input code to the defined position relative to the saved position.
	*
	* @param String code
	* 			String consisting of a code, sometimes followed by a colon and key pegs (e.g. :BBW).
	* @param int up
	*			Amount of lines to move the cursor up before printing.
	* @param int right
	*			Amount of characters to move the cursor across before printing.
	*
	*/
	public static void insertPattern(String code, int up, int right){
		
		Terminal.moveCursor(up, right);
		
		// Splits the test case from the key pegs, then prints the coloured key pegs
		String[] codes = code.split(":");
		System.out.print(getColouredLetters(codes[0]));
		
		// If the key pegs exist, print them separated by blank spaces
		if (codes.length == 2){
			System.out.print(white + "   ");
			for (char keyPeg : codes[1].toCharArray()){
				System.out.print(keyPeg + " ");
			}
		}
		
	}
	
	/**
	* Method. Erases all text below the cursor, replaces the interface, and moves the cursor back to the original position.
	*/
	public static void fix(){
		Terminal.unsaveCursor();
		Terminal.eraseDown();
		starAtEndOfLine();
		print("");
		lineOfStars();
		Terminal.unsaveCursor();
		System.out.print(yellow);
	}
	
	/**
	* Method. Prints a string with a star at the front and end of the line.
	*
	* @param String s
	* 			String to be printed
	* @param (optional) String colour
	* 			Colour in which the string should be printed. Default otherwise.
	*/
	private static void print(String s){
		System.out.print(reset + "* " + s);
		starAtEndOfLine();
	}
	private static void print(String s, String colour){
		System.out.print(reset + "* " + colour + s);
		starAtEndOfLine();
	}
	
	/**
	* Method. Prints out the README.txt file in the correct format.
	*
	* @exception IOException
	* @exception FileNotFoundException
	*/	
	public static void readMe() throws IOException, FileNotFoundException{
		Terminal.moveCursorUp(-2);
		print("");
		
		// Trys to read the README.txt file, if the file does not exist print an error message
		try{
			// Creates a BufferedReader from the README.txt file
			BufferedReader br = new BufferedReader(new FileReader(new File("README.txt")));
			
			// Prints all the lines from the file to the screen
			String s;
			while ((s = br.readLine()) != null){
				print(s, cyan);
			}
			
			br.close();
		}
		catch (FileNotFoundException e){
			print("\tREADME.txt not found, please recheck your files!\t");
		}
		
		// Finsihes the message to work with the format of the display
		print("");
		lineOfStars();
		Terminal.moveCursorUp(2);
	}
	
	/**
	* Method. Prints a message below the interface.
	*
	* @param String message
	*			Message to be printed.
	* @param int colour
	*			Index of colours array.
	*/
	public static void infoMessage(String message, int colour){
		System.out.println("\n\n\n" + colours[colour] + message);
		Terminal.unsaveCursor();
	}
	
	/**
	* Method. Prints a message and moves the cursor for input.
	*
	* @param String[] messages
	* 			Array of messages needed to be printed.
	* @param int cursorDestination
	* 			Number of characters to move the cursor across after printing the message.
	*/
	private static void message(String[] messages, int cursorDestination){
		
		Terminal.eraseDown();
		lineOfStars();
		print("");
		
		// Prints each message in the messages array
		for (String message: messages){
			print(message, cyan);
		}
		
		print("");
		lineOfStars();
		
		// Moves the cursor and changes the colour to yellow
		Terminal.moveCursor(3, cursorDestination);
		Terminal.saveCursor();
		System.out.print(yellow);
	}
	
	/**
	* Method. Prints out the game display.
	*
	* @param int pegs
	*			Number of stars to display for the pegs
	*/
	public static void gameDisplay(int pegs){
		Terminal.moveCursorUp(-1);
		
		// Creates the String array of messages with the set number of Strings
		String[] messages = new String[25];
		
		// Prints 'Hidden Code' in red, with the correct number of dashes
		messages[0] = red + "\tHidden Code:\t    ";
		for (int i = 0; i < pegs; i++){
			messages[0] += "- ";
		}
		
		// Prints 'Attempt n' where n is incremented, and the correct number of stars
		for (int i = 2; i < 25; i+=2){
			messages[i] = reset + "\tAttempt " + (i/2) + ":\t    ";
			
			// Prints the stars for the test cases
			for (int j = 0; j < pegs; j++){
				messages[i] += "* ";
			}
			
			messages[i] += ("   ");
			
			// Prints the stars for the key pegs
			for (int j = 0; j < pegs; j++){
				messages[i] += "* ";
			}
			
			// Prints an empty line between the messages
			messages[i-1] = "";
		}
		message(messages, 0);
		Terminal.moveCursorUp(-3);
	}
	
	/**
	* Duplicates. The following methods are all very similar, they define a group of messages and then call the messages() method.
	*/
		
	public static void welcomeMessage(){
		String[] messages = new String[3];
		messages[0] = "Welcome to Mastermind, a code breaking game invented in 1970!";
		messages[1] = "";
		messages[2] = "   Would you like to display the README.txt file? (y/n)";
		message(messages, 58);
	}
	
	public static void loadGameMessage(){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[1];
		messages[0] = "   Would you like to load a previously saved game? (y/n)";
		message(messages, 59);
	}
	
	public static void requestColours(){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[1];
		messages[0] = "\t How many colours would you like to use? (4-7)";
		message(messages, 55);
	}
	
	public static void requestPegs(){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[1];
		messages[0] = "\t  How many pegs would you like to use? (4-7)";
		message(messages, 53);
	}
	
	public static void requestMode(){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[5];
		messages[0] = yellow + "\t1. " + cyan + "Human (Codemaker) vs. Human (Codebreaker)";
		messages[1] = yellow + "\t2. " + cyan + "Computer (Codemaker) vs. Human (Codebreaker)";
		messages[2] = yellow + "\t3. " + cyan + "Computer (Codemaker) vs. Computer (Codebreaker)";
		messages[3] = "";
		messages[4] = "    Please choose the mode you would like to play: (1-3)";
		message(messages, 59);
	}
	
	public static void codeSetMessage(int colours, int pegs){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[3];
		String availableColours = "bcgprwy".substring(0, colours);
		messages[0] = "Available colours: " + getColouredWords(availableColours);
		messages[1] = "";
		messages[2] = "Please enter " + yellow + pegs + cyan + " colours for the hidden code:";
		message(messages, 46);
	}
	
	public static void codeAttemptMessage(int colours, int pegs){
		String[] messages = new String[3];
		String availableColours = "bcgprwy".substring(0, colours);
		messages[0] = "Available colours: " + getColouredWords(availableColours);
		messages[1] = "";
		messages[2] = "Please enter " + yellow + pegs + cyan + " colours for Attempt " + yellow + "1" + cyan + ":";
		message(messages, 40);
	}
	
	public static void unableToLoadFileMessage(){
		String[] messages = new String[1];
		messages[0] = red + "\t     Unable to load game from command line!";
		message(messages, 50);
		System.out.println(reset + "\n\n");
	}
	
	public static void requestFileName(){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[3];
		messages[0] = "Please enter the file name you wish to save as: (max. 61)";
		messages[1] = "";
		messages[2] = "";
		message(messages, 2);
	}
	
	public static void overwriteMessage(){
		Terminal.moveCursorUp(-1);
		String[] messages = new String[3];
		messages[0] = "File already exists, would you like to overwrite? (y/n)";
		message(messages, 50);
	}
	
	public static void loadGame(String[] files){
		Terminal.moveCursorUp(-1);
		int n = files.length;
		String[] messages = new String[n+5];
		messages[0] = "Please choose the file you wish to load:";
		messages[1] = "";
		for (int i = 0; i < n; i++){
			messages[i+2] = purple + files[i];
		}
		messages[n+2] = "";
		messages[n+3] = "";
		messages[n+4] = "";
		message(messages, 2);		
	}
	
}