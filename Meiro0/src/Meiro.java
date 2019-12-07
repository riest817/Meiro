import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Meiro extends JApplet implements Runnable, KeyListener, ActionListener{
  Thread thread = null;
  Dimension size;
  Image back;
  public Graphics buffer;
  public String message, message01, message02, message10;
  Font font;
  KeyEvent e;
  ImageIcon icon;

  private static final int CS = 32;

  int block[][];
  int dx[] = {0, 1, 0, -1};
  int dy[] = {1, 0, -1, 0};

  int stage;
  int marux;    /* PLAYER の X座標 */
  int maruy;	/* PLAYER の Y座標 */
  int marud;    /* 向き 0～3 */
  int direction;/* 向き PLAYER画像出力用 */
  int oldx;     /* 1つ前の PLAYER の X座標 */
  int oldy;		/* 1つ前の PLAYER の Y座標 */
  int next;   	/* PLAYERの次の位置 */

  public int score = 0; 	// 得点
  public int remain = 20 - score;
  public int sec = 300;		// 残り時間
  Timer timer;

  int item_num = 0;	// アイテムナンバー
  int st_level = 1; // ステージレベル

  int sw;		// ゲーム開始変数

  // チップセット
  private Image floorImage;
  private Image wallImage;
  private Image heroImage;
  private Image boxImage;
  public Image itemImage;
  public Image stageImage;

  // 勇者のアニメーションカウンタ
  private int count;

  // キャラクターアニメーション用スレッド
  private Thread threadAnime;

//-----------------------------
//	初期設定
//-----------------------------
  public void init(){
    stage = 0;

    size = getSize();
    back = createImage(1300, 1000);
    buffer = back.getGraphics();
    addKeyListener(this);
    setFocusable(true);

	message = "【ENTER】でゲーム開始です.";
	message01 = "宝箱は通り抜けられます.";
	message02 = "";
	message10 = "次のLEVELまでscoreがあと 20 点必要です。";
	font = new Font("Monospaced", Font.PLAIN, 12);

    block = new int[21][21];

    // イメージをロード
    loadImage();

    // マップ作製
    makeMaze();

    thread = new Thread(this);
    thread.start();

    // タイマー
    timer = new Timer(1000 , this);

    // キャラクターアニメーション用スレッド開始
    threadAnime = new Thread(new AnimationThread());
    threadAnime.start();
  }

//-----------------------------
//	マップ作成
//-----------------------------
  private void makeMaze(){
    /* 全体をクリア */
    for (int i = 0 ; i < 21 ; i++){
      for (int j = 0 ; j < 21 ; j++){
        block[i][j] = 0;
      }
    }

    /* 外枠をセット */
    for (int i = 0 ; i < 21 ; i++){
      block[0][i] = 1;
      block[20][i] = 1;
      block[i][0] = 1;
      block[i][20] = 1;
    }

    /* 基準点をセット */
    for (int i = 1 ; i <= 9 ; i++){
      for (int j = 1 ; j <= 9 ; j++){
        block[i * 2][j * 2] = 1;
      }
    }

    /* 迷路作成 */
    for (int i = 1 ; i <= 9 ; i++){
      for (int j = 1 ; j <= 9 ; j++){
        if (i == 1){
          int d = (int)(Math.random() * 4);
          block[i * 2 + dx[d]][j * 2 + dy[d]] = 1;
        }else{
          boolean flag = true;
          while(flag){
            int d = (int)(Math.random() * 3);
            if (block[i * 2 + dx[d]][j * 2 + dy[d]] == 0){
              block[i * 2 + dx[d]][j * 2 + dy[d]] = 1;
              flag = false;
            }
          }
        }
      }
    }

    /* アイテム作成*/
    for (int i = 1; i < 20; i++){
      for (int j = 1; j < 20; j++){
        if ( block[i][j] == 0){
        	int num = (int)(Math.random()*50);
          if (num == 5){
            block[i][j] = 2;
          }
        }
      }
    }
  }


//-----------------------------
//	出力部
//-----------------------------
  @Override
  public void paint(Graphics g){


	//--  マップ出力
    if (stage == 0){

      for (int i = 0 ; i < 21 ; i++){
        for (int j = 0 ; j < 21 ; j++){
          if (block[i][j] == 1){
        	  buffer.drawImage(wallImage, j * CS, i * CS, this);
          } else if (block[i][j] == 0 ){
        	  buffer.drawImage(floorImage, j * CS, i * CS, this);
          } else if ( block[i][j] == 2 ) {
        	  buffer.drawImage(boxImage, j * CS, i * CS, this);
          }
        }
      }

      buffer.setColor(Color.blue);
      buffer.fillRect(CS + 1, CS + 1, CS-2, CS-2);

      buffer.setColor(Color.red);
      buffer.fillRect(CS * 19 + 1, CS * 19 + 1, CS-2, CS-2);

      stage = 1;
    }else if (stage == 1){
      buffer.setColor(Color.blue);
      buffer.fillRect(CS + 1, CS + 1, CS-2, CS-2);

      buffer.setColor(new Color(34, 139, 34));
      buffer.fillRect(oldx * CS + 1, oldy * CS + 1, CS-2, CS-2);

      //buffer.setColor(Color.green);
      //buffer.fillRect(marux * CS + 1, maruy * CS + 1, CS-2, CS-2);
      // 勇者を描く
      drawChara(g);
    }

    //--  説明文の出力
	setFont(font);


	buffer.setColor(Color.BLACK);
	buffer.fillRect( size.width - 600, 0, 600, size.height);

	buffer.setColor(Color.WHITE);
	buffer.drawString("<< 宝箱の中身 >>", size.width - 370, 70 );


	buffer.setColor(Color.BLACK);
	buffer.fillRect( size.width - 600, size.height - 430, 600, 430);

	buffer.setColor(Color.WHITE);
	buffer.drawString(message01, size.width - 370,  300);
	buffer.drawString(message02, size.width - 370,  315);
	buffer.drawString(message10, size.width - 370,  450);

	buffer.setColor(Color.BLUE);
	buffer.fillRect(size.width - 370, size.height - 210, 10, 10);
	buffer.drawString("START の位置", size.width - 355, size.height - 200 );

	buffer.setColor(Color.RED);
	buffer.fillRect(size.width - 370, size.height - 195, 10, 10);
	buffer.drawString("GOAL の位置", size.width - 355, size.height - 185 );

	buffer.setColor(Color.GREEN);
	buffer.fillRect(size.width - 370, size.height - 180, 10, 10);
	buffer.drawString("PLAYER の軌跡", size.width - 355, size.height - 170 );

	buffer.setColor(Color.WHITE);
	buffer.drawString("<< 基本操作 >>", size.width - 370, size.height - 145 );
	buffer.drawString("矢印キーで移動", size.width - 370, size.height - 130 );
	buffer.drawString("X：やり直し(5点減点)", size.width - 370, size.height - 115 );
	buffer.drawString(message, size.width - 370, size.height - 100 );

	buffer.setColor(Color.YELLOW);
	buffer.drawString("<< ルール説明 >>", size.width - 370, size.height - 75 );
	buffer.drawString("左上のスタート地点から右下のゴール地点を目指す迷路ゲームです。", size.width - 370, size.height - 60 );
	buffer.drawString("制限時間"+sec+"秒で何面クリアできるかを競います。", size.width - 370, size.height - 45 );
	buffer.drawString("一度通った場所は二度と通ることができません。。", size.width - 370, size.height - 30 );
	buffer.drawString("1面をクリアするごとに10点が加算されます。", size.width - 370, size.height - 15 );

	//-- ゲーム進行状況の出力
	buffer.drawString("現在の スコア は " + score + " 点です.", size.width - 170, 20 );

	if ( sec <= 10 ) {
	  buffer.setColor(Color.RED);

	} else {
	  buffer.setColor(Color.GREEN);
	}
	buffer.drawString("残り時間は " + sec + " secです.", size.width - 170, 40);
	buffer.fillRect(size.width - 170, 50, sec * 2, 10);

	//-- アイテム画像の出力
	switch (item_num){
	  case 1:
	        icon = new ImageIcon(getClass().getResource("image/heart0.gif"));
	        itemImage = icon.getImage();	break;
	  case 2:
		    icon = new ImageIcon(getClass().getResource("image/coin.gif"));
		    itemImage = icon.getImage();	break;
	  case 3:
		    icon = new ImageIcon(getClass().getResource("image/red_coin.png"));
		    itemImage = icon.getImage();	break;
	  case 4:
		    icon = new ImageIcon(getClass().getResource("image/blue_coin.png"));
		    itemImage = icon.getImage();	break;
	  case 5:
		    icon = new ImageIcon(getClass().getResource("image/mimic.png"));
		    itemImage = icon.getImage();	break;
	  case 0:
		icon = new ImageIcon(getClass().getResource("image/takara.png"));
		itemImage = icon.getImage();
	}
	buffer.drawImage(itemImage, size.width - 370, 85, this);

	//-- ステージ画像の出力
		switch (st_level){
		  case 1:
			  message10 = "次のLEVELまでscoreがあと"+ (20 - score) +"点必要です。";
		        icon = new ImageIcon(getClass().getResource("image/level1.png"));
		        stageImage = icon.getImage();	break;
		  case 2:
			  message10 = "次のLEVELまでscoreがあと"+ (50 - score) +"点必要です。";
			    icon = new ImageIcon(getClass().getResource("image/level2.png"));
		        stageImage = icon.getImage();	break;
		  case 3:
			  message10 = "次のLEVELまでscoreがあと"+ (100 - score) +"点必要です。";
			    icon = new ImageIcon(getClass().getResource("image/level3.png"));
		        stageImage = icon.getImage();	break;
		  case 4:
			  message10 = "次のLEVELまでscoreがあと"+ (200 - score) +"点必要です。";
			    icon = new ImageIcon(getClass().getResource("image/level4.png"));
		        stageImage = icon.getImage();	break;
		  case 5:
			  message10 = "";
			    icon = new ImageIcon(getClass().getResource("image/level5.png"));
		        stageImage = icon.getImage();	break;
		}
		buffer.drawImage(stageImage, size.width - 370, 350, this);

	g.drawImage(back, 0, 0, this);

  }


//-----------------------------
//	実行部
//-----------------------------
  public void run(){


    while(true){
      if (stage == 0){
        makeMaze(); /* 迷路作成 */

        if ( sw == 1 ) {
            marux = 1;  /* スタート地点を初期化 */
            maruy = 1;
            marud = 0;
            oldx = 1;
            oldy = 1;
        } else {
        	marux = -1;  /* スタート地点を初期化 */
            maruy = -1;
            marud = 0;
            oldx = -1;
            oldy = -1;
        }

        repaint();


        // 2000ミリ秒待機する
        try{
          Thread.sleep(2000);
        }catch (InterruptedException e){}
      }else if (stage == 1){
        // 迷路を解いている途中

        repaint();

        // 300ミリ秒待機する
        try{
          Thread.sleep(300);
        }catch (InterruptedException e){}

        //move();

        if ((marux == 19) && (maruy == 19)){
            /* ゴールに着いたら地図初期化へ */
            stage = 0;
            // 得点を加算
            score += 10;
            message = "ゴールしました。次のスデージです。";

  		    // ステージレベル判定
  		    if ( score >= 200 ) { st_level = 5; }
  		    else if ( score >= 100 ) { st_level = 4; }
  		    else if ( score >= 50 ) { st_level = 3; }
  		    else if ( score >= 20 ) { st_level = 2; }
  		    // 画像をロード
  		    loadImage();
  		  }
        }
      }
    }

//-----------------------------
//	迷路巡回
//-----------------------------
  private void move(){
    /* まず左に行けるかどうかチェック */
    int leftd = marud + 1;
    if (leftd == 4){
      leftd = 0;
    }

    int left = block[maruy + dy[leftd]][marux + dx[leftd]];

    if (left == 0){
      marud = leftd;

      oldx = marux;
      oldy = maruy;
      marux += dx[marud];
      maruy += dy[marud];

      return;
    }

    /* 次に現在の進行方向に行けるかチェック */
    next = block[maruy + dy[marud]][marux + dx[marud]];

    while(next == 1){
      /* 行けなければ右へ右へと向きを変えて行けるかどうかチェック */
      marud--;
      if (marud == -1){
        marud = 3;
      }

      next = block[maruy + dy[marud]][marux + dx[marud]];
    }

    oldx = marux;
    oldy = maruy;
    marux += dx[marud];
    maruy += dy[marud];
  }


	@Override
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void stop() {
		thread = null;
	}
//-----------------------------
//	入力部
//-----------------------------
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		int flag = 1;

		// 入力操作
		switch (key) {
		  case KeyEvent.VK_LEFT :  marud = 3;  direction = 0; break;
		  case KeyEvent.VK_UP   :  marud = 2;  direction = 2; break;
		  case KeyEvent.VK_DOWN :  marud = 0;  direction = 3; break;
		  case KeyEvent.VK_RIGHT:  marud = 1;  direction = 1; break;
		  case 'X':
			     if ( sw == 0 ) { break; }
			     stage = 0;
		         score -= 5;
		         makeMaze(); /* 迷路作成 */

		         marux = 1;  /* スタート地点を初期化 */
		         maruy = 1;
		         marud = 0;
		         oldx = 0;
		         oldy = 0;

		         repaint();
		     	 message = "やり直しました";		break;

          case  KeyEvent.VK_ENTER:
		         sw = 1;
		         message = "ENTERキー → Xキーで再スタートできます.";
		         timer.start();

		         marux = 1;  /* スタート地点に配置 */
	             maruy = 1;
	             marud = 0;
	             oldx = 1;
	             oldy = 1;

	             //-- 初期化
	             score = 0;
	             sec = 30;
	             st_level = 1;
	             loadImage();  // 画像をロード

		  default :  flag = 0;
		}

		// PLAYERが次に行く位置
		next = block[maruy + dy[marud]][marux + dx[marud]];

		// 壁判定
		if ( next != 1 /*&& flag == 1*/ ){
		  oldx = marux;
		  oldy = maruy;
		  marux += dx[marud];
		  maruy += dy[marud];
		}

		// アイテムナンバー初期化
		item_num = 0;

		// アイテム判定
		if ( next == 2 ){
		  if ( st_level == 1 ){
		    item1();
		  }
		}


		block[oldy + dy[marud]][oldx + dx[marud]] = 1;

	    repaint();
	}

