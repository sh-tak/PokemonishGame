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

    // 正規分布で値を生成
    // 一様分布で生成すると極端な値を取りやすくなって試合が一瞬で終わったり、全然終わらなかったりしやすいので正規分布で生成
    private int generateValue(){
        // 平均16, 標準偏差8の値を生成(このとき値が1以下もしくは32以上になる確率は約5%)
        int gaussianValue = (int) (new Random().nextGaussian()*8 +  16);
        if (gaussianValue > MAX_VALUE) {
            gaussianValue = MAX_VALUE;
        }else if(gaussianValue < 1){
            gaussianValue = 1;
        }
        return gaussianValue;    
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
