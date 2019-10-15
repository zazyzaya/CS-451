/*
 * Created on 2004-2-27
 * @author Jim X. Chen: animate a circle
 */
import javax.media.opengl.*;

// import net.java.games.jogl.*;

public class HW4_Jchen_Rotate extends J1_5_Circle {
	static int depth = 0; // number of subdivisions
	static int cRadius = 2, flip = 1;

	// vertex data for the triangles
	static float cVdata[][] = { { 0.0f, 0.0f, 0.0f }, { 1.0f, -1.0f, 0.0f },
			{ 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, -1.0f, 0.0f } };


	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {

		super.reshape(drawable, x, y, w, h); 
		
		//1. specify drawing into only the back_buffer
		gl.glDrawBuffer(GL.GL_BACK); 
		gl.glEnable(GL.GL_DEPTH_TEST);
		   
		//2. origin at the center of the drawing area
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-w / 2, w / 2, -h / 2, h / 2, -w, w);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity(); 

		//3. interval to swap buffers to avoid rendering too fast
		gl.setSwapInterval(1);
	}

	// Called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {

		// when the circle is too big or small, change
		// the direction (growing or shrinking)
		if (cRadius >= (HEIGHT / 2) || cRadius == 1) {
			//flip = -flip;
			depth++; // number of subdivisions
			depth = depth % 8;
		}
		cRadius += flip; // circle's radius change

		//4. clear the framebuffer and draw a new circle
	    gl.glClear(GL.GL_COLOR_BUFFER_BIT|
	               GL.GL_DEPTH_BUFFER_BIT);
		gl.glColor3f(1, 1, 1); 
		
		gl.glPushMatrix(); 
			gl.glRotatef(cRadius, 0, 0, 1); 
			gl.glRotatef(cRadius, 1, 0, 0); 
			gl.glTranslatef(-WIDTH/4, 0, 0);
			gl.glScalef(WIDTH/2, WIDTH/8, WIDTH/8);
			drawtriangle(cVdata[0], cVdata[1], cVdata[2]); 
		gl.glPopMatrix(); 
		/*
		gl.glLoadIdentity(); 
*/
		gl.glPushMatrix(); 
		gl.glRotatef(cRadius, 0, 1, 0); 
		gl.glRotatef(cRadius, 1, 0, 0); 
		gl.glTranslatef(-WIDTH/4, 0, 0);
		gl.glRotatef(45, 1, 0, 0); 
		gl.glScalef(WIDTH/2, WIDTH/16, WIDTH/16);
		drawColorBox(cVdata[1], cVdata[2], cVdata[3], cVdata[4]); 
		gl.glPopMatrix(); 

		try {
			Thread.sleep(10);
		} catch (Exception ignore) {
		}
	}

	  private void subdivideCylinder(float v1[],
			  float v2[], int depth) {
		  float v11[] = {0, 0, 0};
		  float v22[] = {0, 0, 0};
		  float v0[] = {0, 0, 0};
		  float v12[] = new float[3];

	    int i;

	    if (depth==0) {
	      gl.glColor3d(v1[0]*v1[0], v1[1]*v1[1], v1[2]*v1[2]);

	      for (i = 0; i<3; i++) {
	        v22[i] = v2[i];
	        v11[i] = v1[i];
	      }

	      //drawtriangle(v2, v1, v0);
	      // draw sphere at the cylinder's bottom

	      v11[2] = v22[2] = v0[2] = 1.0f;
	     // drawtriangle(v11, v22, v0);
	      // draw sphere at the cylinder's bottom


	      gl.glBegin(GL.GL_POLYGON);
	      // draw the side rectangles of the cylinder
	      gl.glVertex3fv(v1,0);
	      gl.glVertex3fv(v2,0);
	      gl.glVertex3fv(v22,0);
	      gl.glVertex3fv(v11,0);
	      gl.glEnd();


	      return;
	    }

	    for (i = 0; i<3; i++) {
	      v12[i] = v1[i]+v2[i];

	    }
	    normalize(v12);

	    subdivideCylinder(v1, v12, depth-1);
	    subdivideCylinder(v12, v2, depth-1);
	  }


	  public void drawCylinder() {

	    subdivideCylinder(cVdata[0], cVdata[1], depth);
	    subdivideCylinder(cVdata[1], cVdata[2], depth);
	    subdivideCylinder(cVdata[2], cVdata[3], depth);
	    subdivideCylinder(cVdata[3], cVdata[0], depth);
	  }

	
	
	// draw a circle with center at the origin in xy plane
	public void drawCyl(int cRadius, int depth) {

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

			//6. specify a color related to triangle location
			gl.glColor3d(v1[0] * v1[0], v1[1] * v1[1], v1[2] * v1[2]);

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
	
	public void drawColorBox(float[] v1, float[] v2, float[] v3, float[] v4) {
		gl.glColor3f(1, 0, 0); 
		gl.glPushMatrix(); 
		gl.glRotatef(90, 1, 0, 0); 
		gl.glTranslatef(0, 0, -1);
		drawrec(cVdata[1], cVdata[2], cVdata[3], cVdata[4]); 
		gl.glPopMatrix();
		
		gl.glColor3f(0, 1, 0); 
		gl.glPushMatrix(); 
		gl.glTranslatef(0, 0, -1);
		drawrec(cVdata[1], cVdata[2], cVdata[3], cVdata[4]); 
		gl.glPopMatrix();
	
		gl.glColor3f(0, 0, 1); 
		gl.glPushMatrix(); 
		gl.glRotatef(90, 1, 0, 0); 
		gl.glTranslatef(0, 0, 1);
		drawrec(cVdata[1], cVdata[2], cVdata[3], cVdata[4]); 
		gl.glPopMatrix();
	
		gl.glColor3f(1, 1, 0); 
		gl.glPushMatrix(); 
		gl.glTranslatef(0, 0, 1);
		drawrec(cVdata[1], cVdata[2], cVdata[3], cVdata[4]); 
		gl.glPopMatrix();
	}
	
	public void drawrec(float[] v1, float[] v2, float[] v3, float[] v4) {
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3fv(v1, 0);
		gl.glVertex3fv(v2, 0);
		gl.glVertex3fv(v3, 0);
		gl.glVertex3fv(v4, 0);
		gl.glEnd();
	}

	
	
	public static void main(String[] args) {
		HW4_Jchen_Rotate f = new HW4_Jchen_Rotate();

		f.setTitle("JOGL J1_5_Circle");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}

}
