package server.db;

import java.util.Random;

public class Player {
    int id; //not used
    String name;
    String pw;

    static int count = 0;

    public Player(int id, String name, String pw) {
        this.id = id;
        this.name = name;
        this.pw = pw;
        count++;
    }

    public Player(String name, String pw) {
        this(count, name, pw);
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

    public static void resetCount() {
        count = 0;
    }

    @Override
    public String toString() {
        return String.format(
            "{id:%d, name:\"%s\", pw:\"%s\"}", this.id, this.name, this.pw);
    }
}
