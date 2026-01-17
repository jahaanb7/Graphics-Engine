
public class MyMeshes{
  public static Mesh cube;

  static {
    double size = 10.0; //size of the length of the cube
    cube = new Mesh();

    System.out.println("Mesh created: " + cube);
    System.out.println("Cube tris before add: " + cube.tris);

    Vector3D v0 = new Vector3D(-size, -size, -size);
    Vector3D v1 = new Vector3D(-size, -size,  size);
    Vector3D v2 = new Vector3D(-size,  size, -size);
    Vector3D v3 = new Vector3D(-size,  size,  size);
    Vector3D v4 = new Vector3D( size, -size, -size);
    Vector3D v5 = new Vector3D( size, -size,  size);
    Vector3D v6 = new Vector3D( size,  size, -size);
    Vector3D v7 = new Vector3D( size,  size,  size);
    System.out.println("Debug: success");


    // Front face
    cube.tris.add(new Triangle(v0, v1, v2));
    cube.tris.add(new Triangle(v1, v3, v2));

    // Back face
    cube.tris.add(new Triangle(v4, v6, v5));
    cube.tris.add(new Triangle(v5, v6, v7));

    // Left face
    cube.tris.add(new Triangle(v0, v2, v4));
    cube.tris.add(new Triangle(v4, v2, v6));

    // Right face
    cube.tris.add(new Triangle(v1, v5, v3));
    cube.tris.add(new Triangle(v5, v7, v3));

    // Top face
    cube.tris.add(new Triangle(v2, v3, v6));
    cube.tris.add(new Triangle(v3, v7, v6));

    // Bottom face
    cube.tris.add(new Triangle(v0, v4, v1));
    cube.tris.add(new Triangle(v1, v4, v5));
  }
}
