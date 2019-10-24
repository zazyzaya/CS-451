/*
 * Created on 2004-3-17
 * @author Jim X. Chen: going backwards to the moon in generalized solar system
 */
//import net.java.games.jogl.*;
import com.jogamp.opengl.*;

public class J2_13_TravelSolar_iking extends J2_12_RobotSolar_iking {
  boolean myCameraView = false;

  public void display(GLAutoDrawable glDrawable) {

    cnt++;
    depth = (cnt/80)%7;
    gl.glClear(GL.GL_COLOR_BUFFER_BIT|
               GL.GL_DEPTH_BUFFER_BIT);

    if (cnt%60==0) {
      dalpha = -dalpha;
      dbeta = -dbeta;
      dgama = -dgama;
    }

     alpha += dalpha;
     beta += dbeta;
     gama += dgama;  
      

    modelView.push();
    if (cnt%1000<300 || myCameraView) {
      // look at the solar system from the moon
      myCamera(A, B, C, alpha, beta, gama);
    }
    drawRobot(O, A, B, C, alpha, beta, gama);
    modelView.pop();

  }


  void myCamera(
      float A,
      float B,
      float C,
      float alpha,
      float beta,
      float gama) {

    float E = WIDTH/4;
    float e = 2.5f*cnt;
    float M = WIDTH/6;
    float m = 1.5f*cnt;

    //1. camera faces the negative x axis
    modelView.rotateDegrees(-90, 0, 1, 0);

    //2. camera on positive x axis
    modelView.translate(-M*2, 0, 0);

    //3. camera rotates with the cylinder
    modelView.rotateDegrees(-cylinderm, 0, 1, 0);

    // and so on reversing the solar transformation
    modelView.translate(0, -E, 0);
    modelView.rotateDegrees(-tiltAngle, 0, 0, 1); // tilt angle
    // rotating around the "sun"; proceed angle
    modelView.rotateDegrees(-e, 0, 1, 0);

    // and reversing the robot transformation
    modelView.translate(-C+B, 0, 0);
    modelView.rotateDegrees(-gama, 0, 0, 1);
    modelView.translate(-B+A, 0, 0);
    modelView.rotateDegrees(-beta, 0, 0, 1);
    modelView.translate(-A, 0, 0);
    modelView.rotateDegrees(-alpha, 0, 0, 1);
    modelView.rotateDegrees(-cnt, 0, 1, 0);
  }


  public static void main(String[] args) {
    J2_13_TravelSolar_iking f = new J2_13_TravelSolar_iking();

    f.setTitle("JOGL J2_13_TravelSolar");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }
}
