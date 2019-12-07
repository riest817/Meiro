import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/*
<applet code="EventTest4.class" codebase="class" width="300" height="100">
</applet>
*/

public class EventTest4 extends Applet implements KeyListener{
  String msg1, msg2, msg3;
  Dimension size;
  Image back;
  Graphics buffer;

  public void init(){
    msg1 = "";
    msg2 = "";
    msg3 = "";
    addKeyListener(this);
  }

  public void start(){
	  size = getSize();
	  back = createImage(1000, 1000);
	  buffer = back.getGraphics();
  }

  public void paint(Graphics g){
    g.drawString(msg1, 10, 20);
    g.drawString(msg2, 30, 50);
    g.drawString(msg3, 30, 80);

    requestFocusInWindow();
  }

  public void keyPressed(KeyEvent e){
	int key = e.getKeyCode();
	switch (key) {
	  case 'M':  msgChange("M");  break;
	  case 'W':  msgChange("W");  break;
	  case 'X':  msgChange("X");  break;
	  case 'D':  msgChange("D");  break;
	}
	repaint();
  }

  public void keyReleased(KeyEvent e){
    msgChange("keyReleased");
    repaint();
  }

  public void keyTyped(KeyEvent e){
    msgChange("keyTyped");
    repaint();
  }

  private void msgChange(String msg){
    msg3 = msg2;
    msg2 = msg1;
    msg1 = msg;
  }
}