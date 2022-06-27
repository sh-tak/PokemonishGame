package server.bin;

import java.util.Random;

public class IndivisualValue {
    private static int MAX_VALUE = 32;
    private int value;
    private String name;

    public IndivisualValue(String name){
        this.name = name;
        this.value = generateValue();
    }

    // 最小値10でステータス生成。 10になるのは大体6%
    private int generateValue(){
        int gaussianValue = (int) (new Random().nextGaussian()*4 +  16);
        if (gaussianValue > MAX_VALUE) {
            gaussianValue = MAX_VALUE;
        }else if(gaussianValue < 10){
            gaussianValue = 10;
        }
        return gaussianValue;    
    }

    public int getValue(){
        return this.value;
    }

    public int setValue(int value){
        this.value = value;
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
