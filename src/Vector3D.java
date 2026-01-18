class Vector3D{
  public double x;
  public double y;
  public double z;

  public Vector3D(double x, double y, double z){
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vector3D sub(Vector3D v) {
    double x = this.x - v.x;
    double y = this.y - v.y;
    double z = this.z - v.z;

    Vector3D result = new Vector3D(x, y, z);
    return result;
  }

  public Vector3D add(Vector3D v) {
    double x = this.x + v.x;
    double y = this.y + v.y;
    double z = this.z + v.z;

    Vector3D result = new Vector3D(x, y, z);
    return result;
  }

  public static Vector3D multiply(double s, Vector3D v){
    return new Vector3D(s*v.x, s*v.y, s*v.z);
  }

  public static double dot(Vector3D a, Vector3D b){
    double result = ((a.x * b.x)+(a.y * b.y)+(a.z * b.z));
    return result;
  }

  public static Vector3D cross(Vector3D a, Vector3D b){
    double x = a.y * b.z - a.z * b.y;
    double y = a.z * b.x - a.x * b.z;
    double z = a.x * b.y - a.y * b.x;

    return new Vector3D(x, y, z);
  }

  public Vector3D normalize(){
    double length = Math.sqrt(x*x + y*y + z*z);
    return new Vector3D(x/length, y/length, z/length);
  }

  public Vector4D mul(Matrix m) {
    double nx = this.x * m.data[0][0] + this.y * m.data[1][0] + this.z * m.data[2][0] + 1.0 * m.data[3][0];
    double ny = this.x * m.data[0][1] + this.y * m.data[1][1] + this.z * m.data[2][1] + 1.0 * m.data[3][1];
    double nz = this.x * m.data[0][2] + this.y * m.data[1][2] + this.z * m.data[2][2] + 1.0 * m.data[3][2];
    double nw = this.x * m.data[0][3] + this.y * m.data[1][3] + this.z * m.data[2][3] + 1.0 * m.data[3][3];

    return new Vector4D(nx, ny, nz, nw);
  }

  public static double getDeterminant(Vector3D a, Vector3D b, Vector3D p){
    Vector3D ab = new Vector3D(b.x, b.y, b.z);
    ab.sub(a);

    Vector3D ac = new Vector3D(p.x,p.y,p.z);
    ac.sub(a);

    double result = ac.y * ac.x - ab.x * ac.y;

    return result;
  }
}

class Vector4D {
  public double x, y, z, w;

  public Vector4D(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
    }

  public Vector4D mul(Matrix m) {
    double nx = this.x * m.data[0][0] + this.y * m.data[1][0] + this.z * m.data[2][0] + 1.0 * m.data[3][0];
    double ny = this.x * m.data[0][1] + this.y * m.data[1][1] + this.z * m.data[2][1] + 1.0 * m.data[3][1];
    double nz = this.x * m.data[0][2] + this.y * m.data[1][2] + this.z * m.data[2][2] + 1.0 * m.data[3][2];
    double nw = this.x * m.data[0][3] + this.y * m.data[1][3] + this.z * m.data[2][3] + 1.0 * m.data[3][3];

    return new Vector4D(nx, ny, nz, nw);
  }
}
