/*************************************************
 * Created on August 10, 2018, @author: Jim X. Chen
 *
 * Draw randomly generated lines into both buffers one at a time
 * 
 * This is to implement the text book's example: J_1_2_Line
 */

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import static com.jogamp.opengl.GL4.*;

import com.jogamp.opengl.util.awt.TextRenderer; 
import java.awt.Font;


public class JOGL1_4_3_xFont extends JOGL1_4_3_Triangle {
	private int vao[ ] = new int[1]; // vertex array object (handle), for sending to the vertex shader
	private int vbo[ ] = new int[2]; // vertex buffers objects (handles) to stores position, color, normal, etc

	TextRenderer textRenderer; //	
	//static GLUT glut = new GLUT(); //  handle to glut functions 

	
	public void display(GLAutoDrawable drawable) {		
		// 1. draw into both buffers
	    gl.glDrawBuffer(GL.GL_FRONT_AND_BACK);

		int x0 = (int) ((2*Math.random()-1) * WIDTH);
		int y0 = (int) ((2*Math.random()-1) * HEIGHT);
		int xn = (int) ((2*Math.random()-1) * WIDTH);
		int yn = (int) ((2*Math.random()-1) * HEIGHT);

		// draw a yellow line
		bresenhamLine(x0, y0, xn, yn);

		//textRenderer.beginRendering(0, 0);
		    // optionally set the color
		//textRenderer.setColor(1.0f, 0.2f, 0.2f, 0.8f);
		textRenderer.draw("Text to draw", 0, 0);
		    // ... more draw commands, color changes, etc.
		//textRenderer.endRendering();
		
		
		// character start position
		//gl.glposit(x0, y0, 0); //glRasterpos or glWindowPos
		//gl.glWindowPos3f(x0, y0, 0); //glRasterpos or glWindowPos
		
		// bitmap fonts that starts at (x0, y0)
//		glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, 'S');
	//	glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "tart");

		// stroke fonts that starts at (xn, yn)
		//gl.glPushMatrix();
		//gl.glTranslatef(xn, yn, 0); // end of line position
		//gl.glRotated(360*Math.random(), 0, 0, 1); // rotate
		//gl.glScaled(Math.random(), Math.random(), 1); // size
//		glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, 'E');
//		glut.glutStrokeString(GLUT.STROKE_ROMAN, "nd");
		//gl.glPopMatrix();

	    
		try {
			Thread.sleep(200);
		} catch (Exception ignore) {}

	}

	

	
	// Bresenham's midpoint line algorithm
	public void bresenhamLine(int x0, int y0, int xn, int yn) {
	    int dx, dy, incrE, incrNE, d, x, y, flag = 0;	    
	    
	    if (xn<x0) {
	      //swapd(&x0,&xn);
	      int temp = x0;
	      x0 = xn;
	      xn = temp;

	      //swapd(&y0,&yn);
	      temp = y0;
	      y0 = yn;
	      yn = temp;
	    }
	    if (yn<y0) {
	      y0 = -y0;
	      yn = -yn;
	      flag = 10;
	    }

	    dy = yn-y0;
	    dx = xn-x0;

	    if (dx<dy) {
	      //swapd(&x0,&y0);
	      int temp = x0;
	      x0 = y0;
	      y0 = temp;

	      //swapd(&xn,&yn);
	      temp = xn;
	      xn = yn;
	      yn = temp;

	      //swapd(&dy,&dx);
	      temp = dy;
	      dy = dx;
	      dx = temp;

	      flag++;
	    }

	    x = x0;
	    y = y0;
	    d = 2*dy-dx;
	    incrE = 2*dy;
	    incrNE = 2*(dy-dx);

	    int nPixels = xn - x0 + 1; // number of pixels on the line 	    
	    float[] vPoints = new float[3*nPixels]; // predefined number of pixels on the line
	    float[] vColors = new float[3*nPixels]; // predefined number of pixel colors on the line
	    float xf = x, yf = y; // taking care of different slopes
	    
	    while (x<xn+1) {
	       // taking care of different slopes (mirror vertical, horizontal, and diagonal lines) 
	    	xf = x; yf = y; 
		   if (flag==1) {
			      xf = y; yf = x;
			    } else if (flag==10) {
			      xf = x; yf = -y;
			    } else if (flag==11) {
			      xf = y;  yf = -x;
		  }
		   
	      // write a pixel into the framebuffer, here we write into an array
		  vPoints[(x-x0)*3] = xf / (float) WIDTH; // normalize -1 to 1
		  vPoints[(x-x0)*3 + 1] = yf / (float) HEIGHT; // normalize -1 to 1
		  vPoints[(x-x0)*3 + 2] = 0.0f;
		  
		  // generate corresponding pixel colors
		  vColors[(x-x0)*3] = (float) Math.random(); 
		  vColors[(x-x0)*3 + 1] = (float) Math.random(); 
		  vColors[(x-x0)*3 + 2] = (float) Math.random(); 
		  

	      x++; /* consider next pixel */
	      if (d<=0) {
	        d += incrE;
	      } else {
	        y++;
	        d += incrNE;
	      }
	    }
	    
	    
	    
	    // load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					vBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
						
	    // load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 0 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					cBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer

 		
		//  draw points: VAO has is an array of corresponding vertices and colors
		gl.glDrawArrays(GL_POINTS, 0, (vBuf.limit()/3)); 	    
	  }



	
	public void init(GLAutoDrawable drawable) {
		
		// Allocate textRenderer with the chosen font
		textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 12));
		
			gl = (GL4) drawable.getGL();
			String vShaderSource[], fShaderSource[] ;
						
			vShaderSource = readShaderSource("src/JOGL1_4_3_V.shader"); // read vertex shader
			fShaderSource = readShaderSource("src/JOGL1_4_3_F.shader"); // read fragment shader
			vfPrograms = initShaders(vShaderSource, fShaderSource);		
			
			// 1. generate vertex arrays indexed by vao
			gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
			gl.glBindVertexArray(vao[0]); // use handle 0
			
			// 2. generate vertex buffers indexed by vbo: here vertices and colors
			gl.glGenBuffers(vbo.length, vbo, 0);
			
			// 3. enable VAO with loaded VBO data
			gl.glEnableVertexAttribArray(0); // enable the 0th vertex attribute: position
			gl.glEnableVertexAttribArray(1); // enable the 1th vertex attribute: color
			
			
			// 5. send display size as uniform

			//Connect JOGL variable with shader variable by name
			int widthLoc = gl.glGetUniformLocation(vfPrograms,  "WIDTH"); 
			gl.glProgramUniform1f(vfPrograms,  widthLoc, WIDTH);
			int heightLoc = gl.glGetUniformLocation(vfPrograms,  "HEIGHT"); 
			gl.glProgramUniform1f(vfPrograms,  heightLoc, HEIGHT);

 		}
		
	
	public static void main(String[] args) {
		 new JOGL1_4_3_xFont();

	}
}

