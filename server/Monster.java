package server;

import java.util.Random;

public class Monster {
    public String name;// 名前
    public int health;// HP
    public int attack;// 攻撃
    public int block;// 防御
    public int contact;// 特攻
    public int defense;// 特防
    public int speed;// 素早さ
    String type;// 属性
    public Move moveList[];// 技; 
    public int sum;// 個体値
    public int hp;// 実HP
    public Object[] moveLists;

    public Monster(Move moveList[], String type) {
        this.name = "";
        this.health = generateBaseStats();
        this.attack = generateBaseStats();
        this.block = generateBaseStats();
        this.contact = generateBaseStats();
        this.defense = generateBaseStats();
        this.speed = generateBaseStats();
        this.type = type;
        this.sum = this.health + this.attack + this.block + this.contact + this.defense + this.speed;
        this.hp = (this.sum * 2 + this.health) + 110;
        this.moveList = moveList;
    }
    public static int type2val(String type){
        int value = 0;
        switch(type){
            case "火":
                value = 1;
                break;
            case "水":
                value = 2;
                break;
            case "草":
                value = 3;
                break;
            case "光":
                value = 4;
                break;
            case "闇":
                value = 5;
                break;
        }
        return value;
    }

    private int generateBaseStats(){
        return new Random().nextInt(31);
    }
    public void decreaseHP(int n) {
        this.hp -= n;
    }
}
