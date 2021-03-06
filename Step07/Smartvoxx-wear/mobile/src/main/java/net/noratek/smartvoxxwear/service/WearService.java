package net.noratek.smartvoxxwear.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.rest.model.Link;
import net.noratek.smartvoxxwear.rest.model.Schedules;
import net.noratek.smartvoxxwear.rest.model.Slot;
import net.noratek.smartvoxxwear.rest.model.SlotList;
import net.noratek.smartvoxxwear.rest.model.Speaker;
import net.noratek.smartvoxxwear.rest.model.Talk;
import net.noratek.smartvoxxwear.rest.service.DevoxxApi;
import net.noratek.smartvoxxwear.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
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
    private final String TALK_PATH = "/talk";
    private final String SPEAKERS_PATH = "/speakers";
    private final String SPEAKER_PATH = "/speaker";
    private final String LIST_PATH = "/list";
    private final String DETAIL_PATH = "/detail";
    private final String TWITTER_PATH = "/twitter";


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

        if (path.equalsIgnoreCase(TALK_PATH)) {
            retrieveTalk(data);
            return;
        }

        if (path.equalsIgnoreCase(SPEAKER_PATH)) {
            retrieveSpeaker(data);
            return;
        }

        if (path.equalsIgnoreCase(TWITTER_PATH)) {
            followOnTwitter(data);
            return;
        }

    }


    //
    // Twitter
    //

    // Open the Twitter application or the browser if the app is not installed
    private void followOnTwitter(String inputData) {
        String twitterName = inputData == null ? "" : inputData.trim().toLowerCase();
        twitterName = twitterName.replaceFirst("@", "");

        if (twitterName.isEmpty()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitterName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
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

        // send the schedules
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

        // send the slots
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
    }


    //
    // Talk
    //


    // Send the talk to the watch.
    public void retrieveTalk(String talkId) {

        // retrieve the talk from the server
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                final Talk talk = (Talk) o;

                if (talk == null) {
                    return;
                }

                sendTalk(talk);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("Main", retrofitError.getMessage());

            }
        };
        mMethods.getTalk(mConferenceName, talkId, callback);
    }


    /**
     * Send the talk to the watch.
     */
    private void sendTalk(Talk talk) {
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(TALK_PATH + "/" + talk.getTalkId());

        final DataMap talkDataMap = new DataMap();

        // process the data
        talkDataMap.putString("timestamp", new Date().toString());
        talkDataMap.putString("id", talk.getTalkId());
        talkDataMap.putLong("eventId", talk.getEventId());
        talkDataMap.putString("talkType", talk.getTalkType());
        talkDataMap.putString("track", talk.getTrack());
        talkDataMap.putString("trackId", talk.getTrackId());
        talkDataMap.putString("title", talk.getTitle());
        talkDataMap.putString("lang", talk.getLang());
        talkDataMap.putString("summary", talk.getSummary());

        ArrayList<DataMap> speakersDataMap = new ArrayList<>();

        // process each speaker's data
        if (talk.getSpeakers() != null) {
            for (int index = 0; index < talk.getSpeakers().size(); index++) {

                final DataMap speakerDataMap = new DataMap();

                final Speaker speaker = talk.getSpeakers().get(index);

                if (speaker.getLink() != null) {
                    // process the data
                    speakerDataMap.putString("uuid", Utils.getLastPartUrl(speaker.getLink().getHref()));
                    speakerDataMap.putString("title", speaker.getLink().getTitle());

                    speakersDataMap.add(speakerDataMap);
                }
            }
        }

        if (speakersDataMap.size() > 0) {
            talkDataMap.putDataMapArrayList(SPEAKERS_PATH, speakersDataMap);
        }

        // store the list in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMap(DETAIL_PATH, talkDataMap);

        // send the talk
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
    }


    //
    // Speaker
    //


    // Retrieve and Send the detail of a speaker.
    public void retrieveSpeaker(String speakerId) {

        // retrieve the speaker from the server
        Callback callback = new Callback() {
            @Override
            public void success(Object o, Response response) {
                final Speaker speaker = (Speaker) o;

                if (speaker == null) {
                    return;
                }

                sendSpeaker(speaker);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.d("Main", retrofitError.getMessage());

            }
        };
        mMethods.getSpeaker(mConferenceName, speakerId, callback);
    }


    // Send the speaker's detail to the watch.
    private void sendSpeaker(final Speaker speaker) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SPEAKER_PATH + "/" + speaker.getUuid());

        final DataMap speakerDataMap = new DataMap();

        // process the data
        speakerDataMap.putString("timestamp", new Date().toString());
        speakerDataMap.putString("uuid", speaker.getUuid());
        speakerDataMap.putString("firstName", speaker.getFirstName());
        speakerDataMap.putString("lastName", speaker.getLastName());
        speakerDataMap.putString("company", speaker.getCompany());
        speakerDataMap.putString("bio", speaker.getBio());
        speakerDataMap.putString("blog", speaker.getBlog());
        speakerDataMap.putString("twitter", speaker.getTwitter());
        speakerDataMap.putString("avatarURL", speaker.getAvatarURL());
        speakerDataMap.putString("avatarImage", speaker.getAvatarImage());

        // store the list in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMap(DETAIL_PATH, speakerDataMap);

        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }

        // do we have to retrieve the avatar's image?
        if (((speaker.getAvatarImage() != null) && (speaker.getAvatarImage().isEmpty() == false))) {
            return;
        }

        // retrieve and send speaker's image (if any)
        if (speaker.getAvatarURL() != null)  {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(getApplicationContext())
                            .load(speaker.getAvatarURL())
                            .resize(100, 100)
                            .centerCrop()
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                                    byte[] byteArray = byteArrayOutputStream.toByteArray();

                                    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                                    // update the data map with the avatar
                                    speakerDataMap.putString("avatarImage", encoded);


                                    // as the datamap has changed, a onDataChanged event will be fired on the remote node

                                    // store the list in the datamap to send it to the wear
                                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(SPEAKER_PATH + "/" + speaker.getUuid());
                                    putDataMapRequest.getDataMap().putDataMap(DETAIL_PATH, speakerDataMap);

                                    // send the speaker
                                    if (mApiClient.isConnected()) {
                                        Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
                                    }
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                    Log.d(TAG, errorDrawable.toString());

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                    Log.d(TAG, placeHolderDrawable.toString());
                                }
                            });
                }
            });

        }
    }




}
