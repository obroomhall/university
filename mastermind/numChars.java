public class numChars{
	
	private char c; // Char to be searched for
	private String s; // String to be searched in
	
	// Constructor, gives private variables values
	public numChars(char c, String s){
		this.c = c;
		this.s = s;
	}
	
	// Method, returns occurrences of a character in a String
	public int find(int index, int occurrences){
		
		// Sets index as the next index of the character
		index = s.indexOf(c, index);
		
		// If there is a new index of the character, increment occurrences and index and recurse the function
		if (index != -1){
			occurrences++;
			index++;
			return find(index, occurrences);
		}
		
		// Returns occurrences of the character
		return occurrences;
	}
}