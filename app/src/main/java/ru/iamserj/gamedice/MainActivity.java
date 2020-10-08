package ru.iamserj.gamedice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
	
	private ImageView dieLeft;
	private ImageView dieRight;
	private TextView resultText;
	private TextView shakeHintText;
	
	private SharedPreferences sharedPref;
	private boolean switchSoundValue;
	private boolean switchVibrationValue;
	private boolean switchSummaryResultValue;
	private boolean switchShakeRoll;
	private boolean switchRedDice;
	
	private Vibrator vibro;
	private MediaPlayer mediaPlayer;
	private Sensor sensor;
	private SensorManager sensorManager;
	
	private int dieRandom1 = 0;
	private int dieRandom2 = 0;
	static final String DIE_LEFT_SAVED = "dieLeftSaved";
	static final String DIE_RIGHT_SAVED = "dieRightSaved";
	private int[] diceImagesRed;
	private int[] diceImagesRandom;
	private int[] currentDiceImages;
	private long lastUpdate;
	private boolean buttonEnabled = true;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// disable status bar background color
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			Window w = getWindow();
			w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		}
		
		final Button rollBtn = findViewById(R.id.btn_roll);
		dieLeft = findViewById(R.id.iv_dieLeft);
		dieRight = findViewById(R.id.iv_dieRight);
		resultText = findViewById(R.id.tv_result);
		shakeHintText = findViewById(R.id.tv_shakeHint);
		ImageView settingsBtn = findViewById(R.id.iv_settings);
		
		diceImagesRed = new int[]{R.drawable.die_red_1, R.drawable.die_red_2, R.drawable.die_red_3, R.drawable.die_red_4, R.drawable.die_red_5, R.drawable.die_red_6};
		
		diceImagesRandom = new int[]{R.drawable.die_rnd_1, R.drawable.die_rnd_2, R.drawable.die_rnd_3, R.drawable.die_rnd_4, R.drawable.die_rnd_5, R.drawable.die_rnd_6};
		
		currentDiceImages = diceImagesRed;
		
		vibro = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		lastUpdate = System.currentTimeMillis();
		
		if (savedInstanceState != null) {
			dieRandom1 = savedInstanceState.getInt(DIE_LEFT_SAVED);
			dieRandom2 = savedInstanceState.getInt(DIE_RIGHT_SAVED);
			if (dieRandom1 != 0) {
				dieLeft.setImageResource(currentDiceImages[dieRandom1 - 1]);
				dieRight.setImageResource(currentDiceImages[dieRandom2 - 1]);
				final String output = dieRandom1 + dieRandom2 + "";
				resultText.setText(output);
			}
		}
		
		rollBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rollTheDice();
			}
		});
		
		settingsBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
		
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);     // set all switchers to default values
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		initiateSettings();
	}
	
	private void initiateSettings() {
		switchSoundValue = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_SOUND, false);
		switchVibrationValue = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_VIBRATION, false);
		switchSummaryResultValue = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_SUMMARY_RESULT, false);
		switchShakeRoll = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_SHAKE_ROLL, false);
		switchRedDice = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SWITCH_RED_DICE, false);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		initiateSettings();
		
		resultText.setVisibility(switchSummaryResultValue ? View.VISIBLE : View.INVISIBLE);
		shakeHintText.setVisibility(switchShakeRoll ? View.VISIBLE : View.INVISIBLE);
		if (switchRedDice) {
			currentDiceImages = diceImagesRed;
		} else {
			currentDiceImages = diceImagesRandom;
		}
		if (dieRandom1 > 0) {
			dieLeft.setImageResource(currentDiceImages[dieRandom1 - 1]);
			dieRight.setImageResource(currentDiceImages[dieRandom2 - 1]);
		} else {
			dieLeft.setImageResource(currentDiceImages[0]);
			dieRight.setImageResource(currentDiceImages[1]);
		}
	}
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
		if (dieRandom1 != 0) savedInstanceState.putInt(DIE_LEFT_SAVED, dieRandom1);
		if (dieRandom2 != 0) savedInstanceState.putInt(DIE_RIGHT_SAVED, dieRandom2);
		super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (switchShakeRoll && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			checkAccelerometerData(event);
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
	
	
	private void rollTheDice() {
		
		if (!buttonEnabled) return;
		buttonEnabled = false;
		
		// initialize MediaPlayer
		if (switchSoundValue) mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.drop_sound);
		
		// generate random numbers
		Random rnd = new Random();
		dieRandom1 = rnd.nextInt(6) + 1;
		dieRandom2 = rnd.nextInt(6) + 1;
		final String output = dieRandom1 + dieRandom2 + "";
		
		// FadeOut dice and result text
		YoYo.with(Techniques.FadeOut).duration(200).repeat(0).playOn(dieLeft);
		YoYo.with(Techniques.FadeOut).duration(200).repeat(0).playOn(dieRight);
		YoYo.with(Techniques.FadeOut).duration(200).repeat(0).playOn(resultText);
		
		// play sound, set new imageResources for dice
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				if (switchSoundValue) mediaPlayer.start();
				dieLeft.setImageResource(currentDiceImages[dieRandom1 - 1]);
				dieRight.setImageResource(currentDiceImages[dieRandom2 - 1]);
			}
		}, 200);
		
		// RollIn right die
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				YoYo.with(Techniques.RollIn).duration(500).repeat(0).playOn(dieRight);
			}
		}, 250);
		
		// RollIn left die
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				YoYo.with(Techniques.RollIn).duration(500).repeat(0).playOn(dieLeft);
			}
		}, 450);
		
		// vibrate 1
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				if (switchVibrationValue) vibrate();
			}
		}, 500);
		
		// vibrate 2
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				if (switchVibrationValue) vibrate();
			}
		}, 700);
		
		// set and show result text
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				resultText.setText(output);
				YoYo.with(Techniques.FadeIn).duration(500).repeat(0).playOn(resultText);
			}
		}, 750);
		
		
		// stop sound, enable button
		new android.os.Handler().postDelayed(new Runnable() {
			public void run() {
				if (switchSoundValue) stopSound();
				buttonEnabled = true;
			}
		}, 950);
	}
	
	private void vibrate() {
		int millis = 50;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			vibro.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE));
		} else {
			vibro.vibrate(millis);  //deprecated in API 26
		}
	}
	
	private void stopSound() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}
	
	private void checkAccelerometerData(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		long actualTime = System.currentTimeMillis();
		float accelerationSquareRoot = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		
		short shakeIntensity = 3;
		short shakeTimeout = 1000;
		if (accelerationSquareRoot > shakeIntensity && actualTime - lastUpdate > shakeTimeout) {
			lastUpdate = actualTime;
			rollTheDice();
		}
	}
}

// Possible improvements: add other cube colors