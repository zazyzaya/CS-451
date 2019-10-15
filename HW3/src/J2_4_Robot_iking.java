/*
 * Created on 2004-3-7
 * @author Jim X. Chen: 2D robot transformation in OpenGL
 */
//import net.java.games.jogl.*;
import com.jogamp.opengl.*;

public class J2_4_Robot_iking extends J2_3_Robot2d_iking {	
	public void display(GLAutoDrawable glDrawable) {

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		alpha += dalpha/100;
		beta += dbeta/100;
		gama += dgama/100;

		gl.glLineWidth(7f); // draw a wide line for arm
		drawRobot(A, B, C, alpha, beta, gama);
	}


	void drawRobot(
			float A[],
			float B[],
			float C[],
			float alpha,
			float beta,
			float gama) {
		
		matStack.push();

		// Draw 0-A
		gl.glProgramUniform4f(vfPrograms, colorPtr, 1f, 1f, 0f, 1f);
		matStack.rotate(alpha);
		drawArm(O, A);

		// Draw A-B
		gl.glProgramUniform4f(vfPrograms, colorPtr, 0, 1f, 1f, 1f);
		matStack.translate(A[0], A[1]);
		matStack.rotate(beta);
		matStack.translate(-A[0], -A[1]);
		drawArm(A, B);

		// Draw B-C
		gl.glProgramUniform4f(vfPrograms, colorPtr, 1f, 0, 1f, 1f);
		matStack.translate(B[0], B[1]);
		matStack.rotate(gama);
		matStack.translate(-B[0], -B[1]);
		drawArm(B, C);

		matStack.pop();
	}


	public static void main(String[] args) {
		J2_4_Robot_iking f = new J2_4_Robot_iking();

		f.setTitle("JOGL J2_4_Robot");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
