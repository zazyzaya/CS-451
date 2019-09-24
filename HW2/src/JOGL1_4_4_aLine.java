

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


public class JOGL1_4_4_aLine extends JOGL1_4_3_Triangle {
	private int vao[ ] = new int[1]; // vertex array object (handle), for sending to the vertex shader
	private int vbo[ ] = new int[2]; // vertex buffers objects (handles) to stores position, color, normal, etc

	//static GLUT glut = new GLUT(); //  handle to glut functions 

	
	public void display(GLAutoDrawable drawable) {		
		// 1. draw into both buffers
	    gl.glDrawBuffer(GL.GL_FRONT_AND_BACK);

		int x0 = (int) ((2*Math.random()-1) * WIDTH);
		int y0 = (int) ((2*Math.random()-1) * HEIGHT);
		int xn = (int) ((2*Math.random()-1) * WIDTH);
		int yn = (int) ((2*Math.random()-1) * HEIGHT);

		// draw a yellow line
		antialiasedLine(x0, y0, xn, yn);

	}

	

	
	// Bresenham's midpoint line algorithm with antialiasing
	public void antialiasedLine(int x0, int y0, int xn, int yn) {
	    int dx, dy, incrE, incrNE, d, x, y, flag = 0;	    
		float D = 0, sin_a, cos_a, sin_cos_a, Denom;
	    
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
	    
		Denom = (float) Math.sqrt((double) (dx * dx + dy * dy));
		sin_a = dy / Denom;
		cos_a = dx / Denom;
		sin_cos_a = sin_a - cos_a;

	    int nPixels = xn - x0 + 1; // number of pixels on the line 	    
	    float[][] vPoints = new float[3][3*nPixels]; // predefined number of pixels on the line
	    float[][] vColors = new float[3][3*nPixels]; // predefined number of pixel colors on the line
	    
	    while (x<xn+1) {
	       // taking care of different slopes (mirror vertical, horizontal, and diagonal lines) 
			IntensifyPixel(vPoints[0], vColors[0], x0, x, y, D, flag);
			IntensifyPixel(vPoints[1], vColors[1], x0, x, y + 2, D - 2*cos_a, flag); // N
			IntensifyPixel(vPoints[2], vColors[2], x0, x, y - 2, D + 2*cos_a, flag); // S
						  	
	      	x++; /* consider next pixel */
			if (d <= 0) {
				D += sin_a; // distance to the line from E
				d += incrE;
			} else {
				D += sin_cos_a; // distance to the line: NE
				y++;
				d += incrNE;
			}
	    }
			
	    
	   // load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints[0]);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					vBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
						
	    // load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 0 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors[0]);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					cBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
 		
		//  draw points: VAO has is an array of corresponding vertices and colors
		gl.glDrawArrays(GL_POINTS, 0, (vBuf.limit()/3)); 	    

		gl.glFlush(); 
		try {
			Thread.sleep(500);
		} catch (Exception ignore) {}


	    
	    // load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		 vBuf = Buffers.newDirectFloatBuffer(vPoints[1]);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					vBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
						
	    // load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 0 		
		 cBuf = Buffers.newDirectFloatBuffer(vColors[1]);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					cBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
 		
		//  draw points: VAO has is an array of corresponding vertices and colors
		gl.glDrawArrays(GL_POINTS, 0, (vBuf.limit()/3)); 	    

		gl.glFlush(); 
		try {
			Thread.sleep(500);
		} catch (Exception ignore) {}
    
	    // load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		 vBuf = Buffers.newDirectFloatBuffer(vPoints[2]);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					vBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
						
	    // load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 0 		
		 cBuf = Buffers.newDirectFloatBuffer(vColors[2]);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					cBuf, // the vertex array
					GL_STATIC_DRAW); 
 		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		//  draw points: VAO has is an array of corresponding vertices and colors
		gl.glDrawArrays(GL_POINTS, 0, (vBuf.limit()/3)); 	    
		
		gl.glFlush(); 
		try {
			Thread.sleep(500);
		} catch (Exception ignore) {}

	}

	void IntensifyPixel(float vPoints[], float vColors[], int x0, int x, int y, float D, int flag) {
		
	   float xf = x, yf = y;

	    if (flag==1) {
	      xf = y;
	      yf = x;
	    } else if (flag==10) {
	      xf = x;
	      yf = -y;
	    } else if (flag==11) {
	      xf = y;
	      yf = -x;
	    }
	 
		// write a pixel into the framebuffer, here we write into an array
	    vPoints[(x-x0)*3] = xf / (float) WIDTH; // normalize -1 to 1
	    vPoints[(x-x0)*3 + 1] = yf / (float) HEIGHT; // normalize -1 to 1
		vPoints[(x-x0)*3 + 2] = 0.0f;
	
	 
		if (D < 0) 
				D = -D; // negative if the pixel is above the line
		
	  // generate corresponding pixel colors
	  vColors[(x-x0)*3]     =  1.0f - D/2.5f; 
	  vColors[(x-x0)*3 + 1] =  1.0f - D/2.5f; 
	  vColors[(x-x0)*3 + 2] =  1.0f - D/2.5f;

  }


	
	public void init(GLAutoDrawable drawable) {
				
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
		 new JOGL1_4_4_aLine();

	}
}



