
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class rasterizer extends  JPanel implements Runnable{
  Thread gameThread;

  private static final int screen_height = 800;
  private static final int screen_width = 800;
  private final double frame_speed = 1;

  private double cameraX = 0; 
  private double cameraY = 0;
  private double cameraZ = 0;

  private boolean move_left = false;
  private boolean move_right = false;
  private boolean move_up = false;
  private boolean move_down = false;

  private double scrollDelta = 0;
  private final double scroll_pace = 0.54;

  private int fov = 90;
  private final double aspect = screen_width/(double)screen_height;
  private final double near = 0.1;
  private final int far = 1000;

  private double angle = 0;
  private boolean is_running = false;

  private final int fps = 60;
  private final long frame_time = 1_000_000_000L/fps;
  private final int size = 10;

  Matrix cube = new Matrix(new double[][]{
    {-size, -size, -size, 1},
    {-size, -size,  size, 1},
    {-size,  size, -size, 1},
    {-size,  size,  size, 1},
    { size, -size, -size, 1},
    { size, -size,  size, 1},
    { size,  size, -size, 1},
    { size,  size,  size, 1}
  });

  // each {a,b} means to draw a line from a to b in each vertices in cube
  int[][] edges = {
    {0,1},{0,2},{0,4},
    {1,3},{1,5},
    {2,3},{2,6},
    {3,7},
    {4,5},{4,6},
    {5,7},
    {6,7}
  };  

  LineDrawer drawer = new LineDrawer();
  public static void main(String[] args) {
      JFrame frame = new JFrame();

      frame.add(new rasterizer());
      frame.setSize(screen_width, screen_height);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setTitle("3D Model Graphics");
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
  }

  public rasterizer(){
    setPreferredSize(new Dimension(500, 500));
    setBackground(Color.BLACK);
    setFocusable(true);
    requestFocusInWindow();
    start();

    addKeyListener(new KeyAdapter(){
      @Override
      public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
          case KeyEvent.VK_W -> {move_up = true;}
          case KeyEvent.VK_S -> {move_down = true;}
          case KeyEvent.VK_D -> {move_right = true;}
          case KeyEvent.VK_A -> {move_left = true;}
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()){
          case KeyEvent.VK_W -> {move_up = false;}
          case KeyEvent.VK_S -> {move_down = false;}
          case KeyEvent.VK_D -> {move_right = false;}
          case KeyEvent.VK_A -> {move_left = false;}
        }
      }
    });

    addMouseWheelListener(new MouseWheelListener(){
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        scrollDelta += e.getPreciseWheelRotation();
      }
    });
  }

  private void start() {
    is_running = true;
    gameThread = new Thread(this, "GameThread");
    gameThread.start();
  }

  @Override
    public void run() {
    long lastTime;
    while (is_running) {
      lastTime = System.nanoTime();

      double targetFOV = fov;
      targetFOV -= scrollDelta * 1.3;

      if(targetFOV < 10){targetFOV = 10;}
      if(targetFOV > 140){targetFOV = 140;}

      if(move_left)  {cameraX -= 1;}
      if(move_right) {cameraX += 1;}
      if(move_up)    {cameraY += 1;}
      if(move_down)  {cameraY -= 1;}
      

      fov += (targetFOV - fov) * 0.2;
      scrollDelta = 0;

      angle += frame_speed;
      repaint();

      long elapsed = System.nanoTime() - lastTime;
      long sleepTime = frame_time - elapsed;

      if (sleepTime > 0) {
        try {
            Thread.sleep(sleepTime / 1_000_000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
      }
    }
  }

  

  @Override
  protected void paintComponent(Graphics g){
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    Matrix rotated = cube.combined_rotation(angle); 
    Matrix object = cube.matrix_mul(rotated);   

    for(int i = 0; i < object.rows; i++){
      object.data[i][2] += 50; //shift z in each row of cube by 50
    }

    for(int i=0; i<object.rows; i++){
      object.data[i][0] -= cameraX;
      object.data[i][1] -= cameraY;
      object.data[i][2] -= cameraZ;
    }

    Matrix projected = object.project(fov, aspect,near,far);

    //wireframe --> from which vertice to draw a line to the next vertex
    for (int[] edge : edges) {
      int i = edge[0];
      int j = edge[1];

      double x1 = projected.data[i][0];
      double y1 = projected.data[i][1];

      double x2 = projected.data[j][0];
      double y2 = projected.data[j][1];

      int sx1 = (int)((x1 + 1) * 0.5 * screen_width);
      int sy1 = (int)((1 - y1) * 0.5 * screen_height);

      int sx2 = (int)((x2 + 1) * 0.5 * screen_width);
      int sy2 = (int)((1 - y2) * 0.5 * screen_height);

      drawer.drawline(g2, sx1, sy1, sx2, sy2);
    }
  }
}

class LineDrawer{

  public void put_pixel(Graphics2D graphic, int x1, int y1){
    graphic.setColor(Color.WHITE);
    graphic.fillRect(x1, y1, 1, 1);
  }

  public void draw_line_h(Graphics2D graphic, int x1, int y1, int x2, int y2) {
    if (x1 > x2) {
        int tx = x1;
        int ty = y1;

        x1 = x2; 
        y1 = y2;

        x2 = tx; 
        y2 = ty;
    }

    int dx = x2 - x1;
    int dy = y2 - y1;

    int dir;
    if(dy < 0){
      dir = -1;
    }
    else dir = 1;

    dy *= dir;

    if(dx != 0){
      int y = y1;
      int p = 2*dy - dx;

      for(int x = x1; x < x2+1; x++){
        put_pixel(graphic, x, y);
        if(p >= 0){
          y += dir;
          p = p + 2*dy - 2*dx;
        }
        else{p = p + 2*dy;}
      }
    }
  }

  public void draw_line_v(Graphics2D graphic, int x1, int y1, int x2, int y2) {
    if (y1 > y2) {
        int tx = x1; 
        int ty = y1;

        x1 = x2; 
        y1 = y2;
        
        x2 = tx;
        y2 = ty;
    }

    int dx = x2 - x1;
    int dy = y2 - y1;

    int dir = 0;
    if(dx < 0){
      dir = -1;
    }
    else dir = 1;

    dx *= dir;

    if(dy != 0){
      int x = x1;
      int p = 2*dx - dy;

      for(int y = y1; y < y2+1; y++){
        put_pixel(graphic, x, y);
        if(p >= 0){
          x += dir;
          p = p + 2*dx - 2*dy;
        }
        else{p = p + 2*dx;}
      }
    }
  }

  public void drawline(Graphics2D graphic, int x0, int y0, int x1, int y1){
    
    if(Math.abs(x1 - x0) > Math.abs(y1 - y0)){
      draw_line_h(graphic, x0, y0, x1, y1);
    }
    else{
      draw_line_v(graphic, x0, y0, x1, y1);
    }
  }

  public void connect(Graphics2D graphic, Matrix a){    
    for (int i = 0; i <  a.rows; i++) {
      int next = (i + 1)% a.rows;

      int x1 = (int) a.data[i][0];
      int y1 = (int) a.data[i][1];
      int x2 = (int) a.data[next][0];
      int y2 = (int) a.data[next][1];
      drawline(graphic, x1, y1, x2, y2);
    }
  }
}
