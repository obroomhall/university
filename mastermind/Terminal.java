/**
* Handles all of the cursor movements in the terminal.
*
* @author Oliver Broomhall
* @version 1.0
* @release 05/01/2016
* @see Output.java, http://www.termsys.demon.co.uk/vtansi.htm
*/
public class Terminal extends Output{
	
	private static char escCode = 0x1B;
	
	/**
	* Method. Moves the cursor to any relative position.
	*
	* @param int up
	* 			Lines to move the cursor up, negative for down.
	* @param int down
	*			Characters to move the cursor right, negative for left.
	*/
	public static void moveCursor(int up, int right){
		moveCursorUp(up);
		moveCursorRight(right);
	}
	
	/**
	* Method. Moves the cursor up or down.
	*
	* @param int up
	*			Lines to move the cursor up, negative for down.
	*/
	public static void moveCursorUp(int up){
		// If positive, move up
		if (up < 0){
			System.out.print(String.format(escCode + "[" + up*-1 + "B"));
		}
		// If negative, move down
		else if (up != 0){
			System.out.print(String.format(escCode + "[" + up + "A"));
		}
		// If zero, do nothing
	}
	
	/**
	* Method. Moves the cursor right or left.
	*
	* @param int right
	*			Characters to move the cursor right, negative for left.
	*/
	public static void moveCursorRight(int right){
		// If positive, move right
		if (right < 0){
			System.out.print(String.format(escCode + "[" + right*-1 + "D"));
		}
		// If negative, move left
		else if (right != 0){
			System.out.print(String.format(escCode + "[" + right + "C"));
		}
		// If zero, do nothing
	}
	
	/**
	* Saves the current cursor position.
	*/
	public static void saveCursor(){
		System.out.print(String.format(escCode + "[s"));
	}
	
	/**
	* Repositions the cursor to the save point.
	*/
	public static void unsaveCursor(){
		System.out.print(String.format(escCode + "[u"));
	}
	
	/**
	* Erases all of the text from the current cursor position to the end of the line.
	*/
	public static void eraseEndOfLine(){
		System.out.print(String.format(escCode + "[K"));
	}
	
	/**
	* Erases the whole line.
	*/
	public static void eraseLine(){
		System.out.print(String.format(escCode + "[2K"));
	}
	
	/**
	* Erases every line below the current line.
	*/
	public static void eraseDown(){
		System.out.print(String.format(escCode + "[J"));
	}
	
	/**
	* Clears the whole terminal screen.
	*/
	public static void eraseScreen(){
		System.out.print(String.format(escCode + "[2J"));
	}
	
	/**
	* Repositions the cursor to the first line of the terminal.
	*/
	public static void cursorHome(){
		System.out.print(String.format(escCode + "[H"));
	}
	
}