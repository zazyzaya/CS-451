/*
 * Created on 2004-3-17
 * @author Jim X. Chen: draw a cone solar system with collisions of the moons.
 */
import java.lang.Math;
import com.jogamp.opengl.*;

public class J2_11_ConeSolarCollision_iking extends J2_11_ConeSolar_iking {

  //direction and speed of rotation
  static float coneD = 1;
  static float sphereD = -1;
  static float cylinderD = 1;
  static float spherem = 180, cylinderm = 300;
  static float tmpD = 0, conem = 60;

  // centers of the objects
  static float[] earthC = new float[3];
  static float[] coneC = new float[3];
  static float[] sphereC = new float[3];
  static float[] cylinderC = new float[3];

  // current matrix on the matrix stack
  static float[] currM = new float[16];


  void drawSolar(float E, float e, float M, float m) {

  //coordOff = true; // cjx
   // Global coordinates
    gl.glLineWidth(3);
    drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);

    modelView.push();
    {
      modelView.rotateDegrees(e, 0.0f, 1.0f, 0.0f);
      // rotating around the "sun"; proceed angle
      modelView.rotateDegrees(45, 0.0f, 0.0f, 1.0f); // tilt angle
      modelView.translate(0.0f, E, 0.0f);

      modelView.push();
      modelView.scale(WIDTH/20, WIDTH/20, WIDTH/20);
      drawSphere();
      
      // retrieve the center of the cylinder
      earthC = modelView.getOrigin();
      modelView.pop();

      modelView.push();
      modelView.scale(E/8, E, E/8);
      modelView.rotateDegrees(90, 1.0f, 0.0f, 0.0f);
      // orient the cone
      drawCone();
      modelView.pop();

      modelView.push();
      cylinderm = cylinderm+cylinderD;
      modelView.rotateDegrees(cylinderm, 0.0f, 1.0f, 0.0f);
      // rotating around the "earth"
      modelView.translate(M, 0.0f, 0.0f);
      gl.glLineWidth(3);
      drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);
      modelView.scale(E/8, E/8, E/8);
      drawCylinder();
      
      // retrieve the center of the cylinder
      cylinderC = modelView.getOrigin();
      modelView.pop();

      modelView.push();
      spherem = spherem+sphereD;
      modelView.rotateDegrees(spherem, 0.0f, 1.0f, 0.0f);
      // rotating around the "earth"
      modelView.translate(M, 0.0f, 0.0f);
      drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);
      modelView.scale(E/8, E/8, E/8);
      drawSphere();
      
      // retrieve the center of the sphere
      sphereC = modelView.getOrigin();
      modelView.pop();

      modelView.push();
      conem = conem+coneD;
      modelView.rotateDegrees(conem, 0.0f, 1.0f, 0.0f);
      // rotating around the "earth"
      modelView.translate(M, 0.0f, 0.0f);
      drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);
      modelView.scale(E/8, E/8, E/8);
      drawCone();
      
      // retrieve the center of the cone
      coneC = modelView.getOrigin();
      modelView.pop();
    }
    modelView.pop();

    float dcs = distance(coneC, sphereC);
    float dccy = distance(coneC, cylinderC);
    float dcys = distance(cylinderC, sphereC);
    
    if (distance(coneC, sphereC)<E/5) {
      // collision detected, swap the rotation directions
      tmpD = coneD;
      coneD = sphereD;
      sphereD = tmpD;
    }
    if (distance(coneC, cylinderC)<E/5) {
      // collision detected, swap the rotation directions
      tmpD = coneD;
      coneD = cylinderD;
      cylinderD = tmpD;
    }
    if (distance(cylinderC, sphereC)<E/5) {
      // collision detected, swap the rotation directions
      tmpD = cylinderD;
      cylinderD = sphereD;
      sphereD = tmpD;
    }
  }


  // distance between two points
  float distance(float[] c1, float[] c2) {
    float tmp = (c2[0]-c1[0])*(c2[0]-c1[0])+
                (c2[1]-c1[1])*(c2[1]-c1[1])+
                (c2[2]-c1[2])*(c2[2]-c1[2]);

    return ((float)Math.sqrt(tmp));
  }


  public static void main(String[] args) {
    J2_11_ConeSolarCollision_iking f =
        new J2_11_ConeSolarCollision_iking();

    f.setTitle("JOGL J2_11_ConeSolarCollision");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }
}
