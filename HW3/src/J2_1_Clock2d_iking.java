import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Scanner;
import java.util.Vector;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

/*
 * This will be the base class for all that follow. It will be inherited by all other classes for this 
 * project. It implements all methods specified by GLEventListener and adds all fields required for 
 * VBOs and VAOs
 */



public class J2_1_Clock2d_iking extends Frame implements GLEventListener {
	static GLCanvas canvas; // drawable in a frame
	static GL4 gl; // handle to OpenGL functions
	static int WIDTH = 800, HEIGHT = 800; // used to set the window size
	FPSAnimator animator; // for thread that calls display() repetitively
	
	private String vShaderSourceFile = "src/uniform_colors_iking_v.shader";
	private String fShaderSourceFile = "src/uniform_colors_iking_f.shader";
	
	int vfPrograms; // handle to shader programs
	protected int vao[ ] = new int[1]; // vertex array object (handle), for sending to the vertex shader
	protected int vbo[ ] = new int[2]; // vertex buffers objects (handles) to stores position, color, normal, etc
	protected int POSITION = 0, COLOR = 1;
	
	// Uniform locations
	protected int colorPtr;
	protected int mxPtr;
	
	// array of vertices and colors corresponding to the vertices
	float vPoints[]; 
	float vColors[]; 
	
	static final float PI = 3.1415926f;
	// homogeneous coordinates
	static float c[] = {0, 0, 1};
	static float h[] = {0, WIDTH/6, 1};
	
	private MatrixStack matStack = new MatrixStack();

	static long curTime;
	static float hAngle, hsecond, hminute, hhour;

