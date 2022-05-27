/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// package jframetest;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 *
 * @author ogawa
 */
public class JFrameTest extends JFrame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
      new JFrameTest();
    }

    private JFrameTest() {
      // ペインを取得
      getContentPane().setLayout(new FlowLayout());

      // 閉じるボタン処理
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      // タイトル設定
      setTitle("JFrame Test");
      // サイズ設定
      setSize(300, 600);

      // ボタンの追加
      JButton b1 = new JButton("OK");
      getContentPane().add(b1);

      // トグルボタンの追加
      JToggleButton tb1 = new JToggleButton("TOGGLE");
      getContentPane().add(tb1);

      // ラベルの追加
      JLabel l1 = new JLabel("Hello World!!");
      getContentPane().add(l1);

      // テキストフィールドの追加
      JTextField t1 = new JTextField("Kitty on your lap");
      getContentPane().add(t1);

      // テキストエリアの追加
      JTextArea ta1 = new JTextArea("My head killing me.", 4, 20);
      getContentPane().add(ta1);

      // チェックボックスの追加
      JCheckBox cb1 = new JCheckBox("Agree!!");
      getContentPane().add(cb1);

      // ラジオボタンの追加
      JRadioButton rb1 = new JRadioButton("On", true);
      JRadioButton rb2 = new JRadioButton("Off");
      JRadioButton rb3 = new JRadioButton("Off");
      
      getContentPane().add(rb1);
      getContentPane().add(rb2);
      getContentPane().add(rb3);
      
      ButtonGroup bg1 = new ButtonGroup(); // 3つをグループ化
      bg1.add(rb1);
      bg1.add(rb2);
      bg1.add(rb3);

      // コンボボックスの追加
      JComboBox cm1 = new JComboBox();
      cm1.addItem("Fedora");
      cm1.addItem("Ubuntu");
      cm1.addItem("Vine");
      cm1.addItem("BSD");
      getContentPane().add(cm1);

      // リストの追加
      String[] listData = {"Windows", "Max OS", "Linux", "Android"};
      JList lt1 = new JList(listData);
      getContentPane().add(lt1);

      // プログレスバーの追加
      JProgressBar pb1 = new JProgressBar(1, 100);
      pb1.setValue(45);
      getContentPane().add(pb1);

      // テーブルの追加
      JTable tl1 = new JTable(3, 3);
      tl1.setValueAt("北西", 0, 0);
      tl1.setValueAt("北",   0, 1);
      tl1.setValueAt("北東", 0, 2);
      tl1.setValueAt("西",   1, 0);
      tl1.setValueAt("中心", 1, 1);
      tl1.setValueAt("東",   1, 2);
      tl1.setValueAt("南西", 2, 0);
      tl1.setValueAt("南",   2, 1);
      tl1.setValueAt("南東", 2, 2);
      getContentPane().add(tl1);

      // ツリーの追加(java.swing.tree.*を読み込んでおく)
      DefaultMutableTreeNode nRoot = new DefaultMutableTreeNode("World");
      DefaultMutableTreeNode nJapan = new DefaultMutableTreeNode("Japan");
      DefaultMutableTreeNode nTokyo = new DefaultMutableTreeNode("Tokyo");
      DefaultMutableTreeNode nOsaka = new DefaultMutableTreeNode("Osaka");
      DefaultMutableTreeNode nFormosa = new DefaultMutableTreeNode("Formosa");
      DefaultMutableTreeNode nTaipei = new DefaultMutableTreeNode("Taipei");
      DefaultMutableTreeNode nTainan = new DefaultMutableTreeNode("Tainan");

      JTree tr1 = new JTree(nRoot);
      tr1.setRootVisible(true); // ルートを表示
      getContentPane().add(tr1);

      nRoot.add(nJapan);
        nJapan.add(nTokyo);
        nJapan.add(nOsaka);
      nRoot.add(nFormosa);
        nFormosa.add(nTaipei);
        nFormosa.add(nTainan);

      // フレームを表示
      setVisible(true);
      
    }

}