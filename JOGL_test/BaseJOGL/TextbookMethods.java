import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL2ES2.GL_INFO_LOG_LENGTH;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.glu.GLU;

/**
 * Methods from the Textbook that I think may come in handy for future projects. 
 * All have cited page numbers and are from "Computer Graphics Programming: In OpenGL with Java"
 * unless otherwise specified
 * @author Iking
 *
 */
public class TextbookMethods {
	// Ch2 pp. 38
	static void printShaderLog(int shader) { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[ ] len = new int[1];
		int[ ] chWrittn = new int[1];
		byte[ ] log = null;
		
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
	
	// Ch2 pp. 38
	static void printProgramLog(int prog) { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[ ] len = new int[1];
		int[ ] chWrittn = new int[1];
		byte[ ] log = null;
	
		
		// determine the length of the program linking log
		gl.glGetProgramiv(prog,GL_INFO_LOG_LENGTH,len, 0);
		
		if (len[0] > 0) { 
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0,log, 0);
			
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++) { 
				System.out.print((char) log[i]);
			} 
		} 
	}
	
	// Ch2 pp. 38
	static boolean checkOpenGLError() { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
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

	// Ch2 pp. 41
	static String[ ] readShaderSource(String filename) { 
		Vector<String> lines = new Vector<String>();
		Scanner sc;
	
		try { sc = new Scanner(new File(filename)); }
		catch (IOException e) { 
			System.err.println("IOException reading file: " + e);
			return null;
		}
		
		while (sc.hasNext()) { 
			lines.addElement(sc.nextLine());
		}
		
		String[ ] program = new String[lines.size()];
		for (int i = 0; i < lines.size(); i++) { 
			program[i] = (String) lines.elementAt(i) + "\n";
		}
		
		return program; 
	}
}