/*
 
 
import javax.media.opengl.*;
import com.sun.opengl.util.*;

//import net.java.games.jogl.*;
//import net.java.games.jogl.util.*;

public class J1_3_xFont extends J1_3_Triangle {
	static GLUT glut = new GLUT();

	// called for OpenGL rendering every reshape
	public void display(GLAutoDrawable drawable) {
		//generate a random line;
		int x0 = (int) (Math.random() * WIDTH);
		int y0 = (int) (Math.random() * HEIGHT);
		int xn = (int) ((Math.random() * WIDTH));
		int yn = (int) (Math.random() * HEIGHT);

		// draw a yellow line
		gl.glColor3d(Math.random(), Math.random(), Math.random());
		bresenhamLine(x0, y0, xn, yn);

		// character start position
		gl.glWindowPos3f(x0, y0, 0); //glRasterpos or glWindowPos
		
		// bitmap fonts that starts at (x0, y0)
		glut.glutBitmapCharacter(GLUT.BITMAP_HELVETICA_18, 'S');
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "tart");

		// stroke fonts that starts at (xn, yn)
		gl.glPushMatrix();
		gl.glTranslatef(xn, yn, 0); // end of line position
		gl.glRotated(360*Math.random(), 0, 0, 1); // rotate
		gl.glScaled(Math.random(), Math.random(), 1); // size
		glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, 'E');
		glut.glutStrokeString(GLUT.STROKE_ROMAN, "nd");
		gl.glPopMatrix();

		try {
			Thread.sleep(1000);
		} catch (Exception ignore) {}
	}

	public static void main(String[] args) {
		J1_3_xFont f = new J1_3_xFont();

		f.setTitle("JOGL J1_3_xFont");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}

 * 
 */
 

