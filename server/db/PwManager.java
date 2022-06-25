package server.db;

// import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PwManager {
    private final String SQL_URL = "jdbc:mysql://localhost:3306/pockemonishgame";
    private final String SQL_USER = "root";
    private final String SQL_PW = "";

    // private ArrayList<Player> players = new ArrayList<>();
    private Map<String, String> pwlist = new HashMap<>();

    Statement statement;

    public PwManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PW);
            this.statement = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean reloadPlayers() {
        String sql = "SELECT * FROM players";
        try{
            ResultSet rs = statement.executeQuery(sql);
            // this.players.clear();
            this.pwlist.clear();
            while(rs.next()) {
                String name = rs.getString("name");
                String pw = rs.getString("pw");
                // Player player = new Player(name, pw);
                this.pwlist.put(name, pw);
            }
            return true;
        } catch (Exception e) {
            System.err.println("SQL error");
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPlayer(String name, String pw) {
        String sql = "INSERT INTO pockemonishgame.players(name, pw) VALUES(%s, %s);";
        sql = String.format(sql, name, pw);
        try {
            statement.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("SQL error");
            e.printStackTrace();
            return false;
        }
        pwlist.put(name, pw);
        return true;
    }

    public boolean addPlayer(Player p) {
        return this.addPlayer(p.name, p.pw);
    }

    public boolean check(String name, String pw) {
        return pwlist.get(name).equals(pw);
    }

    public String getPW(String name) {
        return pwlist.get(name);
    }
}
