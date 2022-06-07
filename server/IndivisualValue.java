package server;

import java.util.Random;

public class IndivisualValue {
    private static int MAX_VALUE = 32;
    private int value;
    private String name;

    public IndivisualValue(String name){
        this.name = name;
        this.value = new Random().nextInt(MAX_VALUE);
    }

    public int getValue(){
        return this.value;
    }
    
    public String showStats(){
        if(this.value < 1){
            return "not good";
        }
        else if(this.value < 16){
            return "not bad";
        }
        else if(this.value < 26){
            return "good";
        }
        else if(this.value < 31){
            return "very good";
        }
        else{
            return "excellent";
        }
    }
}
