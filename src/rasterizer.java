import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
  private boolean move_camera_forward = false;
  private boolean move_camera_backward = false;

  private int fov = 90;
  private final double aspect = screen_width/(double)screen_height;
  private final double near = 1;
  private final int far = 1000;

  private double angle = 0;
  private boolean is_running = false;

  private final int fps = 60;
  private final long frame_time = 1_000_000_000L/fps;

  private double f = 1.0/Math.tan(Math.toRadians(fov)/2.0);

  Matrix project = new Matrix(new double[][] {
    {(f/aspect), 0, 0, 0},
    {0, f, 0 , 0},
    {0, 0, ((far + near)/(far - near)), 1},
    {0,0,-((far*near)/(far - near)),0}
  });

  Matrix identity = new Matrix(new double[][]{
    {1,0,0,0},
    {0,1,0,0},
    {0,0,1,0},
    {0,0,0,1}
  });

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
    Color color = new Color(0,0,0);

    setPreferredSize(new Dimension(screen_width, screen_height));
    setBackground(color);
    setFocusable(true);
    requestFocusInWindow();
    setOpaque(true);
    setDoubleBuffered(true);
    start();

    addKeyListener(new KeyAdapter(){
      @Override
      public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
          case KeyEvent.VK_W -> {move_up = true;}
          case KeyEvent.VK_S -> {move_down = true;}
          case KeyEvent.VK_D -> {move_right = true;}
          case KeyEvent.VK_A -> {move_left = true;}
          case KeyEvent.VK_E -> {move_camera_forward = true;}
          case KeyEvent.VK_Q -> {move_camera_backward = true;}
        }
      }

      @Override
      public void keyReleased(KeyEvent e) {
        switch(e.getKeyCode()){
          case KeyEvent.VK_W -> {move_up = false;}
          case KeyEvent.VK_S -> {move_down = false;}
          case KeyEvent.VK_D -> {move_right = false;}
          case KeyEvent.VK_A -> {move_left = false;}
          case KeyEvent.VK_E -> {move_camera_forward = false;}
          case KeyEvent.VK_Q -> {move_camera_backward = false;}
        }
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

      if(move_left)  {cameraX += 0.1;}
      if(move_right) {cameraX -= 0.1;}
      if(move_up)    {cameraY += 0.1;}
      if(move_down)  {cameraY -= 0.1;}

      if(move_camera_forward) {cameraZ += 0.1;}
      if(move_camera_backward) {cameraZ -= 0.1;}

      if(cameraZ > 3){
        cameraZ = 3;
      }

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
    g2.setColor(Color.RED);

    Matrix rotation = new Matrix(new double[4][4]).combined_rotation(angle);

    ArrayList<Triangle> monkey_Triangles = OBJLoader.loadOBJ("monkey.obj");
    Mesh monke = new Mesh();
    monke.tris.addAll(monkey_Triangles);

    ArrayList<Triangle> homer_triangles = OBJLoader.loadOBJ("homer.obj");
    Mesh homer = new Mesh();
    homer.tris.addAll(homer_triangles);

    ArrayList<Triangle> rabbit_triangle = OBJLoader.loadOBJ("rabbit.obj");
    Mesh rabbit = new Mesh();
    rabbit.tris.addAll(rabbit_triangle);
    
    //Graphics Pipeline:

    for(Triangle tri : monke.tris) {

      Vector4D r1 = tri.v1.mul(rotation);
      Vector4D r2 = tri.v2.mul(rotation);
      Vector4D r3 = tri.v3.mul(rotation);

      //Translation/offset into the screen, to avoid drawing behind the camera
      r1.z += 5;
      r2.z += 5;
      r3.z += 5;

      Vector3D a = new Vector3D((r2.x - r1.x), (r2.y - r1.y), (r2.z - r1.z));
      Vector3D b = new Vector3D((r3.x - r1.x), (r3.y - r1.y), (r3.z - r1.z));
      Vector3D normal = (Vector3D.cross(b, a)).normalize();


      Vector3D center = new Vector3D(
        (r1.x + r2.x + r3.x) / 3.0,
        (r1.y + r2.y + r3.y) / 3.0,
        (r1.z + r2.z + r3.z) / 3.0);

      Vector3D view = new Vector3D(center.x - cameraX, center.y - cameraY, center.z - cameraZ);

      if(Vector3D.dot(normal, view) < 0){

        r1.x -= cameraX;      r1.y -= cameraY;      r1.z -= cameraZ;
        r2.x -= cameraX;      r2.y -= cameraY;      r2.z -= cameraZ;
        r3.x -= cameraX;      r3.y -= cameraY;      r3.z -= cameraZ;
        
        //multiply by projection matrix --> project onto screen
        Vector4D p1 = r1.mul(project);
        Vector4D p2 = r2.mul(project);
        Vector4D p3 = r3.mul(project);

        //perspective divide
        if (p1.w != 0){
          p1.x /= p1.w; 
          p1.y /= p1.w; 
          p1.z /= p1.w;
        }

        if (p2.w != 0){
          p2.x /= p2.w; 
          p2.y /= p2.w; 
          p2.z /= p2.w;
        }

        if(p3.w != 0){
          p3.x /= p3.w; 
          p3.y /= p3.w; 
          p3.z /= p3.w;
        }

        // Convert from NDC to screen space
        int sx1 = (int)((p1.x + 1) * 0.5 * screen_width);
        int sy1 = (int)((1- (p1.y + 1) * 0.5) * screen_height);

        int sx2 = (int)((p2.x + 1) * 0.5 * screen_width);
        int sy2 = (int)((1- (p2.y + 1) * 0.5) * screen_height);

        int sx3 = (int)((p3.x + 1) * 0.5 * screen_width);
        int sy3 = (int)((1- (p3.y + 1) * 0.5) * screen_height);

        Vector3D A = new Vector3D(sx1, sy1, p1.z);
        Vector3D B = new Vector3D(sx2, sy2, p2.z);
        Vector3D C = new Vector3D(sx3, sy3, p3.z);

        //drawer.draw_triangle(A,B,C, g2);

        drawer.drawline(g2, sx1, sy1, sx2, sy2);
        drawer.drawline(g2, sx2, sy2, sx3, sy3);     // This is for wireframe and for debugging
        drawer.drawline(g2, sx3, sy3, sx1, sy1);
      }
    }
  }
}


