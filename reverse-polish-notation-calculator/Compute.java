import java.util.Stack;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
* Takes input from the SRPN class and performs operations on
* the string similar to a reverse polish notation calculator
*
* Did not have time to finish the comments
*
* @author Oliver Broomhall
* @version 1.0
* @release 01/12/2015
* @See SRPN.java
*/

public class Compute{
	
	// Creates the stack used for storing integers
	// See correctSaturation() method
	private Stack<Integer> stack = new Stack();
	
	// Creates an ArrayList used when displaying the integers in stack
	// See printStack() method
	private ArrayList<Integer> reverseList = new ArrayList();
	
	// Creates an ArrayList for the list of randomly generated integers
	// See populateRandomList() and pushRandom()
	private ArrayList<Integer> r = new ArrayList();
	// An incrementor for the random list
	private int randomInc=0;
	
	// Boolean varible used to define whether proceding characters are to be commented
	// See splitWhiteSpace() and splitChars()
	private boolean comment=false;
	
	// Contructor
	// Fills the ArrayList r with the random integers stored in a file "random.txt"
	public Compute() throws FileNotFoundException, IOException{ // Throws exceptions needed in populateRandomList() function
		populateRandomList();
	}
	
	// Splits String s by all possible white spaces
	// Strings separated by spaces are considered different operations or operands
	// Sends the new strings individually to splitChars()
	public void splitWhiteSpace(String s){
		
		// String s cannot be a comment before it is checked for "#"
		comment=false;
		
		// If s is equal to "-" then send it immediately to the push()
		// This helps with identifying negative numbers
		// Then return, the String cannot have any more values
		if (s.equals("-")){
			push("-");
			return;
		}
		
		// Tests for all types of white spaces to avoid exceptions
		if (s.contains(" ") ^ s.contains("\t") ^ s.contains("\r") ^ s.contains("\n") ^ s.contains("\f")){
			
			// Splits s into an array with the delimiter of \\s+ (white space)
			String[] noSpaceArray = s.split("\\s+");
			
			// Repeats for length of noSpaceArray
			for (int i = 0; i<noSpaceArray.length; i++){
				
				// If the comment boolean has already been triggered on this line, exit
				if (comment==true){
					comment = false;
					return;
				}
				
				// Otherwise, send String held at an index in noSpaceArray onwards
				splitChars(noSpaceArray[i]);
				
			}
		}
		else{
			
			// There is no need to split a String with no white spaces
			// Send the whole String to splitChars()
			splitChars(s);
			
		}
	}
	
	// Splits String s into an array of chars, unless the characters are numerical
	// Numbers are compiled in a StringBuilder instance
	// Then everything is outputted to the push() method
	public void splitChars(String s){
		
		// Splits s by every character
		String[] charArray = s.split("");
		
		// Creates a new instance of StringBuilder
		StringBuilder sb = new StringBuilder();
		
		// A dash cannot be considered as a negative sign unless it precedes a number directly
		boolean negative = false;
		
		int i = 1;
		// Repeats for the length of the charArray
		for (; i<charArray.length; i++){
			
			// If a dash was inputted in the last loop run
			//  but on this run a non-numeric character was inputted
			//  then send the dash to push()
			if (negative==true && isNumeric(charArray[i])==false){
				push("-");
			}
			
			// If the character is a # then set the comment boolean to true and exit
			if (charArray[i].equals("#")){
				comment = true;
				return;
			}
			
			// If in this run a dash was inputted, then set negative to true
			//  we have to wait until the next run to see whether the dash will
			//  form an operation or part of an operand
			if (charArray[i].equals("-")){
				negative = true;
			}
			
			// If character is numeric then append the character to sb
			if (isNumeric(charArray[i])){
				if (negative==true){
					sb.append("-");
					negative=false;
				}
				sb.append(charArray[i]);
			}
			
			// The character is not negative, thus we should push() it
			else{
				
				// If the sb is empty and the character is not a dash, then push() character
				if (sb.length()==0){
					if (negative==false){
						push(charArray[i]);
					}
				}
				
				// Otherwise, push() the number
				else{
					push(sb.toString());
					sb.delete(0, sb.length());
					push(charArray[i]);
				}
			}	
		}
		if (isNumeric(charArray[i-1])){
			push(sb.toString());
			sb.delete(0, sb.length());
		}
	}
	
