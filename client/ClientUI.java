package client;
import java.awt.*;
// import java.awt.event.*;
// import java.io.IOException;

import javax.swing.*;

public class ClientUI extends JFrame{
    
    public static void main(String[] args) {
        // debug
        new ClientUI();
        
    }
    
    JLabel imageLabel;
    JLabel statusLabel;
    JList<String> wazaList;
    JButton okButton;
    JTextArea logArea;

    ClientUI() {
        setTitle("Pokemonish Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        getContentPane().setLayout(new FlowLayout());
        
        imageLabel = new JLabel("no image");
        statusLabel = new JLabel("wait for status");
        String[] initialList = {"a", "b", "c", "d"};
        wazaList = new JList<>(initialList);
        okButton = new JButton("OK");
        logArea = new JTextArea("log start...\n", 5, 20);

        //TODO: set size of each component
        
        getContentPane().add(imageLabel);
        getContentPane().add(statusLabel);
        getContentPane().add(wazaList);
        getContentPane().add(okButton);
        getContentPane().add(logArea);
        setVisible(true);
    }    

    public void setStatus(String newStatus) {
        statusLabel.setText(newStatus);
    }

    public void setWaza(String[] newWaza) {
        wazaList.setListData(newWaza);
    }

    public void setImage(String file) {
        ImageIcon icon = new ImageIcon(file);
        imageLabel.setIcon(icon);
    }

    public void setButtonAction(Action action) {
        okButton.setAction(action);
    }

    public void log(String log) {
        logArea.append(log);
    }
}
