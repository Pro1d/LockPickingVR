/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.awprog.lockpickingvr.graphic.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.awprog.lockpickingvr.graphic.GLSurfaceView;
import com.awprog.lockpickingvr.graphic.Shader;
import com.awprog.lockpickingvr.graphic.GLSurfaceView.MatrixStack;
import com.awprog.lockpickingvr.model.PolarCoord;

/**
 * A two-dimensional triangle for use as a drawn object in OpenGL ES 2.0.
 */
public class KeyHand {

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer normalBuffer;
    private final FloatBuffer colorBuffer;
    private final IntBuffer indicesBuffer;

    Shader shader;
    
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final int cylOffset = 0, cylCount = PolarCoord.AngleStepCount * 2 * COORDS_PER_VERTEX;
    private final int ringOffset = cylOffset+cylCount, ringCount = 2*PolarCoord.AngleStepCount * PolarCoord.AngleStepCount * COORDS_PER_VERTEX;
    
    private final int vertexCount = (ringOffset+ringCount)/COORDS_PER_VERTEX;
    
    static protected final float radius = 0.13f;
    private final float thickness, length;

    static final float color[] = { 0.5f, 0.5f, 0.5f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public KeyHand(float thickness, float length, float ringRadius) {
    	this.thickness = thickness;
    	this.length = length;
        indicesBuffer = ByteBuffer.allocateDirect((6+PolarCoord.AngleStepCount*6*2) * PolarCoord.AngleStepCount * 4 /*sizeof(int)*/)
        		.order(ByteOrder.nativeOrder()).asIntBuffer();        
        vertexBuffer = ByteBuffer.allocateDirect(vertexCount * COORDS_PER_VERTEX * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalBuffer = ByteBuffer.allocateDirect(vertexCount * COORDS_PER_VERTEX * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorBuffer = ByteBuffer.allocateDirect(vertexCount * color.length * 4/*sizeof(float*/)
        		.order(ByteOrder.nativeOrder()).asFloatBuffer();
       for(int i = 0; i < vertexCount; i++)
    	   colorBuffer.put(color);

       // cylinder
		int triangleIndice[] = { 0, 0, 0 }, triangleCursor = 0;
		for (int a = 0; a < PolarCoord.AngleStepCount; a++) {
			float x = radius * (float) Math.cos(a * PolarCoord.AngleStepRad);
			float y = radius * (float) Math.sin(a * PolarCoord.AngleStepRad);
			for (int i = 0; i <= 1; i++) {
				vertexBuffer.put(x).put(y).put(i * length);
				normalBuffer.put(x).put(y).put(0.0f);

				triangleIndice[triangleCursor % 3] = triangleCursor;
				if (++triangleCursor >= 3) {
					if ((triangleCursor & 1) == 0)
						indicesBuffer.put(triangleIndice);
					else
						indicesBuffer.put(triangleIndice[0])
								.put(triangleIndice[2]).put(triangleIndice[1]);
				}
			}
		}
   	
		triangleIndice[triangleCursor++ % 3] = 0;
		indicesBuffer.put(triangleIndice[0]).put(triangleIndice[2]).put(triangleIndice[1]);
		triangleIndice[triangleCursor++ % 3] = 1;
		indicesBuffer.put(triangleIndice);

		// cylinder
		final int A = PolarCoord.AngleStepCount;
		final int B = PolarCoord.AngleStepCount*2;
		for (int a = 0; a < A; a++) {
			float nx = PolarCoord.cos(a);
			float x = radius * nx;
			float nyPlan = PolarCoord.sin(a);
			float yPlan = radius * nyPlan;// + ringRadius;
			for (int b = 0; b < B; b++) {
				float y = (yPlan + ringRadius) * (float)Math.cos(b*2*Math.PI/B);
				float z = (yPlan + ringRadius * 0.5f) * (float)Math.sin(b*2*Math.PI/B) + length + ringRadius * 0.5f;//-radius;
				float ny = nyPlan * (float)Math.cos(b*2*Math.PI/B);
				float nz = nyPlan * (float)Math.sin(b*2*Math.PI/B);
				vertexBuffer.put(x).put(y).put(z);
				normalBuffer.put(nx).put(ny).put(nz);
				int offset = ringOffset/COORDS_PER_VERTEX;
				indicesBuffer.put(offset + a*B			+ b)
							 .put(offset + ((a+1)%A)*B	+ b)
							 .put(offset + ((a+1)%A)*B 	+ (b+1)%B);
				indicesBuffer.put(offset + a*B			+ b)
							 .put(offset + ((a+1)%A)*B 	+ (b+1)%B)
							 .put(offset + a*B			+ (b+1)%B);
			}
		}

        vertexBuffer.position(0);
        normalBuffer.position(0);
        colorBuffer.position(0);
        indicesBuffer.position(0);
        
        shader = new Shader();
        shader.addLight(0, 0, -4.5f, true, true);
        shader.addLight(-1, 1, -3.5f, false, true);
        shader.createAndlinkProgram();
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix, float[] mvMatrix) {
    	vertexBuffer.position(0);
        normalBuffer.position(0);
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
