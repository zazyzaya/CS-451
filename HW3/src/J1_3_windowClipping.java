/*
 * Created on 2008-1-21
 * @author Jim X. Chen: point clipping inside a window
 */
import javax.media.opengl.*;

import com.sun.opengl.util.*;

//import net.java.games.jogl.*;
//import net.java.games.jogl.util.*;

public class J1_3_windowClipping extends J1_3_Triangle {

	public void display(GLAutoDrawable drawable) {
		double lLeft[] = { WIDTH / 8, HEIGHT / 4, 0 };
		double uRight[] = { 7 * WIDTH / 8, 3 * HEIGHT / 4, 0 };

		super.display(drawable);

		// draw the clipping window
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex2d(lLeft[0] - 1, lLeft[1] - 1);
		gl.glVertex2d(uRight[0] + 1, lLeft[1] - 1);
		gl.glVertex2d(uRight[0] + 1, uRight[1] + 1);
		gl.glVertex2d(lLeft[0] - 1, uRight[1] + 1);
		gl.glEnd();
	}

	public void drawPoint(double x, double y) {
		
		double lLeft[] = { WIDTH / 8, HEIGHT / 4, 0 };
		double uRight[] = { 7 * WIDTH / 8, 3 * HEIGHT / 4, 0 };

		// we should have 3 clipping algorithms, for point, line, and polygon
		// drawClippedLine (x0, y0, x1, y1) {
		//    clip (x0, y0, x1, y1, x00, y00, x11, y11); 
		//    // (x00,y00) and (x11, y11) are the two end points after clipping
		//    line(x00, y00, x11, y11); 
		// }
		
		
		// clip against the window
		if (x < lLeft[0] || x > uRight[0]) {
			return;
		}
		if (y < lLeft[1] || y > uRight[1]) {
			return;
		}

		super.drawPoint(x, y);

	}

	public static void main(String[] args) {
		J1_3_windowClipping f = new J1_3_windowClipping();

		f.setTitle("JOGL J1_3_windowClipping");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
