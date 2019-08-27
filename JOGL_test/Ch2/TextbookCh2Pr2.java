import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.common.nio.Buffers;

public class TextbookCh2Pr2 extends JFrame implements GLEventListener { 
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[ ] = new int[1];
	
	public TextbookCh2Pr2() { 
		setTitle("Chapter2 - program2");
		setSize(600, 400);
		setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true); // This initializes OpenGL and creates a GL4 object
	}
	
	// Generally useful methods for debugging GLSL from textbook Ch2 pp. 38
	private void printShaderLog(int shader) { 
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
	
	void printProgramLog(int prog) { 
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
	
	boolean checkOpenGLError() { 
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

	// Generally useful method from textbook Ch2 pp. 41
	private String[ ] readShaderSource(String filename) { 
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

	
	public static void main(String[ ] args) { 
		new TextbookCh2Pr2();
	}
	
	public void display(GLAutoDrawable drawable) { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Next we load the shader into the OpenGL pipeline stages, and put it into the GPU
		// However, this still doesn't actually run it--it only places it into GPU memory
		gl.glUseProgram(rendering_program);
		
		// As a demonstration, we can set the size of individual pixels to be any number.
		// This is makes the rasterizer decide that a point will take up NxN pixels of room 
		// when it goes to the shader
		gl.glPointSize(30.0f);
		
		// Don't need to load in points, because we already did that in shaders, just tell OpenGL
		// To start drawing, and that there will be 1 point that it needs to display
		gl.glDrawArrays(GL_POINTS, 0, 1);
	}
	
	// Now init actually does something--creates the shaders.
	
	public void init(GLAutoDrawable drawable) { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		rendering_program = createShaderProgram();
		
		// Though we don't need a vertex buffer here, as the point is hardcoded into the shader,
		// OpenGL requires one be built any time shaders are used. So here they are
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}
	
	private int createShaderProgram() { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Purpose of vertex shader is to send a vertex down the pipeline, the gl_Position variable
		// sets a vertex's coordinate position in space. Here it's hard coded to 0,0,0 (the origin) and 1.0 (learn what this is in ch3)
		// gl_Position is a special variable in that it's automatically set to "out"
		String vshaderSource[ ] = { 
			"#version 430 \n",		// OpenGL version (probably always will be 430 for our purposes)
			"void main(void) \n",	
			// We hardcode a single point into the shader source, so we never specify what points to draw elsewhere
			"{ gl_Position = vec4(0.0, 0.0, 0.0, 1.0); } \n",	
		};
		
		// Fragment shader sets individual pixels' colors.
		// Here we set any pixel that comes our way (which will only ever be 1) to RGBA (0,0,1,1), aka blue.
		// Color is set with the 'out' tag so the compiler knows this is output (4d vector output)
		String fshaderSource[ ] = { 
			"#version 430 \n",
			"out vec4 color; \n",
			"void main(void) \n",
			"{ if (gl_FragCoord.x > 293) color = vec4(1.0, 0.0, 0.0, 1.0); else color = vec4(0.0, 0.0, 1.0, 1.0); } \n"
		};
		
		// Creates the shader. But there's nothing in it yet. This just returns the pointer
		// To where it will eventually be. Not unlike malloc
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER); 		
		
		// Loads the GLSL code from the string into the shader
		// still just as a string though. Also calling some mallocs to make room for the strings
		gl.glShaderSource(vShader, 3, vshaderSource, null, 0); // note: 3 lines of code
		
		// Finally, we go from the string, to the compiled code that is used for shading
		gl.glCompileShader(vShader);
		
		
		// Do the same thing for the frag shader
		int fShader=gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, 4, fshaderSource, null, 0); // note: 4 lines of code
		gl.glCompileShader(fShader);
		
		// In a similar way to building shader objects before, we create an empty program object 
		int vfprogram = gl.glCreateProgram();
		
		// Then we tell it what shaders to use in totality (does order matter?)
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		
		// Finally request the GLSL compiler ensures everything is compatible
		gl.glLinkProgram(vfprogram);
		
		// No longer need them individually now that we have compiled/linked version
		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		
		return vfprogram;
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }
	public void dispose(GLAutoDrawable drawable) { }

}
