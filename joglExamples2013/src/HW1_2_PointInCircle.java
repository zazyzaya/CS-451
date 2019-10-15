//import net.java.games.jogl.GL;
//import net.java.games.jogl.GLDrawable; 
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**
 * 
 * 
 * <p>
 * Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D
 * </p>
 * 
 * <p>
 * Description: a point bounces in a circle ...
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * 
 * <p>
 * Company: George Mason University
 * </p>
 * 
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
public class HW1_2_PointInCircle extends HW1_1_PointOnCirlcle {
	static final int pnum = 100; // number of points
	int cnt = 1; 

	double direction[][] = new double[pnum][3]; // direction vectors
	double point[][] = new double[pnum][3]; // points
	double clr[][] = new double[pnum][3]; // colors of the points

	public HW1_2_PointInCircle() {

		for (int i = 0; i < pnum; i++) {
			direction[i][0] = Math.random()-0.5;
			direction[i][1] = Math.random()-0.5;
			direction[i][2] = 0;
			normalize(direction[i]); // make it a unit vector

			// randomly generated colors
			clr[i][0] = Math.random();
			clr[i][1] = Math.random();
			clr[i][2] = Math.random();

			// generate a point inside the circle
			do {
				point[i][0] = Math.random() * WIDTH;
				point[i][1] = Math.random() * HEIGHT;
				point[i][2] = 0;
			} while (r < distance(cx, cy, point[i][0], point[i][1]));
		}
	}

	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {

		cnt++; 
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		// draw a white circle
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		gl.glPointSize(2);
		drawCircle(cx, cy, r);

		gl.glPointSize(4);
		for (int i = 0; i < pnum; i++) {
			// move in a direction
			point[i][0] = point[i][0] + direction[i][0];
			point[i][1] = point[i][1] + direction[i][1];

			// bounce when point on/outside the circle
			if (r <= distance(cx, cy, point[i][0], point[i][1])) {

				// change the direction of the point around r
				bounceInCircle(point[i], direction[i]);
			}
			
			// draw a colored point in Circle
			gl.glColor3dv(clr[i], 0);  // specify the color
			//drawPoint(point[i][0], point[i][1]);
			
			gl.glPushMatrix(); 
			//gl.glTranslated(WIDTH/2, HEIGHT/2, 0); 
			//gl.glRotated(cnt, 0, 0, 1); 
			//gl.glTranslated(-WIDTH/2, -HEIGHT/2, 0); 
			gl.glTranslated(point[i][0], point[i][1], 0); 
			drawPoint(0,0); 
			gl.glPopMatrix(); 
		}
		// sleep to slow down the rendering
		try {
			Thread.sleep(10);
		} catch (Exception ignore) {
		}
	}

	public void bounceInCircle(double point[], double direction[]) {
		double n[] = new double[3];
		double rd[] = new double[3];

		//n: a vector from point to origin
		n[0] =  cx - point[0];
		n[1] = cy - point[1];
		n[2] = 0;
		normalize(n);

		for (int i = 0; i < 3; i++) {
			rd[i] = -direction[i];
			point[i] += n[i]; // so the point remains in circle
		}
		reflect(rd, n, direction); 
	}


	public static void main(String[] args) {
		HW1_2_PointInCircle f = new HW1_2_PointInCircle();

		f.setTitle("HW1_2 - points bounce in a circle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);

	}
}
