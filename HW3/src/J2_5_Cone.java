/*
 * Created on 2004-3-9
 * @author Jim X. Chen: draw a cone by subdivision
 */
//import net.java.games.jogl.*;
import javax.media.opengl.*;

import com.sun.opengl.util.GLUT;

public class J2_5_Cone extends J2_4_Robot {

  public void reshape(
      GLAutoDrawable glDrawable,
      int x,
      int y,
      int w,
      int h) {

    WIDTH = w;
    HEIGHT = h;

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();

    //1. make sure the cone is within the viewing volume
    gl.glOrtho(-w/2, w/2, -h/2, h/2, -w, w); // look at z near and far

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    
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
    if (cnt % 800 < 400)  {
    	gl.glEnable(GL.GL_DEPTH_TEST);
       	gl.glColor3f(1.0f, 1.0f, 1.0f);
    	gl.glWindowPos2d(10, 20); 
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "DEPTH_TEST Enabled");
    }
    else  {
    	gl.glDisable(GL.GL_DEPTH_TEST);
    	gl.glColor3f(1.0f, 0f, 0f);
    	gl.glWindowPos2d(10, 20); 
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "DEPTH_TEST Disabled");
   }
    
 
   
    //5. Test glPolygonMode 
     if (cnt % 100 > 80) gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
    else gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
 
    //6. Test glCullFace
    if (cnt % 500 > 370) {
    	gl.glEnable(GL.GL_CULL_FACE); 
        gl.glCullFace(GL.GL_FRONT); 
    	gl.glColor3f(1.0f, 0f, 1f);
  		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, 
 				"    gl.glCullFace(GL.GL_FRONT);");

    }
    else  if (cnt % 500 > 245) {
    	gl.glEnable(GL.GL_CULL_FACE); 
        gl.glCullFace(GL.GL_BACK); 
    	gl.glColor3f(1.0f, 1f, 0f);
 		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, 
 				"    gl.glCullFace(GL.GL_BACK);");
    }
    else {
    	gl.glDisable(GL.GL_CULL_FACE); 
    }

    //6. draw a triangle for showing hidden surface removal
    float 	v0[] = {-WIDTH/4, -WIDTH/4, -WIDTH}, 
    		v1[] = {WIDTH/4, 0, WIDTH}, 
    		v2[] = {WIDTH/4, HEIGHT/3, 0}; 
    gl.glPushMatrix();
    gl.glLoadIdentity();
    gl.glColor3f(0.5f, 0.5f, 0.5f);
    drawtriangle(v0, v1, v2); 
    gl.glPopMatrix();
    
    // rotate 1 degree alone vector (1, 1, 1)
    gl.glRotatef(1, 1, 1, 1);
    gl.glPushMatrix();
    gl.glScaled(cRadius, cRadius, cRadius);
    drawCone();
    gl.glPopMatrix();

  }


  private void subdivideCone(float v1[],
		  float v2[], int depth) {
	  float v0[] = {0, 0, 0};
	  float v12[] = new float[3];

    if (depth==0) {
      gl.glColor3d(v1[0]*v1[0], v1[1]*v1[1], 0);

      //drawtriangle(v2, v1, v0);
      // bottom cover of the cone

      v0[2] = 1; // height of the cone, the tip on z axis
      drawtriangle(v1, v2, v0); // side cover of the cone

      return;
    }

    for (int i = 0; i<3; i++) {
      v12[i] = v1[i]+v2[i];
    }
    normalize(v12);

    subdivideCone(v1, v12, depth-1);
    subdivideCone(v12, v2, depth-1);
  }


  public void drawCone() {
    subdivideCone(cVdata[0], cVdata[1], depth);
    subdivideCone(cVdata[1], cVdata[2], depth);
    subdivideCone(cVdata[2], cVdata[3], depth);
    subdivideCone(cVdata[3], cVdata[0], depth);
  }


  public static void main(String[] args) {
    J2_5_Cone f = new J2_5_Cone();

    f.setTitle("JOGL J2_5_Cone");
    f.setSize(WIDTH, HEIGHT);
    f.setVisible(true);
  }
}
