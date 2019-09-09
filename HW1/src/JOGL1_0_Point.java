
/*************************************************
 * Created on August 1, 2017, @author: Jim X. Chen
 *
 * Draw a point 
 * with simple vertex shader and fragment shader
 *
 *
 */

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;

public class JOGL1_0_Point extends JOGL1_0_Frame {

	public JOGL1_0_Point() { // 0. it calls super's constructor first
		System.out.println("-Dsun.java2d.d3d=false (turn off direct3d)");
		System.out.println("-Djogl.debug.DebugGL (only when debugging JOGL)");
		System.out.println("-Djogl.debug.TraceGL (only when tracing JOGL)");
	}

	public void display(GLAutoDrawable drawable) { // overwrite super's display
		System.out.println("c) Display is called, which overwrite super's display.");

		gl.glPointSize(10.0f);
		gl.glDrawArrays(GL_POINTS, 0, 1); // first index 0, count 1
		
		// Supposed to send a VAO to the vertex shader for rendering.
	}

	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();

		String vShaderSource[] = { 	"#version 450\n",
									"void main(void) {", 
										"gl_Position = vec4(0.0, 0.0, 0.0, 1.0);", 
									"}", 
		};

		String fShaderSource[] = { 	"#version 450\n",
									"out vec4 color; ", 
									"void main(void) {", 
										"color	= vec4(1.0, 1.0, 1.0, 1.0); ",
									"}", 
		};

		initShaders(vShaderSource, fShaderSource); // shader programs ready

	}

	public int initShaders(String vShaderSource[], String fShaderSource[]) {

		// 1. create, load, and compile vertex shader
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderSource.length, vShaderSource, null, 0);
		gl.glCompileShader(vShader);

		// 2. create, load, and compile fragment shader
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderSource.length, fShaderSource, null, 0);
		gl.glCompileShader(fShader);

		// 3. attach the shader programs
		int vfProgram = gl.glCreateProgram(); // for attaching v & f shaders
		gl.glAttachShader(vfProgram, vShader);
		gl.glAttachShader(vfProgram, fShader);

		// 4. link the program
		gl.glLinkProgram(vfProgram); // successful linking --ready for using

		gl.glDeleteShader(vShader); // attached shader object will be flagged for deletion until 
									// it is no longer attached
		gl.glDeleteShader(fShader);

		// 5. Use the program
		gl.glUseProgram(vfProgram);
		gl.glDeleteProgram(vfProgram); // in-use program object will be flagged for deletion until 
										// it is no longer in-use

		return vfProgram;
	}

	public static void main(String[] args) {
		JOGL1_0_Point f = new JOGL1_0_Point();
		f.setTitle("JOGL1_0_Point: drawing a point");
		f.setSize(WIDTH*3/2, HEIGHT); // set the size of the frame 
		f.setLocation(20, 20); // frame's upper-left corner
	}
}
