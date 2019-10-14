import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class MMFiles{
	
	public static boolean save(MMState mms) throws IOException{
		
		Output.fix();
		
		// Get user input
		String file = Input.getInput();
		
		// Stop trying to save a file
		if (file.equals("quit")){
			return true;
		}
		
		// If file name length is lower than 62
		if (mms.setFileName(file)){
			
			// Save the file
			mms.toFile();
			return true;

		}
		// File name too long or short, ask again
		else{
			save(mms);
		}
		return false;
	}
	
	public static void load(){
		
		/*File dir = new File(".");
		
		ArrayList<File> files = new ArrayList<File>();
		ArrayList<String> fileNames = new ArrayList<String>();
		
		for (File file : dir.listFiles()){
			String f = file.getName();
			if (f.contains(".mm")){
				files.add(file);
				fileNames.add(f.substring(0, f.lastIndexOf(".")));
			}
		}
		
		String[] names = fileNames.toArray(new String[fileNames.size()]);
		
		Output.loadGame(names);
		
		MMState mms = new MMState(4, 4, 1, "bbbb");
		mms.fromFile(Input.getFileName(names));*/
		
	}
}