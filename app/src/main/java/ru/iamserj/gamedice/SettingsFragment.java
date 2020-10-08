package ru.iamserj.gamedice;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;


// blank Fragment for a group of similar settings
public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
	
	// access the RecyclerView object, which contains the PreferenceScreen's items in order to add padding to it
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		final RecyclerView rv = getListView();
		rv.setPadding(0, 48, 0, 0); // (left, top, right, bottom)
	}
}