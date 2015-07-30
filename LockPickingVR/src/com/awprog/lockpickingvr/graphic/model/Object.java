package com.awprog.lockpickingvr.graphic.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.awprog.lockpickingvr.graphic.GLSurfaceView;
import com.awprog.lockpickingvr.graphic.Shader;

import android.opengl.GLES20;

public class Object {
	protected FloatBuffer vertexBuffer;
    protected FloatBuffer normalBuffer;
    protected FloatBuffer colorBuffer;
    protected IntBuffer indicesBuffer;

    protected Shader shader;
    
	// number of coordinates per vertex in this array
    protected static final int COORDS_PER_VERTEX = 3;
    
    protected Object() {
    	
    }
    
    /** concatenates the objects, does not keep the shader of the given object **/
    public Object(Object objects[]) {
    	int vertexSize = 0, normalSize = 0, colorSize = 0, indiceSize = 0;
    	for(Object o : objects) {
    		vertexSize += o.vertexBuffer.capacity();
    		normalSize += o.normalBuffer.capacity();
    		colorSize += o.colorBuffer.capacity();
    		indiceSize += o.indicesBuffer.capacity();
    	}
    	indicesBuffer = ByteBuffer.allocateDirect(indiceSize * 4 /*sizeof(int)*/)
        		.order(ByteOrder.nativeOrder()).asIntBuffer();        
        vertexBuffer = ByteBuffer.allocateDirect(vertexSize * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer = ByteBuffer.allocateDirect(normalSize * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer = ByteBuffer.allocateDirect(colorSize * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(Object o : objects) {
        	int offset = vertexBuffer.position()/COORDS_PER_VERTEX;
        	vertexBuffer.put(o.vertexBuffer);
        	normalBuffer.put(o.normalBuffer);
        	colorBuffer.put(o.colorBuffer);
        	while(o.indicesBuffer.hasRemaining())
        		indicesBuffer.put(offset + o.indicesBuffer.get());
    	}
        
        shader = new Shader();
        shader.addLight(0, 0, -4.5f, true, true);
        shader.addLight(-1, 1, -3.5f, false, true);
        shader.createAndlinkProgram();
    }
    
    public void draw(float[] mvpMatrix, float[] mvMatrix) {
    	vertexBuffer.position(0);
    	normalBuffer.position(0);
    	colorBuffer.position(0);
    	indicesBuffer.position(0);
    	
        // Add program to OpenGL environment
        shader.useProgram();
        GLSurfaceView.checkGlError("glUseProgram");

        shader.setPositionAttribute(vertexBuffer);
        shader.setNormalAttribute(normalBuffer);
        shader.setColorAttribute(colorBuffer);
        shader.setMVMatrixUniform(mvMatrix);
        shader.setMVPMatrixUniform(mvpMatrix);
        
        GLSurfaceView.checkGlError("glGet[...]Location");

        // Draw 
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesBuffer.capacity(), GLES20.GL_UNSIGNED_INT, indicesBuffer);

        // Disable vertex array
//        GLES20.glDisableVertexAttribArray(mPositionHandle);
//        GLES20.glDisableVertexAttribArray(mNormalHandle);
    }
}
