/*
 * Created on 2004-3-9
 * @author Jim X. Chen: draw a cone by subdivision
 */
//import net.java.games.jogl.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL4.GL_LINE;
import static com.jogamp.opengl.GL4.GL_FILL;
import com.jogamp.opengl.util.awt.*;

import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class J2_5_Cone_iking extends J2_4_Robot_iking {
	MatrixStack projection = new MatrixStack();
	MatrixStack modelView = new MatrixStack();
	MatrixStack cameraView = new MatrixStack();
	
	// Position of diffuse light
	protected float[] LIGHT_POS = {-0.25f, -0.5f, -1f, 0f};
	
	// Add another entry to VBO to hold normals for lighting calculations
	protected int vbo[ ] = new int[3];
	protected int NORMALS = 2;
	protected float[] vNorms;
	
	protected int mmxPtr;
	protected int pmxPtr;	// Pointer for projection matrix which is sent to GLSL seperately so 
	protected int nmxPtr; 	// lighting equations can run
	protected int cvPtr;
	protected int isShadingPtr;
	protected int lightPosPtr;
	
	private String vShaderSourceFile = "src/vbo_colors_iking_v.shader";
	private String fShaderSourceFile = "src/vbo_colors_iking_f.shader";
	
	protected ArrayList<Float> vPointList = new ArrayList<Float>();
	protected ArrayList<Float> vColorList = new ArrayList<Float>();
	
	// Copied from J1_5_Circle
	static int depth = 0; // number of subdivisions
	static int cRadius = 2, flip = 2, cnt = 1;
	
	protected TextRenderer tr;

	// vertex data for the triangles
	static float cVdata[][] = { 
			{ 1.0f, 0.0f, 0.0f }, 
			{ 0.0f, 1.0f, 0.0f },
			{ -1.0f, 0.0f, 0.0f }, 
			{ 0.0f, -1.0f, 0.0f } 
	};
	
	public void reshape(
			GLAutoDrawable glDrawable,
			int x,
			int y,
			int w,
			int h) {

		WIDTH = w;
		HEIGHT = h;
		
		//1. make sure the cone is within the viewing volume
		projection.pop();
		projection.pushOrtho(-w/2, w/2, -h/2, h/2, -w, w); // look at z near and far
		
		cameraView.pop();
		cameraView.pushOrtho(-w/2, w/2, -h/2, h/2, -w, w); // look at z near and far

		
		//2. This will enable depth test in general
	 	gl.glEnable(GL.GL_DEPTH_TEST);
	 	gl.setSwapInterval(1);
	}


	public void display(GLAutoDrawable glDrawable) {
		cnt++;		
		cRadius += flip;
		if ((cRadius>(WIDTH/2))|| (cRadius<=1)) {
			depth++;
			depth = depth%7;
					flip = -flip;
			 }
			 
			//3. clear both framebuffer and zbuffer
			gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		 
			
			//4. GL_DEPTH_TEST for hidden-surface removal
			if (cnt % 800 < 400)	{
				gl.glEnable(GL.GL_DEPTH_TEST);
			}
			
			else	{
				gl.glDisable(GL.GL_DEPTH_TEST);
		 }
			
	 
			//5. Test glPolygonMode 
			if (cnt % 100 > 80) gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL_LINE);
			else gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL_FILL);
	 
			//6. Test glCullFace
			if (cnt % 500 > 370) {
				gl.glEnable(GL.GL_CULL_FACE); 
				gl.glCullFace(GL.GL_FRONT); 
			}
			else if (cnt % 500 > 245) {
				gl.glEnable(GL.GL_CULL_FACE); 
				gl.glCullFace(GL.GL_BACK); 
			}
			else {
				gl.glDisable(GL.GL_CULL_FACE); 
			}
			
			//6. draw a triangle for showing hidden surface removal
			float 	v0[] = {-WIDTH/4, -WIDTH/4, -WIDTH}, 
					v2[] = {WIDTH/4, 0, WIDTH}, 
					v1[] = {WIDTH/4, HEIGHT/3, 0}; 
			
			modelView.pushIdentity();
			float[] color = { 0.5f, 0.5f, 0.5f };
			drawtriangle(v0, v1, v2, color); 
			modelView.pop();
			
			// rotate 1 degree alone vector (1, 1, 1)
			modelView.rotateAllAxes(2*PI/360);
			modelView.push();
			modelView.scale(cRadius, cRadius, cRadius);
			drawCone();
			modelView.pop();

	}

	protected float[] normalize(float[] vec) {
		float abs = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
		
		for (int i=0; i<3; i++) {
			vec[i] /= abs;
		}
		
		return vec;
	}

	private void subdivideCone(float v1[], float v2[], int depth) {
		float v0[] = {0, 0, 0};
		float v12[] = new float[3];

		if (depth==0) {
			float[] color = {v1[0]*v1[0], v1[1]*v1[1], 0, 1.0f};

			prepareToDrawTriangle(v1, v2, v0, color);
		    // bottom cover of the cone
			
			v0[2] = 1; // height of the cone, the tip on z axis
			prepareToDrawTriangle(v1, v2, v0, color); // side cover of the cone

			return;
		}

		for (int i = 0; i<3; i++) {
			v12[i] = v1[i]+v2[i];
		}
		normalize(v12);

		subdivideCone(v1, v12, depth-1);
		subdivideCone(v12, v2, depth-1);
	}

	/*
	 * Because it is now more efficient to draw all points at once, and do matrix transforms in the GPU, 
	 * this function adds all points generated by subdivide methods to a growing list, and only empties it
	 * when drawTriangle is finally called (after all points are done being added)
	 */
	public void prepareToDrawTriangle(float[] p1, float[] p2, float[] p3, float[] color) {
		float[][] vectors = {p1, p2, p3};
		for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				vPointList.add(vectors[i][j]);
				vColorList.add(color[j]);
			}
			vPointList.add(1.0f);
			vColorList.add(1.0f);
		}
	}
	
	/**
	 * Populates the vNorms array with normals generated from values in the vPoints array.
	 * Uses algorithm described here: https://www.khronos.org/opengl/wiki/Calculating_a_Surface_Normal
	 */
	protected void generateVectorNorms() {
		vNorms = new float[vPoints.length];
		
		for (int i=0; i<vPoints.length; i += 3*4) {
			float p1[]=new float[3], p2[]=new float[3], p3[]=new float[3];
			float norm[] = new float[4];
			
			for (int j=0; j<3; j++) {
				p1[j] = vPoints[i+j];
				p2[j] = vPoints[i+4+j];
				p3[j] = vPoints[i+8+j];
			}
			
			float v[] = {p2[0]-p1[0], p2[1]-p1[1], p2[2]-p1[2]};
			float u[] = {p3[0]-p1[0], p3[1]-p1[1], p3[2]-p1[2]};

			norm[0] = u[1]*v[2] - u[2]*v[1];
			norm[1] = u[2]*v[0] - u[0]*v[2];
			norm[2] = u[0]*v[1] - u[1]*v[0];
			norm[3] = 0f;
			
			for (int j=0; j<12; j++) {
				vNorms[i+j] = norm[j%4];
			}
		}
	}

	/**
	 * Loads points stored in vPoints and vColors into buffers and updates modelMatrix uniform
	 */
	protected void loadPoints() {
		// Load points into buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// Load colors into buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[COLOR]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES, cBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(COLOR, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
	
		// Load normals into buffer
		generateVectorNorms();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[NORMALS]); // use handle 2 		
		FloatBuffer nBuf = Buffers.newDirectFloatBuffer(vNorms);
		gl.glBufferData(GL_ARRAY_BUFFER, nBuf.limit()*Float.BYTES, nBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(NORMALS, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// Load most recent matrices into uniform
		float[] mmx = modelView.peek();
		float[] pmx = projection.peek();
		float[] cmx = cameraView.peek();
		//float[] mv_lightPos = Matrix_Lib_iking.vecMult(Matrix_Lib_iking.getScaleFactor(mmx), LIGHT_POS);
		 
		gl.glProgramUniformMatrix4fv(vfPrograms, mmxPtr, 1, true, mmx, 0);
		gl.glProgramUniformMatrix4fv(vfPrograms, pmxPtr, 1, true, pmx, 0);
		gl.glProgramUniformMatrix4fv(vfPrograms, cvPtr, 1, true, cmx, 0);
		gl.glProgramUniform4fv(vfPrograms, lightPosPtr, 1, LIGHT_POS, 0);
	}
	
	/**
	 * Loads points that do not require vector norms. Used for drawing lines
	 */
	protected void loadPointsNoNorms() {
		// Load points into buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// Load colors into buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[COLOR]); // use handle 1 		
		FloatBuffer cBuf = Buffers.newDirectFloatBuffer(vColors);
		gl.glBufferData(GL_ARRAY_BUFFER, cBuf.limit()*Float.BYTES, cBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(COLOR, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// Load normals into buffer
		vNorms = new float[vPoints.length];	// Assign memory that's never read so it isn't null
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[NORMALS]); // use handle 2 		
		FloatBuffer nBuf = Buffers.newDirectFloatBuffer(vNorms);
		gl.glBufferData(GL_ARRAY_BUFFER, nBuf.limit()*Float.BYTES, nBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(NORMALS, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer
		
		// Load most recent matrices into uniform
		float[] mmx = modelView.peek();
		float[] pmx = projection.peek();
		
		gl.glProgramUniformMatrix4fv(vfPrograms, mmxPtr, 1, true, mmx, 0);
		gl.glProgramUniformMatrix4fv(vfPrograms, pmxPtr, 1, true, pmx, 0);
	}
	
	/**
	 * Loads points in vPoints and loads vPoints.size/4 copies of color into vColor
	 * @param color
	 */
	protected void loadPoints(float[] color) {
		vColors = new float[vPoints.length];
		
		for (int i=0; i<vColors.length; i++) {
			vColors[i] = color[i%4];
		}
		
		loadPoints();
	}
	
	protected void loadPointsNoNorms(float[] color) {
		vColors = new float[vPoints.length];
		
		for (int i=0; i<vColors.length; i++) {
			vColors[i] = color[i%4];
		}	
		
		loadPointsNoNorms();
	}
	
	/*
	 * Draws whatever is in the vPointList and vColorList, then empties those lists for later use 
	 */
	public void drawTriangle() {
		vPoints = new float[vPointList.size()];
		vColors = new float[vColorList.size()];
		
		for (int i=0; i<vPointList.size(); i++) {
			vPoints[i] = (float) vPointList.get(i);
			vColors[i] = (float) vColorList.get(i);
		}
		
		loadPoints();
		
		// Draw vectors
		gl.glDrawArrays(GL_TRIANGLES, 0, vPoints.length / 4);
		
		// Empty arrays
		vPointList.clear();
		vColorList.clear();
	}
	
	/*
	 * Draws one triangle at a time (inefficient, but required for legacy code)
	 */
	public void drawtriangle(float[] p1, float[] p2, float[] p3, float[] color) {
		prepareToDrawTriangle(p1, p2, p3, color);
		drawTriangle();
	}
	
	public void drawCone() {
		subdivideCone(cVdata[0], cVdata[1], depth);
		subdivideCone(cVdata[1], cVdata[2], depth);
		subdivideCone(cVdata[2], cVdata[3], depth);
		subdivideCone(cVdata[3], cVdata[0], depth);
		drawTriangle();
	}


	public static void main(String[] args) {
		J2_5_Cone_iking f = new J2_5_Cone_iking();

		f.setTitle("JOGL J2_5_Cone");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		tr = new TextRenderer(new Font("Helvetica", Font.BOLD, 18));
		
		vShaderSource = readShaderSource(this.vShaderSourceFile); // read vertex shader
		fShaderSource = readShaderSource(this.fShaderSourceFile); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
		
		// 1. generate vertex arrays indexed by vao
		gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
		System.out.println(vao.length); // we only use one vao
		gl.glBindVertexArray(vao[0]); // use handle 0
		
		// 2. generate vertex buffers indexed by vbo: here vertices and colors
		gl.glGenBuffers(vbo.length, vbo, 0);
		System.out.println(vbo.length); // we use two: position and color
				
		// 5. enable VAO with loaded VBO data
		gl.glEnableVertexAttribArray(POSITION); // enable the 0th vertex attribute: position
		gl.glEnableVertexAttribArray(COLOR); 	// enable the 1th vertex attribute: color
		gl.glEnableVertexAttribArray(NORMALS);	// enable the 2nd vertex attribute: normal vector
		
		// 6. Get locations of matrix ptrs
		mmxPtr = gl.glGetUniformLocation(vfPrograms, "modelview_mx");
		cvPtr = gl.glGetUniformLocation(vfPrograms, "cameraview_mx");
		pmxPtr = gl.glGetUniformLocation(vfPrograms, "projection_mx");
		
		lightPosPtr = gl.glGetUniformLocation(vfPrograms, "light_pos");
		isShadingPtr = gl.glGetUniformLocation(vfPrograms, "isShaded");
		
		gl.glProgramUniform1i(vfPrograms, isShadingPtr, 1);
		gl.glProgramUniform4fv(vfPrograms, lightPosPtr, 1, LIGHT_POS, 0);
	}
}
