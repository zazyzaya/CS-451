/*************************************************
 * Created on August 10, 2018, @author: Jim X. Chen
 *
 * Draw randomly generated lines into both buffers one at a time: Bresenham's algorithm
 * 
 * This is to implement the text book's example: J_1_3_Line
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


public class JOGL1_4_3_LineCircle extends JOGL1_4_3_Line {
	private int vao[ ] = new int[1]; // vertex array object (handle), for sending to the vertex shader
	private int vbo[ ] = new int[2]; // vertex buffers objects (handles) to stores position, color, normal, etc
	float vPoint[] = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; 
	float vColor[] = {1.0f, 1.0f, 0.0f}; 

	

	public void display(GLAutoDrawable drawable) {		
		// 1. draw into both buffers
	    gl.glDrawBuffer(GL.GL_FRONT_AND_BACK);

		// 2. generate 2 random end points		
		vPoint[0] = (float) (2*Math.random() - 1);
		vPoint[1] = (float) (2*Math.random() - 1);
		vPoint[3] = (float) (2*Math.random() - 1);
		vPoint[4] = (float) (2*Math.random() - 1);
		
		vColor[0] = 1; // make it white color
		vColor[1] = 1; // make it white color
		vColor[2] = 1; // make it white color
		

		// wait for the previous display to stay for a while
		try {
			Thread.sleep(550);
		}  catch (Exception ignore) {}
		
		// 3. draw the line with the color using JOGL line function
	    drawLineJOGL(vPoint, vColor);
	    drawable.swapBuffers(); // display it. The line is draw into the back buffer first
	    
	    // wait for the current line to stay for a while
		try {
				Thread.sleep(550);
		} catch (Exception ignore) {}
		
		// DRAW A LINE: using Bresenham's algorithm
		
	    int x0, y0, xn, yn, dx, dy;
 
	    x0 = (int) (vPoint[0]*WIDTH);
	    y0 = (int) (vPoint[1]*HEIGHT);
	    xn = (int) (vPoint[3]*WIDTH);
	    yn = (int) (vPoint[4]*HEIGHT);

	    //2. draw the line by using the Bresenham's algorithm
		vColor[0] = (float) Math.random();
		vColor[1] = (float) Math.random();
		vColor[2] = (float) Math.random();
	    bresenhamLine(x0, y0, xn, yn); 
		vColor[2] = 0; // make it yellow color
		
		//swap buffer is automatic 

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
						
		// 5. send color data to vertex shader through uniform
 		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColor);

		//Connect JOGL variable with shader variable by name
		int colorLoc = gl.glGetUniformLocation(vfPrograms,  "vColor"); 
		gl.glProgramUniform3fv(vfPrograms,  colorLoc, 1, cBuf);
			
		// 6. draw points: VAO has 1 array of corresponding vertices 
		gl.glDrawArrays(GL_POINTS, 0, (vBuf.limit()/3)); 	    
	  }

	  
	 // specify to draw a line using OpenGL
	  public void drawLineJOGL(float[] vPoint, float[] vColor) {

		// 1. load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoint);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					vBuf, // the vertex array
					GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
						
		// 2. send color data to vertex shader through uniform (array)
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColor);

		//Connect JOGL variable with shader variable by name
		int colorLoc = gl.glGetUniformLocation(vfPrograms,  "vColor"); 
		gl.glProgramUniform3fv(vfPrograms,  colorLoc, 1, cBuf);
			
		// 3. draw 3 points: VAO has two arrays of corresponding vertices and colors
		gl.glDrawArrays(GL_LINES, 0, 2); 
	}
	
		public void init(GLAutoDrawable drawable) {
			gl = (GL4) drawable.getGL();
			String vShaderSource[], fShaderSource[] ;
						
			vShaderSource = readShaderSource("src/JOGL1_4_2_V.shader"); // read vertex shader
			fShaderSource = readShaderSource("src/JOGL1_4_2_F.shader"); // read fragment shader
			vfPrograms = initShaders(vShaderSource, fShaderSource);		
			
			// 1. generate vertex arrays indexed by vao
			gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
			gl.glBindVertexArray(vao[0]); // use handle 0
			
			// 2. generate vertex buffers indexed by vbo: here vertices and colors
			gl.glGenBuffers(vbo.length, vbo, 0);
			
			// 3. enable VAO with loaded VBO data
			gl.glEnableVertexAttribArray(0); // enable the 0th vertex attribute: position
			gl.glEnableVertexAttribArray(1); // enable the 1th vertex attribute: color
 		}
		
	
	public static void main(String[] args) {
		 new JOGL1_4_3_LineCircle();

	}
}




