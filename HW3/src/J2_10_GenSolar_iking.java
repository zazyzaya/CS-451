/*
 * Created on 2004-3-12
 * @author Jim X. Chen: draw a generalized solar system
 */
//import net.java.games.jogl.*;
import com.jogamp.opengl.*;
import static com.jogamp.opengl.GL.GL_LINES;

public class J2_10_GenSolar_iking extends J2_9_Solar_iking {
	static float tiltAngle = 45;

	void drawSolar(float earthDistance,
								 float earthAngle,
								 float moonDistance,
								 float moonAngle) {
		
		// Global coordinates
		gl.glLineWidth(5);
		coordOff = false; // cjx for book images
		drawColorCoord(WIDTH/4, WIDTH/4, WIDTH/4);

		coordOff = true; // cjx for book images

		
		modelView.push();
		modelView.rotateDegrees(earthAngle, 0.0f, 1.0f, 0.0f);
		// rotating around the "sun"; proceed angle
		modelView.rotateDegrees(tiltAngle, 0.0f, 0.0f, 1.0f);
		
		// tilt angle, angle between the center line and y axis	
		vPoints = new float[] {
				0.0f, 0.0f, 0.0f, 1.0f,
				0.0f, earthDistance, 0.0f, 1.0f
		};
		
		// glColor wasn't specified, so just using white
		loadPoints(new float[] {1f, 1f, 1f, 1f} );
		gl.glDrawArrays(GL_LINES, 0, vPoints.length / 4);
		
		modelView.translate(0.0f, earthDistance, 0.0f);
		// cjx gl.glLineWidth(3);
		modelView.push();
		drawColorCoord(WIDTH/6, WIDTH/6, WIDTH/6);
		modelView.scale(WIDTH/20, WIDTH/20, WIDTH/20);
		drawSphere();
		modelView.pop();

		modelView.rotateDegrees(moonAngle, 0.0f, 1.0f, 0.0f);
		// rotating around the "earth"
		modelView.translate(moonDistance, 0.0f, 0.0f);
		gl.glLineWidth(3);
		drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);
		modelView.scale(WIDTH/40, WIDTH/40, WIDTH/40);
		drawSphere();
		modelView.pop();
	}


	public static void main(String[] args) {

		J2_10_GenSolar_iking f = new J2_10_GenSolar_iking();

		f.setTitle("JOGL J2_10_GenSolar");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
