package com.awprog.lockpickingvr.graphic;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import android.opengl.GLES20;

public class Shader {
	private ArrayList<Light> lights = new ArrayList<Light>();

	private int mProgramHandle;

	private String getVertexShader() {
		return  // This matrix member variable provides a hook to manipulate
	            // the coordinates of the objects that use this vertex shader
	            "uniform mat4 uMVPMatrix;" +  // A constant representing the combined model/view/projection matrix.
			    "uniform mat4 uMVMatrix;"+   // A constant representing the combined model/view matrix.
	            "attribute vec4 aPosition;" +
	            "attribute vec3 aNormal;" +
	            "attribute vec4 aColor;" +
	            
	            // This will be passed into the fragment shader.
	            "varying vec4 v_Color;"+
	            "varying vec4 v_Position;"+
	            "varying vec3 v_Normal;"+
	            
	            "void main() {" +
	            // the matrix must be included as a modifier of gl_Position
	            // Note that the uMVPMatrix factor *must be first* in order
	            // for the matrix multiplication product to be correct.
	            "  gl_Position = uMVPMatrix * aPosition;" +
	            "  v_Position = uMVMatrix * aPosition;" +
	    		// Transform the normal's orientation into eye space.
	  		  	"  v_Normal = normalize(vec3(uMVMatrix * vec4(aNormal, 0.0)));"+
	            "  v_Color = aColor;"+
	            "}";
	}
	private String getFragmentShader() {
		String lightsDeclaration = "";
		String lightsDiffuse = "float cosin_sum = 0.0;";
		String lightsSpec = "float spec_sum = 0.0;";
		for(int i = 0; i < lights.size(); i++) {
			lightsDeclaration += "vec3 lightPos"+i+
								 " = vec3("+lights.get(i).pos[0]+", "+lights.get(i).pos[1]+", "+lights.get(i).pos[2]+");";
			
			if(lights.get(i).applyViewMatrix)
				lightsDiffuse += "lightPos"+i+" = uMVMatrix * lightPos"+i;
			
			lightsDiffuse += "vec3 lightDir"+i+" = lightPos"+i+" - v_Position.xyz;";
			lightsDiffuse += "float cosin"+i+" = max(dot(normalize(lightDir"+i+"), normalize(v_Normal)), 0.0);";
			//* Toon: */lightsDiffuse += "if(cosin"+i+">0.8) cosin"+i+" = 1.0; else cosin"+i+" = 0.0;";
			lightsDiffuse += "cosin_sum += cosin"+i+" * " +
								"1.0 / (1.0 + length(lightDir"+i+") * length(lightDir"+i+") * 0.25);";
			if(lights.get(i).specEnabled)
				lightsSpec += "if(cosin"+i+" > 0.0) {" +
								"vec3 r = -normalize(lightDir"+i+") + 2.0 * cosin"+i+" * normalize(v_Normal);"+//reflect(-normalize(lightDir"+i+"), normalize(v_Normal));" +
								"float spec = pow(dot(r, -normalize(v_Position.xyz)), 20.0);" +
								//"if(spec > 0.0 && spec < 1.0)" +
								" spec_sum += spec;" +
							  "}";
		}
		return  "precision highp float;" +
	            "varying vec4 v_Color;" +
	            "varying vec4 v_Position;" +
	            "varying vec3 v_Normal;" +
	            
	            lightsDeclaration +
	            
	            "void main() {" +
	            lightsDiffuse + 
	            lightsSpec + 
	            "  gl_FragColor = clamp(vec4(spec_sum, spec_sum, spec_sum, 1.0),0.0,1.0) + mix(v_Color*clamp(cosin_sum, 0.0,1.0), v_Color, 0.2);"+     // Pass the color directly through the pipeline.
	            "}";
	}
	
	public void createAndlinkProgram() {
        int vertexShader = GLSurfaceView.loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader());
        int fragmentShader = GLSurfaceView.loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());
        
        mProgramHandle = GLES20.glCreateProgram();             // create empty OpenGL Program
        
        GLES20.glAttachShader(mProgramHandle, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgramHandle, fragmentShader); // add the fragment shader to program

        GLES20.glLinkProgram(mProgramHandle);                  // create OpenGL program executables
	}

	public void addLight(Light light) {
		lights.add(light);
	}
	public void addLight(float x, float y, float z, boolean spec, boolean viewCoord) {
		lights.add(new Light(x,y,z, spec, viewCoord));
	}
	
	public void useProgram() {
        GLES20.glUseProgram(mProgramHandle);
	}

	public void setPositionAttribute(FloatBuffer buffer) {
		int handle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
		if(handle != -1) {
	        GLES20.glVertexAttribPointer(handle, 3, GLES20.GL_FLOAT, false, 0, buffer.position(0));
	        GLES20.glEnableVertexAttribArray(handle);
		}
	}
	public void setColorAttribute(FloatBuffer buffer) {
		int handle = GLES20.glGetAttribLocation(mProgramHandle, "aColor");
		if(handle != -1) {
	        GLES20.glVertexAttribPointer(handle, 4, GLES20.GL_FLOAT, false, 0, buffer.position(0));
	        GLES20.glEnableVertexAttribArray(handle);
		}	
	}
	public void setNormalAttribute(FloatBuffer buffer) {
		int handle = GLES20.glGetAttribLocation(mProgramHandle, "aNormal");
		if(handle != -1) {
	        GLES20.glVertexAttribPointer(handle, 3, GLES20.GL_FLOAT, false, 0, buffer.position(0));
	        GLES20.glEnableVertexAttribArray(handle);
		}	
	}
	public void setMVPMatrixUniform(float[] mvpMatrix) {
        int handle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        if(handle != -1)
        	GLES20.glUniformMatrix4fv(handle, 1, false, mvpMatrix, 0);
	}
	public void setMVMatrixUniform(float[] mvMatrix) {
        int handle = GLES20.glGetUniformLocation(mProgramHandle, "uMVMatrix");
        if(handle != -1)
        	GLES20.glUniformMatrix4fv(handle, 1, false, mvMatrix, 0);
	}
	
	public static class Light {
		private float[] pos;
		private boolean applyViewMatrix;
		private boolean specEnabled;
		
		/** viewCoord true of position xyz are in view */
		Light(float x, float y, float z, boolean enableSpec, boolean viewCoord) {
			pos = new float[]{x,y,z};
			applyViewMatrix = !viewCoord;
			specEnabled = enableSpec;
		}
	}
}
