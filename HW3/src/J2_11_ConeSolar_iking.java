/*
 * Created on 2004-3-17
 * @author Jim X. Chen: draw a cone solar system
 */
public class J2_11_ConeSolar_iking extends J2_10_GenSolar_iking {

	void drawSolar(float E, float e, float M, float m) {

		// Global coordinates
		gl.glLineWidth(5);
		//coordOff = false; // cjx
		drawColorCoord(WIDTH/4, WIDTH/4, WIDTH/4);
		//coordOff = true; // cjx
		modelView.push();
		modelView.rotateDegrees(e, 0.0f, 1.0f, 0.0f);
		// rotating around the "sun"; proceed angle
		modelView.rotateDegrees(tiltAngle, 0.0f, 0.0f, 1.0f); // tilt angle
		modelView.translate(0.0f, E, 0.0f);
		modelView.push();
		modelView.scale(WIDTH/20, WIDTH/20, WIDTH/20);
		drawSphere();
		modelView.pop();
		modelView.push();
		modelView.scale(E/8, E, E/8);
		modelView.rotateDegrees(90, 1.0f, 0.0f, 0.0f); // orient the cone
		drawCone();
		modelView.pop();

		modelView.rotateDegrees(m, 0.0f, 1.0f, 0.0f);
		// rotating around the "earth"
		modelView.translate(M, 0.0f, 0.0f);
		gl.glLineWidth(3);
		drawColorCoord(WIDTH/8, WIDTH/8, WIDTH/8);
		modelView.scale(E/8, E/8, E/8);
		drawSphere();
		modelView.pop();
	}


	public static void main(String[] args) {

		J2_11_ConeSolar_iking f = new J2_11_ConeSolar_iking();

		f.setTitle("JOGL J2_11_ConeSolar");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
