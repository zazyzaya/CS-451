/*
 * Created on 2004-3-9
 * @author Jim X. Chen: draw a cylinder by subdivision
 */
//import net.java.games.jogl.*;
import com.jogamp.opengl.*;

public class J2_6_Cylinder_iking extends J2_5_Cone_iking {

	public void display(GLAutoDrawable glDrawable) {

		if ((cRadius>(WIDTH/2))||(cRadius<=1)) {
			flip = -flip;
			depth++;
			depth = depth%7;
		}

		cRadius += flip;

		// clear both framebuffer and zbuffer
		gl.glClear(GL.GL_COLOR_BUFFER_BIT|
							 GL.GL_DEPTH_BUFFER_BIT);

		modelView.rotate(2*PI/360, 2*PI/360, 2*PI/360);
		// rotate 1 degree alone vector (1, 1, 1)
		modelView.push();
		modelView.scale(cRadius, cRadius, cRadius);
		drawCylinder();
		modelView.pop();
	}


	private void subdivideCylinder(float v1[],
		float v2[], int depth) {
		float v11[] = {0, 0, 0};
		float v22[] = {0, 0, 0};
		float v0[] = {0, 0, 0};
		float v12[] = new float[3];

		int i;

		if (depth==0) {
			float[] color = { v1[0]*v1[0], v1[1]*v1[1], v1[2]*v1[2] };

			for (i = 0; i<3; i++) {
				v22[i] = v2[i];
				v11[i] = v1[i];
			}

			prepareToDrawTriangle(v2, v1, v0, color);
			// draw sphere at the cylinder's bottom

			v11[2] = v22[2] = v0[2] = 1.0f;
			prepareToDrawTriangle(v11, v22, v0, color);
			// draw sphere at the cylinder's bottom

			
			// draw the side rectangles of the cylinder
			prepareToDrawTriangle(v1, v2, v22, color);
			prepareToDrawTriangle(v22, v11, v1, color);
			
			/*
			gl.glBegin(GL.GL_POLYGON);
			gl.glVertex3fv(v1,0);
			gl.glVertex3fv(v2,0);
			gl.glVertex3fv(v22,0);
			gl.glVertex3fv(v11,0);
			gl.glEnd();
			*/

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
		drawTriangle();
	}


	public static void main(String[] args) {
		J2_6_Cylinder_iking f = new J2_6_Cylinder_iking();

		f.setTitle("JOGL J2_6_Cylinder");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
