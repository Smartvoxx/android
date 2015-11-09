package net.noratek.smartvoxxwear.alarm;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by eloudsa on 26/09/15.
 */
public class AlarmService extends IntentService {

    public static final String BROADCAST =
            "net.noratek.smartvoxxwear.AlarmService.BROADCAST";

    private static Intent broadcast = new Intent(BROADCAST);



    public AlarmService() {
        super("AlarmService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        // Relay the request code linked to the alarm service
        int requestCode = intent.getIntExtra("requestCode", 0);
        broadcast.putExtra("requestCode", requestCode);

        Long eventId = intent.getLongExtra("eventId", 0L);
        broadcast.putExtra("eventId", eventId);

        String talkId = intent.getStringExtra("talkId");
        broadcast.putExtra("talkId", talkId);

        String eventTitle = intent.getStringExtra("title");
        broadcast.putExtra("title", eventTitle);

        String pixyncId = intent.getStringExtra("roomName");
        broadcast.putExtra("roomName", pixyncId);

        long startDate = intent.getLongExtra("startTime", 0L);
        broadcast.putExtra("startTime", startDate);

        long endDate = intent.getLongExtra("endTime", 0L);
        broadcast.putExtra("endTime", endDate);


        sendOrderedBroadcast(broadcast, null);

    }
}
