/**
* Creates and shows a GUI for a Client or Bot. Clients and Bots have different buttons available.
*
* @author Oliver Broomhall
* @version 1.0
* @release 04/04/2016
* @see Player.java, JFrame.java, ActionListener.java
*/

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.IOException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

public class GameGUI extends JFrame implements ActionListener{
	
	private Player game;
	private int lines;
	private JLabel[][] map;
	
	private JProgressBar progress;
	private boolean icons;
	private JButton buttonIcons;
	
	private JButton buttonNorth;
	private JButton buttonEast;
	private JButton buttonSouth;
	private JButton buttonWest;
	
	private JButton buttonPickup;
	private JButton buttonQuit;
	
	private JLabel goldLeft;
	private int gold;
	private boolean progressSet;
	
	private JFrame myFrame;
	
	public void giveResponse(String response){
		
		// Gets the number equivalent of the response for the switch statement
		IdentifyResponse ir = new IdentifyResponse();
		int n = ir.getInt(response);
		
		// Acts on the response
		switch (n){
			// Response: SUCCESS, GOLD COINS: []
			// Reduces the number of gold coins needed
			// Updates the progress bar
			case 4:
				gold--;
				goldLeft.setText("Gold left: " + gold);
				progress.setValue(progress.getValue() + 1);
				break;
			case 5:
			// Response: GOLD []
			// Updates the gold left and progress bar
				gold = Integer.parseInt(response.substring(6));
				goldLeft.setText("Gold left: " + gold);
				if (!progressSet){
					progress.setMinimum(0);
					progress.setMaximum(gold);
					progress.setValue(0);
					progressSet = true;
				}
				
				break;
			case 7:
			// Game has been won
				System.exit(0);
				break;
			case 8:
			// Attempted to use SwingWorker for the computations in updateMap
				/*SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>(){
					@Override
					protected void doInBackground(){
						updateMap(GameGUI.this.response);
					}
				};
				worker.execute();*/
				updateMap(response);
		}
	}
	
	// Updates the map based on whether the user has chosen icons or text
	public void updateMap(String response){
		if (lines == 0){
			lines = 5;
		}
		for (int i = 0; i < 5; i++){
			if (!icons){
				if (response.charAt(i) == '.'){
					map[5-lines][i].setIcon(new ImageIcon());
				}
				else if (response.charAt(i) == 'P'){
					addImage(lines, i, "player");
				}
				else if (response.charAt(i) == 'E'){
					addImage(lines, i, "door");
				}
				else if (response.charAt(i) == 'G'){
					addImage(lines, i, "gold");
				}
				else if (response.charAt(i) == '#'){
					addImage(lines, i, "wall");
				}
				else if (response.charAt(i) == 'X'){
					addImage(lines, i, "x");
				}
			}
			else{
				map[5-lines][i].setIcon(new ImageIcon());
				if (response.charAt(i) == '.'){
					map[5-lines][i].setText("");
				}
				else{
					map[5-lines][i].setText(Character.toString(response.charAt(i)));
				}
			}
			
		}
		lines--;
	}
	
	// Gets the correct image, resizes it, then sets a JLabel
	public void addImage(int i, int j, String name){
		map[5-i][j].setText("");
		ImageIcon image = new ImageIcon("images/" + name + ".png");
		Image scaled = image.getImage().getScaledInstance(50, 50, Image.SCALE_DEFAULT);
		map[5-i][j].setIcon(new ImageIcon(scaled));
	}
	
