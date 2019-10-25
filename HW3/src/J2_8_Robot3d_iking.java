/*
 * Created on 2004-3-12
 * @author Jim X. Chen: 3D three segments arm transformation
 */
//import net.java.games.jogl.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.GL4.GL_FILL;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

public class J2_8_Robot3d_iking extends J2_7_Sphere_iking {

	float O = 0;
	float A = (float)0.3*WIDTH;
	float B = (float)0.55*WIDTH;
	float C = (float)0.7*WIDTH;

	public void display(GLAutoDrawable glDrawable) {
		depth = (cnt/100)%7;
		cnt++;
		alpha += dalpha;
		beta += dbeta;
		gama += dgama;

		gl.glClear(GL.GL_COLOR_BUFFER_BIT|
							 GL.GL_DEPTH_BUFFER_BIT);
		drawRobot(O, A, B, C, alpha, beta, gama);
	}


	void drawArm(float End1, float End2) {

		float scale = End2-End1;

		modelView.push();
		// the cylinder lies in the z axis;
		// rotate it to lie in the x axis
		modelView.rotateDegrees(-90.0f, 0.0f, 1.0f, 0.0f);
		modelView.scale(scale/5.0f, scale/5.0f, scale);
		
		if (cnt%1500<500) {
			drawCylinder();
		} else if (cnt%1500<1000) {
			drawCone();
		} else {
			modelView.scale(0.5f, 0.5f, 0.5f);
			modelView.translate(0, 0, 1);
			drawSphere();
		}
		modelView.pop();
	}


	void drawRobot(float O, float A, float B, float C,
								 float alpha, float beta, float gama) {
		// the robot arm is rotating around y axis
		modelView.rotateDegrees(5.0f, 0.0f, 1.0f, 0.0f);
		modelView.push();

		modelView.rotateDegrees(alpha, 0.0f, 0.0f, 1.0f);
		drawArm(O, A);
		// R_z(alpha) is on top of the matrix stack
		

		modelView.translate(A, 0.0f, 0.0f);
		modelView.rotateDegrees(beta, 0.0f, 0.0f, 1.0f);
		// R_z(alpha)T_x(A)R_z(beta) is on top of the stack
		drawArm(A, B);

		modelView.translate(B-A, 0.0f, 0.0f);
		modelView.rotateDegrees(gama, 0.0f, 0.0f, 1.0f);
		// R_z(alpha)T_x(A)R_z(beta)T_x(B)R_z(gama) is on top
		drawArm(B, C);

		modelView.pop();
	}


	public static void main(String[] args) {
		J2_8_Robot3d_iking f = new J2_8_Robot3d_iking();

		f.setTitle("JOGL J2_8_Robot3d");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
