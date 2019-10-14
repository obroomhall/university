import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Map {
	
	private char[][] map;
	private String mapName;
	private int totalGoldOnMap;
	
	public Map(){
		map = null;
		mapName = "";
		totalGoldOnMap = -1;
	}
	
	public Map(File mapFile){
		this();
		readMap(mapFile);
	}
	
	/**
	 * Reads a map from a given file with the format:
	 * name <mapName>
	 * win <totalGold>
	 * 
	 * @par am map A File pointed to a correctly formatted map file
	 */
	public void readMap(File mapFile) {
		// a buffered reader for the map
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(mapFile));
		} catch (FileNotFoundException e) {
			try {
				reader = new BufferedReader(new FileReader(new File("maps","example_map.txt")));
			} catch (FileNotFoundException e1) {
				System.err.println("no valid map name given and default file example_map.txt not found");
				System.exit(-1);
			}
		}
		
		try {
			map = loadMap(reader);
		} catch (IOException e){
			System.err.println("map file invalid or wrongly formatted");
			System.exit(-1);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}
	private char[][] loadMap(BufferedReader reader) throws IOException
	{
		
		boolean error = false;
		ArrayList<char[]> tempMap = new ArrayList<char[]>();
		int width = -1;
		
		String in = reader.readLine();
		if (in.startsWith("name")){
			error = setName(in);
		}
		
		in = reader.readLine();
		if (in.startsWith("win")){
				error = setWin(in);
		}
		
		in = reader.readLine();
		if (in.charAt(0) == '#' && in.length() > 1)
			width = in.trim().length();
		
		while (in != null && !error)
		{

			char[] row = new char[in.length()];
			if  (in.length() != width)
				error = true;
			
			for (int i = 0; i < in.length(); i++)
			{
				row[i] = in.charAt(i);
			}

			tempMap.add(row);

			in = reader.readLine();
		}
		
		if (error) {
			setName("");
			setWin("");
			return null;
		}
		char[][] map = new char[tempMap.size()][width];
		
		for (int i=0;i<tempMap.size();i++){
			map[i] = tempMap.get(i);
		}
		return map;
	}
	private boolean setWin(String in) {
		if (!in.startsWith("win "))
			return true;
		int win = 0;
		try { win = Integer.parseInt(in.split(" ")[1].trim());
		} catch (NumberFormatException n){
			System.err.println("the map does not contain a valid win criteria!");
		}
		if (win < 0) 
			return true;
		this.totalGoldOnMap = win;
		
		return false;
	}
	private boolean setName(String in) {
		if (!in.startsWith("name ") && in.length() < 4)
			return true;
		String name = in.substring(4).trim();
		
		if (name.length() < 1) 
			return true;
		
		this.mapName = name;
		
		return false;
	}
	
	/**
	 * The method replaces a char at a given position of the map with a new char
	 * @param y the vertical position of the tile to replace
	 * @param x the horizontal position of the tile to replace
	 * @param tile the char character of the tile to replace
	 * @return The old character which was replaced will be returned.
	 */
	protected char replaceTile(int y, int x, char tile) {
		char output = map[y][x];
		map[y][x] = tile;
		return output;
	}
	
	protected void printMap(){
	
	for (int y = 0; y < getMapHeight(); y++) {
		for (int x = 0; x < getMapWidth(); x++) {
			System.out.print(map[y][x]);
		}
		System.out.println();
	}
	}
	
	/**
	 * The method returns the Tile at a given location. The tile is not removed.
	 * @param y the vertical position of the tile to replace
	 * @param x the horizontal position of the tile to replace
	 * @param tile the char character of the tile to replace
	 * @return The old character which was replaced will be returned.
	 */
	protected char lookAtTile(int y, int x) {
		if (y < 0 || x < 0 || y >= map.length || x >= map[0].length)
			return '#';
		char output = map[y][x];
		
		return output;
	}
	
	/**
	 * This method is used to retrieve a map view around a certain location.
	 * The method should be used to get the look() around the player location.
	 * @param y Y coordinate of the location
	 * @param x X coordinate of the location
	 * @param radius The radius defining the area which will be returned. 
	 * Without the usage of a lamp the standard value is 5 units.
	 * @return
	 */
	protected char[][] lookWindow(int y, int x, int radius) {
		char[][] reply = new char[radius][radius];
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				int posX = x + j - radius/2;
				int posY = y + i - radius/2;
				if (posX >= 0 && posX < getMapWidth() && 
						posY >= 0 && posY < getMapHeight())
					reply[j][i] = map[posY][posX];
				else
					reply[j][i] = '#';
			}
		}
		reply[0][0] = 'X';
		reply[radius-1][0] = 'X';
		reply[0][radius-1] = 'X';
		reply[radius-1][radius-1] = 'X';
		
		return reply;
	}

	public int getWin() {
		return totalGoldOnMap;
	}

	public String getMapName() {
		return mapName;
	}

	protected int getMapWidth() {
		return map[0].length;
	}

	protected int getMapHeight() {
		return map.length;
	}

}