//-----------------------------
//	終了処理部
//-----------------------------
//-----------------------------

//-----------------------------
	public void actionPerformed(ActionEvent e){

	  if (sec == 0){
		buffer.setColor(Color.BLACK);
		buffer.fillRect( 175, 300, 250, 100);
		setFont(font);
		buffer.setColor(Color.RED);
		buffer.drawString("タイムオーバー", 250, 350 );
		buffer.setColor(Color.YELLOW);
		buffer.drawString("あたなのスコアは" + score + "点です.", 225, 375 );

	    timer.stop();
	    sw = 0;

	    //-- 配置を初期化
	    marux = -1;
        maruy = -1;
        marud = 0;
        oldx = -1;
        oldy = -1;
	  }else{
		sec--;
	  }

	}

//-----------------------------
//	画像の読み込み
//-----------------------------
//	時間処理部

    private void loadImage() {
        icon = new ImageIcon(getClass().getResource("image/hero.gif"));
        heroImage = icon.getImage();

        if ( st_level == 1 ){
          icon = new ImageIcon(getClass().getResource("image/floor1.jpg"));
        } else if ( st_level == 2 ){
          icon = new ImageIcon(getClass().getResource("image/floor2.gif"));
        }  else if ( st_level == 3 ){
          icon = new ImageIcon(getClass().getResource("image/floor3.jpg"));
        }  else if ( st_level == 4 ){
          icon = new ImageIcon(getClass().getResource("image/floor4.JPG"));
        }  else if ( st_level == 5 ){
          icon = new ImageIcon(getClass().getResource("image/floor5.jpg"));
        }
        floorImage = icon.getImage();

        if ( st_level == 1 || st_level == 2 ){
          icon = new ImageIcon(getClass().getResource("image/wall.gif"));
        } else if ( st_level == 3 || st_level == 4 ){
        	icon = new ImageIcon(getClass().getResource("image/wall3.jpg"));
        } else if ( st_level == 5 ){
            icon = new ImageIcon(getClass().getResource("image/wall5.jpg"));
        }
        wallImage = icon.getImage();

        icon = new ImageIcon(getClass().getResource("image/box.jpg"));
        boxImage = icon.getImage();
    }


    private void drawChara(Graphics g) {
        // countとdirectionの値に応じて表示する画像を切り替える
        g.drawImage(heroImage, marux * CS, maruy * CS, marux * CS + CS, maruy * CS + CS,
            count * CS, direction * CS, CS + count * CS, direction * CS + CS, this);
    }

