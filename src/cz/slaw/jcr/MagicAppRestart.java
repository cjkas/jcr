package cz.slaw.jcr;

import android.app.Activity;
import android.content.Intent;

/** This activity shows nothing; instead, it restarts the android process */
public class MagicAppRestart extends Activity {
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		finish();
		System.exit(0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startActivityForResult(new Intent(this, MainActivity.class), 0);
	}
}