	// Creates and shows the GUI
	public GameGUI(Player game, boolean enableButtons) throws IOException{
		
		this.game = game;
		
		// Creates the frame
		myFrame = new JFrame("Dungeon of Dooom!!!");
		myFrame.setMinimumSize(new Dimension(400, 500));
		myFrame.setBackground(Color.lightGray);
		
		// Creates the panels
		JPanel panelTop = new JPanel();
		JPanel panelCenter = new JPanel();
		JPanel panelBottom = new JPanel();
		panelBottom.setPreferredSize(new Dimension(100, 100));
		
		// Sets the layout
		myFrame.getContentPane().add(panelTop, BorderLayout.PAGE_START);
		myFrame.getContentPane().add(panelCenter, BorderLayout.CENTER);
		myFrame.getContentPane().add(panelBottom, BorderLayout.PAGE_END);
		
		panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.PAGE_AXIS));

		panelCenter.setPreferredSize(new Dimension(100, 200));
		panelCenter.setLayout(new GridLayout(5, 5, 10, 10));
		panelCenter.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Creates the graphics pane
		map = new JLabel[5][5];
		
		for (int i = 0; i < 5; i++){
			for (int j = 0; j < 5; j++){
				JLabel label = new JLabel("", SwingConstants.CENTER);
				label.setFont(label.getFont().deriveFont(64.0f));
				label.setBorder(BorderFactory.createLoweredBevelBorder());
				panelCenter.add(label);
				map[i][j] = label;
			}
		}

		// Creates more panels and layouts
		panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.PAGE_AXIS));
		JPanel panelBottom1 = new JPanel();
		JPanel panelBottom2 = new JPanel();
		SpringLayout layoutEWButtons = new SpringLayout();
		panelBottom2.setLayout(layoutEWButtons);
		JPanel panelBottom3 = new JPanel();

		panelBottom.add(panelBottom1);
		panelBottom.add(panelBottom2);
		panelBottom.add(panelBottom3);
		
		// Creates the buttons and disables if the GUI is for a bot
		buttonNorth = new JButton("N");
		buttonEast = new JButton("E");
		buttonSouth = new JButton("S");
		buttonWest = new JButton("W");
		buttonNorth.setEnabled(enableButtons);
		buttonEast.setEnabled(enableButtons);
		buttonSouth.setEnabled(enableButtons);
		buttonWest.setEnabled(enableButtons);
		
		// Anchors the buttons in the correct place
		layoutEWButtons.putConstraint(SpringLayout.WEST, buttonEast, (int)buttonEast.getPreferredSize().getWidth()/2, SpringLayout.HORIZONTAL_CENTER, panelBottom2);
		layoutEWButtons.putConstraint(SpringLayout.EAST, buttonWest, (int)-buttonWest.getPreferredSize().getWidth()/2, SpringLayout.HORIZONTAL_CENTER, panelBottom2);
		
		panelBottom1.add(buttonNorth);
		panelBottom2.add(buttonWest);
		panelBottom3.add(buttonSouth);
		panelBottom2.add(buttonEast);
		
		// Allows the buttons to be clicked
		buttonNorth.addActionListener(this);
		buttonEast.addActionListener(this);
		buttonSouth.addActionListener(this);
		buttonWest.addActionListener(this);
		
		goldLeft = new JLabel("Gold left: ");
		panelTop.add(goldLeft);
		
		// Creates the other 2 buttons
		buttonPickup = new JButton("PICKUP");
		buttonPickup.setEnabled(enableButtons);
		buttonQuit = new JButton("QUIT");
		buttonQuit.setPreferredSize(buttonPickup.getPreferredSize());
		panelBottom2.add(buttonPickup);
		panelBottom2.add(buttonQuit);
		layoutEWButtons.putConstraint(SpringLayout.EAST, buttonQuit, -30, SpringLayout.EAST, panelBottom2);
		layoutEWButtons.putConstraint(SpringLayout.WEST, buttonPickup, 30, SpringLayout.WEST, panelBottom2);
		buttonPickup.addActionListener(this);
		buttonQuit.addActionListener(this);
		
		// Creates the progress bar
		progress = new JProgressBar();
		panelTop.add(progress);
		buttonIcons = new JButton("O");
		buttonIcons.setPreferredSize(new Dimension(20, 20));
		panelBottom2.add(buttonIcons);
		layoutEWButtons.putConstraint(SpringLayout.EAST, buttonIcons, (int)buttonIcons.getPreferredSize().getWidth()/2, SpringLayout.HORIZONTAL_CENTER, panelBottom2);
		buttonIcons.addActionListener(this);
		
		// Sends the first LOOK and HELLO to populate the empty panels
		game.toServer.println("HELLO");
		game.toServer.println("LOOK");
		
		myFrame.pack();
		myFrame.setVisible(true);
	}
	
	// Used with the 'gui' command in terminal
	public void displayGUI(){
		myFrame.setVisible(true);
	}
	
	/**
	* Method. Identifies the button pressed and sends the corresponding
	* 	message to the server.
	*
	* @param ActionEvent evt
	*/
	public void actionPerformed(ActionEvent evt){
		Object src = evt.getSource();
		String cmd = "";
		
		if (src == buttonNorth){
			cmd = "MOVE N";
		}
		else if (src == buttonEast){
			cmd = "MOVE E";
		}
		else if (src == buttonSouth){
			cmd = "MOVE S";
		}
		else if (src == buttonWest){
			cmd = "MOVE W";
		}
		else if (src == buttonPickup){
			cmd = "PICKUP";
		}
		else if (src == buttonQuit){
			game.toServer.println("QUIT");
			System.exit(0);
		}
		else if (src == buttonIcons){
			if (icons){
				icons = false;
			}
			else{
				icons = true;
			}
		}
		
		game.toServer.println(cmd);
		game.toServer.println("LOOK");
	}
	
}