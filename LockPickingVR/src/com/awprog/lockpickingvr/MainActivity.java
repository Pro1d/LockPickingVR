package com.awprog.lockpickingvr;

import java.util.UUID;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.fbessou.sofa.GamePadIOHandler;
import com.fbessou.sofa.GamePadInformation;
import com.fbessou.sofa.InputEvent;

public class MainActivity extends ActionBarActivity implements SensorEventListener {
	private float mRotationZ = 0.0f; // rad
	
	private TextView mValuesText;
	private GamePadIOHandler mGamePadIOHandler;
	KeySensor mSensorKeyForward;
	KeySensor mSensorKeyBackward;
    RotationSensor mSensorRotation;
	class RotationSensor extends com.fbessou.sofa.sensor.Sensor {
		public RotationSensor() {
			super(SensorType.ANALOG_2D);
		}
		public void putValues(float x, float y) {
			triggerEvent(InputEvent.createMotion2DEvent(0, x, y, 0));
		}
	}
	class KeySensor extends com.fbessou.sofa.sensor.Sensor {
		boolean on;
		int id;
		public KeySensor(boolean init, int id) {
			super(SensorType.KEY);
			on = init;
			this.id = id;
		}
		public void setValue(boolean s) {
			if(s != on)
				triggerEvent(on ? InputEvent.createKeyUpEvent(id, id):InputEvent.createKeyDownEvent(id, id));
			on = s;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Init accelerometer
        SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        
        // Get view's references
        mValuesText = (TextView) findViewById(R.id.tv_values);
        /*findViewById(R.id.ib_forward).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				sendEventActionForward();
			}
		});
        findViewById(R.id.ib_backward).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				sendEventActionBackward();
			}
		});*/
        
        mGamePadIOHandler = new GamePadIOHandler();
        mGamePadIOHandler.start(this, new GamePadInformation("Key", UUID.randomUUID()));
        com.fbessou.sofa.sensor.Sensor.init();
        mSensorKeyForward = new KeySensor(false, 1);
        mSensorKeyBackward = new KeySensor(false,0);
        mSensorRotation = new RotationSensor();
    	mGamePadIOHandler.attachSensor(mSensorKeyBackward);
        mGamePadIOHandler.attachSensor(mSensorKeyForward);
        mGamePadIOHandler.attachSensor(mSensorRotation);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.unregisterListener(this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        /*if(mGameBinder != null)
        	getFragmentManager().beginTransaction().remove(mGameBinder).commit();*/
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mSensorKeyBackward.setValue(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			mSensorKeyForward.setValue(false);
			return true;
		default:
			return super.onKeyUp(keyCode, event);
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			mSensorKeyBackward.setValue(true);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			mSensorKeyForward.setValue(true);
			return true;
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	int mod = 0;
	public void sendEventAccelerometer(float x, float y, float z) {
		if((++mod)%2 == 0)
			mSensorRotation.putValues(x, -z);
	}
	
	
	/** SensorEventListener interface's methods **/
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		sendEventAccelerometer(event.values[0], event.values[1],event.values[2]);
		
		mRotationZ = (float) Math.atan2(-event.values[2], event.values[0]);
		mValuesText.setText(String.format("Rz %.2f°\n", (mRotationZ*180/Math.PI)));
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
