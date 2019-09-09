/*************************************************
 * Created on August 1, 2017, @author: Jim X. Chen
 *
 * Animate a point 
 * Animator and Uniform: 
 * 		Animator starts a thread that calls display() repetitively
 * 		Uniform sends a variable value from JOGL main program to the shader program
 *
 */


import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.FPSAnimator;


public class JOGL1_2_Animate extends JOGL1_1_PointVFfiles {
	FPSAnimator animator; // for thread that calls display() repetitively
	int vfPrograms; // handle to shader programs
	float delta=0.005f, pos=0.0f; 

	public JOGL1_2_Animate() { // it calls supers constructor first
		
		// Frame per second animator 
		animator = new FPSAnimator(canvas, 40); // 40 calls per second; frame rate
		animator.start();
		System.out.println("A thread that calls display() repeatitively.");
	}

	public void display(GLAutoDrawable drawable) {
		
		gl.glViewport(0, 0, 400, 400);
		
		// clear the display every frame
		float bgColor[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bgColorBuffer = Buffers.newDirectFloatBuffer(bgColor);
		//gl.glClearBufferfv(GL_COLOR, 0, bgColorBuffer); // clear every frame

		// gl.glDrawBuffer(GL.GL_FRONT_AND_BACK); //if you want a still image

		// pos goes from -1 to 1 back and forth
		pos += delta; 
		if (pos<=-1.0f) delta = 0.005f; 
		else if (pos>=1.0f) delta = -0.005f; 
		
		//Connect JOGL variable with shader variable by name
		int posLoc = gl.glGetUniformLocation(vfPrograms,  "sPos"); 
		gl.glProgramUniform1f(vfPrograms,  posLoc,  pos);
		
		
		gl.glPointSize(4.0f); 
		gl.glDrawArrays(GL_POINTS, 0, 1);
		
		gl.glViewport(400, 400, 800, 800);
		gl.glDrawArrays(GL_POINTS, 0, 1);

		

	}
	
	
	public void init(GLAutoDrawable drawable) { // reading new vertex & fragment shaders
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		vShaderSource = readShaderSource("src/JOGL1_2_V.shader"); // read vertex shader
		fShaderSource = readShaderSource("src/JOGL1_2_F.shader"); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
	}

	
	public void dispose(GLAutoDrawable drawable) {
		animator.stop(); // stop the animator thread
		System.out.println("Animator thread stopped.");
		//super.dispose(drawable);
	}
	
	public static void main(String[] args) {
		new JOGL1_2_Animate();
		

	}
}
