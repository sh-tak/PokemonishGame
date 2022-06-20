package server.bin;

import java.util.Random;

// TODO Healの追加
public class Move {
    public static final int STRUGGLE_DAMAGE = 50;
    public String name;
    public int damage;
    public int maxcount;
    public int count;
    public int typeValue;
    public boolean isPhysical;

    public Move(String name, int damage, int maxcount, int typeValue, boolean isPhysical) {
        this.name = name;
        this.damage = damage;
        this.maxcount = maxcount;
        this.count = maxcount;
        this.typeValue = typeValue;
        this.isPhysical = isPhysical;
    }

    @Override
    public String toString() {
        String s1 = null, s2 = null;
        switch (typeValue) {
            case 1:
                s1 = "火";
                break;
            case 2:
                s1 = "水";
                break;
            case 3:
                s1 = "草";
                break;
            case 4:
                s1 = "光";
                break;
            case 5:
                s1 = "闇";
                break;
        }
        if (this.isPhysical){
            s2 = "物理";
        } else{
            s2 = "特殊";
        }
        return ("技名: " + this.name + " 威力: " + this.damage + 
                " 最大使用可能回数: " + this.count + " 属性: " + s1 + " 技種類: " + s2 +"\n");
    }

    public double calculateMultiplier(Monster myMonster, Monster oppMonster) {
        double same = 1.0; // タイプ一致
        double con = 1.0; // タイプ相性
        double k = 1.0; // 物理特殊倍率
        double me = 1.0; // 自分の光闇
        double you = 1.0; // 相手の光闇

        if (Monster.type2val(myMonster.type) == this.typeValue) {
            same = 1.5;
        }
        switch (Monster.type2val(myMonster.type)) {
            case 1:
                if (this.typeValue == 2) {
                    con = 0.5;
                }
                if (this.typeValue == 3) {
                    con = 2.0;
                }
                break;
            case 2:
                if (this.typeValue == 3) {
                    con = 0.5;
                }
                if (this.typeValue == 1) {
                    con = 2.0;
                }
                break;
            case 3:
                if (this.typeValue == 1) {
                    con = 0.5;
                }
                if (this.typeValue == 2) {
                    con = 2.0;
                }
                break;
        }

        if (this.isPhysical) {
            k = (double)myMonster.attack.getValue() / oppMonster.block.getValue();
        } else {
            k = (double) myMonster.contact.getValue() / oppMonster.defense.getValue();
        }
        if (k > 2.){ //kは最大32で大きすぎると一発で試合が終わるので最大を2にする
            k = 2.; 
        }

        if (Monster.type2val(myMonster.type) == 4) {
            me = 0.5;
        } else if (Monster.type2val(myMonster.type) == 5) {
            me = 2.0;
        }

        if (Monster.type2val(oppMonster.type) == 4) {
            you = 0.5;
        } else if (Monster.type2val(oppMonster.type) == 5) {
            you = 2.0;
        }
        return same * con * k * me * you;
    }

    public int getStruggleGamage() {
        return new Random().nextInt(STRUGGLE_DAMAGE);
    }

    public void reset() {
        this.count = this.maxcount;
    }
}