class LineDrawer{

  public void put_pixel(Graphics2D graphic, int x1, int y1, Color color){
    graphic.setColor(color);
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
        put_pixel(graphic, x, y,  Color.WHITE);
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
        put_pixel(graphic, x, y,  Color.WHITE);
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

  public double edge_function(Vector3D a, Vector3D b, Vector3D c){
    return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
  }

public void draw_triangle(Vector3D v1, Vector3D v2, Vector3D v3, Graphics2D g) {

    Vector3D[] v = { v1, v2, v3 };
    java.util.Arrays.sort(v, (a, b) -> Double.compare(a.y, b.y));

    Vector3D v_top = v[0]; // top
    Vector3D v_mid = v[1]; // middle
    Vector3D v_bottom = v[2]; // bottom

    Color color = new Color(234,32,154);

    g.setColor(color);

    if (v_mid.y == v_bottom.y) {
        flat_bottom(v_top, v_mid, v_bottom, g); // check to see if its a flat bottom triangle
    }

    else if (v_top.y == v_mid.y) {
        flat_top(v_top, v_mid, v_bottom, g); // check to see if its a flat top triangle
    }

    // if its not a flat top or bottom triangle that means it an arbitary triangle
    else {
        double alpha = (v_mid.y - v_top.y) / (v_bottom.y - v_top.y); //percentage of distance covered  by v_mid between v_top to v_bottom

        //calculates the intersection point for the long edge 
        Vector3D vi = new Vector3D(
            v_top.x + alpha * (v_bottom.x - v_top.x),
            v_mid.y,
            v_top.z + alpha * (v_bottom.z - v_top.z)
        );

        flat_bottom(v_top, v_mid, vi, g);
        flat_top(v_mid, vi, v_bottom, g);
    }
  }

  private void flat_bottom(Vector3D v_top, Vector3D v_mid, Vector3D v_bottom, Graphics2D g) {
    double slope1 = (v_mid.x - v_top.x)/(v_mid.y - v_top.y);
    double slope2 = (v_bottom.x - v_top.x)/(v_bottom.y - v_top.y);

    double curx1 = v_top.x;
    double curx2 = v_top.x;

    for (int y = (int)Math.ceil(v_top.y); y <= (int)Math.ceil(v_mid.y); y++) {
        int xStart = (int)Math.ceil(Math.min(curx1, curx2));
        int xEnd   = (int)Math.ceil(Math.max(curx1, curx2));

        for (int x = xStart; x <= xEnd; x++) {
            g.drawLine(x, y, x, y);
        }

        curx1 += slope1;
        curx2 += slope2;
    }
  }

  private void flat_top(Vector3D v_top, Vector3D v_mid, Vector3D v_bottom, Graphics2D g) {
    double slope1 = (v_bottom.x - v_top.x)/(v_bottom.y - v_top.y);
    double slope2 = (v_bottom.x - v_mid.x)/(v_bottom.y - v_mid.y);

    double curx1 = v_bottom.x;
    double curx2 = v_bottom.x;

    for (int y = (int)Math.ceil(v_bottom.y); y >= (int)Math.ceil(v_top.y); y--) {
        int xStart = (int)Math.ceil(Math.min(curx1, curx2));
        int xEnd   = (int)Math.ceil(Math.max(curx1, curx2));

        for (int x = xStart; x <= xEnd; x++) {
            g.drawLine(x, y, x, y);
        }

        curx1 -= slope1;
        curx2 -= slope2;
    }
  }
}

