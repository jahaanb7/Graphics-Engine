class Matrix{
  private int screen_height = 600;
  private int screen_width = 600;
  
  public int rows;
  public int columns;
  public double[][] data;
  
  public Matrix(double[][] values){
    this.rows = values.length;
    this.columns = values[0].length;

    data = new double[rows][columns];

    double[][] m = new double[rows][columns];

    for(int i = 0; i < rows; i++){
      for(int j = 0; j < columns; j++){
        this.data[i][j] = values[i][j];
      }
    }
  }

  public Matrix add(Matrix a){
    Matrix result = new Matrix(new double[rows][columns]);
    for(int i = 0; i < rows; i++){
      for(int j = 0; j < columns; j++){
        result.data[i][j] = this.data[i][j] + a.data[i][j];
      }
    }
    return result;
  }

  public Matrix matrix_mul(Matrix b){

    int rowsA = this.rows;   
    int colsA = this.columns;     
    int rowsB = b.rows;      
    int colsB = b.columns; 

    if (colsA != rowsB){
      throw new RuntimeException("Cannot be broadcasted together");
    }
    
    double[][] result_matrix = new double[rowsA][colsB];
    for (int i = 0; i < rowsA; i++) {
      for (int j = 0; j < colsB; j++) {
        double sum = 0;
          for (int k = 0; k < colsA; k++) {
            sum += this.data[i][k] * b.data[k][j];
          }
          result_matrix[i][j] = sum;
        }
      }
    return new Matrix(result_matrix);
  }

  public Matrix scalar_mul(double n){
    Matrix result = new Matrix(new double[rows][columns]);
    for (int i = 0; i < rows; i++){
      for (int j = 0; j < columns; j++){
        result.data[i][j] = this.data[i][j] * n;
      }
    }
    return result;
  }

  public Matrix transpose(){    
    double[][] transposed = new double[columns][rows];
    for(int i = 0; i < rows; i++){
      for(int j = 0; j < columns; j++){
          transposed[j][i] = data[i][j];
      }
    }
    return new Matrix(transposed);
  }

  public void print_matrix(){
    for(int i = 0; i < rows; i++){
      for(int j = 0; j < columns; j++){
        System.out.println(this.data[i][j] + ",");
      }
      System.out.println();
    }
  }

  public static Matrix rotateZ(double theta){
    double radian_theta = ((Math.PI)/180) * theta;

    double sin = Math.sin(radian_theta);
    double cos = Math.cos(radian_theta);

    Matrix rotated_z = new Matrix(new double[][]{
      {cos,-sin,0,0},
      {sin,cos,0,0},
      {0,0,1,0},
      {0,0,0,1}});

    return rotated_z;
  }

  public static Matrix rotateY(double theta){
    double radian_theta = ((Math.PI)/180) * theta;

    double sin = Math.sin(radian_theta);
    double cos = Math.cos(radian_theta);

    Matrix rotated_y = new Matrix(new double[][]{
      {cos,0,sin,0},
      {0,1,0,0},
      {-sin,0,cos,0},
      {0,0,0,1}});

    return rotated_y;
  }

  public static Matrix rotateX(double theta){
    double radian_theta = ((Math.PI)/180) * theta;

    double sin = Math.sin(radian_theta);
    double cos = Math.cos(radian_theta);

    Matrix rotated_x = new Matrix(new double[][]{
      {1,0,0,0},
      {0,cos,-sin,0},
      {0,sin,cos,0},
      {0,0,0,1.0}});

    return rotated_x;
  }

  public Matrix combined_rotation(double angle){
    Matrix Rz = rotateZ(angle);
    Matrix Ry = rotateY(angle);
    Matrix Rx = rotateX(angle);

    return Rz.matrix_mul(Ry).matrix_mul(Rx);
  }

public Matrix project(double fov, double aspect, double near, double far) {
    double f = 1.0/Math.tan(Math.toRadians(fov)/2.0);

    Matrix project = new Matrix(new double[][] {
      {(f/aspect), 0, 0, 0},
      {0,f,0,0},
      {0, 0,((far + near)/(far - near)), 1},
      {0,0,-((far*near)/(far - near)),0}
    });

    Matrix projected = this.matrix_mul(project);

    for (int i = 0; i < projected.rows; i++) { // divide by w which is just 
        double w = projected.data[i][3];
        if (w != 0.0) {
            projected.data[i][0] /= w;
            projected.data[i][1] /= w;
            projected.data[i][2] /= w;
        }
      }
    return projected;
  }

  public static void main(String[] args){
      
  }
}