//-----------------------------
//	アニメーションクラス
//-----------------------------
    private class AnimationThread extends Thread {
        public void run() {
            while (true) {
                // countを切り替える
                if (count == 0) {
                    count = 1;
                } else if (count == 1) {
                    count = 0;
                }

                repaint();

                // 300ミリ秒休止＝300ミリ秒おきに勇者の絵を切り替える
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }




//-----------------------------
//	Main文
//-----------------------------
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			/* タイトルバーに表示する文字列を指定できる */
			JFrame frame = new JFrame("迷路");
			/* Bong はクラスの名前にあわせる */
			JApplet applet = new Meiro();
			/* アプレット部分のサイズを指定する */
			applet.setPreferredSize(new Dimension(1300, 700));
			frame.add(applet);
			frame.pack();
			frame.setVisible(true);
			applet.init();
			applet.start();
			/* ×ボタンを押したときの動作を指定する */
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		});
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

//-----------------------------
//	アイテム処理部
//-----------------------------
	private void item1() {
	// TODO 自動生成されたメソッド・スタブ
	  double num = Math.random()*10000;

	  if ( num > 6000 ){
		  item_num = 1;
		  message01 = "ハートを手に入れました.";
		  message02 = "残り時間が 15 秒回復します.";
		  sec += 15;
	  } else if ( num > 3000 ){
		  item_num = 2;
		  message01 = "コインを手に入れました.";
		  message02 = "score が 1 加点されます.";
		  score += 1;
	  } else if ( num > 2000 ){
		  item_num = 3;
		  message01 = "赤コインを手に入れました.";
		  message02 = "score が 5 加点されます.";
		  score += 5;
	  } else if ( num > 1500 ){
		  item_num = 4;
		  message01 = "青コインを手に入れました.";
		  message02 = "score が 20 加点されます.";
		  score += 20;
	  } else if ( num > 500 ){
		  item_num = 5;
		  message01 = "ミミックが出現しました.";
		  message02 = "score が 3 減点されます. 残り時間が 3 秒減ります.";
		  score -= 3;	sec -= 3;
	  }

	}

}
