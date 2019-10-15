/*
 * Created on 2004-3-17
 * @author Jim X. Chen: 3D transformation/viewing
 */
//import net.java.games.jogl.*;
import javax.media.opengl.*;

public class J2_13_ViewPort extends
J2_12_RobotSolar {


  public void display(GLAutoDrawable glDrawable) {

	  cnt++; 
	  
	  
	  if ((cnt % 1000) < 200) 
		  gl.glViewport(0, 0, WIDTH, HEIGHT);
	  else if ((cnt % 1000) < 400)
		  gl.glViewport(0, 0, WIDTH/2, HEIGHT/2);
	  else if ((cnt % 1000) < 600)
		  gl.glViewport(WIDTH/2, HEIGHT/2, WIDTH/2, HEIGHT/2);
		  
	  super.display(glDrawable);    

 }



  public static void main(String[] args) {
	  J2_13_ViewPort f = new J2_13_ViewPort();

    f.setTitle("JOGL J2_13_ViewPort");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }
}
