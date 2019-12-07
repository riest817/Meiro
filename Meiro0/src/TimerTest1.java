import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TimerTest1 extends JFrame implements ActionListener{

  Timer timer;
  JLabel label;
  int sec;

  public static void main(String[] args){
    TimerTest1 frame = new TimerTest1();

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setBounds(10, 10, 300, 200);
    frame.setTitle("タイトル");
    frame.setVisible(true);
  }

  public TimerTest1(){
    sec = 60;
    label = new JLabel();

    JPanel labelPanel = new JPanel();
    labelPanel.add(label);

    timer = new Timer(1000 , this);

    getContentPane().add(labelPanel, BorderLayout.CENTER);

    timer.start();
  }

  public void actionPerformed(ActionEvent e){
    label.setText(sec + " sec");

    if (sec == 0){
      timer.stop();
    }else{
      sec--;
    }
  }
}