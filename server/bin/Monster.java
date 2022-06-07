package server.bin;

import java.util.Random;

public class Monster {
    public String name;// 名前
    public IndivisualValue health;// hp
    public IndivisualValue attack;// 攻撃
    public IndivisualValue block;// 防御
    public IndivisualValue contact;// 特攻
    public IndivisualValue defense;// 特防
    public IndivisualValue speed;// 素早さ
    public String type;// 属性
    public Move moves[];// 技; 
    public int sum;// 個体値
    public int hp;// 実hp

    public Monster(Move moves[], String name) {
        this.name = name;
        this.health = new IndivisualValue("HP");
        this.attack = new IndivisualValue("攻撃");
        this.block = new IndivisualValue("防御");
        this.contact = new IndivisualValue("特攻");
        this.defense = new IndivisualValue("特防");
        this.speed = new IndivisualValue("素早さ");
        String[] tmp = {"火", "水", "草", "光", "闇"};
        this.type = tmp[new Random().nextInt(5)]; 
        this.sum = this.health.getValue() + this.attack.getValue() + this.block.getValue() +
            this.contact.getValue() + this.defense.getValue() + this.speed.getValue();
        this.hp = (this.sum * 2 + this.health.getValue()) + 110;
        this.moves = moves;
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
    public String getType(){
        return this.type;
    }

    @Override
    public String toString() {
        String partition = "---------------";
        String evaluation;
        if (sum < 91) {
            evaluation = "まずまずな個体";
        } else if (sum < 151) {
            evaluation = "平均以上な個体";
        } else if (sum < 151) {
            evaluation = "相当優秀な個体";
        } else {
            evaluation = "素晴らしい個体!";
        }

        // Rem: 10行 
        return (partition + "\n" +
                "属性は " + getType() + "\n" + 
                "hp値は " + health.getValue() + "\n" +
                "攻撃は " + attack.getValue() + "\n" +
                "防御は " + block.getValue() + "\n" +
                "特攻は" + contact.getValue() + "\n" +
                "特防は " + defense.getValue() + "\n" +
                "素早さは " + speed.getValue() + "\n" +
                "総合評価: " + evaluation + "\n" +
                partition );
    }
    public void decreaseHp(int damage) {
        this.hp -= damage;
    }
}