	// The main method for this class
	// Distinguishes between different characters in the input string
	// Pushes integers to the stack
	// Performs the specified operations (+,-,*,/,%,^,d,r,=)
	public void push(String s){
		
		// Pushes intgers to the stack
		if (isNumeric(s)){ // Checks whether the string is an integer
		
			// The stack cannot store greater than 23 integers
			if (stack.size()>=23){System.err.println("Stack overflow.");}
			
			// Numbers that begin with 0 or -0 are to be treated as Octal values
			else if (s.startsWith("0") ^ s.startsWith("-0")){
				octal(s);
			}
			
			// SRPN deals only with integer numbers, anything larger is to be saturated
			// Integer numbers can have a maximum of 11 digits in Java
			else{
				if (s.length()>11){ // If s is longer than the max value of integer
					s=s.substring(0,12); // Shorten the number to allow it to be parsed as long
				}
				long parsedLong = Long.parseLong(s);
				correctSaturation(parsedLong); // Checks whether long number is higher than the max int value, or lower than min int value
			}
		}
		
		// Displays integers from the stack
		else if (s.equals("d")){printStack();}
		
		// Pushes random integers from ArrayList r
		else if (s.equals("r")){
			if (stack.size()>=23){System.err.println("Stack overflow.");}
			else{pushRandom();}
		}
		// Displays topmost integer in stack
		else if (s.equals("=")){System.out.println(stack.peek());}
		
		// Prints an error if the stack size is less than 2
		// At least two operands are needed to perform an operation
		else if (stack.size()<2){
			if (s.equals("+") ^ s.equals("-") ^ s.equals("*") ^ s.equals("/") ^ s.equals("%") ^ s.equals("^")){
				System.err.println("Stack underflow.");
			}
			else{
				System.err.println("Unrecognised operator or operand \"" + s + "\".");
			}
		}
		
		// Only characters left will be operational characters
		else{performOperation(s);}
	}
		
	public boolean isNumeric(String s){
		if (s.startsWith("-")){s=s.substring(1, s.length());}
		try{
			for (; s.length()>18; s=s.substring(0, s.length()-18)){
				Long.parseLong(s.substring(s.length()-18));
			}
			Long.parseLong(s);
		}  
		catch(NumberFormatException nfe){
			return false;  
		}  
		return true;  
	}
	
	public void octal(String s){
		long decimal=0;
		int j=0;
		
		if (s.length()>13){
			s=s.substring(0, 14);
		}
		
		for (int i = s.length()-1; i>0; i--){
			decimal+=(long)Math.pow(8, j)*(s.charAt(i)-48);
			j++;
		}
		
		if (s.startsWith("-")){decimal*=-1;}
		
		correctSaturation(decimal);
	}
	
	public void correctSaturation(long sat){
		if (sat>=2147483647){
			stack.push(2147483647);
		}
		else if (sat<=-2147483648){
			stack.push(-2147483648);
		}
		else{
			stack.push((int)sat);
		}
	}
	
	public void printStack(){
		while (!stack.isEmpty()){
			reverseList.add(stack.pop());
		}
		for (int i=reverseList.size()-1; i>=0; i--){
			stack.push(reverseList.get(i));
			System.out.println(reverseList.get(i));
		}
		reverseList.clear();
	}
	
	// Cycles through 100 random integer values from input file
	// Pushes the current random integer to the stack
	public void pushRandom(){
		
		// If the incrementor has reached 100, reset to 0
		if (randomInc==100){randomInc=0;}
		
		// Push the integer and increment
		stack.push(r.get(randomInc));
		randomInc++;
	}
	
	// 
	public void performOperation(String op){
		
		int op1=stack.pop();
		int op2=stack.pop();
		
		long ln1=(long)op1;
		long ln2=(long)op2;
		
		String s1=Integer.toString(op1);
		String s2=Integer.toString(op2);
		
		long sat;
		
		if (op.equals("+")){correctSaturation(ln2+ln1);}
		
		else if (op.equals("-")){correctSaturation(ln2-ln1);}
		
		else if (op.equals("/")){
			if (op1==0){
				System.err.println("Divide by 0.");
				stack.push(op2);
				stack.push(op1);
			}
			else{correctSaturation(op2/op1);}
		}
		
		else if (op.equals("*")){correctSaturation(op2*op1);}
		
		else if (op.equals("%")){
			stack.push(op2%op1);
		}
		else if (op.equals("^")){
			if (op1<0){
				System.err.println("Negative power.");
				stack.push(op2);
				stack.push(op1);
			}
			else if (op1==0){stack.push(1);}
			else if (op1==1){stack.push(op2);}
			else{
				stack.push((int)Math.pow(op2, op1));
			}
		}
		else{
			System.err.println("Unrecognised operator or operand \"" + op + "\".");
		}
		
	}
	
	public void populateRandomList() throws FileNotFoundException, IOException{
		
		File f = new File("random.txt");
		if (!f.exists()){
			f = new File("SRPN/random.txt");
		}

		FileReader in = new FileReader(f);
		BufferedReader br = new BufferedReader(in);
		String currentLine;
		
		while ((currentLine=br.readLine())!=null){
			r.add(Integer.parseInt(currentLine));
		}
		
		in.close();
	}
	
}