package net.noratek.smartvoxxwear.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

/**
 * This activity is used to wake-up the application if it was idle.
 *
 * Created by eloudsa on 15/10/15.
 */
public class WindowActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        super.finish();
    }
}
