/**
 * <p>Title: Foundations of 3D Graphics Programming : Using JOGL and Java3D </p>
 *
 * <p>Description: Cg Example: texture mapping  </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: George Mason University</p>
 *
 * @author Dr. Jim X. Chen
 * @version 1.0
 */
import com.sun.opengl.cg.*;

import javax.media.opengl.*;

import com.sun.opengl.util.GLUT;

public class J7_3_Cg extends J4_7_TexObjects {
	CGcontext cgcontext;
	CGprogram vertexprog;
	CGprogram fragmentprog;
	static final int VERTEXPROFILE = CgGL.CG_PROFILE_ARBVP1,
			FRAGMENTPROFILE = CgGL.CG_PROFILE_ARBFP1;
	static CGparameter modelviewprojection, modelview, inversetranspose, myLa,
			myLd, myLs, myLightPosition, myEyePosition, myMe, myMa, myMd, myMs,
			myShininess, 
			imgtexture; // texture object name

	public void init(GLAutoDrawable glDrawable) {

		super.init(glDrawable);
		if (!CgGL.cgGLIsProfileSupported(VERTEXPROFILE)) {
			System.out.println("Profile not supported");
			System.exit(1);
		}

		cgcontext = CgGL.cgCreateContext();

		CgGL.cgGLSetOptimalOptions(VERTEXPROFILE);
		vertexprog = CgGL.cgCreateProgramFromFile(cgcontext, CgGL.CG_SOURCE,
				"J7_3_VP.cg", VERTEXPROFILE, null, null);
		CgGL.cgGLLoadProgram(vertexprog);

		modelview = CgGL.cgGetNamedParameter(vertexprog, "modelView");
		modelviewprojection = CgGL.cgGetNamedParameter(vertexprog,
				"modelViewProjection");
		inversetranspose = CgGL.cgGetNamedParameter(vertexprog,
				"inverseTranspose");

		if (!CgGL.cgGLIsProfileSupported(FRAGMENTPROFILE)) {
			System.out.println("Fragment profile not supported");
			System.exit(1);
		}

		CgGL.cgGLSetOptimalOptions(FRAGMENTPROFILE);
		fragmentprog = CgGL.cgCreateProgramFromFile(cgcontext, CgGL.CG_SOURCE,
				"J7_3_FP.cg", FRAGMENTPROFILE, null, null);
		CgGL.cgGLLoadProgram(fragmentprog);

		myLa = CgGL.cgGetNamedParameter(fragmentprog, "La");
		myLd = CgGL.cgGetNamedParameter(fragmentprog, "Ld");
		myLs = CgGL.cgGetNamedParameter(fragmentprog, "Ls");
		myLightPosition = CgGL.cgGetNamedParameter(fragmentprog,
				"lightPosition");
		myEyePosition = CgGL.cgGetNamedParameter(fragmentprog, "eyePosition");
		myMe = CgGL.cgGetNamedParameter(fragmentprog, "Me");
		myMa = CgGL.cgGetNamedParameter(fragmentprog, "Ma");
		myMd = CgGL.cgGetNamedParameter(fragmentprog, "Md");
		myMs = CgGL.cgGetNamedParameter(fragmentprog, "Ms");
		myShininess = CgGL.cgGetNamedParameter(fragmentprog, "shininess");

		// texture object name for Fragment Shader
		imgtexture = CgGL.cgGetNamedParameter(fragmentprog, "imgTexure");

		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_NORMALIZE);

