package client;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class clientGUI extends JFrame {

	clientGUI() {
		this.setTitle("Pokemonish Game");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit out of the game
		this.setSize(800, 600);
		this.setVisible(true);
		JTextField inputArea; //input field 
		JTextArea freeArea; // output field 
		Client client = null; // Client object
		String host = "localhost";
		JTextField hostField, portField;
		JButton connectBut ,closeBut, quitBut;
		int port = 28000;
		Thread thread = null;
		JScrollPane scrollpane = null;

		// ImageIcon icon = new ImageIcon("/Users/pokemon.png");
		// this.setIconImage(icon.getImage());
		// this.getContentPane().add(new JLabel(icon)); // change color of background
		//

	}

	public static void main(String[] args) {
		clientGUI frame = new clientGUI();
	}
}