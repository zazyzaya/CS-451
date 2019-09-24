

import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import static com.jogamp.opengl.GL4.*;


public class JOGL1_4_1_Point extends JOGL1_3_VertexArray {
	private int vao[ ] = new int[1]; // vertex array object (handle), for sending to the vertex shader
	private int vbo[ ] = new int[2]; // vertex buffers objects (handles) to stores position, color, normal, etc
	float vPoint[] = {0.0f, 0.0f, 0.0f}; 
	float vColor[] = {1.0f, 0.0f, 0.0f}; 

	

	public void display(GLAutoDrawable drawable) {
		
		// 1. draw into both buffers
		gl.glDrawBuffer(GL.GL_FRONT_AND_BACK);

		// 2. generate a random point		
		vPoint[0] = (float) (2*Math.random() - 1);
		vPoint[1] = (float) (2*Math.random() - 1);

		// 3. generate a random color		
		vColor[0] = (float) Math.random();
		vColor[1] = (float) Math.random();
		vColor[2] = (float) Math.random();
		
		gl.glPointSize(4.0f); 
		// specify to draw the point with the color
		drawPoint(vPoint, vColor);
	}

	
	// specify to draw a point
	  public void drawPoint(float[] vPoint, float[] vColor) {

		// 1. load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoint);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					vBuf, // the vertex array
					GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
						
		// 2. load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColor);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
					cBuf, //the color array
					GL_STATIC_DRAW); 		
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[1] with active vao buffer
			
		// 3. draw 3 points: VAO has two arrays of corresponding vertices and colors
		gl.glDrawArrays(GL_POINTS, 0, 1); 
	}
	
		public void init(GLAutoDrawable drawable) {
			gl = (GL4) drawable.getGL();
			String vShaderSource[], fShaderSource[] ;
						
			vShaderSource = readShaderSource("src/JOGL1_4_1_V.shader"); // read vertex shader
			fShaderSource = readShaderSource("src/JOGL1_4_1_F.shader"); // read fragment shader
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
		 new JOGL1_4_1_Point();

	}
}
