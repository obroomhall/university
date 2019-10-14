import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SRPN{
	public static void main(String[] args) throws Exception{
		
		// Creates a Buffered Reader instance using input from the keyboard
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		// Creates an instance of the Compute class for running non-static methods in that class
		Compute c = new Compute();
		
		// Tells the Buffered Reader to wait for a line of text input
		// Then calls the removeSpaces method in Compute
		String s;
		while ((s=br.readLine())!=null){
			c.splitWhiteSpace(s);
		}
		return;
	}
}

/*
find -name *.class -delete && javac -cp SRPN -d SRPN SRPN/SRPN.java
java -cp SRPN SRPN
cat t-single/01 | ./srpn.lcpu
cat t-single/01 | java -cp SRPN SRPN
*/