

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import com.jogamp.opengl.*;


public class JOGL1_1_PointVFfiles extends JOGL1_0_Point {

//	public JOGL1_1_PointVFfiles() { // it calls supers constructor first
//	}

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
	
	
	public void init(GLAutoDrawable drawable) {
		gl = (GL4) drawable.getGL();
		String vShaderSource[], fShaderSource[] ;
		
		vShaderSource = readShaderSource("src/JOGL1_1_V.shader"); // read vertex shader
		fShaderSource = readShaderSource("src/JOGL1_1_F.shader"); // read fragment shader
		initShaders(vShaderSource, fShaderSource);		
	}

	
	public static void main(String[] args) {
		new JOGL1_1_PointVFfiles();
	}
}
