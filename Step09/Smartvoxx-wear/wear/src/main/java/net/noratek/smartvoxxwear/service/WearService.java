package net.noratek.smartvoxxwear.service;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import net.noratek.smartvoxx.common.utils.Constants;
import net.noratek.smartvoxxwear.activity.WindowActivity;

/**
 * Created by eloudsa on 15/10/15.
 */
public class WearService extends WearableListenerService {

    private final static String TAG = WearService.class.getCanonicalName();


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        // Processing the message
        String path = messageEvent.getPath();

        if (path.equalsIgnoreCase(Constants.WAKEUP_PATH)) {

            // wake-up the application if it has hidden by a notification screen
            Intent startWindow = new Intent(this, WindowActivity.class);
            startWindow.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startWindow);

            return;
        }

    }
}
