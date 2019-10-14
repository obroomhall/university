public class Computer{
	
	private char[] testColourArray;
		
	private char[] confirmedColours;
	private String availableColours;
	private String testCase;
	private int testColour;
	private String testColourString;
	private int colours;
	private int pegs;
	private Play game;
	private int blackPegs;
	private int whitePegs;
	
	public Computer(Play game){
		colours = game.getColours();
		pegs = game.getPegs();
		confirmedColours = new char[pegs];
		availableColours = "bcgprwy".substring(0, colours);
		this.game = game;
	}
	
	/* PSEUDO CODE FOR COMPUTER ALGORITHM
	
	Test case with all pegs the same colour (i.e. BBBBBBB)
	- Increment colours until a test case is found with no key pegs
	- This colour is called the 'control colour'
	
	Test case with half pegs as the control colour, half as a new colour (i.e. BBBCCCC)
	- Black key pegs indicate the colour is located in the second half of test case
	- White key pegs indicate colour location to be in the first half
	- BBBCCCC [BWW], shows two C in first half, one C in second half
	
	If white, find the location by logic (i.e. CBBBBBB, BCBBBBB, BBCBBBB)
	If black, do the same in the second half (i.e. BBBCBBB, BBBBCBB, BBBBBCB, BBBBBBC)
	- Change position of colour in control string until black key peg is awarded
	- When a black key peg is awarded, add colour and position to confirmedColours	
	
	*/
	
	public void testCase() throws StringIndexOutOfBoundsException{
		try{
		
		String controlColour = getControlColour(); // if null, every colour is included
		String testCaseWithControl = getTestCaseWithControl(controlColour);
		
		testColour = -1;
		getKeyPegsString(testCaseWithControl);
		
		if (whitePegs == 0){
			
		}
		else{
			
			testCase = "";
			testColourString = availableColours.substring(testColour, testColour+1);
			
			for (int i = 0; i < whitePegs; i++){
				testCase += testColourString;
			}
			while (testCase.length() < pegs){
				testCase += controlColour;
			}
			System.out.println();
			
			setBlackWhitePegs(game.input(testCase));
			
			if (whitePegs == 0){
				// send to confirmedColours
			}
			else{
				
			}
			
		}
		
		System.out.println(blackPegs + " " + whitePegs);
		System.out.println(availableColours);
		
		System.exit(1);
		
		}
		catch (StringIndexOutOfBoundsException e){
			System.out.println(testCase);
			System.exit(1);
		}
	}
	
	private String getControlColour(){
		for (char colour : availableColours.toCharArray()){
			
			testCase = "";
			
			String colourString = Character.toString(colour);
			for (int i = 0; i < pegs; i++){
				testCase += colourString;
			}
			
			System.out.println();
			
			if (game.input(testCase) == null){
				availableColours = availableColours.replace(colourString, "");
				return colourString;
			}
			testColour++;
		}
		return null;
	}
	
	private String getTestCaseWithControl(String controlColour){
		String testCaseWithControl = "";
		for (int i = 0; i < pegs/2; i++){
			testCaseWithControl += controlColour;
		}
		return testCaseWithControl;
	}
	
	private String getKeyPegsString(String testCaseWithControl){
		while (testColour < pegs){
			testCase = testCaseWithControl;
			testColour++;
			testColourString = availableColours.substring(testColour, testColour+1);
			while (testCase.length() < pegs){
				testCase += testColourString;
			}
			System.out.println();
			String keyPegs;
			
			if ((keyPegs = game.input(testCase)) == null){
				availableColours = availableColours.replace(testColourString, "");
			}
			else{
				return keyPegs;
			}			
		}
		return null;
	}
	
	private void setBlackWhitePegs(String keyPegs){
		if (keyPegs.indexOf("W") > -1){
			String blackPegsString = keyPegs.substring(0, keyPegs.indexOf("W"));
			blackPegs = blackPegsString.length();
			whitePegs = keyPegs.length() - blackPegs;
		}
		else{
			blackPegs = keyPegs.length();
			whitePegs = 0;
		}
	}
	
}