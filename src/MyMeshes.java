public class MyMeshes {
  public static Mesh cube;

  static {
    double size = 1.0; // length
    cube = new Mesh();

    Vector3D v0 = new Vector3D(-size, -size, -size);
    Vector3D v1 = new Vector3D(-size, -size,  size);
    Vector3D v2 = new Vector3D(-size,  size, -size);
    Vector3D v3 = new Vector3D(-size,  size,  size);
    Vector3D v4 = new Vector3D( size, -size, -size);
    Vector3D v5 = new Vector3D( size, -size,  size);
    Vector3D v6 = new Vector3D( size,  size, -size);
    Vector3D v7 = new Vector3D( size,  size,  size);

    // Front (+Z)
    cube.tris.add(new Triangle(v1, v3, v5));
    cube.tris.add(new Triangle(v3, v7, v5));

    // Back (-Z)
    cube.tris.add(new Triangle(v4, v6, v0));
    cube.tris.add(new Triangle(v6, v2, v0));

    // Left (-X)
    cube.tris.add(new Triangle(v0, v2, v1));
    cube.tris.add(new Triangle(v2, v3, v1));

    // Right (+X)
    cube.tris.add(new Triangle(v5, v7, v4));
    cube.tris.add(new Triangle(v7, v6, v4));

    // Top (+Y)
    cube.tris.add(new Triangle(v2, v6, v3));
    cube.tris.add(new Triangle(v6, v7, v3));

    // Bottom (-Y)
    cube.tris.add(new Triangle(v1, v5, v0));
    cube.tris.add(new Triangle(v5, v4, v0));
  }
}
