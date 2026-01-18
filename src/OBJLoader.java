import java.io.*;
import java.util.*;

public class OBJLoader {

  /**
   * Loads an OBJ file and returns a list of Triangles
   * @param filename path to the OBJ file
   * @return ArrayList of Triangle objects
   */
  
  public static ArrayList<Triangle> loadOBJ(String filename) {

      ArrayList<Vector3D> vertices = new ArrayList<>();
      ArrayList<Triangle> triangles = new ArrayList<>();

      try (BufferedReader b_read = new BufferedReader(new FileReader(filename))){

          String line;

          while ((line = b_read.readLine()) != null){ //until there is something to read
              line = line.trim(); //remove any whitespaces

              if (line.startsWith("v ")) { //check for vertices

                  // Vertex line: v --> {x y z}
                  String[] tokens = line.split("\\s+"); //tokenize

                  double x = Double.parseDouble(tokens[1]);
                  double y = Double.parseDouble(tokens[2]);
                  double z = Double.parseDouble(tokens[3]);

                  vertices.add(new Vector3D(x, y, z)); //add to vertices array as a new Vector3D
              }

              else if (line.startsWith("f ")) { //check for faces to join the vertices in the given order

                  // Face line: f --> {i j k} 
                  String[] tokens = line.split("\\s+");

                  // btw: indexing obj files means that vertices/faces are indexed from 1 to n

                  int idx1 = Integer.parseInt(tokens[1].split("/")[0]) - 1; // subtract by one to convert the one-index starting to zero-index
                  int idx2 = Integer.parseInt(tokens[2].split("/")[0]) - 1; // "/" means to ignore/split everything after '/' which is usually normals and textures
                  int idx3 = Integer.parseInt(tokens[3].split("/")[0]) - 1; // do the same for the three vertices (idx1, idx2, idx3)

                  Vector3D v1 = vertices.get(idx1);
                  Vector3D v2 = vertices.get(idx2);
                  Vector3D v3 = vertices.get(idx3);

                  triangles.add(new Triangle(v1, v2, v3)); // add the 
              }
          }
      } catch (IOException e) {
          e.printStackTrace();
    }
      return triangles;
  }
}
