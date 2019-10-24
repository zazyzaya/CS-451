/*
 * Created on 2004-11-16
 * @author Jim X. Chen: simulate gluLookAt and display in multiple viewports
 */
//import net.java.games.jogl.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;

import java.lang.Math;

public class J2_15_LookAt_iking extends J2_14_Perspective_iking {
  GLU glu = new GLU(); // interface to GLU library

  public void display(GLAutoDrawable glDrawable) {

    cnt++;
    depth = (cnt/50)%6;

   if (cnt%60==0) {
     dalpha = -dalpha;
     dbeta = -dbeta;
     dgama = -dgama;
   }

   alpha += dalpha;
   beta += dbeta;
   gama += dgama;

   gl.glClear(GL.GL_COLOR_BUFFER_BIT|
               GL.GL_DEPTH_BUFFER_BIT);

    viewPort1();
    drawSolar(WIDTH, cnt, WIDTH/3, cnt);
    // the objects' centers are retrieved from above call

    viewPort2();
    drawSolar(WIDTH/4, cnt, WIDTH/12, cnt);

    viewPort3();
    drawSolar(WIDTH/4, cnt, WIDTH/12, cnt);

    viewPort4();
    drawRobot(O, A, B, C, alpha, beta, gama);
	try {
		Thread.sleep(10);
	} catch (Exception ignore) {
	}
  }


  public void viewPort1() {
    int w = WIDTH, h = HEIGHT;

    gl.glViewport(0, 0, w/2, h/2);

    // use a different projection
    projection.pop();
    projection.pushOrtho(-w/2, w/2, -h/2, h/2, -w, w);
    projection.scale(1/2f);

  }


  public void viewPort2() {
    int w = WIDTH, h = HEIGHT;

    gl.glViewport(w/2, 0, w/2, h/2);

    projection.pop();
    projection.pushFrustum(-w/8, w/8, -h/8, h/8, w/2, 4*w);
    projection.translate(0, 0, -2*w);

    // earthC retrieved in drawSolar() before viewPort2
    glu.gluLookAt(5*earthC[0], 5*earthC[1], 5*earthC[2], 
             0, 0, 0, 0, 1, 0);
  }


  public void viewPort3() {
    int w = WIDTH, h = HEIGHT;

    gl.glViewport(w/2, h/2, w/2, h/2);

    projection.pop();
    // make sure the cone is within the viewing volume
    projection.pushFrustum(-w/8, w/8, -h/8, h/8, w/2, 4*w);
    projection.translate(0, 0, -2*w);

    // earthC retrieved in drawSolar() before viewPort3
    mygluLookAt(cylinderC[0], cylinderC[1], cylinderC[2],
                earthC[0], earthC[1], earthC[2],
                earthC[0], earthC[1], earthC[2]);
    
    
  }


  public void viewPort4() {

    int w = WIDTH, h = HEIGHT;

    gl.glViewport(0, h/2, w/2, h/2);

    // implemented in superclass J2_14_Perspective
    myPerspective(40, w/h, w/2, 4*w);
    // this is the default look
    glu.gluLookAt(0, 0, 0, 0, 0, -1, 0, 1, 0);
    projection.translate(0, 0, -2*w);
  }


  public void mygluLookAt(
      double eX, double eY, double eZ,
      double cX, double cY, double cZ,
      double upX, double upY, double upZ) {
    //eye and center are points, but up is a vector

    double[] F = new double[3];
    double[] UP = new double[3];
    double[] s = new double[3];
    double[] u = new double[3];

    F[0] = cX-eX;
    F[1] = cY-eY;
    F[2] = cZ-eZ;
    UP[0] = upX;
    UP[1] = upY;
    UP[2] = upZ;
    normalize(F);
    normalize(UP);
    crossProd(F, UP, s);
    crossProd(s, F, u);

    double[] M = new double[16];

    // Switching to row-major order
    M[0] = s[0];
    M[4] = u[0];
    M[8] = -F[0];
    M[12] = 0;
    M[1] = s[1];
    M[5] = u[1];
    M[9] = -F[1];
    M[13] = 0;
    M[2] = s[2];
    M[6] = u[2];
    M[10] = -F[2];
    M[14] = 0;
    M[3] = 0;
    M[7] = 0;
    M[11] = 0;
    M[15] = 1;

    projection.multiply(M);
    projection.translate((float)-eX, (float)-eY, (float)-eZ);
  }


  public void normalize(double v[]) {

    double d = Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);

    if (d==0) {
      System.out.println("0 length vector: normalize().");
      return;
    }
    v[0] /= d;
    v[1] /= d;
    v[2] /= d;
  }


  public void crossProd(double U[],
                        double V[], double W[]) {
    // W = U X V
    W[0] = U[1]*V[2]-U[2]*V[1];
    W[1] = U[2]*V[0]-U[0]*V[2];
    W[2] = U[0]*V[1]-U[1]*V[0];
  }


  public static void main(String[] args) {
    J2_15_LookAt_iking f = new J2_15_LookAt_iking();

    f.setTitle("JOGL J2_15_LookAt");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }
}
