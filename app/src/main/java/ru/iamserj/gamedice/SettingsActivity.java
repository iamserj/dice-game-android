package ru.iamserj.gamedice;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
	
	public static final String KEY_PREF_SWITCH_SOUND = "switch_sound";
	public static final String KEY_PREF_SWITCH_VIBRATION = "switch_vibration";
	public static final String KEY_PREF_SWITCH_SUMMARY_RESULT = "switch_summary_result";
	public static final String KEY_PREF_SWITCH_SHAKE_ROLL = "switch_shake_roll";
	public static final String KEY_PREF_SWITCH_RED_DICE = "switch_red_dice";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// display the Fragment in SettingsActivity
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment())
				.commit();
	}
}