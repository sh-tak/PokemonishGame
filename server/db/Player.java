package server.db;

import java.util.Random;

public class Player {
    int id;
    String name;
    String pw;

    static int idStackTop = 0;

    // id is not guaranted 
    // real id is max(preferedId, idStackTop)
    public Player(int preferedId, String name, String pw) {
        this.id = Math.max(idStackTop, preferedId);
        this.name = name;
        this.pw = pw;
        idStackTop = this.id++;
    }

    public Player(String name, String pw) {
        this.id = idStackTop++;
        this.name = name;
        this.pw = pw;
    }

    // generated at https://randomwordgenerator.com/name.php
    private static String[] randomNames = {
        "Miles Blake","Nigel Bennett","Ora Rivera","Ericka Lyons","Jacquelyn Drake",
        "Betty Knapp","Shannon Oconnell","Chong Leon","Darren Rocha","Wanda Hogan",
        "James Chaney","Eileen West","Cristobal Valenzuela","Kurtis Graves","Madeline Zuniga",
        "Len Jensen","Lynda Mullen","Jon Hancock","Myron Boyle","Margarito Coffey",
    };

    public Player(String pw) {
        this(null, pw);
        Random random = new Random();
        this.name = randomNames[random.nextInt(randomNames.length)];
    }
}
