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
import net.noratek.smartvoxxwear.rest.model.Slot;
import net.noratek.smartvoxxwear.rest.model.SlotList;
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
    private final String SLOTS_PATH = "/slots";
    private final String LIST_PATH = "/list";


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

        if (path.equalsIgnoreCase(SLOTS_PATH)) {
            retrieveSlots(data);
            return;
        }
    }

    //
    // Schedules
    //

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
        putDataMapRequest.getDataMap().putDataMapArrayList(LIST_PATH, schedulesDataMap);

        // send the list
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
    }


    //
    // Slots
    //


    // Retrieve and Send the slots for a specific schedule.
    private void retrieveSlots(final String day) {

        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                final SlotList slotList = (SlotList) o;

                if (slotList == null) {
                    return;
                }

                sendSLots(slotList.getSlots(), day);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("Main", retrofitError.getMessage());

            }
        };
        mMethods.getSchedule(mConferenceName, day, callback);
    }


    // Send the schedule's slots to the watch.
    private void sendSLots(List<Slot> slotList, String day) {

        if (slotList == null) {
            return;
        }

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SLOTS_PATH + "/" + day);

        ArrayList<DataMap> slotsDataMap = new ArrayList<>();

        for (int index = 0; index < slotList.size(); index++) {

            final DataMap scheduleDataMap = new DataMap();

            final Slot slot = slotList.get(index);

            // process the data
            scheduleDataMap.putString("roomName", slot.getRoomName());
            scheduleDataMap.putLong("fromTimeMillis", slot.getFromTimeMillis());
            scheduleDataMap.putLong("toTimeMillis", slot.getToTimeMillis());

            if (slot.getBreakSession() != null) {
                DataMap breakDataMap = new DataMap();

                //breakDataMap.putString("id", slot.getBreak().getId());
                breakDataMap.putString("nameEN", slot.getBreakSession().getNameEN());
                breakDataMap.putString("nameFR", slot.getBreakSession().getNameFR());

                scheduleDataMap.putDataMap("break", breakDataMap);
            }


            if (slot.getTalk() != null) {
                DataMap talkDataMap = new DataMap();

                talkDataMap.putString("id", slot.getTalk().getTalkId());
                talkDataMap.putLong("eventId", slot.getTalk().getEventId() == null ? 0L : slot.getTalk().getEventId());
                talkDataMap.putString("trackId", slot.getTalk().getTrackId());
                talkDataMap.putString("title", slot.getTalk().getTitle());
                talkDataMap.putString("lang", slot.getTalk().getLang());

                scheduleDataMap.putDataMap("talk", talkDataMap);
            }

            slotsDataMap.add(scheduleDataMap);
        }

        // store the list in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMapArrayList(LIST_PATH, slotsDataMap);

        // send the list
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
    }





}
