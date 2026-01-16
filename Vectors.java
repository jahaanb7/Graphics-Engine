import java.awt.Color;

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

  public static Vector3D cross(Vector3D a, Vector3D b){
    double x = a.y * b.z - a.z * b.y;
    double y = a.z * b.x - a.x * b.z;
    double z = a.x * b.y - a.y * b.x;

    return new Vector3D(x, y, z);
  }

  public Vector3D normalize(){
    double length = Math.sqrt(x*x + y*y + z*z);
    return new Vector3D(x / length, y / length, z / length);
  }
}

class Triangle{
  public Vector3D v1;
  public Vector3D v2;
  public Vector3D v3;

  public Color color;
  public Vector3D normal;

  public Triangle(Vector3D v1, Vector3D v2, Vector3D v3, Color color){
    this.v1 = v1;
    this.v2 = v2;
    this.v3 = v3;

    this.color = color;
    this.normal = new Vector3D(0,0,0);
  }

  public void setNormal(Vector3D n){
    this.normal = n;
  }

  public void computeNormal() {
    Vector3D edge1 = v2.sub(v1);
    Vector3D edge2 = v3.sub(v1);
    this.normal = Vector3D.cross(edge1, edge2).normalize();
  }
}