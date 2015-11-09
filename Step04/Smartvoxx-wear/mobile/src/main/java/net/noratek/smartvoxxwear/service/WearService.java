package net.noratek.smartvoxxwear.service;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.rest.model.Link;
import net.noratek.smartvoxxwear.rest.model.Schedules;
import net.noratek.smartvoxxwear.rest.service.DevoxxApi;
import net.noratek.smartvoxxwear.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by eloudsa on 28/10/15.
 */
public class WearService extends WearableListenerService {

    private final static String TAG = WearService.class.getCanonicalName();

    // Messages path
    private final String SCHEDULES_PATH = "/schedules";

    // Play services
    private GoogleApiClient mApiClient;

    // Rest
    private RestAdapter mRestAdapter;
    private DevoxxApi mMethods;
    private String mConferenceName;


    @Override
    public void onCreate() {
        super.onCreate();

        // Connect to Play Services
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();

        // prepare the REST build
        mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(getResources().getString(R.string.devoxx_rest_api))
                .build();

        mMethods = mRestAdapter.create(DevoxxApi.class);
        mConferenceName = getResources().getString(R.string.devoxx_conference);

    }


    @Override
    public void onDestroy() {
        if ((mApiClient != null) && (mApiClient.isConnected())) {
            mApiClient.disconnect();
        }

        super.onDestroy();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        // Processing the incoming message
        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());

        if (path.equalsIgnoreCase(SCHEDULES_PATH)) {
            retrieveSchedules();
            return;
        }
    }


    // Retrieve schedules from Devoxx
    private void retrieveSchedules() {
        // retrieve the schedules list from the server
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                // retrieve schedule from REST
                Schedules scheduleList = (Schedules) o;
                if (scheduleList == null) {
                    Log.d(TAG, "No schedules!");
                    return;
                }

                sendSchedules(scheduleList.getLinks());
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d(TAG, retrofitError.getMessage());
            }
        };
        mMethods.getSchedules(mConferenceName, callback);
    }


    // send Schedules to the watch
    private void sendSchedules(List<Link> schedules) {
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SCHEDULES_PATH);

        ArrayList<DataMap> schedulesDataMap = new ArrayList<>();

        // process each schedule
        for (Link schedule : schedules) {

            final DataMap scheduleDataMap = new DataMap();

            // process and push schedule's data
            scheduleDataMap.putString("day", Utils.getLastPartUrl(schedule.getHref()));
            scheduleDataMap.putString("title", schedule.getTitle());

            schedulesDataMap.add(scheduleDataMap);
        }

        // store the list in the datamap to send it to the watch
        putDataMapRequest.getDataMap().putDataMapArrayList("/list", schedulesDataMap);

        // send the list
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
    }


}
