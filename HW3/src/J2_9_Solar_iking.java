/*
 * Created on 2004-3-12
 * @author Jim X. Chen: draw a simplified solar system
 */
//import net.java.games.jogl.*;
//import net.java.games.jogl.util.*;

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.*;


public class J2_9_Solar_iking extends J2_8_Robot3d_iking {
	static boolean coordOff=false; // cjx for images
	private float[] coordColors = {
			0, 1f, 0, 1f,
			0, 1f, 0, 1f,
			1f, 0, 0, 1f,
			1f, 0, 0, 1f,
			0, 0.3f, 1f, 1f,
			0, 0.3f, 1f, 1f
	};
	
	public void display(GLAutoDrawable glDrawable) {

		depth = (cnt/100)%7;
		cnt++;

		gl.glClear(GL.GL_COLOR_BUFFER_BIT|
							 GL.GL_DEPTH_BUFFER_BIT);

		drawSolar(WIDTH/4, 2*cnt, WIDTH/12, cnt);
	}


	public void drawColorCoord(float xlen, float ylen, float zlen) {
		// Specify points to send to VBO
		float[] coordPoints = {
				xlen, 0, 0, 1f,
				0, 0, 0, 1f, 
				0, ylen, 0, 1f,
				0, 0, 0, 1f,
				0, 0, zlen, 1f,
				0, 0, 0, 1f,
		};
		
		vPoints = coordPoints;
		vColors = coordColors;
		
		// Draw
		loadPoints();
		gl.glDrawArrays(GL_LINES, 0, vPoints.length / 4);
	}


	void drawSolar(float E, float e, float M, float m) {

		drawColorCoord(WIDTH/4, WIDTH/4, WIDTH/4);
		
		modelView.push();
		
		modelView.push();
		modelView.scale(WIDTH/20f, WIDTH/20f, WIDTH/20f);
		drawSphere();
		modelView.pop();


		modelView.rotateDegrees(e, 0.0f, 1.0f, 0.0f);
		// rotating around the "sun"; proceed angle
		modelView.translate(E, 0.0f, 0.0f);

		drawColorCoord(WIDTH/6, WIDTH/6, WIDTH/6);
		modelView.push();
		modelView.scale(WIDTH/20f, WIDTH/20f, WIDTH/20f);
		modelView.rotateDegrees(e+m, 0.0f, 1.0f, 0.0f); //earth's self rotation
		drawSphere();
		modelView.pop();

		modelView.rotateDegrees(m, 0.0f, 1.0f, 0.0f);
		// rotating around the "earth"
		modelView.translate(M, 0.0f, 0.0f);
		drawColorCoord(WIDTH/8f, WIDTH/8f, WIDTH/8f);
		modelView.scale(WIDTH/40f, WIDTH/40f, WIDTH/40f);
		drawSphere();

		modelView.pop();
	}


	public static void main(String[] args) {
		J2_9_Solar_iking f = new J2_9_Solar_iking();

		f.setTitle("JOGL J2_9_Solar");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
