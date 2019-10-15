/*
 * Created on 2004-2-27
 * @author Jim X. Chen: animate a circle
 */
import javax.media.opengl.*;

// import net.java.games.jogl.*;

public class J1_5_Circle extends J1_4_Line {
	static int depth = 0; // number of subdivisions
	static int cRadius = 2, flip = 2;

	// vertex data for the triangles
	static float cVdata[][] = { { 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f },
			{ -1.0f, 0.0f, 0.0f }, { 0.0f, -1.0f, 0.0f } };


	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {

		super.reshape(drawable, x, y, w, h); 
		
		//1. specify drawing into only the back_buffer
		gl.glDrawBuffer(GL.GL_BACK); 

		//2. origin at the center of the drawing area
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-w / 2, w / 2, -h / 2, h / 2, -1, 1);

		// matrix operation on MODELVIEW matrix
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		//3. interval to swap buffers to avoid rendering too fast
		//   an argument of 1 causes the application to wait until 
		//   the next vertical refresh, so it will wait at least 
		//   1/60 = 17 millisecond 
		gl.setSwapInterval(1);
	}

	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {

		// when the circle is too big or small, change
		// the direction (growing or shrinking)
		if (cRadius >= (HEIGHT / 2) || cRadius <= 1) {
			flip = -flip;
			depth++; // number of subdivisions
			depth = depth % 7;
		}
		cRadius += flip; // circle's radius change

		//4. clear the framebuffer and draw a new circle
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		drawCircle(cRadius, depth);
	}

	// draw a circle with center at the origin in xy plane
	public void drawCircle(int cRadius, int depth) {

		subdivideCircle(cRadius, cVdata[0], cVdata[1], depth);
		subdivideCircle(cRadius, cVdata[1], cVdata[2], depth);
		subdivideCircle(cRadius, cVdata[2], cVdata[3], depth);
		subdivideCircle(cRadius, cVdata[3], cVdata[0], depth);
	}

	// subdivide a triangle recursively, and draw them
	private void subdivideCircle(int radius, float[] v1, float[] v2, int depth) {
		float v11[] = new float[3];
		float v22[] = new float[3];
		float v00[] = { 0, 0, 0 };
		float v12[] = new float[3];

		if (depth == 0) {

			//6. specify a color related to the triangle location
			gl.glColor3d(v1[0] * v1[0], v1[1] * v1[1], 0);

			for (int i = 0; i < 3; i++) {
				v11[i] = v1[i] * radius;
				v22[i] = v2[i] * radius;
			}
			drawtriangle(v11, v22, v00);
			return;
		}

		v12[0] = v1[0] + v2[0];
		v12[1] = v1[1] + v2[1];
		v12[2] = v1[2] + v2[2];

		normalize(v12);

		// subdivide a triangle recursively, and draw them
		subdivideCircle(radius, v1, v12, depth - 1);
		subdivideCircle(radius, v12, v2, depth - 1);
	}



	public void drawtriangle(double[] v1, double[] v2, double[] v3) {
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3dv(v1, 0);
		gl.glVertex3dv(v2, 0);
		gl.glVertex3dv(v3, 0);
		gl.glEnd();
	}

	public void drawtriangle(float[] v1, float[] v2, float[] v3) {
		gl.glBegin(GL.GL_TRIANGLES);
		gl.glVertex3fv(v1, 0);
		gl.glVertex3fv(v2, 0);
		gl.glVertex3fv(v3, 0);
		gl.glEnd();
	}
	
	
	public static void main(String[] args) {
		J1_5_Circle f = new J1_5_Circle();

		f.setTitle("JOGL J1_5_Circle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}

}
