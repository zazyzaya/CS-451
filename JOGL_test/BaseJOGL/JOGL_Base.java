
/*************************************************
 * Created on August 1, 2017, @author: Jim X. Chen
 *
 * JOGL initial set up: 
 * Open a frame with a background color
 *
 *
 */

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.*;
//import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

public class JOGL_Base extends Frame implements GLEventListener {
	static GLCanvas canvas; // drawable in a frame
	static GL4 gl; // handle to OpenGL functions
	static int WIDTH = 800, HEIGHT = 800; // used to set the window size

	public JOGL_Base() { // constructor that runs at instantiation

		// 1. specify a drawable: canvas
		canvas = new GLCanvas();
		// 2. listen to the events related to canvas: init, reshape, display, and
		// dispose
		canvas.addGLEventListener(this); // "this" is the current instantiated object in main()
		this.add(canvas);

		// 3. set the size of the frame and make it visible
		setSize(WIDTH, HEIGHT);
		setVisible(true);

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

	public static void main(String[] args) {
		// 0. starts by instantiating a class, which calls the constructor
		new JOGL_Base();
		// 5. after the instantiation, the system gets into the event loop
	}

	// called once for OpenGL initialization
	public void init(GLAutoDrawable drawable) {
		System.out.println("\na) init called once for initialization.");

		// 6. interface to OpenGL functions
		gl = (GL4) drawable.getGL();
	}

	// called for handling drawing area when it is reshaped
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		
		System.out.println("b) reshape is called whenever the frame is resized.");
	}

	// called for OpenGL rendering for every reshaping
	public void display(GLAutoDrawable drawable) {
		System.out.println("c) display called for every reshape.");

		// 7. specify background color
		float bgColor[] = {0.0f, 1.0f, 1.0f, 1.0f }; // Cyan
		FloatBuffer bgColorBuffer = Buffers.newDirectFloatBuffer(bgColor);
		// Java does not have pointers.

		// 8. clear the back-buffer into the background color
		gl.glClearBufferfv(GL_COLOR, 0, bgColorBuffer); //0 -- left front buffer

		// Any parameter in OpenGL that is a pointer is changed in JOGL. In this case,
		// JOGL utilizes a FloatBuffer instead.
	}

	// For releasing of all OpenGL resources related to a drawable, manually called
	// at the end 
	public void dispose(GLAutoDrawable drawable) {
		System.out.println("d) dispose is called before exiting.");
	}
}
