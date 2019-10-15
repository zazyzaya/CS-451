//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**


 * <p>Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D </p>
 *
 * <p>Description: Draw triangle with generated patterns ...</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: George Mason University</p>
 *
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
public class HW1_6_TrianglePattern extends HW1_5_Clipping {
 
  int v[][] = new int[3][3];


  public void display(GLAutoDrawable drawable) {

    super.display(drawable);

    // generate a random triangle and display

    if (cnt % 100 < 1) {// generate a triangle 
    	for (int i = 0; i<3; i++) { // three vertices
    		v[i][0] = (int)(WIDTH*Math.random());
    		v[i][1] = (int)(HEIGHT*Math.random());
    		v[i][2] = 0;
    	}
    }
    else  {
  	    for (int i = 0; i<3; i++) { //float the triangle 
		      v[i][0]++;
		      v[i][1]++;
		      //labelVertex(v[i][0]+5, v[i][1], i);
		}
  	    drawtriangle(v); // scan-convert triangle
    }
  }



  void span(int x2, int x1, int y) {
		  
	if (x1 > x2) {
			int tmp = x2;
			x2 = x1;
			x1 = tmp;
	}
    for (int x = x1; x<x2; x++) {
    	if (cnt % 100 < 50) // triangle pattern anchored on frame
    		gl.glColor3d(Math.sin(x/5), Math.cos(x/5), 1);
    	else // triangle pattern anchored on triangle coordinates
    		gl.glColor3d(Math.sin((x-x1)/5), Math.cos((x-x1)/5), 1);
      drawPoint(x, y);
    }
  }
  



  public static void main(String[] args) {
	  HW1_6_TrianglePattern f = new HW1_6_TrianglePattern();

    f.setTitle("HW1_7_Triangle - draw triangle with pattern");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }

}