	public J2_1_Clock2d_iking() {
		// 1. specify a drawable: canvas
		canvas = new GLCanvas();
		System.out.println("Hello, JOGL!\n");

		// 2. listen to the events related to canvas: init, reshape, display, and
		// dispose
		canvas.addGLEventListener(this); // "this" is the current instantiated object in main()
		this.add(canvas);

		// 3. set the size of the frame and make it visible
		setSize(WIDTH, HEIGHT);
		setVisible(true);
		
		animator = new FPSAnimator(canvas, 40); // 40 calls per second; frame rate
		animator.start();

		System.out.println("Select 'Run->Run Configurations', 'Arguments'; add the following lines under 'VM arguments': ");
		System.out.println("-Djogamp.gluegen.UseTempJarCache=false");

		// 4. external window destroy event: dispose resources before exit
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose(); // dispose the window and its subclasses, it calls GLEventListener's dispose()
							// as well
				System.exit(0);
			}
		});
	}
	
	public void display(GLAutoDrawable glDrawable) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		curTime = System.currentTimeMillis()/1000;

		hsecond = curTime%60;
		curTime = curTime/60;
		hminute = curTime%60 + hsecond/60;
		curTime = curTime/60;
		hhour = (curTime%12)+(hminute/60)+8; // adjust for EST
											// Daylight savings will be in effect when this is submitted
											// so it may be an hour off? 

		hAngle = -(hsecond*PI)/30; // arc angle
		
		// Second hand
		matStack.push();
		gl.glProgramUniform4f(vfPrograms, colorPtr, 1f, 0f, 0f, 1f);
		float[] transform = matStack.peek();
	
		matStack.translate(c[0], c[1]);
		matStack.rotate(hAngle);
		matStack.translate(-c[0], -c[1]);
		gl.glLineWidth(1);
		transDrawClock(c, h);

		
		// Minute hand
		matStack.pop();
		matStack.push();
		gl.glProgramUniform4f(vfPrograms, colorPtr, 0f, 1f, 0f, 1f); // minute hand in green
	
		hAngle = -(hminute*PI)/30; // arc angle
		matStack.translate(c[0], c[1]);
		matStack.scale(0.8f, 0.8f); // minute hand shorter
		matStack.rotate(hAngle);
		matStack.translate(-c[0], -c[1]);
		gl.glLineWidth(2);
		transDrawClock(c, h);

		// Hour hand
		matStack.pop();
		matStack.push();
		gl.glProgramUniform4f(vfPrograms, colorPtr, 0f, 0f, 1f, 1f); // hour hand in blue
		
		hAngle = -(hhour*PI)/6; // arc angle
		matStack.translate(c[0], c[1]);
		matStack.scale(0.5f, 0.5f); // hour hand shortest
		matStack.rotate(hAngle);
		matStack.translate(-c[0], -c[1]);
		gl.glLineWidth(3);
		transDrawClock(c, h);
		
		matStack.pop();
	}


	public void transDrawClock(float C[], float H[]) {
		vPoints = new float[] {
				C[0], C[1], C[2], 1.0f,
				H[0], H[1], H[2], 1.0f
		};
		
		// Load points into buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[POSITION]); // use handle 0 		
		FloatBuffer vBuf = Buffers.newDirectFloatBuffer(vPoints);
		gl.glBufferData(GL_ARRAY_BUFFER, vBuf.limit()*Float.BYTES, vBuf, GL_STATIC_DRAW); 
		gl.glVertexAttribPointer(POSITION, 4, GL_FLOAT, false, 0, 0); // associate vbo[0] with active VAO buffer

		// Load most recent matrix into uniform
		float[] mmx = matStack.peek();
		gl.glProgramUniformMatrix4fv(vfPrograms, mxPtr, 1, true, mmx, 0);

		// Draw line
		gl.glDrawArrays(GL_LINES, 0, vPoints.length / 4);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		vShaderSource = readShaderSource(vShaderSourceFile); // read vertex shader
		fShaderSource = readShaderSource(fShaderSourceFile); // read fragment shader
		vfPrograms = initShaders(vShaderSource, fShaderSource);		
		
		// 1. generate vertex arrays indexed by vao
		gl.glGenVertexArrays(vao.length, vao, 0); // vao stores the handles, starting position 0
		System.out.println(vao.length); // we only use one vao
		gl.glBindVertexArray(vao[0]); // use handle 0
		
		// 2. generate vertex buffers indexed by vbo: here vertices and colors
		gl.glGenBuffers(vbo.length, vbo, 0);
		System.out.println(vbo.length); // we use two: position and color
				
		// 5. enable VAO with loaded VBO data
		gl.glEnableVertexAttribArray(0); // enable the 0th vertex attribute: position
		gl.glEnableVertexAttribArray(1); // enable the 1th vertex attribute: color
		
		colorPtr = gl.glGetUniformLocation(vfPrograms, "color");
		mxPtr = gl.glGetUniformLocation(vfPrograms, "modelMatrix");
	}

	public static void main(String[] args) {
		J2_1_Clock2d_iking f = new J2_1_Clock2d_iking();

		f.setTitle("JOGL J2_1_Clock2d");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}

	
	/*
	 * Helper methods provided in new JOGL files
	 */
	
	public int initShaders(String vShaderSource[], String fShaderSource[]) {

		// 1. create, load, and compile vertex shader
		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		gl.glShaderSource(vShader, vShaderSource.length, vShaderSource, null, 0);
		gl.glCompileShader(vShader);

		// 2. create, load, and compile fragment shader
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);
		gl.glShaderSource(fShader, fShaderSource.length, fShaderSource, null, 0);
		gl.glCompileShader(fShader);

		// 3. attach the shader programs
		int vfProgram = gl.glCreateProgram(); // for attaching v & f shaders
		gl.glAttachShader(vfProgram, vShader);
		gl.glAttachShader(vfProgram, fShader);

		// 4. link the program
		gl.glLinkProgram(vfProgram); // successful linking --ready for using

		gl.glDeleteShader(vShader); // attached shader object will be flagged for deletion until 
									// it is no longer attached
		gl.glDeleteShader(fShader);

		// 5. Use the program
		gl.glUseProgram(vfProgram);
		gl.glDeleteProgram(vfProgram); // in-use program object will be flagged for deletion until 
										// it is no longer in-use

		return vfProgram;
	}
	
	public String[] readShaderSource(String filename) { // read a shader file into an array
		Vector<String> lines = new Vector<String>(); // Vector object for storing shader program
		Scanner sc;
		
		try {
			sc = new Scanner(new File(filename)); //Scanner object for reading a shader program
		} catch (IOException e) {
			System.err.println("IOException reading file: " + e);
			return null;
		}
		while (sc.hasNext()) {
			lines.addElement(sc.nextLine());
		}
		String[] shaderProgram = new String[lines.size()];
		for (int i = 0; i < lines.size(); i++) {
			shaderProgram[i] = (String) lines.elementAt(i) + "\n";
		}
		sc.close(); 
		return shaderProgram; //  a string of shader programs
	}


	@Override
	public void dispose(GLAutoDrawable drawable) {
		System.out.println("d) dispose is called before exiting.");
	}


	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		System.out.println("b) reshape is called whenever the frame is resized.");
	}
}
