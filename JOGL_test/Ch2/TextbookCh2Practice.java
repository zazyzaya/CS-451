import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.common.nio.Buffers;

public class TextbookCh2Practice extends JFrame implements GLEventListener { 
	private TextbookMethods tm;
	private GLCanvas myCanvas;
	private int rendering_program;
	private int vao[ ] = new int[1];
	
	private Random rnd = new Random();
	private boolean firstRun=true;
	private float locationX = rnd.nextFloat();	// Location of triangle
	private float locationY = rnd.nextFloat();
	private float incX = rnd.nextFloat() / 100;		// Offset for moving the triangle
	private float incY = rnd.nextFloat() / 100; 	
	private float colors[] = {0.0f, 0.0f, 0.1f};
	
	public TextbookCh2Practice() { 
		setTitle("Chapter2 - Practice");
		setSize(600, 600);
		setLocation(300, 300);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true); // This initializes OpenGL and creates a GL4 object
		
		// Create new animator which updates the canvas 50 times per second
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}
	
	public static void main(String[ ] args) { 
		new TextbookCh2Practice();
	}
	
	public void display(GLAutoDrawable drawable) { 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Next we load the shader into the OpenGL pipeline stages, and put it into the GPU
		// However, this still doesn't actually run it--it only places it into GPU memory
		gl.glUseProgram(rendering_program);
		
		// Clear background every frame
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
		
		// Update where the triangle should be translated to
		locationX += incX;
		locationY += incY;
		
		boolean isBounce = false;
		if (locationX > 0.75 || locationX < -0.75) {
			incX = -incX;
			isBounce = true;
		}
		
		if (locationY > 0.75 || locationY < -0.75) {
			incY = -incY;
			isBounce = true;
		}
		
		if (isBounce || firstRun) {
			firstRun = false; 
			
			String names[] = {"r", "g", "b"};
			int ptr;
			
			for (int i=0; i<3; i++) {
				ptr = gl.glGetUniformLocation(rendering_program, names[i]);
				gl.glProgramUniform1f(rendering_program, ptr, rnd.nextFloat());
			}
		}
		
		// Update variable "offset" in shader programs
		int offsetX_ptr = gl.glGetUniformLocation(rendering_program, "offsetX");
		int offsetY_ptr = gl.glGetUniformLocation(rendering_program, "offsetY");
		gl.glProgramUniform1f(rendering_program, offsetX_ptr, locationX);
		gl.glProgramUniform1f(rendering_program, offsetY_ptr, locationY);
		
		// Don't need to load in points, because we already did that in shaders, just tell OpenGL
		// To start drawing, and that there will be 3 points that it needs to display as a triangle
		gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
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
		String vshaderSource[ ] = tm.readShaderSource("Shaders\\Ch2V.shader");
		
		// Fragment shader sets individual pixels' colors.
		String fshaderSource[ ] = tm.readShaderSource("Shaders\\Ch2F.shader");
		
		// Creates the shader. But there's nothing in it yet. This just returns the pointer
		// To where it will eventually be. Not unlike malloc
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER); 		
		
		// Loads the GLSL code from the string into the shader
		// still just as a string though. Also calling some mallocs to make room for the strings
		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0); // note: 3 lines of code
		
		// Finally, we go from the string, to the compiled code that is used for shading
		gl.glCompileShader(vShader);
		
		
		// Do the same thing for the frag shader
		int fShader=gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0); // note: 4 lines of code
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

