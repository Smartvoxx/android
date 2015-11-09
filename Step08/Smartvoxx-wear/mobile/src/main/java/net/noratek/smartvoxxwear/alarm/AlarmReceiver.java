package net.noratek.smartvoxxwear.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxxwear.helper.CalendarHelper;
import net.noratek.smartvoxxwear.rest.model.Talk;
import net.noratek.smartvoxxwear.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

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

        Log.d(TAG, "Received alarm for: " + information);
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
