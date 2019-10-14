import java.io.IOException;

/**
* Manages all the classes needed for the Mastermind game to function.
*
* @author Oliver Broomhall
* @version 1.0
* @release 05/01/2016
* @see Setup.java, Terminal.java, Play.java, MMState.java, Computer.java, Input.java, Output.java
*/
public class Mastermind{
	
	private Setup s;
	private Play p;
	private MMState mms;
	private Computer codeBreaker;
	
	private int colours;
	private int pegs;
	private int mode;
	
	private String hiddenPegs;
	
	public Mastermind(){
		s = new Setup();
	}
	
	public static void main(String[] args) throws IOException, PegsException{
		
		Mastermind mm = new Mastermind();
		mm.getParameters(args);
		mm.doGame(args);
		
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
	
	public void getParameters(String[] args) throws IOException{
		
		int[] parameters = new int[3];
		
		if (s.giveParameters(args)){
			for (int i = 0; i < 3; i++){
				parameters[i] = Integer.parseInt(args[i+1]);
			}
		}
		else{
			Terminal.eraseScreen();
			Terminal.cursorHome();
			if (args.length > 0){
				Output.unableToLoadFileMessage();
				System.exit(1);
			}
			parameters = s.getParameters();
		}
		
		colours = parameters[0];
		pegs = parameters[1];
		mode = parameters[2];
		hiddenPegs = s.hiddenPegs;
		
		p = new Play(colours, pegs, mode, hiddenPegs);
		mms = new MMState(colours, pegs, mode, hiddenPegs);
		s.finishSetup(colours, pegs);

		codeBreaker = new Computer(p);
		if (args[3].equals("3")){
			codeBreaker.testCase();
		}
	}
	
	public boolean tryInputtingTestCases(String[] args) throws IOException, PegsException{
		try{
			int testCases = Integer.parseInt(args[5]);
			if (testCases != args.length-6){
				throw new PegsException("Error: Number of test cases does not match its defined value");
			}
			for (int i = 0; i < testCases; i++){
				if (!Input.pegsCheck(colours, pegs, args[i+6].toCharArray())){
					throw new PegsException("Error: Argument " + (i+6) + " \"" + args[i+6] + "\" is not a correct code");
				}
			}
			for (int i = 0; i < testCases; i++){
				mms.addAttempt(args[i+6]);
				System.out.println();
				p.input(args[i+6]);
			}
			return true;
		}
		catch (NumberFormatException e){
			Output.infoMessage("Error: Could not load game", 4);
			return false;
		}
		catch (PegsException e){
			Output.infoMessage(e.getMessage(), 4);
			return false;
		}
	}
	
	public void doGame(String[] args) throws IOException, PegsException{
		
		if (args.length > 6){
			tryInputtingTestCases(args);
		}
		
		while (!p.end()){
			
			String in = Input.getInput();
			if (Input.pegsCheck(colours, pegs, in.toCharArray())){
				mms.addAttempt(in);
				p.input(in);
				Output.fix();
			}
			
			else if (in.equals("save")){
				Output.requestFileName();
				
				if (MMFiles.save(mms)){
					Terminal.moveCursor(7, 40);
					Terminal.saveCursor();
					Output.fix();
					Output.infoMessage("Info: Saved file successfully", 6);
				}
				else{
					Terminal.moveCursor(7, 40);
					Terminal.saveCursor();
					Output.fix();
					Output.infoMessage("Error: File did not save, type quit to exit", 4);
				}
			}
			else if (in.equals("quit")){
				System.out.println("\n");
				return;
			}
			else if(in.equals("load")){
				//mms.fromFile();
				MMFiles.load();
			}
			
		}
		
		Output.insertPattern(hiddenPegs, 30, -12);
		Terminal.unsaveCursor();
		
		if (p.checkWin() == true){
			System.out.println("YOU WON!");
		}
		else{
			System.out.println("YOU LOST!");
		}
		System.out.println("\n");
	}
	
}
