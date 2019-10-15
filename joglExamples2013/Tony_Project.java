import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sun.opengl.cg.CGcontext;
import com.sun.opengl.cg.CGparameter;
import com.sun.opengl.cg.CGprogram;
import com.sun.opengl.cg.CgGL;
import com.sun.opengl.util.Animator;


public class Tony_Project extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//
	// Global caching information for spheres that are
	// subdivided three times.
	//
	
	float sVdata[][];
	float Vdata[][];
	float Tdata[][];
	float Bdata[][];
	float STdata[][];
	int StackCounter;
	boolean Generated=false;

	//
	// Global caching information for spheres that are
	// subdivided only two times.
	//
	
	float roughsVdata[][];
	float roughVdata[][];
	float roughTdata[][];
	float roughBdata[][];
	float roughSTdata[][];
	int roughStackCounter;
	boolean roughGenerated=false;

	//
	// All the tags I will need for the various
	// textures that I switch in and out to
	// give the different views of the object.
	//
	
	final int[] COLOR_TEX = new int[1];
	final int[] NORMAL_TEX = new int[1];
	final int[] WALL_COLOR_TEX = new int[1];
	final int[] WALL_NORMAL_TEX = new int[1];
	final int[] WALL_LUM_TEX = new int[1];
	final int[] LUM_TEX = new int[1];
	final int[] LO_COLOR_TEX = new int[1];
	final int[] LO_NORMAL_TEX = new int[1];
	final int[] LO_LUM_TEX = new int[1];
	final int[] WHITE_TEX = new int[1];
	final int[] BLACK_TEX = new int[1];
	final int[] GRAY_TEX = new int[1];
	final int[] FLAT_TEX = new int[1];

	//
	// The rotation amount for the spinning sphere
	// in 'display sphere' mode.
	//
	
	float Deg;

	//
	// The position of the Light Slider that allows
	// movement of the main light source (defined as
	// an integer, since that's what the slider returns.
	//
	
	int lightX=30;

	//
	// Parameter tags to allow me to link into the various
	// properties of the vertex and fragment programs.
	//
	
	static CGprogram fragmentprog;
    static final int FRAGMENTPROFILE=CgGL.CG_PROFILE_ARBFP1;
	static CGcontext cgcontext;
	static CGprogram vertexprog;
	CGparameter 
	modelviewprojection, // modelviewProjection matrix
	modelview, // modelview matrix
	inversetranspose, //inverse transpose of the modelview matrix
	TVector,
	BVector,
	myLa, //light source ambient
	myLd, //light source diffuse
	myLs, //light source specular
	myLightPosition, // light source position
	myLa2, //light source ambient
	myLd2, //light source diffuse
	myLs2, //light source specular
	myLightPosition2, // light source position
	myEyePosition, 
	myMe, // material emission
	myMa, // material ambient
	myMd, // material diffuse
	myMs, // material specular
	myShininess, // material shininess
	myPulse,     // Pulse of light
	imgTexture, // Texture
	normalTexture, // Normal map
    lumTexture,    // Luminosity/colorswap map
    swapTexture,   // Color swap map
    mySwap;        // Color swap specification
	
	//
	// Some global space for pulling out the current
	// matrix in order to find a transformed position for
	// lights to send to the CG programs.
	//
	
	float[] currM = new float[16];


	static final int VERTEXPROFILE = CgGL.CG_PROFILE_ARBVP1;

	public abstract class TonyGLCanvas extends GLCanvas implements GLEventListener {

		//
		// The TonyGLCanvas extends a GLCanvas in order to support
		// Draw-Frame and Update procedures.  These link into the
		// Animator to create a constant series of calls.
		//

		private static final long serialVersionUID = 1L;
		Animator  animator; // drive display() in a loop
		GL gl; // interface to OpenGL
		long lastDisplay;
		int WIDTH;
		int HEIGHT;
		
		public TonyGLCanvas(int wide, int high) {
			
			WIDTH = wide;
			HEIGHT = high;
			
			addGLEventListener(this);
		    
			gl = getGL();		
			
		}
			
		public void init(GLAutoDrawable drawable) {

			//
			// Clear the background the first time,
			// just to be tidy.
			//

			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			
			//
			// Create an animator and start up its thread
			// in order to get constant calls.
			//

			animator = new Animator(this);
			animator.start(); // start animator thread

			// Get a starting point for the update timer.
					
			lastDisplay=System.currentTimeMillis();
		}
		
		public void reshape(GLAutoDrawable drawable, int x, int y, int width,
				int height) {

			WIDTH = width; // new width and height saved
			HEIGHT = height;

			// Specify the drawing area and perspective

			gl.glMatrixMode(GL.GL_PROJECTION);
		    gl.glLoadIdentity();
			gl.glFrustum(-width/200, width/200, -height/200, height/200, 10.0, 5000.0);		
		    gl.glMatrixMode(GL.GL_MODELVIEW);
		    gl.glLoadIdentity();
					
		}
		
		public void display(GLAutoDrawable drawable) {
			
			long currTime=System.currentTimeMillis();
			
			long increment = currTime-lastDisplay;
			Update(increment);
			DrawFrame(drawable);
			
			currTime=System.currentTimeMillis();
			
			//
			// Don't hog more than 60 frames-per-second worth
			// of processing cycles.  Figure out how many
			// milliseconds it has cost you to draw your
			// image, and pad that out with a sleep command
			// to a round 15 milliseconds.
			//
			
			int sleep=15-(int)(currTime-lastDisplay);
			lastDisplay = currTime;		

			if (sleep>0)
			{
				try { Thread.sleep(sleep); } catch (Exception ignore) {	}				
			}

		}
		
		// called if display mode or device are changed
		public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
				boolean deviceChanged) {
		}

		//
		// These abstract procedures will be filled in by a class
		// that implements this schema.
		//
		
		public abstract void DrawFrame(GLAutoDrawable drawable);
		public abstract void Update(long increment);
		
	}
	
	public class T2_Sphere {

		float CenterX;
		float CenterY;
		float CenterZ;
		float Radius;
		float pivotx;
		float pivoty;
		float pivotz;

		float Deg;

		float[] Color;
		float Pulse;


		//
		// A whole batch of basic constructors, in integer and float
		// with and without specified radius.
		//
		  
		public T2_Sphere(int x, int y, int z) {
			CenterX = (float)x;
			CenterY = (float)y;
			CenterZ = (float)z;
			Radius = 10.0f;
			Color= new float[4];
			Color[0]=1.0f;
			Color[1]=1.0f;
			Color[2]=1.0f;
			Color[3]=1.0f;
			GeneratePoints();
		}
		public T2_Sphere(float x, float y, float z) {
			CenterX = x;
			CenterY = y;
			CenterZ = z;
			Radius = 10.0f;
			Color= new float[4];
			Color[0]=1.0f;
			Color[1]=1.0f;
			Color[2]=1.0f;
			Color[3]=1.0f;
			GeneratePoints();
		}
		public T2_Sphere(int x, int y, int z, int r) {
			CenterX = (float)x;
			CenterY = (float)y;
			CenterZ = (float)z;
			Radius = (float)r;
			Color= new float[4];
			Color[0]=1.0f;
			Color[1]=1.0f;
			Color[2]=1.0f;
			Color[3]=1.0f;
			GeneratePoints();
		}
		public T2_Sphere(float x, float y, float z, float r) {
			CenterX = x;
			CenterY = y;
			CenterZ = z;
			Radius = r;
			Color= new float[4];
			Color[0]=1.0f;
			Color[1]=1.0f;
			Color[2]=1.0f;
			Color[3]=1.0f;
			GeneratePoints();
		}
		
		//
		// An access procedure to allow setting of the unique
		// color for the sphere.
		//
		
		public void SetColor(float r, float g, float b)
		{
			Color[0]=r;
			Color[1]=g;
			Color[2]=b;
		}
					
		//
		// Stack operators for filling in the global vertex
		// data for the sphere the first time it is used.
		//
		
		public void ClearStack() {
			StackCounter=0;
		}
		
		public void PushVertex(float[] vertex, float[] Tspace, float[] Bspace, boolean WrapEdge) {

			//
			// Vertex information is saved, as well as vector
			// information for the T and B vectors of the tangent
			// space.  Normal information is not needed, since the
			// data is being saved for a unit-radius sphere centered
			// on the origin.  Therefore the position is the same as
			// the normal.
			//
			
			Vdata[StackCounter][0]=vertex[0];
			Vdata[StackCounter][1]=vertex[1];
			Vdata[StackCounter][2]=vertex[2];
			Tdata[StackCounter][0]=Tspace[0];
			Tdata[StackCounter][1]=Tspace[1];
			Tdata[StackCounter][2]=Tspace[2];
			Bdata[StackCounter][0]=Bspace[0];
			Bdata[StackCounter][1]=Bspace[1];
			Bdata[StackCounter][2]=Bspace[2];

			//
			// Convert the vertex data into three dimensional polar
			// (spherical) coordinates.  This will be used to map
			// textures seamlessly onto the sphere.
			//
			// Texture data, of course, must be pre-distorted to
			// counter the way that pixels cluster at the poles.
			//
			
			float T_value = (float)Math.asin(vertex[1]);
			T_value /= (float)Math.PI;
			T_value += 0.5;
			float D = (float)Math.sqrt(1-(vertex[1]*vertex[1]));
			float S_value;
			if (D==0)
			{
				S_value=0.5f;
			} else
			{
				float tempV = vertex[0]/D;
				
				if (tempV>1) { tempV = 1; }
				if (tempV<-1) { tempV = -1; }
				
				if (vertex[2]<0)
				{
					S_value = (float)Math.asin(tempV);
					S_value /= (float)2*Math.PI;
					S_value += 0.25;				
				} else
				{
					S_value = (float)Math.asin(-tempV);
					S_value /= (float)2*Math.PI;
					S_value += 0.75;				
				} 	
				if ((S_value > 0.99f) && WrapEdge)
				{
					S_value = 0.0f;
				}
			}

			STdata[StackCounter][0]=S_value;
			STdata[StackCounter][1]=T_value;
			
			StackCounter++;
		}
		
		//
		// This procedure creates the cached geometry for a sphere.
		//
		
		public void GeneratePoints() {
			if (!Generated) 
			{
				sVdata = new float[][] { {1, 0, 0}
										, {0, 1, 0}
										, {0, 0, 1}
										, {-1, 0, 0}
										, {0, -1, 0}
										, {0, 0, -1} };
				
				float[][] sTdata = new float[][] { { 0, -1, 0}
												  , {-1, 0, 0}
												  , {0, -1, 0}
												  , {0, -1, 0}
												  , {1, 0, 0}
												  , {0, -1, 0} };

				float[][] sBdata = new float[][] { { 0, 0, 1}
				  								  , {0, 0, 1}
				  								  , {-1, 0, 0}
				  								  , {0, 0, -1}
				  								  , {0, 0, -1}
				  								  , {1, 0, 0} };

				Vdata = new float[1600][3];
				Tdata = new float[1600][3];
				Bdata = new float[1600][3];
				STdata = new float[1600][2];
				int depth=3;
		  
				ClearStack();
		  
				subdivideSphere(sVdata[1], sTdata[1], sBdata[1],
							    sVdata[2], sTdata[2], sBdata[2],
							    sVdata[0], sTdata[0], sBdata[0], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
							    sVdata[0], sTdata[0], sBdata[0],
							    sVdata[2], sTdata[2], sBdata[2], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
						        sVdata[5], sTdata[5], sBdata[5],
						        sVdata[0], sTdata[0], sBdata[0],depth);
				subdivideSphere(sVdata[1], sTdata[1], sBdata[1],
						        sVdata[0], sTdata[0], sBdata[0],
						        sVdata[5], sTdata[5], sBdata[5], depth);

				subdivideSphere(sVdata[1],  sTdata[1], sBdata[1],
						        sVdata[5], sTdata[5], sBdata[5],
						        sVdata[3], sTdata[3], sBdata[3], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
						        sVdata[3], sTdata[3], sBdata[3],
						        sVdata[5], sTdata[5], sBdata[5], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
						        sVdata[2], sTdata[2], sBdata[2],
						        sVdata[3], sTdata[3], sBdata[3], depth);
				subdivideSphere(sVdata[1], sTdata[1], sBdata[1],
						        sVdata[3], sTdata[3], sBdata[3],
						        sVdata[2], sTdata[2], sBdata[2], depth);
				
				Generated = true;
		  
			}
		}
		
		//
		// This is our old standby that takes a triangle on the
		// sphere and subdivides it for curvature.  It is updated
		// to maintain T and B vectors (for tangent space) as well
		// as to maintain the tricky wrapping of the texture
		// coordinate system.
		//
		
		public void subdivideSphere(float v1[],
				  float t1[],
				  float b1[],
			      float v2[],
			      float t2[],
			      float b2[],          
			      float v3[],
			      float t3[],
			      float b3[],
			      int depth) {
			    float v12[] = new float[3];
			    float v23[] = new float[3];
			    float v31[] = new float[3];
			    float t12[] = new float[3];
			    float t23[] = new float[3];
			    float t31[] = new float[3];
			    float b12[] = new float[3];
			    float b23[] = new float[3];
			    float b31[] = new float[3];

			    if (depth<=0) {
			
			    	
			    	//
			    	// This checks whether the vertex is near
			    	// an edge that wraps around.  These edges
			    	// mess with the arc-cosine functionality,
			    	// and can result in a texture interpolating
			    	// all the way back to zero, rather than
			    	// interpolating around to a value of one.
			    	//
			    	
			      boolean WrapEdge = false;
			      
			      if (v1[2]<0) { WrapEdge = true; }
			      if (v2[2]<0) { WrapEdge = true; }
			      if (v3[2]<0) { WrapEdge = true; }

			      PushVertex(v1, t1, b1, WrapEdge);
			      PushVertex(v2, t2, b2, WrapEdge);
			      PushVertex(v3, t3, b3, WrapEdge);
			      return;
			    }
			    v12[0] = v1[0]+v2[0];
			    v23[0] = v2[0]+v3[0];
			    v31[0] = v3[0]+v1[0];
			    v12[1] = v1[1]+v2[1];
			    v23[1] = v2[1]+v3[1];
			    v31[1] = v3[1]+v1[1];
			    v12[2] = v1[2]+v2[2];
			    v23[2] = v2[2]+v3[2];
			    v31[2] = v3[2]+v1[2];

			    normalize(v12);
			    normalize(v23);
			    normalize(v31);

			    t12[0] = v12[0];
			    t12[1] = v12[1]-1;
			    t12[2] = v12[2];
			    t23[0] = v23[0];
			    t23[1] = v23[1]-1;
			    t23[2] = v23[2];
			    t31[0] = v31[0];
			    t31[1] = v31[1]-1;
			    t31[2] = v31[2];
			    
			    crossproduct(t12, v12, b12);
			    normalize(b12);
			    crossproduct(v12, b12, t12);
			    normalize(t12);
			    crossproduct(t23, v23, b23);
			    normalize(b23);
			    crossproduct(v23, b23, t23);
			    normalize(t23);
			    crossproduct(t31, v31, b31);
			    normalize(b31);
			    crossproduct(v31, b31, t31);
			    normalize(t31);
			    			    
			    subdivideSphere(v1, t1, b1, v12, t12, b12, v31, t31, b31, depth-1);
			    subdivideSphere(v2, t2, b2, v23, t23, b23, v12, t12, b12, depth-1);
			    subdivideSphere(v3, t3, b3, v31, t31, b31, v23, t23, b23, depth-1);
			    subdivideSphere(v12, t12, b12, v23, t23, b23, v31, t31, b31, depth-1);
			  }
		
		public void normalize(float vector[]) {
			float d = (float) Math.sqrt(vector[0] * vector[0] + vector[1]
					* vector[1] + vector[2] * vector[2]);

			if (d == 0) {
				System.out.println("0 length vector: normalize().");
				return;
			}
			vector[0] /= d;
			vector[1] /= d;
			vector[2] /= d;
		}
		
		public void crossproduct(float a[], float b[], float result[])
		{
			result[0]=a[1]*b[2]-a[2]*b[1];
			result[1]=a[2]*b[0]-a[0]*b[2];
			result[2]=a[0]*b[1]-a[1]*b[0];
		}

		public void draw(GL gl)
		{

			drawSphere(gl);

		}

		//
		// An accessor function, in order to have something to
		// overload in other classes that extend T_Sphere.
		//
		
		public int numVertices()
		{
			return StackCounter;
		}
	
		//
		// This function pulls out the vertex (and therefore the
		// normal) information, the T and B vectors (for tangent
		// space) and the S and T data for texture coordinates,
		// then renders some vertices into the vertex program.
		//
		
		public void drawVertex(GL gl, int a) 
		{
				float temp[]=new float[4];
				temp[3]=1.0f;

				gl.glTexCoord2f(STdata[a][0], STdata[a][1]);
				temp[0]=Tdata[a][0];
				temp[1]=Tdata[a][1];
				temp[2]=Tdata[a][2];
				CgGL.cgSetParameter4fv(TVector, temp, 0);
				temp[0]=Bdata[a][0];
				temp[1]=Bdata[a][1];
				temp[2]=Bdata[a][2];
				CgGL.cgSetParameter3fv(BVector, Bdata[a], 0);
				gl.glNormal3f(Vdata[a][0], Vdata[a][1], Vdata[a][2]);
				gl.glVertex3fv(Vdata[a], 0);

		}

		//
		// Here is the actual code to draw a sphere.
		//
		
		public void drawSphere(GL gl) {

				ColorOut(gl);

				//
				// Position the sphere, including rotating it (for
				// later derived classes that have spin).
				//
				
				gl.glPushMatrix();
				gl.glTranslatef(CenterX, CenterY, CenterZ);
				gl.glScalef(Radius, Radius, Radius);
				gl.glRotatef(Deg, pivotx, pivoty, pivotz);

				//
				// Pass the transforms into the CG program.
				//
				
				CgGL.cgGLSetStateMatrixParameter(modelview,
						CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
				CgGL.cgGLSetStateMatrixParameter(inversetranspose,
						CgGL.CG_GL_MODELVIEW_MATRIX,
						CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
				CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
						CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
						CgGL.CG_GL_MATRIX_IDENTITY);

				//
				// If color swapping is active, then throw the sphere's
				// unique color into the fragment program, so its veins
				// can be rendered in that color.  Otherwise default to
				// purple.
				// 
				
				if (SwapMap)
				{
					CgGL.cgGLSetParameter3fv(mySwap, Color, 0);
				}
				else
				{
					CgGL.cgGLSetParameter3f(mySwap, 0.55f, 0.19f, 0.745f);
				}

				CgGL.cgGLEnableProfile(VERTEXPROFILE);
				CgGL.cgGLBindProgram(vertexprog);
				CgGL.cgGLEnableProfile(FRAGMENTPROFILE);
				CgGL.cgGLBindProgram(fragmentprog);

				//
				// If the sphere has been given a pulse (a short-lasting
				// burst of self-illumination for the luminous veins)
				// then pass that value on to the fragment program.
				//
				
				CgGL.cgSetParameter1f(myPulse, Pulse);

				int top=numVertices();
				for(int i=0;i<top;i+=3) {
					float temp[]=new float[4];
					temp[3]=1.0f;

					gl.glBegin(GL.GL_TRIANGLES);
					for(int j=0;j<3;j++) {
						drawVertex(gl, i+j);
					}
					gl.glEnd();
				}
				CgGL.cgSetParameter1f(myPulse, 0.0f);

				CgGL.cgGLDisableProfile(VERTEXPROFILE);
				CgGL.cgGLDisableProfile(FRAGMENTPROFILE);

				gl.glPopMatrix();
			  }

		public void ColorOut(GL gl)
		{
		}

		//
		// Tests whether two spheres intersect each other.
		//
		
		public boolean Intersect(T2_Sphere target)
		{
			float dx=CenterX-target.CenterX;
			float dy=CenterY-target.CenterY;
			float dz=CenterZ-target.CenterZ;
			float Rads=Radius+target.Radius;
			
			return (Math.sqrt(dx*dx+dy*dy+dz*dz)<=Rads);
		}
		
	}

	//
	// A Rough_Sphere is just like a normal sphere, but
	// rendered to a lower level of fidelity.  This is
	// useful for speeding up rendering in the cage display,
	// which uses numerous small spheres that don't really
	// need a full 3 levels of subdivision.
	//
	// Unfortunately, this does require pushing information
	// into a whole new global cache.  Therefore the stack
	// functions are overloaded to aim at this new data.
	//
	
	public class T2_Rough_Sphere extends T2_Sphere {
		public T2_Rough_Sphere(int x, int y, int z) {
			super(x, y, z);
		}
		public T2_Rough_Sphere(float x, float y, float z) {
			super(x, y, z);
		}
		public T2_Rough_Sphere(int x, int y, int z, int r) {
			super(x, y, z, r);
		}
		public T2_Rough_Sphere(float x, float y, float z, float r) {
			super(x, y, z, r);
		}

		public void ClearStack() {
			roughStackCounter=0;
		}

		public void PushVertex(float[] vertex, float[] Tspace, float[] Bspace, boolean WrapEdge) {
			roughVdata[roughStackCounter][0]=vertex[0];
			roughVdata[roughStackCounter][1]=vertex[1];
			roughVdata[roughStackCounter][2]=vertex[2];
			roughTdata[roughStackCounter][0]=Tspace[0];
			roughTdata[roughStackCounter][1]=Tspace[1];
			roughTdata[roughStackCounter][2]=Tspace[2];
			roughBdata[roughStackCounter][0]=Bspace[0];
			roughBdata[roughStackCounter][1]=Bspace[1];
			roughBdata[roughStackCounter][2]=Bspace[2];

			float T_value = (float)Math.asin(vertex[1]);
			T_value /= (float)Math.PI;
			T_value += 0.5;
			float D = (float)Math.sqrt(1-(vertex[1]*vertex[1]));
			float S_value;
			if (D==0)
			{
				S_value=0.5f;
			} else
			{
				float tempV = vertex[0]/D;
				
				if (tempV>1) { tempV = 1; }
				if (tempV<-1) { tempV = -1; }
				
				if (vertex[2]<0)
				{
					S_value = (float)Math.asin(tempV);
					S_value /= (float)2*Math.PI;
					S_value += 0.25;				
				} else
				{
					S_value = (float)Math.asin(-tempV);
					S_value /= (float)2*Math.PI;
					S_value += 0.75;				
				} 	
				if ((S_value > 0.99f) && WrapEdge)
				{
					S_value = 0.0f;
				}
			}

			roughSTdata[roughStackCounter][0]=S_value;
			roughSTdata[roughStackCounter][1]=T_value;
			
			roughStackCounter++;
		}
				
		public void GeneratePoints() {

			if (!roughGenerated) 
			{
				sVdata = new float[][] { {1, 0, 0}
										, {0, 1, 0}
										, {0, 0, 1}
										, {-1, 0, 0}
										, {0, -1, 0}
										, {0, 0, -1} };
				
				float[][] sTdata = new float[][] { { 0, -1, 0}
												  , {-1, 0, 0}
												  , {0, -1, 0}
												  , {0, -1, 0}
												  , {1, 0, 0}
												  , {0, -1, 0} };

				float[][] sBdata = new float[][] { { 0, 0, 1}
				  								  , {0, 0, 1}
				  								  , {-1, 0, 0}
				  								  , {0, 0, -1}
				  								  , {0, 0, -1}
				  								  , {1, 0, 0} };

				roughVdata = new float[400][3];
				roughTdata = new float[400][3];
				roughBdata = new float[400][3];
				roughSTdata = new float[400][2];
				int depth=2;
		  
				ClearStack();
		  
				subdivideSphere(sVdata[1], sTdata[1], sBdata[1],
							    sVdata[2], sTdata[2], sBdata[2],
							    sVdata[0], sTdata[0], sBdata[0], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
							    sVdata[0], sTdata[0], sBdata[0],
							    sVdata[2], sTdata[2], sBdata[2], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
						        sVdata[5], sTdata[5], sBdata[5],
						        sVdata[0], sTdata[0], sBdata[0],depth);
				subdivideSphere(sVdata[1], sTdata[1], sBdata[1],
						        sVdata[0], sTdata[0], sBdata[0],
						        sVdata[5], sTdata[5], sBdata[5], depth);

				subdivideSphere(sVdata[1],  sTdata[1], sBdata[1],
						        sVdata[5], sTdata[5], sBdata[5],
						        sVdata[3], sTdata[3], sBdata[3], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
						        sVdata[3], sTdata[3], sBdata[3],
						        sVdata[5], sTdata[5], sBdata[5], depth);
				subdivideSphere(sVdata[4], sTdata[4], sBdata[4],
						        sVdata[2], sTdata[2], sBdata[2],
						        sVdata[3], sTdata[3], sBdata[3], depth);
				subdivideSphere(sVdata[1], sTdata[1], sBdata[1],
						        sVdata[3], sTdata[3], sBdata[3],
						        sVdata[2], sTdata[2], sBdata[2], depth);
				
				Generated = true;
		  
			}
		}

		public int numVertices()
		{
			return roughStackCounter;
		}

		//
		// And then, of course, the drawVertex procedure draws
		// from this new data source as well.
		//
		
		public void drawVertex(GL gl, int a) {
			float temp[]=new float[4];
			temp[3]=1.0f;

			gl.glTexCoord2f(roughSTdata[a][0], roughSTdata[a][1]);
			temp[0]=roughTdata[a][0];
			temp[1]=roughTdata[a][1];
			temp[2]=roughTdata[a][2];
			CgGL.cgSetParameter4fv(TVector, temp, 0);
			temp[0]=roughBdata[a][0];
			temp[1]=roughBdata[a][1];
			temp[2]=roughBdata[a][2];
			CgGL.cgSetParameter3fv(BVector, roughBdata[a], 0);
			gl.glNormal3f(roughVdata[a][0], roughVdata[a][1], roughVdata[a][2]);
			gl.glVertex3fv(roughVdata[a], 0);

		}
		
	}

	//
	// A cage maintains and draws a box that the spheres can
	// bounce around in.
	//
	
	public class T2_Cage {

		int X_Crossings;
		int Y_Crossings;
		int Z_Crossings;
		float Left[][];
		float Right[][];
		float Top[][];
		float Bottom[][];
		float Front[][];
		float Back[][];
		boolean frontDraw=false;
		
		public T2_Cage (int x, int y, int z)
		{

			//
			// A cage has a resolution, and creates
			// the specified number of squares.  These
			// squares can be lit when they are collided
			// with by a sphere, so the cage has to maintain
			// data structures that update how the lighting
			// fades over time.
			//
			
			X_Crossings=x;
			Y_Crossings=y;
			Z_Crossings=z;
			Left=new float[Y_Crossings][Z_Crossings];
			Right=new float[Y_Crossings][Z_Crossings];
			Top=new float[X_Crossings][Z_Crossings];
			Bottom=new float[X_Crossings][Z_Crossings];
			Front=new float[X_Crossings][Y_Crossings];
			Back=new float[X_Crossings][Y_Crossings];
			for(int ix=0;ix<X_Crossings;ix++) {
				for(int iy=0;iy<Y_Crossings;iy++) {
					for(int iz=0;iz<Z_Crossings;iz++) {
						Left[iy][iz]=0.0f;
						Right[iy][iz]=0.0f;
						Top[ix][iz]=0.0f;
						Bottom[ix][iz]=0.0f;
						Front[ix][iy]=0.0f;
						Back[ix][iy]=0.0f;
					}
				}
				
			}
		}

		//
		// Given cage coordinates, get the position in global space.
		//
		
		public void GetCoords(int x, int y, int z, float coords[]) {
			coords[0]=(float)(x-((X_Crossings-1)/2));
			coords[1]=(float)(y-((Y_Crossings-1)/2));
			coords[2]=(float)(z-((Z_Crossings-1)/2));			
		}

		//
		// Given a position in global space, round down to cage
		// coordinates.  Used for knowing where a collision has
		// occurred.
		//
		
		public void GetIndex(float x, float y, float z, int index[]) {
			index[0]=(int)(Math.floor(x+((X_Crossings-1)/2)));
			index[1]=(int)(Math.floor(y+((Y_Crossings-1)/2)));
			index[2]=(int)(Math.floor(z+((Z_Crossings-1)/2)));
		}
		
		//
		// This procedures draws the cage, leaving the front open
		// so that the viewer can see into the space.
		//
		
		public void DrawOpen(GL gl)
		{
			int ax;
			int ay;
			int az;

			//
			// Grab the transformation matrices and start up the
			// CG rendering programs.
			//
			
			CgGL.cgGLSetStateMatrixParameter(modelview,
					CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
			CgGL.cgGLSetStateMatrixParameter(inversetranspose,
					CgGL.CG_GL_MODELVIEW_MATRIX,
					CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
			CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
					CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
					CgGL.CG_GL_MATRIX_IDENTITY);

			CgGL.cgGLEnableProfile(VERTEXPROFILE);
			CgGL.cgGLBindProgram(vertexprog);
			CgGL.cgGLEnableProfile(FRAGMENTPROFILE);
			CgGL.cgGLBindProgram(fragmentprog);
			
			//
			// The variables 'Ma' and 'Pulse' are altered in the running
			// of this procedure.  Make sure we can put them back the way
			// they were, at the end.
			//
			
			float[] tempMa = new float[3];
			float[] tempPulse = new float[3];

			CgGL.cgGetParameterValuefc(myPulse, 3, FloatBuffer.wrap(tempPulse));
			CgGL.cgGetParameterValuefc(myMa, 3, FloatBuffer.wrap(tempMa));

			//
			// The cage has terrible light facings, so bump its material ambience
			// way up, so that it will be visible no matter where the
			// lights get moved.
			//
			
			CgGL.cgSetParameter3f(myMa, 1.5f, 1.5f, 1.5f);

			//
			// Loop through all combinations of X and Z coordinates,
			// drawing the left and right squares.
			//
			
			for(ay=0;ay<Y_Crossings-1;ay++) {
				for(az=0;az<Z_Crossings-1;az++) {
					float coords[][] = new float[8][3];
					float corners[] = new float[4];

					//
					// The 'corners' array holds the points in texture
					// space that this square covers.  By keeping these
					// updated we spread one texture out across a grid
					// of many squares.
					//
					
					corners[0]=((float)ay)/Y_Crossings;
					corners[1]=((float)ay+1)/Y_Crossings;
					corners[2]=((float)az)/Z_Crossings;
					corners[3]=((float)az+1)/Z_Crossings;
					
					GetCoords(X_Crossings-1,ay,az, coords[0]);
					GetCoords(X_Crossings-1,ay,az+1, coords[1]);
					GetCoords(X_Crossings-1,ay+1,az+1, coords[2]);
					GetCoords(X_Crossings-1,ay+1,az, coords[3]);
						
					CgGL.cgSetParameter3f(myPulse, Right[ay][az],  Right[ay][az], Right[ay][az]);
					gl.glBegin(GL.GL_POLYGON);
					CgGL.cgSetParameter4f(TVector, 0, 1, 0, 0);
					CgGL.cgSetParameter4f(BVector, 0, 0, 1, 0);

					gl.glNormal3f(-1, 0, 0);
					gl.glTexCoord2f(corners[0],corners[2]);
					gl.glVertex3f(coords[0][0], coords[0][1], coords[0][2]);							
					gl.glTexCoord2f(corners[0],corners[3]);
					gl.glVertex3f(coords[1][0], coords[1][1], coords[1][2]);							
					gl.glTexCoord2f(corners[1],corners[3]);
					gl.glVertex3f(coords[2][0], coords[2][1], coords[2][2]);							
					gl.glTexCoord2f(corners[1],corners[2]);
					gl.glVertex3f(coords[3][0], coords[3][1], coords[3][2]);							
					gl.glEnd();
				
					GetCoords(0,ay,az, coords[0]);
					GetCoords(0,ay+1,az, coords[1]);
					GetCoords(0,ay+1,az+1, coords[2]);
					GetCoords(0,ay,az+1, coords[3]);
						
					gl.glColor3f(Left[ay][az], Left[ay][az], Left[ay][az]);
					CgGL.cgSetParameter3f(myPulse, Left[ay][az], Left[ay][az], Left[ay][az]);
					gl.glBegin(GL.GL_POLYGON);
					
					//
					// Because the squares are flat, we can define one T
					// and B vector for the tangent space for the whole
					// wall.
					//
					
					CgGL.cgSetParameter4f(TVector, 0, 1, 0, 0);
					CgGL.cgSetParameter4f(BVector, 0, 0, 1, 0);
					gl.glNormal3f(1, 0, 0);
					gl.glTexCoord2f(corners[0],corners[2]);
					gl.glVertex3f(coords[0][0], coords[0][1], coords[0][2]);							
					gl.glTexCoord2f(corners[1],corners[2]);
					gl.glVertex3f(coords[1][0], coords[1][1], coords[1][2]);							
					gl.glTexCoord2f(corners[1],corners[3]);
					gl.glVertex3f(coords[2][0], coords[2][1], coords[2][2]);							
					gl.glTexCoord2f(corners[0],corners[3]);
					gl.glVertex3f(coords[3][0], coords[3][1], coords[3][2]);							
					gl.glEnd();
				}
			}

			//
			// As above, we now loop through all combinations of X and 
			// Z coordinates, drawing the top and bottom squares.
			//

			
			for(ax=0;ax<X_Crossings-1;ax++) {
				for(az=0;az<Z_Crossings-1;az++) {
						float coords[][] = new float[8][3];
						float corners[] = new float[4];

						corners[0]=((float)ax)/X_Crossings;
						corners[1]=((float)ax+1)/X_Crossings;
						corners[2]=((float)az)/Z_Crossings;
						corners[3]=((float)az+1)/Z_Crossings;
					
						GetCoords(ax,Y_Crossings-1,az, coords[0]);
						GetCoords(ax+1,Y_Crossings-1,az, coords[1]);
						GetCoords(ax+1,Y_Crossings-1,az+1, coords[2]);
						GetCoords(ax,Y_Crossings-1,az+1, coords[3]);
						
						gl.glColor3f(Top[ax][az], Top[ax][az], Top[ax][az]);

						CgGL.cgSetParameter3f(myPulse, Top[ax][az], Top[ax][az], Top[ax][az]);
						gl.glBegin(GL.GL_POLYGON);
						CgGL.cgSetParameter4f(TVector, 0, 0, -1, 0);
						CgGL.cgSetParameter4f(BVector, 1, 0, 0, 0);

						gl.glNormal3f(0, -1, 0);
						gl.glTexCoord2f(corners[0],corners[2]);
						gl.glVertex3f(coords[0][0], coords[0][1], coords[0][2]);							
						gl.glTexCoord2f(corners[1],corners[2]);
						gl.glVertex3f(coords[1][0], coords[1][1], coords[1][2]);							
						gl.glTexCoord2f(corners[1],corners[3]);
						gl.glVertex3f(coords[2][0], coords[2][1], coords[2][2]);							
						gl.glTexCoord2f(corners[0],corners[3]);
						gl.glVertex3f(coords[3][0], coords[3][1], coords[3][2]);							

						gl.glEnd();		
				
						GetCoords(ax,0,az, coords[0]);
						GetCoords(ax,0,az+1, coords[1]);
						GetCoords(ax+1,0,az+1, coords[2]);
						GetCoords(ax+1,0,az, coords[3]);
					
						CgGL.cgSetParameter3f(myPulse, Bottom[ax][az], Bottom[ax][az], Bottom[ax][az]);
						gl.glBegin(GL.GL_POLYGON);
						CgGL.cgSetParameter4f(TVector, 0, 0, 1, 0);
						CgGL.cgSetParameter4f(BVector, 1, 0, 0, 0);

						gl.glNormal3f(0, 1, 0);
						gl.glTexCoord2f(corners[0],corners[2]);
						gl.glVertex3f(coords[0][0], coords[0][1], coords[0][2]);							
						gl.glTexCoord2f(corners[0],corners[3]);
						gl.glVertex3f(coords[1][0], coords[1][1], coords[1][2]);							
						gl.glTexCoord2f(corners[1],corners[3]);
						gl.glVertex3f(coords[2][0], coords[2][1], coords[2][2]);							
						gl.glTexCoord2f(corners[1],corners[2]);
						gl.glVertex3f(coords[3][0], coords[3][1], coords[3][2]);							

						gl.glEnd();		
				}
			}
			
			//
			// And now all X and Y combinations, in order to draw the back wall.
			//
			
			for(ax=0;ax<X_Crossings-1;ax++) {
				for(ay=0;ay<Y_Crossings-1;ay++) {

					float coords[][] = new float[8][3];
					float corners[] = new float[4];

					corners[0]=((float)ax)/X_Crossings;
					corners[1]=((float)ax+1)/X_Crossings;
					corners[2]=((float)ay)/Y_Crossings;
					corners[3]=((float)ay+1)/Y_Crossings;

					GetCoords(ax,ay,0, coords[0]);
					GetCoords(ax+1,ay,0, coords[1]);
					GetCoords(ax+1,ay+1,0, coords[2]);
					GetCoords(ax,ay+1,0, coords[3]);
					
					CgGL.cgSetParameter3f(myPulse, Back[ax][ay], Back[ax][ay], Back[ax][ay]);
					gl.glBegin(GL.GL_POLYGON);
					CgGL.cgSetParameter4f(TVector, 1, 0, 0, 0);
					CgGL.cgSetParameter4f(BVector, 0, 1, 0, 0);
						
					gl.glNormal3f(0, 0, 1);
					gl.glTexCoord2f(corners[0],corners[2]);
					gl.glVertex3f(coords[0][0], coords[0][1], coords[0][2]);							
					gl.glTexCoord2f(corners[1],corners[2]);
					gl.glVertex3f(coords[1][0], coords[1][1], coords[1][2]);							
					gl.glTexCoord2f(corners[1],corners[3]);
					gl.glVertex3f(coords[2][0], coords[2][1], coords[2][2]);							
					gl.glTexCoord2f(corners[0],corners[3]);
					gl.glVertex3f(coords[3][0], coords[3][1], coords[3][2]);							

					gl.glEnd();		
				}
			}

			CgGL.cgSetParameter3fv(myMa, tempMa, 0);
			CgGL.cgSetParameter3fv(myPulse, tempPulse, 0);
			
			CgGL.cgGLDisableProfile(VERTEXPROFILE);
			CgGL.cgGLDisableProfile(FRAGMENTPROFILE);

		}

		//
		// Run through all the pulse arrays, dimming any pulse that
		// exists by an amount dependent on the time increment.
		//
		
		public void Update(long increment) {
			int ix;
			int iy;
			int iz;
			for(iy=0;iy<Y_Crossings;iy++) {
				for(iz=0;iz<Z_Crossings;iz++) {
					Left[iy][iz]-=((float)increment)/1000.0f;
					if (Left[iy][iz]<0.0f) { Left[iy][iz]=0.0f; }
					Right[iy][iz]-=((float)increment)/1000.0f;
					if (Right[iy][iz]<0.0f) { Right[iy][iz]=0.0f; }
				}
			}
			for(ix=0;ix<X_Crossings;ix++) {
				for(iz=0;iz<Z_Crossings;iz++) {
					Top[ix][iz]-=((float)increment)/1000.0f;
					if (Top[ix][iz]<0.0f) { Top[ix][iz]=0.0f; }
					Bottom[ix][iz]-=((float)increment)/1000.0f;
					if (Bottom[ix][iz]<0.0f) { Bottom[ix][iz]=0.0f; }
				}
			}
			for(ix=0;ix<X_Crossings;ix++) {
				for(iy=0;iy<Y_Crossings;iy++) {
					Front[ix][iy]-=((float)increment)/1000.0f;
					if (Front[ix][iy]<0.0f) { Front[ix][iy]=0.0f; }
					Back[ix][iy]-=((float)increment)/1000.0f;
					if (Back[ix][iy]<0.0f) { Back[ix][iy]=0.0f; }
				}
			}
		}
		
	}

	// 
	// A Cage_Sphere has its own self-maintaining velocity and spin,
	// as well as overloaded functions for how to bounce off of a
	// surface (a wall or another sphere).
	//
	
	public class T2_Cage_Sphere extends T2_Rough_Sphere {
		
		float dx;
		float dy;
		float dz;
		float Spin;
		
		public void SetVelocity(float vx, float vy, float vz) {
			dx=vx;
			dy=vy;
			dz=vz;
		}

		public T2_Cage_Sphere() {
			super(0.0f, 0.0f, 0.0f, 0.2f+((float)Math.random()/10));
			float velocity[]=new float[3];
			
			//
			// Create two non-zero, normalized vectors ... one for
			// velocity and one to give the sphere a unique
			// pivot around which to rotate.  Also, pick a jewel tone
			// to set the unique color of the sphere's veins.
			//
			
			velocity[0]=0.0f;

			while(velocity[0]==0.0f)
			{
				velocity[0]=(float)Math.random();
				velocity[1]=(float)Math.random();
				velocity[2]=(float)Math.random();
			}
			
			normalize(velocity);
						
			SetVelocity(velocity[0], velocity[1], velocity[2]);
			JewelTone();
			Pulse=0.0f;

			velocity[0]=0.0f;

			while(velocity[0]==0.0f)
			{
				velocity[0]=(float)Math.random();
				velocity[1]=(float)Math.random();
				velocity[2]=(float)Math.random();
			}

			normalize(velocity);

			pivotx=velocity[0];
			pivoty=velocity[1];
			pivotz=velocity[2];
			Deg = ((float)Math.random())*360;
			Spin = ((float)Math.random())*5;
		
		}

		//
		// Random color generation sometimes gave me pale, washed
		// out colors that looked awful.  So I've coded this procedure
		// to take a random hue, and punch it up to 90% saturation
		// and value, then convert into RGB.
		//
		
		public void JewelTone()
		{
			double H = Math.random()*360;
			double S = 0.9;
			float V = 0.9f;
			
			int hi = ((int)Math.floor(H/60))%6;
			
			double f = (H/60) - Math.floor(H/60);
			float p = V * (1.0f - (float)S);
		    float q = V * (1.0f - (float)(f*S));
		    float t = V * (1.0f - (float)((1.0 - f) * S));
		    
		    if (hi==0) { SetColor(V, t, p); }
		    if (hi==1) { SetColor(q, V, p); }
		    if (hi==2) { SetColor(p, V, t); }
		    if (hi==3) { SetColor(p, q, V); }
		    if (hi==4) { SetColor(t, p, V); }
		    if (hi==5) { SetColor(V, p, q); }
			
		}

		public void ColorOut(GL gl)
		{
			gl.glColor3f(Color[0]+Pulse, Color[1]+Pulse, Color[2]+Pulse);
		}
		
		//
		// Update manages the motion of the sphere.
		//
		
		public void Update(long increment, T2_Cage cage)
		{
			float coords[] = new float[3];
			
			cage.GetCoords(0, 0, 0, coords);
			float BoundX=Math.abs(coords[0])-Radius;
			float BoundY=Math.abs(coords[1])-Radius;
			float BoundZ=Math.abs(coords[2])-Radius;
			
			//
			// Check whether the sphere is trying to leave the
			// bounding cage, and if so bounce it off of the
			// wall (pulsing both wall and sphere to illuminate).
			//
			
			CenterX+=dx*increment/500;
			if (CenterX>BoundX)
			{
					int index[]=new int[3];
			    	dx=-dx;
			    	CenterX=BoundX*2-CenterX;
			    	cage.GetIndex(CenterX, CenterY, CenterZ, index);
			    	cage.Right[index[1]][index[2]]=0.5f;
			    	Pulse=0.3f;
			}
			if (CenterX<-BoundX)
			{
					int index[]=new int[3];
			    	dx=-dx;
			    	CenterX=(-BoundX)*2-CenterX;
			    	cage.GetIndex(CenterX, CenterY, CenterZ, index);
			    	cage.Left[index[1]][index[2]]=0.5f;
			    	Pulse=1.0f;
			}
			CenterY+=dy*increment/500;
			if (CenterY>BoundY)
			{
					int index[]=new int[3];
			    	dy=-dy;
			    	CenterY=BoundY*2-CenterY;
			    	cage.GetIndex(CenterX, CenterY, CenterZ, index);
			    	cage.Top[index[0]][index[2]]=0.5f;
			    	Pulse=1.0f;
			}
			if (CenterY<-BoundY)
			{
					int index[]=new int[3];
			    	dy=-dy;
			    	CenterY=(-BoundY)*2-CenterY;
			    	cage.GetIndex(CenterX, CenterY, CenterZ, index);
			    	cage.Bottom[index[0]][index[2]]=0.5f;
			    	Pulse=1.0f;
			}
			CenterZ+=dz*increment/500;
			if (CenterZ>BoundZ)
			{
					int index[]=new int[3];
			    	dz=-dz;
			    	CenterZ=BoundZ*2-CenterZ;
			    	cage.GetIndex(CenterX, CenterY, CenterZ, index);
			    	cage.Front[index[0]][index[1]]=0.5f;
			    	Pulse=1.0f;
			}
			if (CenterZ<-BoundZ)
			{
					int index[]=new int[3];
			    	dz=-dz;
			    	CenterZ=(-BoundZ)*2-CenterZ;
			    	cage.GetIndex(CenterX, CenterY, CenterZ, index);
			    	cage.Back[index[0]][index[1]]=0.5f;
			    	Pulse=1.0f;
			}
			
			Pulse-=((float)increment)/500;
			if (Pulse<0.0f) { Pulse=0.0f; }
			
			//
			// Spin, spin my pretty.
			//
			
			Deg+=((float)increment)*Spin/10;
			Deg%=360;
			
		}
		
		public float DotProduct(float a1, float a2, float a3, float b1, float b2, float b3) {

			return (a1 * b1 + a2 * b2 + a3 * b3);
		}

		
		public void Reflect(float NormX, float NormY, float NormZ)
		{
			float Dot = DotProduct(NormX, NormY, NormZ, -dx, -dy, -dz);
			if (Dot>0.0)
			{
				dx = 2 * Dot * NormX + dx;
				dy = 2 * Dot * NormY + dy;
				dz = 2 * Dot * NormZ + dz;
				Pulse=0.5f;
			}
		}

		//
		// When two spheres intersect, this calculates the line through their
		// centers, and bounces their velocities off of it.
		//
		
		public void Bounce(T2_Cage_Sphere other)
		{
			float NormX = CenterX-other.CenterX;
			float NormY = CenterY-other.CenterY;
			float NormZ = CenterZ-other.CenterZ;
			float Mag = (float)Math.sqrt(NormX*NormX+NormY*NormY+NormZ*NormZ);
			
			if (Mag>0.0)
			{
				NormX/=Mag;
				NormY/=Mag;
				NormZ/=Mag;
				other.Reflect(-NormX, -NormY, -NormZ);
				Reflect(NormX, NormY, NormZ);
				other.Pulse=1.0f;
				Pulse=1.0f;
			}		
		}

		
	}
		
	//
	// The TestSphereCanvase class extends the TonyGLCanvas, above,
	// providing implementations for the all-important DrawFrame
	// and Update functions, as well as overloading init to set
	// up all the CG material and property setting.
	//
	
	public class TestSphereCanvas extends TonyGLCanvas {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		T2_Sphere sphere;
		T2_Cage cage;
		T2_Cage_Sphere Spheres[];
		int numSpheres;
		boolean tilted=false;
		
		public TestSphereCanvas(int width, int height) {
			super(width, height);

			//
			// Create all the objects we'll need (whether
			// they are displayed now or not).
			//
			
			sphere=new T2_Sphere(0, 0, 0);
			sphere.Pulse = 1.0f;
			cage = new T2_Cage(5, 5, 5);
			Spheres=new T2_Cage_Sphere[25];
			for(int i=0;i<25;i++)
			{
				Spheres[i]=new T2_Cage_Sphere();
			}
			numSpheres=5;
			
			sphere.SetColor(0.1f, 0.1f, 1.0f);
			
		}

		//
		// This procedure sets up all of the CG programming
		// interfaces.
		//
		
		public void CGSetup() {

			if (!CgGL.cgGLIsProfileSupported(VERTEXPROFILE)) {
				System.out.println("Profile not supported");
				System.exit(1);
			}

			cgcontext = CgGL.cgCreateContext();

			CgGL.cgGLSetOptimalOptions(VERTEXPROFILE);

			vertexprog = CgGL.cgCreateProgramFromFile(cgcontext, CgGL.CG_SOURCE,
					"src/Tony_VP.cg", VERTEXPROFILE, null, null);
			if (vertexprog != null) 
			{ CgGL.cgGLLoadProgram(vertexprog); }
			
			int err = CgGL.cgGetError();

			if (err != CgGL.CG_NO_ERROR)
			{
				System.err.println("CG error: " + CgGL.cgGetErrorString(err));
				throw new RuntimeException("CG error: " + CgGL.cgGetErrorString(err));
			}			

			modelview = CgGL.cgGetNamedParameter(vertexprog, "modelView");
			modelviewprojection = CgGL.cgGetNamedParameter(vertexprog,
					"modelViewProjection");
			inversetranspose = CgGL.cgGetNamedParameter(vertexprog, "inverseTranspose");

			TVector = CgGL.cgGetNamedParameter(vertexprog, "iTVector");
			BVector = CgGL.cgGetNamedParameter(vertexprog, "iBVector");
			
			CgGL.cgGLSetOptimalOptions(FRAGMENTPROFILE);
			fragmentprog = CgGL.cgCreateProgramFromFile(cgcontext, CgGL.CG_SOURCE,
					"src/Tony_FP.cg", FRAGMENTPROFILE, null, null);

			if (fragmentprog!=null)  { CgGL.cgGLLoadProgram(fragmentprog); }

			err = CgGL.cgGetError();

			if (err != CgGL.CG_NO_ERROR)
			{
				System.err.println("CG error: " + CgGL.cgGetErrorString(err));
				throw new RuntimeException("CG error: " + CgGL.cgGetErrorString(err));
			}			
			
			//Light source properties 
			myLa = CgGL.cgGetNamedParameter(fragmentprog, "La");
			myLd = CgGL.cgGetNamedParameter(fragmentprog, "Ld");
			myLs = CgGL.cgGetNamedParameter(fragmentprog, "Ls");
			myLightPosition = CgGL.cgGetNamedParameter(fragmentprog,
					"lightPosition");
			myLa2 = CgGL.cgGetNamedParameter(fragmentprog, "La2");
			myLd2 = CgGL.cgGetNamedParameter(fragmentprog, "Ld2");
			myLs2 = CgGL.cgGetNamedParameter(fragmentprog, "Ls2");
			myLightPosition2 = CgGL.cgGetNamedParameter(fragmentprog,
					"lightPosition2");
			myEyePosition = CgGL.cgGetNamedParameter(fragmentprog,
					"eyePosition");
			
			//Material properties 
			myMe = CgGL.cgGetNamedParameter(fragmentprog, "Me");
			myMa = CgGL.cgGetNamedParameter(fragmentprog, "Ma");
			myMd = CgGL.cgGetNamedParameter(fragmentprog, "Md");
			myMs = CgGL.cgGetNamedParameter(fragmentprog, "Ms");
			mySwap = CgGL.cgGetNamedParameter(fragmentprog, "ColorSwap");
			myShininess = CgGL.cgGetNamedParameter(fragmentprog,
					"shininess");
			myPulse = CgGL.cgGetNamedParameter(fragmentprog, "Pulse");

			imgTexture = CgGL.cgGetNamedParameter(fragmentprog, "imgTexture");
			normalTexture = CgGL.cgGetNamedParameter(fragmentprog, "normalTexture");
			lumTexture = CgGL.cgGetNamedParameter(fragmentprog, "lumTexture");
			swapTexture = CgGL.cgGetNamedParameter(fragmentprog, "swapTexture");
			
			//
			// Set the default properties for materials and
			// lights.
			//
			
			CgGL.cgSetParameter3f(myLa, 0.5f, 0.5f, 0.5f);
			CgGL.cgSetParameter3f(myLd, 1, 1, 1);
			CgGL.cgSetParameter3f(myLs, 1, 1, 1);
			CgGL.cgSetParameter3f(myLa2, 0.25f, 0.25f, 0.25f);
			CgGL.cgSetParameter3f(myLd2, 0.5f, 0.5f, 0.5f);
			CgGL.cgSetParameter3f(myLs2, 0.25f, 0.25f, 0.25f);
			CgGL.cgSetParameter3f(myMe, 0, 0, 0);
			CgGL.cgSetParameter3f(myMa, 0.5f, 0.5f, 0.5f);
			CgGL.cgSetParameter3f(myMd, 1, 1, 1);
			CgGL.cgSetParameter3f(myMs, 1, 1, 1);
			CgGL.cgSetParameter1f(myPulse, 0);
			CgGL.cgSetParameter1f(myShininess, 50);

			float[] eyePos = {0, 0, 2000};

			CgGL.cgSetParameter3fv(myEyePosition, eyePos, 0);
			
		}
		
		//
		// This initializes the OpenGL material, as well as
		// loading and tagging all of the textures that will be
		// needed by the program.
		//
		
		public void init(GLAutoDrawable drawable) {
			
			super.init(drawable);
					
			//
			// We only need a bare minimum of OpenGL properties
			// enabled, because most of the heavy lifting will be
			// done by the vertex and fragment processors.
			//
			
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
			gl.glEnable(GL.GL_CULL_FACE); 
			gl.glCullFace(GL.GL_BACK); 
			
			readImage(gl, "DT_HiRes_color.jpg", COLOR_TEX);

			readImage(gl, "DT_HiRes_normal.jpg", NORMAL_TEX);
			
			readImage(gl, "DT_HiRes_lum.jpg", LUM_TEX);

			readImage(gl, "DT_LoRes_color.jpg", LO_COLOR_TEX);

			readImage(gl, "DT_LoRes_normal.jpg", LO_NORMAL_TEX);
			
			readImage(gl, "DT_LoRes_lum.jpg", LO_LUM_TEX);

			readImage(gl, "T2_White.jpg", WHITE_TEX);

			readImage(gl, "T2_Black.jpg", BLACK_TEX);

			readImage(gl, "T2_Gray.jpg", GRAY_TEX);

			readImage(gl, "T2_Flat.jpg", FLAT_TEX);
			
			readImage(gl, "Wall_color.jpg", WALL_COLOR_TEX);
			readImage(gl, "Wall_normal.jpg", WALL_NORMAL_TEX);
			readImage(gl, "Wall_lum.jpg", WALL_LUM_TEX);

			CGSetup();


		}

		public void DrawFrame(GLAutoDrawable drawable)
		{
		    gl.glClear(GL.GL_COLOR_BUFFER_BIT|
		               GL.GL_DEPTH_BUFFER_BIT);

		    gl.glLoadIdentity();
		    gl.glScalef(20, 20, 20);
		    
		    //
		    // Initializes the textures for drawing a sphere,
		    // according to the toggles currently set.
		    //
		    
			if (LuminosityMap)
			{
				if (spinDisplay)
				{
					CgGL.cgGLSetTextureParameter(lumTexture, LUM_TEX[0]);
				}
				else
				{
					CgGL.cgGLSetTextureParameter(lumTexture, LO_LUM_TEX[0]);					
				}
				CgGL.cgGLEnableTextureParameter( lumTexture );
			} else
			{
				CgGL.cgGLSetTextureParameter(lumTexture, BLACK_TEX[0]);
				CgGL.cgGLEnableTextureParameter( lumTexture );
			}

			if (BumpMap)
			{
				if (spinDisplay)
				{
					CgGL.cgGLSetTextureParameter(normalTexture, NORMAL_TEX[0]);
				}
				else
				{
					CgGL.cgGLSetTextureParameter(normalTexture, LO_NORMAL_TEX[0]);					
				}
				CgGL.cgGLEnableTextureParameter( normalTexture );
			} else
			{
				CgGL.cgGLSetTextureParameter(normalTexture, FLAT_TEX[0]);
				CgGL.cgGLEnableTextureParameter( normalTexture );
			}

			if (ColorMap)
			{
				if (spinDisplay)
				{
					CgGL.cgGLSetTextureParameter(imgTexture, COLOR_TEX[0]);
				}
				else
				{
					CgGL.cgGLSetTextureParameter(imgTexture, LO_COLOR_TEX[0]);					
				}
				CgGL.cgGLEnableTextureParameter( imgTexture );
				if (spinDisplay)
				{
					CgGL.cgGLSetTextureParameter(swapTexture, LUM_TEX[0]);
				}
				else
				{
					CgGL.cgGLSetTextureParameter(swapTexture, LO_LUM_TEX[0]);					
				}
				CgGL.cgGLEnableTextureParameter( swapTexture );
			} else
			{
				CgGL.cgGLSetTextureParameter(imgTexture, GRAY_TEX[0]);
				CgGL.cgGLEnableTextureParameter( imgTexture );
				if (SwapMap)
				{
					if (spinDisplay)
					{
						CgGL.cgGLSetTextureParameter(swapTexture, LUM_TEX[0]);
					}
					else
					{
						CgGL.cgGLSetTextureParameter(swapTexture, LO_LUM_TEX[0]);					
					}
					CgGL.cgGLEnableTextureParameter( swapTexture );
				} else
				{
					CgGL.cgGLSetTextureParameter(swapTexture, BLACK_TEX[0]);
					CgGL.cgGLEnableTextureParameter( swapTexture );
				}
			}

			//
			// If the Fill Light is active, gives it lighting properties,
			// otherwise sets it dark.
			//
			
			if (FillLight) {
				CgGL.cgSetParameter3f(myLa2, 0.25f, 0.25f, 0.25f);
				CgGL.cgSetParameter3f(myLd2, 0.5f, 0.5f, 0.5f);
				CgGL.cgSetParameter3f(myLs2, 0.25f, 0.25f, 0.25f);
				
			} else
			{
				CgGL.cgSetParameter3f(myLa2, 0,0,0);
				CgGL.cgSetParameter3f(myLd2, 0,0,0);
				CgGL.cgSetParameter3f(myLs2, 0,0,0);
				
			}

			//
			// Calls one of two possible displays, depending on the
			// current setting of the Spin Display radio button.
			//
			
			if (spinDisplay)
			{
			    gl.glTranslatef(0, 0, -55);
				gl.glPushMatrix();
				gl.glTranslatef((float)lightX, 30, 20);
				gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, currM, 0);
				gl.glPopMatrix();
				
				float[] lightPos=new float[3];
				
				lightPos[0] = currM[12];
				lightPos[1] = currM[13];
				lightPos[2] = currM[14];

				CgGL.cgSetParameter3fv(myLightPosition, lightPos, 0);

				gl.glPushMatrix();
				gl.glTranslatef(-20, -20, 10);
				gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, currM, 0);
				gl.glPopMatrix();
								
				lightPos[0] = currM[12];
				lightPos[1] = currM[13];
				lightPos[2] = currM[14];

				CgGL.cgSetParameter3fv(myLightPosition2, lightPos, 0);

				gl.glRotatef(Deg, 0, 1, 0);
			
				sphere.draw(gl);
			}
			else
			{
				gl.glTranslatef(0,0,-10);

				//
				// Transform the origin to the two points set for
				// lighting, and poll the information on their
				// global position from the current transform
				// matrix.
				//
				
				gl.glPushMatrix();
				gl.glTranslatef((float)lightX, 30, 20);
				gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, currM, 0);
				gl.glPopMatrix();
				
				float[] lightPos=new float[3];
				
				lightPos[0] = currM[12];
				lightPos[1] = currM[13];
				lightPos[2] = currM[14];

				CgGL.cgSetParameter3fv(myLightPosition, lightPos, 0);

				gl.glPushMatrix();
				gl.glTranslatef(-20, -20, 10);
				gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, currM, 0);
				gl.glPopMatrix();
								
				lightPos[0] = currM[12];
				lightPos[1] = currM[13];
				lightPos[2] = currM[14];

				CgGL.cgSetParameter3fv(myLightPosition2, lightPos, 0);


				for(int i=0;i<numSpheres;i++)
				{
				    Spheres[i].draw(gl);				
				}

				//
				// Once the spheres are drawn, set to the new (wall)
				// textures before drawing the cage.
				//
				
				CgGL.cgGLSetTextureParameter(swapTexture, BLACK_TEX[0]);					
				CgGL.cgGLEnableTextureParameter( swapTexture );
				CgGL.cgGLSetTextureParameter(lumTexture, BLACK_TEX[0]);
				CgGL.cgGLEnableTextureParameter( lumTexture );				
				if (ColorMap)
				{
					CgGL.cgGLSetTextureParameter(imgTexture, WALL_COLOR_TEX[0]);					
					CgGL.cgGLEnableTextureParameter( imgTexture );
				} else
				{
					CgGL.cgGLSetTextureParameter(imgTexture, GRAY_TEX[0]);
					CgGL.cgGLEnableTextureParameter( imgTexture );
				}
				if (BumpMap)
				{
					CgGL.cgGLSetTextureParameter(normalTexture, WALL_NORMAL_TEX[0]);					
					CgGL.cgGLEnableTextureParameter( normalTexture );
				} else
				{
					CgGL.cgGLSetTextureParameter(normalTexture, FLAT_TEX[0]);
					CgGL.cgGLEnableTextureParameter( imgTexture );
				}
				if (LuminosityMap) {
					CgGL.cgGLSetTextureParameter(lumTexture, WALL_LUM_TEX[0]);					
					CgGL.cgGLEnableTextureParameter( lumTexture );										
				} else
				{
					CgGL.cgGLSetTextureParameter(lumTexture, BLACK_TEX[0]);					
					CgGL.cgGLEnableTextureParameter( lumTexture );					
				}
				cage.DrawOpen(gl);
			}

		    gl.glDisable(GL.GL_TEXTURE_2D);
		}

		//
		// Update all spheres, checking for intersections,
		// and also update the cage itself.
		//
		
		public void Update(long increment)
		{
			for(int i=0;i<numSpheres;i++)
			{
				Spheres[i].Update(increment, cage);
			}
			for(int j=0;j<numSpheres;j++)
			{
				for(int k=j+1;k<numSpheres;k++)
				{
					if (Spheres[j].Intersect(Spheres[k]))
					{
						Spheres[j].Bounce(Spheres[k]);
					}
				}
			}
			cage.Update(increment);

			Deg+=((float)(increment%7200))/20;
			Deg%=360;
		}
			
		public void reshape(GLAutoDrawable drawable, int x, int y, int width,
				int height) {

			super.reshape(drawable, x, y, width, height);
					
		}

		//
		// When the number-of-spheres slider calls for an increase,
		// this procedure makes sure that a newly called sphere doesn't
		// end up already intersecting with an existing one.  That can
		// case feedback positions where they lock together and can't get
		// away from each other.
		//
		
		public void addSphere()
		{
			float x = (float)cage.X_Crossings-1.15f;
			float y = (float)cage.Y_Crossings-1.15f;
			float z = (float)cage.Z_Crossings-1.15f;
			if (numSpheres<25) {
				boolean intersectRisk = true;
				while (intersectRisk) {
					intersectRisk=false;
					Spheres[numSpheres].CenterX=((float)Math.random()-0.5f)*x;
					Spheres[numSpheres].CenterY=((float)Math.random()-0.5f)*y;
					Spheres[numSpheres].CenterZ=((float)Math.random()-0.5f)*z;
					for(int i=0;i<numSpheres;i++)
					if (Spheres[i].Intersect(Spheres[numSpheres])) { intersectRisk=true; }
				}
				numSpheres++;
			}
		}


	}

	int imgW;
	int imgH;
	int imgType;
	byte[] img;

	//
	// readImage is a utility function that reads a texture file
	// and tags it into the system.
	//
	
	public void readImage(GL gl, String fileName, int[] point) {
		File f = new File(fileName);
		BufferedImage bufimg;

		gl.glGenTextures(1, IntBuffer.wrap(point));
		gl.glBindTexture(GL.GL_TEXTURE_2D, point[0]);
	
		gl.glTexParameteri(GL.GL_TEXTURE_2D,
	                      GL.GL_TEXTURE_MIN_FILTER,
	                      GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D,
	                      GL.GL_TEXTURE_MAG_FILTER,
                       GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D,
						  GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		gl.glTexParameteri(GL.GL_TEXTURE_2D,
				  GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

		try {
		      // read the image into BufferredImage structure
		      bufimg = ImageIO.read(f);
		      imgW = bufimg.getWidth();
		      imgH = bufimg.getHeight();
		      imgType = bufimg.getType();
		      System.out.println(fileName+" -- BufferedImage WIDTH&HEIGHT: "+imgW+", "+imgH);
		      System.out.println("BufferedImage type TYPE_3BYTE_BGR 5; GRAY 10: "+imgType);
		      //TYPE_BYTE_GRAY  10
		      //TYPE_3BYTE_BGR 	5

		      // retrieve the pixel array in raster's databuffer
		      Raster raster = bufimg.getData();

		      DataBufferByte dataBufByte = (DataBufferByte)raster.
		                                   getDataBuffer();
		      img = dataBufByte.getData();
		      System.out.println("Image data's type TYPE_BYTE 0: "+
		                         dataBufByte.getDataType());
		      // TYPE_BYTE 0

			} catch (IOException ex) {
				try {
					  File f2 = new File("src/"+fileName);
				      // read the image into BufferredImage structure
				      bufimg = ImageIO.read(f2);
				      imgW = bufimg.getWidth();
				      imgH = bufimg.getHeight();
				      imgType = bufimg.getType();
				      System.out.println(fileName+" -- BufferedImage WIDTH&HEIGHT: "+imgW+", "+imgH);
				      System.out.println("BufferedImage type TYPE_3BYTE_BGR 5; GRAY 10: "+imgType);
				      //TYPE_BYTE_GRAY  10
				      //TYPE_3BYTE_BGR 	5

				      // retrieve the pixel array in raster's databuffer
				      Raster raster = bufimg.getData();

				      DataBufferByte dataBufByte = (DataBufferByte)raster.
				                                   getDataBuffer();
				      img = dataBufByte.getData();
				      System.out.println("Image data's type TYPE_BYTE 0: "+
				                         dataBufByte.getDataType());
				      // TYPE_BYTE 0

					} catch (IOException ex2) {
						System.out.println("Image: "+fileName+" not found! ("+ex2.getMessage()+")");
						System.exit(1);
					}
			}

			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB8,
                    imgW, imgH, 0, GL.GL_BGR,
                    GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(img));

		}

	//
	// User Interface controls, and their listeners.
	//
	
	TestSphereCanvas canvas;
	JPanel controls;
	JRadioButton spinButton;
	boolean spinDisplay=false;
	ButtonGroup buttonGroup;
	JCheckBox lumCheck;
	boolean LuminosityMap=true;
	JCheckBox bumpCheck;
	boolean BumpMap=true;
	JCheckBox colorCheck;
	boolean ColorMap=true;
	JCheckBox swapCheck;
	boolean SwapMap=true;
	JCheckBox fillCheck;
	boolean FillLight=false;
	JSlider ballSlider;
	JSlider lightSlider;

	//
	// The constructor for the project creates a SphereCanvas, and also
	// creates the user-interface for running the program.
	//
	
	public Tony_Project ()
	{
        JPanel pane = (JPanel)getContentPane();
        JPanel controltop = new JPanel();
        JPanel controlbottom = new JPanel();
        JPanel controlmiddle = new JPanel();
        GridBagConstraints c = new GridBagConstraints();

		canvas = new TestSphereCanvas(wide, high-100);
		
		pane.add(canvas, BorderLayout.CENTER);
		
		controls= new JPanel();
		pane.add(controls, BorderLayout.SOUTH);
		controls.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 1;
		c.weightx = 0.5;
		controls.add(controltop, c);
		
		controltop.setLayout(new GridLayout(1, 2));

		ballSlider = new JSlider(JSlider.HORIZONTAL, 1, 11, 5);
		ballSlider.setPaintTicks(false);
		Hashtable labelTable = new Hashtable();
		labelTable.put( new Integer( 1 ), new JLabel("One Sphere") );
		labelTable.put( new Integer( 11 ), new JLabel("Many Spheres") );
		ballSlider.setLabelTable( labelTable );
		ballSlider.setPaintLabels(true);
		ballSlider.addChangeListener(new SliderListener());
		
		controltop.add(ballSlider);

		lightSlider = new JSlider(JSlider.HORIZONTAL, -50, 50, 20);
		lightSlider.setPaintTicks(false);
		Hashtable labelTable2 = new Hashtable();
		labelTable2.put( new Integer( -50 ), new JLabel("Light Far Left") );
		labelTable2.put( new Integer( 50 ), new JLabel("Light Far Right") );
		lightSlider.setLabelTable( labelTable2 );
		lightSlider.setPaintLabels(true);
		lightSlider.addChangeListener(new LightSliderListener());
		
		controltop.add(lightSlider);

		JRadioButton spinButton = new JRadioButton("Display Sphere");
		spinButton.setActionCommand("Spin");
		spinButton.setSelected(false);
		JRadioButton cageButton = new JRadioButton("Bouncing Cage");
		cageButton.setActionCommand("Cage");
		cageButton.setSelected(true);
		
		spinButton.addActionListener(new SpinRadioListener());
		cageButton.addActionListener(new SpinRadioListener());
		
		buttonGroup=new ButtonGroup();
		
		buttonGroup.add(spinButton);
		buttonGroup.add(cageButton);
		
		c.gridy = 2;
		c.weightx = 0.0;
		controls.add(controlmiddle, c);
				
		controlmiddle.add(spinButton, BorderLayout.CENTER);
		controlmiddle.add(cageButton, BorderLayout.CENTER);
		
		c.gridy = 3;
		controls.add(controlbottom, c);
		controlbottom.setLayout(new FlowLayout());
		fillCheck = new JCheckBox("Fill Light");
		
		controlbottom.add(fillCheck);
		fillCheck.addItemListener(new FillCheckListener());

		lumCheck = new JCheckBox("Glow Effects", null, true);
		
		controlbottom.add(lumCheck);
		lumCheck.addItemListener(new LumCheckListener());

		bumpCheck = new JCheckBox("Normal Map", null, true);
		
		controlbottom.add(bumpCheck);
		bumpCheck.addItemListener(new BumpCheckListener());

		colorCheck = new JCheckBox("Color Map", null, true);
		
		controlbottom.add(colorCheck);
		colorCheck.addItemListener(new ColorCheckListener());

		swapCheck = new JCheckBox("Unique Colors", null, true);
		
		controlbottom.add(swapCheck);
		swapCheck.addItemListener(new SwapCheckListener());
		
	}
	
	//
	// Listeners.  Always listening ... always listening.
	//
	
	public class LumCheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent e)
		{
			LuminosityMap=lumCheck.isSelected();
		}
	}

	public class BumpCheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent e)
		{
			BumpMap=bumpCheck.isSelected();
		}
	}

	public class ColorCheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent e)
		{
			ColorMap=colorCheck.isSelected();
		}
	}

	public class SwapCheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent e)
		{
			SwapMap=swapCheck.isSelected();
		}
	}

	public class FillCheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent e)
		{
			FillLight=fillCheck.isSelected();
		}
	}

	public class SpinRadioListener implements ActionListener {
		public void actionPerformed(ActionEvent e)
		{
			spinDisplay=(e.getActionCommand()=="Spin");
			if (spinDisplay) 
			{
				ballSlider.setVisible(false);
			} else
			{
				ballSlider.setVisible(true);				
			}
		}
	}

	public class SliderListener implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        if (!source.getValueIsAdjusting()) {
	            int balls = (int)source.getValue();
	            if (balls<=canvas.numSpheres)
	            {
	            	canvas.numSpheres=balls;
	            }
	            while (balls>canvas.numSpheres)
	            {
	            	canvas.addSphere();
	            }
	        }    
	    }
	}

	public class LightSliderListener implements ChangeListener {
	    public void stateChanged(ChangeEvent e) {
	        JSlider source = (JSlider)e.getSource();
	        lightX = (int)source.getValue();
	    }
	}

	
	static int high = 800, wide = 800;

	//
	// After all that, the main program is disappointingly small.
	// We create a project frame, and everything starts running
	// automatically.
	//
	
	public static void main(String[] args) {
		Tony_Project frame = new Tony_Project();

		// 8. set the size of the frame and make it visible
		frame.setTitle("Project");
		frame.setSize(wide, high);
		
		frame.setVisible(true);
		
	}
	
}
