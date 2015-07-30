package com.awprog.lockpickingvr;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.awprog.lockpickingvr.graphic.GLSurfaceView;
import com.awprog.lockpickingvr.model.Game;
import com.awprog.lockpickingvr.util.SystemUiHider;
import com.fbessou.sofa.GameIOHandler;
import com.fbessou.sofa.GameIOHandler.GamePadInputEvent;
import com.fbessou.sofa.GameIOHandler.GamePadStateChangedEvent;
import com.fbessou.sofa.GameIOHandler.StateChangedEventListener;
import com.fbessou.sofa.GameInformation;
import com.fbessou.sofa.GameMessageReceiver;
import com.fbessou.sofa.StringSender;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements KeyController, com.fbessou.sofa.GameIOHandler.InputEventListener, StateChangedEventListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	

	private TextView mValuesText;
	private KeyController mKeyController;
	private Game game;
	
	GameMessageReceiver gameMsgReceiver = new GameMessageReceiver();
	StringSender gameMsgSender;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.gl_surfaceview);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		
		/**** Game's views ****/

        // Get view's references
        mValuesText = (TextView) findViewById(R.id.tv_values);
        
        // Set the key controller
        setKeyController(this);
        
        game = new Game();
        ((GLSurfaceView)findViewById(R.id.gl_surfaceview)).setGame(game);
        
        GameIOHandler easyIO = new GameIOHandler(this, this);
        easyIO.start(this, new GameInformation("Lock Picking VR"));
		
		frameHandler  = new Handler();
	}
	
	private static final int frameDuration = 30; // 30ms
	Handler frameHandler;
	Runnable lastRunnable;
	public void frame() {
		game.updateDisplayedAngle();
		game.updateDisplayedDepth();
		mValuesText.setText(String.format("Rz %.2f°\n", (game.getDisplayedKeyAngle()*180/Math.PI)));
		
		frameHandler.postDelayed(lastRunnable = new Runnable() {
			@Override
			public void run() {
				frame();
			}
		}, frameDuration);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if(lastRunnable != null)
			frameHandler.removeCallbacks(lastRunnable);
	}
	@Override
	protected void onStart() {
		super.onStart();
		frame();
	}
	
	public void setKeyController(KeyController kc) {
		mKeyController = kc;
	}

	/** Key controller interface's methods **/
	
	@Override
	public void rotateAbs(float absoluteFogglePosition) {
		game.moveTowardAngle(absoluteFogglePosition*2);
	}

	@Override
	public void rotateRel(float relativeFogglePosition) {
	}

	@Override
	public void moveRel(int relativeDepthPosition) {
		game.moveDepth(relativeDepthPosition);
	}

	@Override
	public void moveAbs(int absDepthPosition) {
	}
	

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	
	@Override
	public void onInputEvent(GamePadInputEvent event) {
		switch(event.event.eventType) {
		case FLOATMOVE:
			float angle = (float)Math.atan2(event.event.getY(), event.event.getX());
			mKeyController.rotateAbs(angle);
			break;
		case KEYDOWN:
			if(event.event.padId == 1) {
				mKeyController.moveRel(+1);
			} else if(event.event.padId == 0) {
				mKeyController.moveRel(-1);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onPadEvent(GamePadStateChangedEvent event) {
		// TODO Auto-generated method stub
		
	}
}
