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
public class HW1_7_Antialiasing extends HW1_5_Clipping {

	// draw antialiased lines instead
	public void bresenhamLine(int x0, int y0, int xn, int yn) {
		
		antialiasedLine(x0, y0, xn, yn); 
	}
	

  public static void main(String[] args) {
    HW1_7_Antialiasing f = new HW1_7_Antialiasing();

    f.setTitle("HW1_7_Antialiasing - draw antialiased lines");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }

}
