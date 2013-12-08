package de.vanita5.twittnuker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.StrictModeUtils;
import de.vanita5.twittnuker.util.Utils;

public class MainActivity extends Activity implements Constants {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (Utils.isDebugBuild()) {
			StrictModeUtils.detectAllVmPolicy();
			StrictModeUtils.detectAllThreadPolicy();
		}
		super.onCreate(savedInstanceState);
		final Intent intent = new Intent(this, HomeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		finish();
	}

}