		gl.glEnable(GL.GL_LIGHT0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, blackish, 0);
		CgGL.cgSetParameter4fv(myLa, blackish, 0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, white, 0);
		CgGL.cgSetParameter4fv(myLd, white, 0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, white, 0);
		CgGL.cgSetParameter4fv(myLs, white, 0);
		coordOff = true;
	}

	public void display(GLAutoDrawable drawable) {

		CgGL.cgGLEnableProfile(VERTEXPROFILE);
		CgGL.cgGLBindProgram(vertexprog);
		CgGL.cgGLEnableProfile(FRAGMENTPROFILE);
		CgGL.cgGLBindProgram(fragmentprog);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		super.displayView();

		CgGL.cgGLDisableProfile(VERTEXPROFILE);
		CgGL.cgGLDisableProfile(FRAGMENTPROFILE);
	}

	public void drawSolar(float E, float e, float M, float m) {

		gl.glPushMatrix();
		gl.glRotatef(e, 0, 1, 0);
		// rotating around the "sun"; proceed angle
		gl.glRotatef(tiltAngle, 0, 0, 1); // tilt angle
		gl.glTranslatef(0, 1.5f * E, 0);

		gl.glPushMatrix();
		gl.glTranslatef(0, 1.5f * E, 0);
		gl.glScalef(E * 2, E * 1.5f, E * 2);
		gl.glRotatef(-(m + e) / 4, 0, 1, 0); // self rotation
		myMaterialColor(blackish, white, white, black);
		drawSphere();
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glScalef(E / 2, 1.5f * E, E / 2);
		gl.glRotatef(90, 1, 0, 0); // orient the cone

		myMaterialColor(blackish, red, white, black);
		drawCone();
		gl.glPopMatrix();

		gl.glTranslatef(0, E / 2, 0);
		gl.glRotatef(m, 0, 1, 0); // 1st moon
		gl.glPushMatrix();
		gl.glTranslatef(3.5f * M, 0, 0);
		gl.glLineWidth(1);
		gl.glScalef(E / 4, E / 4, E / 4);

		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, origin, 0);

		gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, currM, 0);
		sphereC[0] = currM[12];
		sphereC[1] = currM[13];
		sphereC[2] = currM[14];
		CgGL.cgSetParameter3fv(myLightPosition, sphereC, 0);
		sphereC[0] = 0;
		sphereC[1] = 0;
		sphereC[2] = 100;
		CgGL.cgSetParameter3fv(myEyePosition, sphereC, 0);

		myMaterialColor(whitish, white, white, whitish);
		drawSphere();
		gl.glPopMatrix();
		gl.glPopMatrix();

		myMaterialColor(blackish, yellow, white, black);

		drawSphere();
		gl.glPopMatrix();
		gl.glPopMatrix();
	}

	public void myMaterialColor(float myA[], float myD[], float myS[],
			float myE[]) {

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, myA, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, myD, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, myS, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, myE, 0);
		CgGL.cgSetParameter4fv(myMe, myE, 0);
		CgGL.cgSetParameter4fv(myMa, myA, 0);
		CgGL.cgSetParameter4fv(myMd, myD, 0);
		CgGL.cgSetParameter4fv(myMs, myS, 0);
		CgGL.cgSetParameter1f(myShininess, 50);
	}

	public void drawSphere() {

		// gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_POINT);
		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);

		if ((cnt % 1503) < 500) {
			// set texture object name for Fragment Shader
			gl.glBindTexture(GL.GL_TEXTURE_2D, EARTH_TEX[0]);
			CgGL.cgGLSetTextureParameter(imgtexture, EARTH_TEX[0]);
		} else {
			gl.glBindTexture(GL.GL_TEXTURE_2D, IRIS_TEX[0]);
			CgGL.cgGLSetTextureParameter(imgtexture, IRIS_TEX[0]);
		}
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

		// enable texture object name for Fragment Shader
		CgGL.cgGLEnableTextureParameter(imgtexture);
		gl.glEnable(GL.GL_TEXTURE_2D);

		subdivideSphere(sVdata[0], sVdata[1], sVdata[2], depth);
		subdivideSphere(sVdata[0], sVdata[2], sVdata[4], depth);
		subdivideSphere(sVdata[0], sVdata[4], sVdata[5], depth);
		subdivideSphere(sVdata[0], sVdata[5], sVdata[1], depth);

		subdivideSphere(sVdata[3], sVdata[1], sVdata[5], depth);
		subdivideSphere(sVdata[3], sVdata[5], sVdata[4], depth);
		subdivideSphere(sVdata[3], sVdata[4], sVdata[2], depth);
		subdivideSphere(sVdata[3], sVdata[2], sVdata[1], depth);
	}

	public void drawCylinder() {

		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);
		super.drawSphere();
	}

	public void drawCone() {

		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);
		super.drawCone();
	}

	public void drawColorCoord(float xlen, float ylen, float zlen) {

		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);

		if (coordOff)
			return; // cjx for images

		gl.glBegin(GL.GL_LINES);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, red, 0);
		gl.glColor3f(1, 0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 0, zlen);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, green, 0);
		gl.glColor3f(0, 1, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, ylen, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, blue, 0);
		gl.glColor3f(0, 0, 1);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(xlen, 0, 0);
		gl.glEnd();

		// coordinate labels: X, Y, Z
		gl.glPushMatrix();
		gl.glTranslatef(xlen, 0, 0);
		gl.glScalef(xlen / WIDTH, xlen / WIDTH, 1);
		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);
		glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, 'X');
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, green, 0);
		gl.glColor3f(0, 1, 0);
		gl.glTranslatef(0, ylen, 0);
		gl.glScalef(ylen / WIDTH, ylen / WIDTH, 1);
		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);
		glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, 'Y');
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, red, 0);
		gl.glColor3f(1, 0, 0);
		gl.glTranslatef(0, 0, zlen);
		gl.glScalef(zlen / WIDTH, zlen / WIDTH, 1);
		CgGL.cgGLSetStateMatrixParameter(modelview,
				CgGL.CG_GL_MODELVIEW_MATRIX, CgGL.CG_GL_MATRIX_IDENTITY);
		CgGL.cgGLSetStateMatrixParameter(inversetranspose,
				CgGL.CG_GL_MODELVIEW_MATRIX,
				CgGL.CG_GL_MATRIX_INVERSE_TRANSPOSE);
		CgGL.cgGLSetStateMatrixParameter(modelviewprojection,
				CgGL.CG_GL_MODELVIEW_PROJECTION_MATRIX,
				CgGL.CG_GL_MATRIX_IDENTITY);
		glut.glutStrokeCharacter(GLUT.STROKE_ROMAN, 'Z');
		gl.glPopMatrix();

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, black, 0);
	}

	public static void main(String[] args) {
		J7_3_Cg f = new J7_3_Cg();

		f.setTitle("JOGL J7_3_Cg");
		f.setSize(WIDTH, HEIGHT);
		f.setVisible(true);
	}
}
