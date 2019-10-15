/*
 * Created on 2004-4-21
 * @author Jim X. Chen: draw bitmap and stroke characters and strings
 */
import javax.media.opengl.*;
import com.sun.opengl.util.*;

//import net.java.games.jogl.*;
//import net.java.games.jogl.util.*;

public class J1_3_xFont extends J1_3_Triangle {
	static GLUT glut = new GLUT();

	// called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {
		//generate a random line;
		int x0 = (int) (Math.random() * WIDTH);
		int y0 = (int) (Math.random() * HEIGHT);
		int xn = (int) ((Math.random() * WIDTH));
		int yn = (int) (Math.random() * HEIGHT);

		// draw a yellow line
		gl.glColor3d(Math.random(), Math.random(), Math.random());
		bresenhamLine(x0, y0, xn, yn);

		// character start position
		gl.glWindowPos3f(x0, y0, 0); //glRasterpos or glWindowPos
		
		// bitmap fonts that starts at (x0, y0)
		glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, 'S');
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "tart");

		// stroke fonts that starts at (xn, yn)
		gl.glPushMatrix();
		gl.glTranslatef(xn, yn, 0); // end of line position
		gl.glRotated(360*Math.random(), 0, 0, 1); // rotate
		gl.glScaled(Math.random(), Math.random(), 1); // size
		glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, 'E');
		glut.glutStrokeString(GLUT.STROKE_ROMAN, "nd");
		gl.glPopMatrix();

		try {
			Thread.sleep(1000);
		} catch (Exception ignore) {}
	}

	public static void main(String[] args) {
		J1_3_xFont f = new J1_3_xFont();

		f.setTitle("JOGL J1_3_xFont");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
