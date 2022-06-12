package server.bin;

import java.util.Random;

public class IndivisualValue {
    private static int MAX_VALUE = 32;
    private int value;
    private String idName;

    public IndivisualValue(String idName){
        this.idName = idName;
        this.value = new Random().nextInt(MAX_VALUE);
    }

    public int getValue(){
        return this.value;
    }
    
    public String showStats(){
        if(this.value < 1){
            return "ダメかも";
        }
        else if(this.value < 16){
            return "よくない";
        }
        else if(this.value < 26){
            return "悪くない";
        }
        else if(this.value < 31){
            return "良い";
        }
        else{
            return "素晴らしい";
        }
    }
}
