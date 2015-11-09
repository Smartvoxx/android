package net.noratek.smartvoxxwear.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxx.common.model.Speaker;
import net.noratek.smartvoxx.common.model.Talk;
import net.noratek.smartvoxx.common.utils.Constants;
import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.adapter.TalkGridPageAdapter;
import net.noratek.smartvoxxwear.event.AddFavoriteEvent;
import net.noratek.smartvoxxwear.event.ConfirmationEvent;
import net.noratek.smartvoxxwear.event.FavoriteEvent;
import net.noratek.smartvoxxwear.event.FavoriteRemovedEvent;
import net.noratek.smartvoxxwear.event.GetSpeakerEvent;
import net.noratek.smartvoxxwear.event.GetTalkEvent;
import net.noratek.smartvoxxwear.event.GetTalkSummaryEvent;
import net.noratek.smartvoxxwear.event.RemoveFavoriteEvent;
import net.noratek.smartvoxxwear.event.SpeakerDetailEvent;
import net.noratek.smartvoxxwear.event.TalkEvent;
import net.noratek.smartvoxxwear.event.TalkSummaryEvent;
import net.noratek.smartvoxxwear.wrapper.SpeakerDetailWrapper;
import net.noratek.smartvoxxwear.wrapper.TalkWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 06/09/15.
 */
