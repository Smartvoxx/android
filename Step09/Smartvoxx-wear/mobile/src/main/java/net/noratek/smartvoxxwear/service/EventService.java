package net.noratek.smartvoxxwear.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxx.common.utils.Constants;
import net.noratek.smartvoxxwear.calendar.CalendarHelper;

/**
 * Created by eloudsa on 29/09/15.
 */
public class EventService extends Service {

    private final static String TAG = EventService.class.getCanonicalName();

    private GoogleApiClient mApiClient;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String action = bundle.getString("action");
            if (action == null) {
                return START_NOT_STICKY;
            }

            if (action.equalsIgnoreCase("remove_event")) {
                Long eventId = bundle.getLong("eventid");
                final String talkId = bundle.getString("talkid");
                Integer notificationId = bundle.getInt("notificationid");

                // remove the event from the calendar
                CalendarHelper calendarHelper = new CalendarHelper(this);
                calendarHelper.removeEvent(eventId);

                 // cancel the notification
                NotificationManagerCompat manager = NotificationManagerCompat.from(this);
                manager.cancel(notificationId);


                // event not more defined on the calendar -> inform the watch
                mApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addApi(Wearable.API)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.d(TAG, "Event removed. Informing remote devices.");

                                // Inform remote devices that the favorite has been removed
                                sendFavorite(talkId, 0L);

                                // wake-up remote application
                                sendMessage(Constants.WAKEUP_PATH, "");
                            }
                            @Override
                            public void onConnectionSuspended(int cause) {

                            }
                        }).build();
                mApiClient.connect();


            }
        }
        return START_NOT_STICKY;
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


    // send a message to the watch
    protected void sendMessage(final String path, final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // broadcast the message to all connected devices
                final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message.getBytes()).await();

                }
            }
        }).start();
    }



    @Override
    public void onDestroy() {
        mApiClient.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
