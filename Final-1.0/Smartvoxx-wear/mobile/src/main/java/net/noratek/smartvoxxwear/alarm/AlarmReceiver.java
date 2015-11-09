package net.noratek.smartvoxxwear.alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxx.common.model.Talk;
import net.noratek.smartvoxx.common.utils.Constants;
import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.calendar.CalendarHelper;
import net.noratek.smartvoxxwear.service.EventService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by eloudsa on 26/09/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private final static String TAG = AlarmReceiver.class.getCanonicalName();

    // Play services
    private GoogleApiClient mApiClient;


    @Override
    public void onReceive(Context context, Intent intent) {

        Long eventId = intent.getLongExtra("eventId", 0L);
        final String talkId = intent.getStringExtra("talkId");

        CalendarHelper calendarHelper = new CalendarHelper(context);
        Talk talk = calendarHelper.getTalkByEventId(eventId);
        if (talk == null) {
            // event not more defined on the calendar -> inform the watch
            mApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(TAG, "Event removed. Informing remote devices.");

                            // Inform remote devices
                            sendFavorite(talkId, 0L);
                        }

                        @Override
                        public void onConnectionSuspended(int cause) {

                        }
                    }).build();
            mApiClient.connect();

            return;
        }

        String information = talk.getRoomName();
        information += "\n" + new SimpleDateFormat("dd-MMM, HH:mm").format(new Date(talk.getFromTimeMillis()));

        int notificationId = new Random().nextInt(Integer.MAX_VALUE) + 1;

        Bundle bundle = new Bundle();
        bundle.putString("action", "remove_event");
        bundle.putString("talkid", talkId);
        bundle.putLong("eventid", eventId);
        bundle.putInt("notificationid", notificationId);

        // Create an intent for the reply action
        Intent actionIntent = new Intent(context, EventService.class);
        actionIntent.putExtras(bundle);
        PendingIntent actionPendingIntent =
                PendingIntent.getService(context, 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_calendar,
                        context.getText(R.string.remove_event), actionPendingIntent)
                        .build();

        // Add a notification with the same action on mobile and watch
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(talk.getTitle())
                .setContentText(information)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(action);

        /*
       // Add a notification with an action only visible on the watch
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(talk.getTitle())
                .setContentText(information)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
                .setDefaults(Notification.DEFAULT_ALL)
                .extend(new NotificationCompat.WearableExtender().addAction(action));
*/

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender(mBuilder.build());

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.ic_black);
        wearableExtender.setBackground(bitmap);

        wearableExtender.extend(mBuilder);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notificationId, mBuilder.build());
    }


    // send Favorite to the watch
    private void sendFavorite(String talkId, Long eventId) {

        // send the event
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.FAVORITE_PATH + "/" + talkId);

        // store the data
        DataMap dataMap = new DataMap();
        dataMap.putLong("eventId", eventId);

        // store the event in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMap(Constants.DETAIL_PATH, dataMap);

        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }

    }


}
