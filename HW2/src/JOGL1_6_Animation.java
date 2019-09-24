


import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.glu.GLU;
import java.nio.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import com.jogamp.opengl.util.*;

public class JOGL1_6_Animation extends JOGL1_0_Frame {

	private int rendering_program;
	private int vao[] = new int[1];
	
	private float x = 0.0f; // location of triangle
	private float inc = 0.01f; // offset for moving the triangle

	public JOGL1_6_Animation() {
		// The second parameter specifies the frames per second
		FPSAnimator animtr = new FPSAnimator(canvas, 50);
		animtr.start();
	}

	private void printShaderLog(int shader) {
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;
		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

	void printProgramLog(int prog) {
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;
		// determine the length of the program linking log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0) {
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++) {
				System.out.print((char) log[i]);
			}
		}
	}

	boolean checkOpenGLError() {
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR) {
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}

	private String[] readShaderSource(String filename) {
		Vector<String> lines = new Vector<String>();
		Scanner sc;
		try {
			sc = new Scanner(new File(filename));
		} catch (IOException e) {
			System.err.println("IOException reading file: " + e);
			return null;
		}
		while (sc.hasNext()) {
			lines.addElement(sc.nextLine());
		}
		String[] program = new String[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			program[i] = (String) lines.elementAt(i) + "\n";
		}
		return program;
	}

	
	public void display(GLAutoDrawable drawable) {
		//gl.glPointSize(30.0f);
		gl.glUseProgram(rendering_program);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f }; // clear the background to black, each time
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		x+=inc; 
		if (x<=-1.0f) inc = 0.01f; 
		if (x>=1.0f) inc = -0.01f; 
		int offset_loc = gl.glGetUniformLocation(rendering_program,  "offset"); 
		gl.glProgramUniform1f(rendering_program,  offset_loc,  x);; 
		
		
		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	public void init(GLAutoDrawable drawable) {
		gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}

	private int createShaderProgram() {
		String vshaderSource[] ;
		String fshaderSource[] ;
		
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];

		vshaderSource = readShaderSource("src/JOGL1_6_Vert.shader");
		fshaderSource = readShaderSource("src/JOGL1_4_Frag.shader");
		
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0); // note: 3 lines of code
		gl.glCompileShader(vShader);

		checkOpenGLError();
		gl.glGetShaderiv(vShader, GL_COMPILE_STATUS, vertCompiled, 0);
		if (vertCompiled[0] == 1) {
			System.out.println(". . . vertex compilation success.");
		} else {
			System.out.println(". . . vertex compilation failed.");
			printShaderLog(vShader);
		}

		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0); // note: 4 lines of code
		gl.glCompileShader(fShader);

		checkOpenGLError(); // can use returned boolean
		gl.glGetShaderiv(fShader, GL_COMPILE_STATUS, fragCompiled, 0);
		if (fragCompiled[0] == 1) {
			System.out.println(". . . fragment compilation success.");
		} else {
			System.out.println(". . . fragment compilation failed.");
			printShaderLog(fShader);
		}
		if ((vertCompiled[0] != 1) || (fragCompiled[0] != 1)) {
			System.out.println("\nCompilation error; return-flags:");
			System.out.println(" vertCompiled = " + vertCompiled[0] + "fragCompiled = " + fragCompiled[0]);
		} else {
			System.out.println("Successful compilation");
		}

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);

		gl.glGetProgramiv(vfprogram, GL_LINK_STATUS, linked, 0);
		if (linked[0] == 1) {
			System.out.println(". . . linking succeeded.");
		} else {
			System.out.println(". . . linking failed.");
			printProgramLog(vfprogram);
		}

		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		return vfprogram;
	}

	public static void main(String[] args) {
		new JOGL1_6_Animation();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	public void dispose(GLAutoDrawable drawable) {
	}
}
