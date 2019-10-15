//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**


 * <p>Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D </p>
 *
 * <p>Description: draw a pentagon rotating in the circle </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: George Mason University</p>
 *
 * @author Dr. Jim X. Chen
 * @version 1.0
 */

public class HW1_3_Pentagon extends HW1_2_PointInCircle {


  // Called for OpenGL rendering every reshape
  public void display(GLAutoDrawable drawable) {

    super.display(drawable);

    // delta generates about a unit length in logical coord.
    theta = theta-delta;

    // starting point
    int x0 = (int)(r*Math.cos(theta)+cx);
    int y0 = (int)(r*Math.sin(theta)+cy);

    for (int i = 1; i<=5; i++) {
      int x = (int)(r*Math.cos(theta+2*i*Math.PI/5)+cx);
      int y = (int)(r*Math.sin(theta+2*i*Math.PI/5)+cy);

      gl.glPointSize(1);
      gl.glColor3f(0.0f, 1.0f, 0.0f);
      bresenhamLine(x0, y0, x, y);
      x0 = x;
      y0 = y;

      //draw a red point on Circle
      gl.glPointSize(5);
      gl.glColor3f(1.0f, 0.0f, 0.0f);
      drawPoint(x, y);
    }
  }


  public static void main(String[] args) {
    HW1_3_Pentagon f = new HW1_3_Pentagon();

    f.setTitle("HW1_3 - rotating pentagon");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);

  }

}
