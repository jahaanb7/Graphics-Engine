
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
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

  Mesh monke = new Mesh();
  Mesh homer = new Mesh();
  Mesh rabbit = new Mesh();
  Mesh sphere = new Mesh();

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

    monke.tris.addAll(OBJLoader.loadOBJ("monkey.obj"));

    homer.tris.addAll(OBJLoader.loadOBJ("homer.obj"));

    rabbit.tris.addAll(OBJLoader.loadOBJ("rabbit.obj"));

    sphere.tris.addAll(OBJLoader.loadOBJ("sphere.obj"));

    setPreferredSize(new Dimension(screen_width, screen_height));
    setBackground(Color.WHITE);
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

      if(move_left)  {cameraX -= 0.1;}
      if(move_right) {cameraX += 0.1;}
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
    g2.setColor(Color.WHITE);

    BufferedImage screen = new BufferedImage(screen_width, screen_height, BufferedImage.TYPE_INT_RGB);

    Matrix rotation = new Matrix(new double[4][4]).combined_rotation(angle);

    //Graphics Pipeline:
    for(Triangle tri : monke.tris) {

      Vector4D r1 = tri.v1.mul(rotation);
      Vector4D r2 = tri.v2.mul(rotation);
      Vector4D r3 = tri.v3.mul(rotation);

      //Translation/offset into the screen, to avoid drawing behind the camera
      double scale = 2;   // scale model
      double zOffset = 10;    // offset the model in the z-axis to push it forward
      
      r1.scalar_mul(scale);
      r2.scalar_mul(scale);
      r3.scalar_mul(scale);

      r1.z += zOffset;
      r2.z += zOffset;
      r3.z += zOffset;

      Vector3D a = new Vector3D((r2.x - r1.x), (r2.y - r1.y), (r2.z - r1.z));
      Vector3D b = new Vector3D((r3.x - r1.x), (r3.y - r1.y), (r3.z - r1.z));
      Vector3D normal = (Vector3D.cross(b, a)).normalize();


      Vector3D center = new Vector3D(
        (r1.x + r2.x + r3.x)/3.0,
        (r1.y + r2.y + r3.y)/3.0,
        (r1.z + r2.z + r3.z)/3.0);

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
        if (p1.w != 0){p1.x /= p1.w; p1.y /= p1.w; p1.z /= p1.w;}
        if (p2.w != 0){p2.x /= p2.w; p2.y /= p2.w; p2.z /= p2.w;}
        if(p3.w != 0){p3.x /= p3.w; p3.y /= p3.w; p3.z /= p3.w;}

        // Convert from NDC to screen space
        int sx1 = (int)((p1.x + 1) * 0.5 * screen_width); // 0.5 to get it to the center of the screen, and + 1 to get it in infront of camera.
        int sy1 = (int)((1 - (p1.y + 1) * 0.5) * screen_height);

        int sx2 = (int)((p2.x + 1) * 0.5 * screen_width);
        int sy2 = (int)((1 - (p2.y + 1) * 0.5) * screen_height);

        int sx3 = (int)((p3.x + 1) * 0.5 * screen_width);
        int sy3 = (int)((1 - (p3.y + 1) * 0.5) * screen_height);

        Vector3D A = new Vector3D(sx1, sy1, p1.z);
        Vector3D B = new Vector3D(sx2, sy2, p2.z);
        Vector3D C = new Vector3D(sx3, sy3, p3.z);

        Color v1 = new Color(25,34,32);
        Color v2 = new Color(135,44,52);
        Color v3 = new Color(215,64,32);

        drawer.draw_triangle(A,B,C,screen, v1, v2, v3);

        //drawer.drawline(screen, sx1, sy1, sx2, sy2);
        //drawer.drawline(screen, sx2, sy2, sx3, sy3);     // This is for wireframe and for debugging
        //drawer.drawline(screen, sx3, sy3, sx1, sy1);
      }
    }
    g.drawImage(screen, 0, 0, null);
  }
}


class LineDrawer{

  public void put_pixel(BufferedImage screen, int x1, int y1, Color color){
    screen.setRGB(x1,y1,color.getRGB());
  }

