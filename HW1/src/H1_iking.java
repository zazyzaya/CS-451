import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_COLOR;
import static com.jogamp.opengl.GL4.*;
import java.nio.FloatBuffer;
import java.util.Random;

public class H1_iking extends JOGL1_3_VertexArray {
	private int vao[ ] = new int[1];
	private int vbo[ ] = new int[2];	// Position, color
	private static int POSITION=0, COLOR=1;
	private Random rnd = new Random();
	private float velocities[];
	private float vPoints[];
	private float vColors[];
	
	// Tweak these for nicer renderings
	private static int 		NUM_PARTICLES = 100;
	private static float 	MAX_VELOCITY = 0.01f;
	
	public static void main(String[] args) {
		 new H1_iking();
	}
	
	private float[] genRandomColors() {
		float retList[] = new float[NUM_PARTICLES*4 + 16];
		
		// We ignore the z axis in the vertex shader, but it's left so the frag shader can 
		// Use this method for color also
		for (int i=0; i<NUM_PARTICLES*4; i++) {
			// Set all particle alphas to 1.0
			if (i%4 == 3) {
				retList[i] = 1.0f;
				continue;
			}
			
			// Min 0.5 so nothing is invisible
			retList[i] = rnd.nextFloat() * 0.5f + 0.5f;
			// Make them negative 50% of the time
			retList[i] = (rnd.nextInt(2) % 2 == 0) ? retList[i] * -1 : retList[i];
		}
		
		// Shader checks if alpha is 0 in color buffer then checks if on circle circumference
		// if so, it raises it to 1
		float corners[] = {
				1.0f, 1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 1.0f, 0.0f
		};
		
		// Move corner points into list as well
		System.arraycopy(corners, 0, retList, NUM_PARTICLES*4, 16);
		return retList;
	}
	
	private float[] genRandomPoints() {
		float retList[] = new float[NUM_PARTICLES*3 + 16];
		
		// We ignore the z axis in the vertex shader, but it's left so the frag shader can 
		// Use this method for color also
		for (int i=0; i<NUM_PARTICLES*3; i++) {
			// Approx (root 2)/2 so we know it's within the circle no matter what
			retList[i] = rnd.nextFloat() * 0.7f;
			// Make them negative 50% of the time
			retList[i] = (rnd.nextInt(2) % 2 == 0) ? retList[i] * -1 : retList[i];
		}
		
		float corners[] = {
				-1.0f, 1.0f, 0.0f,
				1.0f, 1.0f, 0.0f,
				1.0f, -1.0f, 0.0f,
				-1.0f, -1.0f, 0.0f
		};
		
		// Move corner points into list as well
		System.arraycopy(corners, 0, retList, NUM_PARTICLES*3, 12);
		return retList;
	}
	
	private float[] genRandom2d() {
		float retList[] = new float[NUM_PARTICLES*2];
		
		for (int i=0; i<NUM_PARTICLES*2; i++) {
			// Approx (root 2)/2 so we know it's within the circle no matter what
			retList[i] = rnd.nextFloat() * MAX_VELOCITY;
			// Make them negative 50% of the time
			retList[i] = (rnd.nextInt(2) % 2 == 0) ? retList[i] * -1 : retList[i];
		}
		
		System.out.println(retList);
		return retList;
	}
	
	// Transform a vector [vx, vy] into a reflection about [x,y] (presumed to be a unit vector)
	private float[] reflect(float x, float y, float vx, float vy) {
		float scalerV = (float)Math.sqrt(vx*vx + vy*vy) * 2;
		float scalerC = (float)Math.sqrt(x*x + y*y);
		
		// Luckily, if it hits the edge of the circle
		float[] projection = { scalerV * (x/scalerC), scalerV * (y/scalerC)};
		
		// then subtract original from projection
		projection[0] -= vx; projection[1] -= vy;
		
		// finally negate x and y then return
		projection[0] *= -1; projection[1] *= -1; 
		return projection;
	}
	
	public void display(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		
		// clear the display every frame
		float bgColor[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bgColorBuffer = Buffers.newDirectFloatBuffer(bgColor);
		gl.glClearBufferfv(GL_COLOR, 0, bgColorBuffer); // clear every frame
		
		// Cant think of a way to do this on GPU. Need some way for shaders to write to buffer
		// which I think is illegal
		for (int i=0; i<NUM_PARTICLES; i++) {
			float x,y;
			x = vPoints[i*3]; 
			y = vPoints[i*3 + 1]; 
			
			if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) >= 1) {
				float[] newV = reflect(x, y, velocities[i*2], velocities[i*2 + 1]);
				velocities[i*2] = newV[0];
				velocities[i*2 + 1] = newV[1];
			}
			
			vPoints[i*3] += velocities[i*2];
			vPoints[i*3 + 1] += velocities[i*2 + 1];
		}
		
		// load vbo[0] with vertex data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// load vbo[1] with color data
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES, cBuf, GL_STATIC_DRAW); 		
		gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0); // associate vbo[1] with active vao buffer
		
		gl.glPointSize(6.0f);
		gl.glDrawArrays(GL_POINTS, 0, NUM_PARTICLES);
		gl.glDrawArrays(GL_QUADS, NUM_PARTICLES, 4);
		
		
	}
	
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		vShaderSource = readShaderSource("src/H1_iking_V.shader"); // read vertex shader
		fShaderSource = readShaderSource("src/H1_iking_F.shader"); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
		
		// 1. generate vertex arrays indexed by vao
		gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
		System.out.println(vao.length); // we only use one vao
		gl.glBindVertexArray(vao[0]); // use handle 0
		
		// 2. generate vertex buffers indexed by vbo: here vertices and colors
		gl.glGenBuffers(vbo.length, vbo, 0);
		System.out.println(vbo.length); 
		
		// First, generate some random points inside the circle
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 	
		vPoints = genRandomPoints();
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
				vBuf, // the vertex array
				GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // associate vbo[0] with active vao buffer
		
		// Then load vbo[1] with color data
		vColors = genRandomColors();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[COLOR]); // use handle 1 		
		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES,  //# of float * size of floats in bytes
				cBuf, //the color array
				GL_STATIC_DRAW); 		
		gl.glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0); // associate vbo[1] with active vao buffer
		
		// Generate starting velocities for points
		velocities = genRandom2d();
		
		// 5. enable VAO with loaded VBO data
		gl.glEnableVertexAttribArray(0); // enable the 0th vertex attribute: position
		gl.glEnableVertexAttribArray(1); // enable the 1th vertex attribute: color
	}
}