public class TalkActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private final static String TAG = TalkActivity.class.getCanonicalName();


    // Google Play Services
    private GoogleApiClient mApiClient;

    // Layout widgets and adapters
    private TalkGridPageAdapter mTalkGridPageAdapter;
    private GridViewPager mPager;
    private DotsPageIndicator mDotsPageIndicator;

    // local cache
    private Talk mTalk;
    private LinkedHashMap<String, Speaker> mSpeakers = new LinkedHashMap<>();

    // data retrieved from the slot.
    // This will be used to initialize the layout and to add additional information to the talk
    private String mTalkId;
    private String mTalkTitle;
    private String mRoomName;
    private Long mFromTimeMillis;
    private Long mToTimeMillis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTalkId = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTalkId = bundle.getString("talkId");
            mTalkTitle = bundle.getString("talkTitle");
            mRoomName = bundle.getString("roomName");
            mFromTimeMillis = bundle.getLong("fromTimeMillis");
            mToTimeMillis = bundle.getLong("toTimeMillis");
        }

        setContentView(R.layout.talk_activity);

        mPager = (GridViewPager) findViewById(R.id.pager);

        // we prepare the view with initial values gathered from the Slot
        mTalkGridPageAdapter = new TalkGridPageAdapter(this, getFragmentManager(), mTalkTitle);
        mPager.setAdapter(mTalkGridPageAdapter);

        mDotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        mDotsPageIndicator.setPager(mPager);
    }


    @Override
    protected void onResume() {
        super.onResume();


        if (mTalk != null) {
            //The activity can have been awakened up by a notification (remove favorite).
            //In this case, we ensure that the favorites status did not changed.
            getFavoriteFromCache(mTalk);
            return;
        }

        if (mTalkId == null) {
            return;
        }

        // Retrieve the talk
        getTalkFromCache(mTalkId);
    }


    @Override
    protected void onStart() {
        super.onStart();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        if (null != mApiClient && mApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mApiClient, this);
            mApiClient.disconnect();
        }

        mTalkGridPageAdapter.unRegister();

        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

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

    protected void sendMessage(final String path, final byte[] message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // broadcast the message to all connected devices
                final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message);

                }
            }
        }).start();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            // Check if we have received our speakers
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.TALK_PATH + "/" + mTalkId)) {

                TalkWrapper talkWrapper = new TalkWrapper();

                final Talk talk = talkWrapper.getTalk(event);

                if (talk == null) {
                    return;
                }

                mTalk = talk;

                // add additional information coming from the Slot
                mTalk.setRoomName(mRoomName);
                mTalk.setFromTimeMillis(mFromTimeMillis);
                mTalk.setToTimeMillis(mToTimeMillis);

                EventBus.getDefault().postLocal(new TalkEvent(mTalk));


                // retrieve detail of each speaker
                for (Speaker speaker : mTalk.getSpeakers()) {
                    mSpeakers.put(speaker.getUuid(), speaker);
                }

                // EventBus.getDefault().postLocal(new TalkSpeakersListEvent(mSpeakers));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTalkGridPageAdapter.addSpeakers(mSpeakers);
                        mTalkGridPageAdapter.notifyDataSetChanged();
                    }
                });

                return;
            }


            // Check if we have received some details for a speaker
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.SPEAKER_PATH)) {
                SpeakerDetailWrapper speakerDetailWrapper = new SpeakerDetailWrapper();

                Speaker speaker = speakerDetailWrapper.getSpeakerDetail(event);
                if (speaker == null) {
                    return;
                }

                if ((mTalk == null) || (mTalk.getSpeakers() == null)) {
                    return;
                }

                // check if the speaker event is related to the current talk
                String speakerUuid = null;
                for (Speaker speakerTalk : mTalk.getSpeakers()) {
                    if (speakerTalk.getUuid().equalsIgnoreCase(speaker.getUuid())) {
                        speakerUuid = speaker.getUuid();
                        break;
                    }
                }

                if (speakerUuid == null) {
                    // this speaker event is not related to the talk
                    return;
                }

                mSpeakers.put(speaker.getUuid(), speaker);

                EventBus.getDefault().postLocal(new SpeakerDetailEvent(speaker));

                return;
            }

            // Event received when a change occurred in the favorite
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.FAVORITE_PATH + "/" + mTalkId)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                if (dataMapItem == null) {
                    return;
                }

                DataMap dataMap = dataMapItem.getDataMap().getDataMap(Constants.DETAIL_PATH);
                if (dataMap == null) {
                    return;
                }

                mTalk.setEventId(dataMap.getLong("eventId"));
                EventBus.getDefault().postLocal(new FavoriteEvent(mTalk.getEventId()));

                return;
            }
        }

    }


    // Get Talk from the data item repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getTalkFromCache(final String talkId) {

        final String dataPath = Constants.TALK_PATH + "/" + talkId;

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                DataMap dataMap = null;
                                if (dataItems.getCount() > 0) {
                                    // retrieve the talk from the cache
                                    dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                }

                                if (dataMap == null) {
                                    // unable to fetch data -> retrieve the talk from the Mobile
                                    sendMessage(Constants.TALK_PATH, talkId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the talk from the cache
                                TalkWrapper talkWrapper = new TalkWrapper();

                                final Talk talk = talkWrapper.getTalk(dataMap);

                                mTalk = talk;

                                // add additional information coming from the Slot
                                mTalk.setRoomName(mRoomName);
                                mTalk.setFromTimeMillis(mFromTimeMillis);
                                mTalk.setToTimeMillis(mToTimeMillis);

                                EventBus.getDefault().postLocal(new TalkEvent(mTalk));


                                // retrieve detail of each speaker
                                for (Speaker speaker : mTalk.getSpeakers()) {
                                    mSpeakers.put(speaker.getUuid(), speaker);
                                }

                                dataItems.release();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTalkGridPageAdapter.addSpeakers(mSpeakers);
                                        mTalkGridPageAdapter.notifyDataSetChanged();

                                        // retrieve the status of the favorite
                                        getFavoriteFromCache(mTalk);
                                    }
                                });
                            }
                        }
                );
    }


    // Get favorite status of the talk from the data item repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getFavoriteFromCache(final Talk talk) {

        final String dataPath = Constants.FAVORITE_PATH + "/" + talk.getId();

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                DataMap dataMap = null;
                                if (dataItems.getCount() > 0) {
                                    // retrieve the favorite from the cache
                                    dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                }

                                // retrieve the favorite from the cache
                                if (dataMap == null) {
                                    // Prepare the data map
                                    DataMap favoriteDataMap = new DataMap();
                                    favoriteDataMap.putString("talkId", talk.getId());
                                    favoriteDataMap.putString("title", talk.getTitle());
                                    favoriteDataMap.putLong("fromTimeMillis", talk.getFromTimeMillis());
                                    favoriteDataMap.putLong("toTimeMillis", talk.getToTimeMillis());

                                    // unable to fetch data -> retrieve the favorite status from the Mobile
                                    sendMessage(Constants.FAVORITE_PATH, favoriteDataMap.toByteArray());
                                    dataItems.release();
                                    return;
                                }

                                DataMap favoriteMap = dataMap.getDataMap(Constants.DETAIL_PATH);
                                if (favoriteMap == null) {
                                    dataItems.release();
                                    return;
                                }

                                mTalk.setEventId(favoriteMap.getLong("eventId"));
                                EventBus.getDefault().postLocal(new FavoriteEvent(mTalk.getEventId()));

                                dataItems.release();
                            }
                        }
                );
    }


    // Get Speaker from the data items repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getSpeakerFromCache(final String speakerId) {

        final String dataPath = Constants.SPEAKER_PATH + "/" + speakerId;

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                DataMap dataMap = null;
                                if (dataItems.getCount() > 0) {
                                    // retrieve the speaker from the cache
                                    dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                }

                                if (dataMap == null) {
                                    // unable to fetch data -> refresh the list of slots from Mobile
                                    sendMessage(Constants.SPEAKER_PATH, speakerId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the speaker from the cache
                                SpeakerDetailWrapper speakerDetailWrapper = new SpeakerDetailWrapper();

                                final Speaker speaker = speakerDetailWrapper.getSpeakerDetail(dataMap);

                                mSpeakers.put(speaker.getUuid(), speaker);

                                EventBus.getDefault().postLocal(new SpeakerDetailEvent(speaker));

                                dataItems.release();
                            }
                        }
                );
    }


    public Map<String, Speaker> getSpeakers() {
        return mSpeakers;
    }


    public Talk getTalk() {
        return mTalk;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    //
    // Events
    //

    public void onEvent(ConfirmationEvent confirmationEvent) {

        // Retrieve the list of speakers from the mobile
        sendMessage(confirmationEvent.getPath(), confirmationEvent.getMessage());
    }


    public void onEvent(GetTalkEvent getTalkEvent) {
        EventBus.getDefault().postLocal(new TalkEvent(mTalk));
    }


    public void onEvent(GetTalkSummaryEvent getTalkSummaryEvent) {

        if (mTalk == null) {
            return;
        }

        EventBus.getDefault().postLocal(new TalkSummaryEvent(mTalk.getTitle(), mTalk.getSummary(), mTalk.getTalkType()));
    }

    public void onEvent(GetSpeakerEvent getSpeakerEvent) {

        if (getSpeakerEvent == null) {
            return;
        }

        getSpeakerFromCache(getSpeakerEvent.getUuid());
    }

    public void onEvent(FavoriteRemovedEvent favoriteRemovedEvent) {
        mTalk.setEventId(0L);
        EventBus.getDefault().postLocal(new TalkEvent(mTalk));
    }

    public void onEvent(AddFavoriteEvent addFavoritesEvent) {

        if (addFavoritesEvent == null) {
            return;
        }

        if (addFavoritesEvent.getTalk() == null) {
            return;
        }

        Talk talk = addFavoritesEvent.getTalk();
        DataMap dataMap = new DataMap();
        dataMap.putString("talkId", talk.getId());
        dataMap.putString("title", talk.getTitle());
        dataMap.putString("summary", talk.getSummary());
        dataMap.putString("roomName", talk.getRoomName());
        dataMap.putLong("fromTimeMillis", talk.getFromTimeMillis());
        dataMap.putLong("toTimeMillis", talk.getToTimeMillis());

        sendMessage(Constants.ADD_FAVORITE_PATH, dataMap.toByteArray());

    }

    public void onEvent(RemoveFavoriteEvent removeFavoritesEvent) {

        if (removeFavoritesEvent == null) {
            return;
        }

        if (removeFavoritesEvent.getTalkId() == null) {
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putString("talkId", removeFavoritesEvent.getTalkId());
        dataMap.putLong("eventId", removeFavoritesEvent.getEventId());

        sendMessage(Constants.REMOVE_FAVORITE_PATH, dataMap.toByteArray());

    }
}
