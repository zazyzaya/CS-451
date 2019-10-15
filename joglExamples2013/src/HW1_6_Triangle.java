//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**


 * <p>Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D </p>
 *
 * <p>Description: What this class is about ...</p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: George Mason University</p>
 *
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
public class HW1_6_Triangle extends HW1_5_Clipping {
    int v[][] = new int[3][3];
	
  public void display(GLAutoDrawable drawable) {
   // generate a random triangle and display

        if (Math.random()*10 < 3) 
    	for (int i = 0; i<3; i++) { // three vertices
	        v[i][0] = (int)(WIDTH*Math.random());
	        v[i][1] = (int)(HEIGHT*Math.random());
	        v[i][2] = 0;
	        labelVertex(v[i][0]+5, v[i][1], i);        
    	}
    
    super.display(drawable);
    drawtriangle(v); // scan-convert triangle    
  }



  public static void main(String[] args) {
    HW1_6_Triangle f = new HW1_6_Triangle();

    f.setTitle("HW1_6_Triangle - draw triangle");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }

}
