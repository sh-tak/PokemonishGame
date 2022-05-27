package client;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.tree.*;
import server.*;


public class clientGUI extends JFrame {
	clientGUI() throws IOException{
		this.setTitle("Pokemonish Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit out of the game
		this.setSize(800, 600);
		// this.setVisible(true);
		getContentPane().setLayout(new FlowLayout());
		JTextField inputArea; //input field 
		JTextArea freeArea; // output field 
		// Client client = null; // Client object
		// String host = "localhost";
		JTextField hostField, portField;
		JButton connectBut ,closeBut, quitBut;
		int port = 28000;
		Thread thread = null;
		JScrollPane scrollpane = null;
		JList wazaList;

		// ImageIcon icon = new ImageIcon("/Users/pokemon.png");
		// this.setIconImage(icon.getImage());
		// this.getContentPane().add(new JLabel(icon)); // change color of background
		//
		freeArea = new JTextArea("hello world!", 4, 20);
		getContentPane().add(freeArea);

		ServerThread st = new ServerThread(null, null, Server.generateMonster(), true);

		String[] listData = {"waza1", "waza2", "waza3", "waza4"};
		wazaList = new JList();
		wazaList.setListData(st.monster.moveList);
		getContentPane().add(wazaList);
		setVisible(true);
  
		int selectednumber = wazaList.getSelectedIndex();

		freeArea.setText(Integer.toString(selectednumber));
		setVisible(true);
	}

	public static void main(String[] args) {
		try {
			clientGUI frame = new clientGUI();
			
		} catch (Exception e) {
			//TODO: handle exception
		}
	}
}