  public void draw_line_h(BufferedImage screen, int x1, int y1, int x2, int y2) {
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
        put_pixel(screen, x, y,  Color.WHITE);
        if(p >= 0){
          y += dir;
          p = p + 2*dy - 2*dx;
        }
        else{p = p + 2*dy;}
      }
    }
  }

  public void draw_line_v(BufferedImage screen, int x1, int y1, int x2, int y2) {
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
        put_pixel(screen, x, y,  Color.WHITE);
        if(p >= 0){
          x += dir;
          p = p + 2*dx - 2*dy;
        }
        else{p = p + 2*dx;}
      }
    }
  }

  public void drawline(BufferedImage screen, int x0, int y0, int x1, int y1){
    
    if(Math.abs(x1 - x0) > Math.abs(y1 - y0)){
      draw_line_h(screen, x0, y0, x1, y1);
    }
    else{
      draw_line_v(screen, x0, y0, x1, y1);
    }
  }

  public void connect(BufferedImage screen, Matrix a){    
    for (int i = 0; i <  a.rows; i++) {
      int next = (i + 1)% a.rows;

      int x1 = (int) a.data[i][0];
      int y1 = (int) a.data[i][1];
      int x2 = (int) a.data[next][0];
      int y2 = (int) a.data[next][1];
      drawline(screen, x1, y1, x2, y2);
    }
  }

  public double edge_function(Vector3D a, Vector3D b, Vector3D c){
    return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
  }

public void draw_triangle(Vector3D v1, Vector3D v2, Vector3D v3, BufferedImage screen, Color c1, Color c2, Color c3) {
  
  //bounding box
  int minX = (int)Math.floor(Math.min(v1.x, Math.min(v2.x, v3.x)));
  int maxX = (int)Math.ceil(Math.max(v1.x, Math.max(v2.x, v3.x)));
  int minY = (int)Math.floor(Math.min(v1.y, Math.min(v2.y, v3.y)));
  int maxY = (int)Math.ceil(Math.max(v1.y, Math.max(v2.y, v3.y)));

  double area = edge_function(v1, v2, v3);

  //go over every pixel in the bounding box
  for (int y = minY; y <= maxY; y++) {
    for (int x = minX; x <= maxX; x++) {

        //set an intital pixel to detect
        Vector3D p = new Vector3D(x + 0.5, y + 0.5, 0);

        //barycentric weights
        double w1 = (edge_function(v2, v3, p))/area;
        double w2 = (edge_function(v3, v1, p))/area;
        double w3 = (edge_function(v1, v2, p))/area;

        //point inside the triangle
        if (w1 >= 0 && w2 >= 0 && w3 >= 0) {
            Color col = interpolateColor(p, v1, v2, v3, c1, c2, c3);
            
          if (x >= 0 && x < screen.getWidth() && y >= 0 && y < screen.getHeight()) { //prevents the entire screen from becoming a 'triangle'
              screen.setRGB(x, y, col.getRGB());
          }
        }
      }
    }
  }

  private Color interpolateColor(Vector3D p, Vector3D v1, Vector3D v2, Vector3D v3, Color c1, Color c2, Color c3) {
    double area = edge_function(v1, v2, v3);

    double w1 = edge_function(v2, v3, p) / area;
    double w2 = edge_function(v3, v1, p) / area;
    double w3 = edge_function(v1, v2, p) / area;

    w1 = Math.max(0, Math.min(1, w1));
    w2 = Math.max(0, Math.min(1, w2));
    w3 = Math.max(0, Math.min(1, w3));

    // Normalize just in case
    double sum = w1 + w2 + w3;
    w1 /= sum;
    w2 /= sum;
    w3 /= sum;

    int r = (int)Math.round(c1.getRed() * w1 + c2.getRed() * w2 + c3.getRed() * w3);
    int g = (int)Math.round(c1.getGreen() * w1 + c2.getGreen() * w2 + c3.getGreen() * w3);
    int b = (int)Math.round(c1.getBlue() * w1 + c2.getBlue() * w2 + c3.getBlue() * w3);

    return new Color(Math.min(255,r), Math.min(255,g), Math.min(255,b));
  }
}
