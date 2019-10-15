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
	private float theta = 0f;
	
	// Tweak these for nicer renderings
	private static int 		NUM_PARTICLES = 1000;
	private static float 	MAX_VELOCITY = 0.01f;
	private static float 	ROT_SPEED = 0.01f;
	private static float 	DUST_SIZE = 1f;
	private static float 	MOVER_SIZE = 20f;
	
	public static void main(String[] args) {
		 new H1_iking();
	}
	
	/*
	 * Generates random 4d vectors to be used as colors for points
	 * Makes colors associated with background have alpha of 0 so we can more
	 * easilly identify them
	 */
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
			
			// Min 0.2 so nothing is invisible
			retList[i] = rnd.nextFloat() * 0.8f + 0.2f;
		}
		
		// Shader checks if alpha is 0 in color buffer then checks if on circle circumference
		// if so, it raises it to 1
		float corners[] = {
				0.3f, 0.3f, 0.3f, 0.3f,
				0.3f, 0.3f, 0.3f, 0.3f,
				0.3f, 0.3f, 0.3f, 0.3f,
				0.3f, 0.3f, 0.3f, 0.3f
		};
		
		// Move corner points into list as well
		System.arraycopy(corners, 0, retList, NUM_PARTICLES*4, 16);
		return retList;
	}
	
	/*
	 * Generates random points inside the unit circle 
	 * (Did this a little lazilly, they just cant have x or y > root2/2 so they're inside a square
	 * area we know for sure fits in the circle)
	 */
	private float[] genRandomPoints() {
		float retList[] = new float[NUM_PARTICLES*2 + 16];
		
		// We ignore the z axis in the vertex shader, but it's left so the frag shader can 
		// Use this method for color also
		for (int i=0; i<NUM_PARTICLES*2; i++) {
			// Approx (root 2)/2 so we know it's within the circle no matter what
			retList[i] = rnd.nextFloat() * 0.7f;
			// Make them negative 50% of the time
			retList[i] = (rnd.nextInt(2) % 2 == 0) ? retList[i] * -1 : retList[i];
		}
		
		// Spinning point
		// Has special coordinates so GLSL knows which one it is
		retList[0] = 0.0f;
		retList[1] = 1.0f;
		
		float corners[] = {
				-1.0f, 1.0f,
				1.0f, 1.0f,
				1.0f, -1.0f,
				-1.0f, -1.0f
		};
		
		// Move corner points into list as well
		System.arraycopy(corners, 0, retList, NUM_PARTICLES*2, 8);
		return retList;
	}
	
	/*
	 * Generates random 2d vectors which we use for particles' velocities 
	 *
	 */
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
		// Proj_b(a) (projection of b onto a) =
		// a * b  
		// ----- * a
		// |a|^2
		
		float abs_a = (float)Math.sqrt(x*x + y*y);
		float dot_ab = x*vx + y*vy;
		float quot = dot_ab / (abs_a * abs_a);
		
		float proj[] = { x*quot, y*quot };
		
		// We now double the projection
		proj[0] *= 2;
		proj[1] *= 2;
		
		// Subtract the original from it
		proj[0] -= vx;
		proj[1] -= vy;
		
		// And return the negated vector
		proj[0] *= -1;
		proj[1] *= -1;
		
		return proj;
	}
	
	public void display(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		
		// clear the display every frame
		float bgColor[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bgColorBuffer = Buffers.newDirectFloatBuffer(bgColor);
		gl.glClearBufferfv(GL_COLOR, 0, bgColorBuffer); // clear every frame
		
		// Cant think of a way to do this on GPU. Need some way for shaders to write to buffer
		// which I think is illegal
		for (int i=1; i<NUM_PARTICLES; i++) {
			float x,y;
			x = vPoints[i*2] += velocities[i*2];
			y = vPoints[i*2 + 1] += velocities[i*2 + 1];
			
			if (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) >= 1) {
				float[] newV = reflect(x, y, velocities[i*2], velocities[i*2 + 1]);
				velocities[i*2] = newV[0];
				velocities[i*2 + 1] = newV[1];
			}
		}
		
		// Update location of dust pixels
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// Update location of circling pixel
		theta += ROT_SPEED;
		int tPointer = gl.glGetUniformLocation(vfPrograms, "theta");
		gl.glProgramUniform1f(vfPrograms, tPointer, theta);
		
		gl.glDrawArrays(GL_QUADS, NUM_PARTICLES, 4); 	// Draw the circle around the dust
		gl.glDrawArrays(GL_POINTS, 0, NUM_PARTICLES); 	// Draw the dust and circling pixel
	}
	
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		// Initialize shaders
		vShaderSource = readShaderSource("src/H1_iking_V.shader"); // read vertex shader
		fShaderSource = readShaderSource("src/H1_iking_F.shader"); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
		
		// Generate vertex arrays indexed by vao
		gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
		System.out.println(vao.length); // we only use one vao
		gl.glBindVertexArray(vao[0]); // use handle 0
		
		// Generate vertex buffers indexed by vbo: here vertices and colors
		gl.glGenBuffers(vbo.length, vbo, 0);
		System.out.println(vbo.length); 
		
		// Load initial points for dust and last 8 for square
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 	
		vPoints = genRandomPoints();
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 2, GL_FLOAT, false, 0, 0); 
		
		// Give points random color data
		vColors = genRandomColors();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[COLOR]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES, cBuf, GL_STATIC_DRAW); 		
		gl.glVertexAttribPointer(COLOR, 4, GL_FLOAT, false, 0, 0); 
		
		// Generate starting velocities for points
		velocities = genRandom2d();
		
		// Enable VAO with loaded VBO data
		gl.glEnableVertexAttribArray(POSITION); // enable the 0th vertex attribute: position
		gl.glEnableVertexAttribArray(COLOR); // enable the 1th vertex attribute: color
		
		// Set up uniforms that remain static
		int dsPointer = gl.glGetUniformLocation(vfPrograms, "dust_size");
		int psPointer = gl.glGetUniformLocation(vfPrograms, "mover_size");
		int wPointer = gl.glGetUniformLocation(vfPrograms, "width");
		int hPointer = gl.glGetUniformLocation(vfPrograms, "height");
		
		// I have no clue why these need to be adjusted like this. I assume it has something to do with
		// The toolbar at the top?
		gl.glProgramUniform1f(vfPrograms, wPointer, this.getWidth()-15);
		gl.glProgramUniform1f(vfPrograms, hPointer, this.getHeight()-38);
		gl.glProgramUniform1f(vfPrograms, dsPointer, DUST_SIZE);
		gl.glProgramUniform1f(vfPrograms, psPointer, MOVER_SIZE);
		
		// So rotating pixel is bigger and we can specify this in GLSL 
		gl.glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);
	}
}
