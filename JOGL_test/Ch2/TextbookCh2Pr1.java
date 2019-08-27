import java.nio.*;
import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

public class TextbookCh2Pr1 extends JFrame implements GLEventListener
	{ 
	private GLCanvas myCanvas;
	
	public TextbookCh2Pr1()
	{ 
		setTitle("Chapter2 - program1");
		setSize(600, 400);
		setLocation(200, 200);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		setVisible(true); // This initializes OpenGL and creates a GL4 object
	}
	
	// The OpenGL object created in the constructor calls init first, and passes it myCanvas
	// Then, the OpenGL object calls display
	public void display(GLAutoDrawable drawable)
	{ 
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// The C OpenGL function for glClearBuffer expects a pointer; Java does not have pointers
		// However, we can use a floatbuffer object instead, which essentially is a pointer to bkg
		// Note that color is a 4D vector for RGB and A
		float bkg[] = { 1.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		
		// We use the GL4 interface to make calls to the underlying C OpenGL library
		// This call tells OpenGL we are putting a color buffer into it's 0th slot for such buffers
		// The final param is the pointer to the data that we are loading in
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);
	}
	
	public static void main(String[ ] args)
	{ 
		new TextbookCh2Pr1();
	}
	
	public void init(GLAutoDrawable drawable) { }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }
	public void dispose(GLAutoDrawable drawable) { }
}
