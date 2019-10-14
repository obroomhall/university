import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

public class MMState{
	
	private int colours;
	private int pegs;
	private int mode;
	private String hiddenPegs;
	private ArrayList<String> attempts;
	private int i = 0;
	private String fileName;
	
	public MMState(int colours, int pegs, int mode, String hiddenPegs){
		this.colours = colours;
		this.pegs = pegs;
		this.mode = mode;
		this.hiddenPegs = hiddenPegs;
		attempts = new ArrayList<String>();
	}
	
	public void addAttempt(String attempt){
		attempts.add(attempt);
		i++;
	}
	
	public boolean setFileName(String fileName){
		if (fileName.length() > 1 && fileName.length() < 62){
			this.fileName = fileName + ".mm";
			return true;
		}
		return false;
	}
	
	public void toFile() throws IOException{
		
		Writer w = new FileWriter(fileName);
		
		w.write("n\n");
		w.write(colours + "\n");
		w.write(pegs + "\n");
		w.write("1\n");
		w.write(hiddenPegs + "\n");
		w.write(i + "\n");
		
		for (String attempt : attempts){
			w.write(attempt + "\n");
		}
		
		w.close();

	}
	
	public void fromFile(String file) throws FileNotFoundException, IOException, PegsException{

		BufferedReader br = new BufferedReader(new FileReader(new File(file)));

		ArrayList<String> args = new ArrayList<String>();
		String line;
		int i = 0;
		
		while ((line = br.readLine()) != null){
			args.add(line);
			i++;
		}

		String[] argsArray = args.toArray(new String[args.size()]);
		
		Mastermind mm = new Mastermind();
		mm.doGame(argsArray);
		
	}
	
}