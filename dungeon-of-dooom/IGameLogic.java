

import java.io.File;

public interface IGameLogic {
	
	
	
	public void setMap(File file);
	

	public String hello(int pno);
	

	public String move(char direction, int pno);
	

	public String pickup(int pno);
	

	public String look(int pno);
	
	public boolean gameRunning();
	
	
	public void quitGame();
	
}
