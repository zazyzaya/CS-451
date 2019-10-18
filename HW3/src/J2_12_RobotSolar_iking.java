/*
 * Created on 2004-3-17
 * @author Jim X. Chen: 3D transformation/viewing
 */
//import net.java.games.jogl.*;
import com.jogamp.opengl.*;

public class J2_12_RobotSolar_iking extends
		J2_11_ConeSolarCollision_iking {

	public void reshape(
			GLAutoDrawable glDrawable, int x, int y, int w, int h) {

		WIDTH = w; HEIGHT = h;
 
		// enable zbuffer for hidden-surface removal
		gl.glEnable(GL.GL_DEPTH_TEST);

		// specify the drawing area within the frame window
		gl.glViewport(0, 0, w, h);

		//1. make sure the cone is within the viewing volume
		projection.pop();
		projection.pushFrustum(-w/2, w/2, -h/2, h/2, -w, w); // look at z near and far
		projection.translate(0, 0, -2*w);

		
		//2. This will enable depth test in general
	 	gl.glEnable(GL.GL_DEPTH_TEST);
	 	gl.setSwapInterval(1);

	}


	public void display(GLAutoDrawable glDrawable) {

		cnt++;
		depth = (cnt/50)%7;

		gl.glClear(GL.GL_COLOR_BUFFER_BIT|
							 GL.GL_DEPTH_BUFFER_BIT);

		if (cnt%60==0) {
			dalpha = -dalpha;
			dbeta = -dbeta;
			dgama = -dgama;
		}
		alpha += dalpha;
		beta += dbeta;
		gama += dgama;

		drawRobot(O, A, B, C, alpha, beta, gama);

	}


	void drawRobot (
			float O,
			float A,
			float B,
			float C,
			float alpha,
			float beta,
			float gama) {

		// Global coordinates
		gl.glLineWidth(4);
		drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);

		modelView.push();

		modelView.rotateDegrees(cnt, 0, 1, 0);
		modelView.rotateDegrees(alpha, 0, 0, 1);
		// R_z(alpha) is on top of the matrix stack
		drawArm(O, A);

		modelView.translate(A, 0, 0);
		modelView.rotateDegrees(beta, 0, 0, 1);
		// R_z(alpha)T_x(A)R_z(beta) is on top of the stack
		drawArm(A, B);
//		drawSolar(WIDTH/4, 2.5f*cnt, WIDTH/6, 1.5f*cnt);

		modelView.translate(B-A, 0, 0);
		modelView.rotateDegrees(gama, 0, 0, 1);
		// R_z(alpha)T_x(A)R_z(beta)T_x(B)R_z(gama) is on top
		drawArm(B, C);

		// put the solar system at the end of the robot arm
		modelView.translate(C-B, 0, 0);
		drawSolar(WIDTH/4, 2.5f*cnt, WIDTH/6, 1.5f*cnt);

		modelView.pop();
	}


	public static void main(String[] args) {
		J2_12_RobotSolar_iking f = new J2_12_RobotSolar_iking();

		f.setTitle("JOGL J2_12_RobotSolar");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
