/*************************************************
 * Created on August 1, 2017, @author: Jim X. Chen
 *
 * Draw multiple points (in parallel)
 * 
 * a) Another method of sending values to the vertex shader(s) respectively
 * b) Efficient transfer of default vertex values: position, color, normal, texture coordinates, etc.
 * 
 * VBO: arrays to store vertex positions, colors, and other per-vertex information
 * VAO: an array that packs multiple VBO for transferring to the vertex shader
 *
 */


import static com.jogamp.opengl.GL.GL_POINTS;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;

import java.nio.FloatBuffer;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import static com.jogamp.opengl.GL4.*;


public class JOGL1_3_VertexArray extends JOGL1_2_Animate {
	private int vao[ ] = new int[1]; // vertex array object (handle), for sending to the vertex shader
	private int vbo[ ] = new int[2]; // vertex buffers objects (handles) to stores position, color, normal, etc
	float vPoints[] = {-0.5f, 0.0f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f}; 
	// array of vertices and colors corresponding to the vertices
	float vColors[] = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f}; 
	

	public void display(GLAutoDrawable drawable) {
		
		// clear the display every frame
		float bgColor[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bgColorBuffer = Buffers.newDirectFloatBuffer(bgColor);
		gl.glClearBufferfv(GL_COLOR, 0, bgColorBuffer); // clear every frame

		
		// pos goes from -1 to 1 back and forth
		pos += delta; 
		if (pos<=-1.0f) delta = 0.005f; 
		else if (pos>=1.0f) delta = -0.005f; 
		
		//Connect JOGL variable with shader variable by name
		int posLoc = gl.glGetUniformLocation(vfPrograms,  "sPos"); 
		gl.glProgramUniform1f(vfPrograms,  posLoc,  pos);
		
		// demonstrate variable points coordinates
		vPoints[3] = pos;  
		vPoints[6] = -pos; 
	
		// load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
				vBuf, // the vertex array
				GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
				cBuf, //the color array
				GL_STATIC_DRAW); 		
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[1] with active vao buffer
		
		gl.glPointSize(6.0f); 
		
		// 6. draw 3 points: VAO has two arrays of corresponding vertices and colors
		gl.glDrawArrays(GL_POINTS, 0, 3); 
		gl.glDrawArrays(GL_TRIANGLES, 0, 3); 
	}
	
	
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		
		vShaderSource = readShaderSource("src/JOGL1_3_V.shader"); // read vertex shader
		fShaderSource = readShaderSource("src/JOGL1_3_F.shader"); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
		
		// 1. generate vertex arrays indexed by vao
		gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
		System.out.println(vao.length); // we only use one vao
		gl.glBindVertexArray(vao[0]); // use handle 0
		
		// 2. generate vertex buffers indexed by vbo: here vertices and colors
		gl.glGenBuffers(vbo.length, vbo, 0);
		System.out.println(vbo.length); // we use two: position and color
		
/*		// 3. load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
				vBuf, // the vertex array
				GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active vao buffer
		
		// 4. load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
				cBuf, //the color array
				GL_STATIC_DRAW); 		
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // associate vbo[1] with active vao buffer
*/		
		// 5. enable VAO with loaded VBO data
		gl.glEnableVertexAttribArray(0); // enable the 0th vertex attribute: position
		gl.glEnableVertexAttribArray(1); // enable the 1th vertex attribute: color
		
		//

	}

	
	public static void main(String[] args) {
		 new JOGL1_3_VertexArray();

	}
}
