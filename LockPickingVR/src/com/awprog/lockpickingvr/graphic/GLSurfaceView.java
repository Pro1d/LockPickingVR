package com.awprog.lockpickingvr.graphic;

import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import com.awprog.lockpickingvr.graphic.model.KeyHand;
import com.awprog.lockpickingvr.graphic.model.KeyNotch;
import com.awprog.lockpickingvr.graphic.model.NotchConstraint;
import com.awprog.lockpickingvr.graphic.model.NotchHole;
import com.awprog.lockpickingvr.graphic.model.Object;
import com.awprog.lockpickingvr.model.Game;


public class GLSurfaceView extends android.opengl.GLSurfaceView implements android.opengl.GLSurfaceView.Renderer {
	private Game game;
    private KeyNotch mKey;
    private KeyHand mKeyHand;
    private Object mkeyhole;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
	
    private final float depthScale = 1.4f;
    
    public void setGame(Game game) {
    	this.game = game;
    }
    
	public GLSurfaceView(Context context, AttributeSet attrs) {//Game game) {
		super(context,attrs);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);
        
		setRenderer(this);
    }

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		//GLES20.glCullFace(GLES20.GL_FRONT); 
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
        // Set the background frame color
		GLES20.glClearColor(0, 0, 0, 1);
        mKeyHand = new KeyHand(0.05f, depthScale*0.25f, depthScale*0.75f);
        mKey = new KeyNotch(0.05f, depthScale);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

	    // make adjustments for screen ratio
	    float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	}
	long lastT;
	@Override
	public void onDrawFrame(GL10 unsued) {
		long t = SystemClock.elapsedRealtime();
		if(game == null)
			return;
		else if(mkeyhole == null) {
			NotchHole nh[] = new NotchHole[game.getKeyhole().getDepth()];
			NotchConstraint nc[] = new NotchConstraint[game.getKeyhole().getDepth()];
			Object[] o = new Object[nh.length+nc.length];
			for(int i = 0; i < nh.length; i++) {
				(nh[i] = new NotchHole(0.05f, i*-depthScale)).update(game.getKeyhole().mNotches.get(i).mHole, i*-depthScale);
				(nc[i] = new NotchConstraint(depthScale*0.5f-0.05f, (0.5f+i)*-depthScale)).update(game.getKeyhole().mNotches.get(i).mPositionRestriction, (0.5f+i)*-depthScale);
				o[2*i] = nh[i];
				o[2*i+1] = nc[i];
			}
			mkeyhole = new Object(o);
		}

		float[] scratch = new float[16];
		float[] MV = new float[16];
		MatrixStack ms = new MatrixStack();

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 5f, 0.0f, 0.0f, 0.0f, 0f, 1f, 0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Matrix.translateM(ms.get(), 0, 0, 0, -0.5f);
        Matrix.rotateM(ms.get(), 0, 60, 2.f/5, 3.f/5, 0);
        Matrix.scaleM(ms.get(), 0, 0.5f, 0.5f, 0.5f);
        ms.push();

        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, ms.get(), 0);
        Matrix.multiplyMM(MV, 0, mViewMatrix, 0, ms.get(), 0);
        mkeyhole.draw(scratch, MV);
        /*
        // KEYHOLE
        for(int d = 0; d < game.getKeyhole().getDepth(); d++) {
            ms.push();
            //Matrix.rotateM(ms.get(), 0, (float) Math.toDegrees(game.getRealKeyAngle())/*angle* /, 0, 0, 1);
            
            // Combine the rotation matrix with the projection and camera view
            // Note that the mMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, ms.get(), 0);
            Matrix.multiplyMM(MV, 0, mViewMatrix, 0, ms.get(), 0);
            mNotch.draw(scratch, MV, game.getKeyhole().mNotches.get(d));

            Matrix.translateM(ms.get(), 0, 0, 0, -depthScale*0.5f);
            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, ms.get(), 0);
            Matrix.multiplyMM(MV, 0, mViewMatrix, 0, ms.get(), 0);
            mConstraint.draw(scratch, MV, game.getKeyhole().mNotches.get(d));
        	
        	ms.pop();
            Matrix.translateM(ms.get(), 0, 0, 0, -depthScale);
        }*/
        ms.pop();

        ms.push();
        Matrix.translateM(ms.get(), 0, 0, 0, -depthScale*(game.getDisplayedKeyDepth()-game.getKey().getLength()+1));
        ms.push();
        // KEY
        for(int d = 0; d < game.getKey().getLength(); d++) {
            ms.push();
            Matrix.rotateM(ms.get(), 0, (float) Math.toDegrees(game.getDisplayedKeyAngle())/*angle*/, 0, 0, 1);

            Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, ms.get(), 0);
            Matrix.multiplyMM(MV, 0, mViewMatrix, 0, ms.get(), 0);
            mKey.draw(scratch, MV, game.getKey().mNotches.get(d));
            
            ms.pop();
            Matrix.translateM(ms.get(), 0, 0, 0, -depthScale);
        }
        ms.pop();

        Matrix.translateM(ms.get(), 0, 0, 0, +depthScale);
        Matrix.rotateM(ms.get(), 0, (float) Math.toDegrees(game.getDisplayedKeyAngle())/*angle*/, 0, 0, 1);

        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, ms.get(), 0);
        Matrix.multiplyMM(MV, 0, mViewMatrix, 0, ms.get(), 0);
        mKeyHand.draw(scratch, MV);
        
        ms.pop();
		long t2 = SystemClock.elapsedRealtime();
		//Log.i("###", "frame:"+(t2-t)+" interframe:"+(t-lastT));
		lastT = t;
	}
	

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
    * Utility method for debugging OpenGL calls. Provide the name of the call
    * just after making it:
    *
    * <pre>
    * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
    * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
    *
    * If the operation is not successful, the check throws an error.
    *
    * @param glOperation - Name of the OpenGL call to check.
    */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("###", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
    
    
    public static class MatrixStack {
    	Stack<float[]> stack = new Stack<float[]>();
    	public MatrixStack() {
			stack.push(new float[16]);
			Matrix.setIdentityM(get(), 0);
		}
    	
    	public void push() {
    		stack.push(stack.peek().clone());
    	}
    	public void pop() {
    		stack.pop();
    	}
    	public float[] get() {
    		return stack.peek();
    	}
    }
}
/*

// New class members
/** Allocate storage for the final combined matrix. This will be passed into the shader program. * /
private float[] mMVPMatrix = new float[16];
 
/** How many elements per vertex. * /
private final int mStrideBytes = 7 * mBytesPerFloat;
 
/** Offset of the position data. * /
private final int mPositionOffset = 0;
 
/** Size of the position data in elements. * /
private final int mPositionDataSize = 3;
 
/** Offset of the color data. * /
private final int mColorOffset = 3;
 
/** Size of the color data in elements. * /
private final int mColorDataSize = 4;
 
/**
 * Draws a triangle from the given vertex data.
 *
 * @param aTriangleBuffer The buffer containing the vertex data.
 * /
private void drawTriangle(final FloatBuffer aTriangleBuffer)
{
    // Pass in the position information
    aTriangleBuffer.position(mPositionOffset);
    GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer);
 
    GLES20.glEnableVertexAttribArray(mPositionHandle);
 
    // Pass in the color information
    aTriangleBuffer.position(mColorOffset);
    GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer);
 
    GLES20.glEnableVertexAttribArray(mColorHandle);
 
    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
    // (which currently contains model * view).
    Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
 
    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
    // (which now contains model * view * projection).
    Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
 
    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
}

// Pass in the position information
aTriangleBuffer.position(mPositionOffset);
GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        mStrideBytes, aTriangleBuffer);
GLES20.glEnableVertexAttribArray(mPositionHandle);
*/