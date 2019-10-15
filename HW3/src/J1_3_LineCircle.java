/*
 * Created on 2008-1-7
 * @author Jim X. Chen: draw a circle 
 */
import javax.media.opengl.*;
//import net.java.games.jogl.*;


public class J1_3_LineCircle extends J1_3_Line {

  // called for OpenGL rendering every reshape
  public void display(GLAutoDrawable drawable) {

    // a blue circle 
	gl.glColor3f(0, 1, 1);
    circle(WIDTH/2, HEIGHT/2, HEIGHT/3);
  }


  // Our circle algorithm: center (cx, cy); radius: r
  void circle(double cx, double cy, double r) {
    double xn, yn, theta = 0;
    double delta = 10/r; // the delta angle for a line segment: 100 pixels
    double x0 = r*Math.cos(theta)+cx;
    double y0 = r*Math.sin(theta)+cy;


    while (theta<2*Math.PI) {
    	theta = theta + delta; 
         xn = r*Math.cos(theta)+cx;
         yn = r*Math.sin(theta)+cy;
         bresenhamLine((int)x0, (int)y0, (int)xn, (int)yn);
   	     x0 = xn; 
   	     y0 = yn; 
    }
  }


  public static void main(String[] args) {
    J1_3_LineCircle f = new J1_3_LineCircle();

    f.setTitle("JOGL J1_3_CircleLine");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }
}

