package net.noratek.smartvoxxwear.activity;

import android.app.Activity;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.adapter.TalkGridPageAdapter;
import net.noratek.smartvoxxwear.event.AddFavoriteEvent;
import net.noratek.smartvoxxwear.event.ConfirmationEvent;
import net.noratek.smartvoxxwear.event.FavoriteRemovedEvent;
import net.noratek.smartvoxxwear.event.GetSpeakerEvent;
import net.noratek.smartvoxxwear.event.GetTalkEvent;
import net.noratek.smartvoxxwear.event.GetTalkSummaryEvent;
import net.noratek.smartvoxxwear.event.RemoveFavoriteEvent;
import net.noratek.smartvoxxwear.event.ScrollToPageEvent;
import net.noratek.smartvoxxwear.event.SpeakerDetailEvent;
import net.noratek.smartvoxxwear.event.TalkEvent;
import net.noratek.smartvoxxwear.event.TalkSummaryEvent;
import net.noratek.smartvoxxwear.model.Speaker;
import net.noratek.smartvoxxwear.model.Talk;
import net.noratek.smartvoxxwear.wrapper.SpeakerDetailWrapper;
import net.noratek.smartvoxxwear.wrapper.TalkWrapper;

import java.util.LinkedHashMap;
import java.util.Map;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 06/09/15.
 */
public class TalkActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener, DataApi.DataListener {

    private final static String TAG = TalkActivity.class.getCanonicalName();


    // Data path
    private final String TALK_PATH = "/talk";
    private final String SPEAKER_PATH = "/speaker";


    protected GoogleApiClient mApiClient;

    private TalkGridPageAdapter talkGridPageAdapter;
    private GridViewPager pager;

    // local cache
    private Talk mTalk;
    private LinkedHashMap<String, Speaker> mSpeakers = new LinkedHashMap<>();

    private String talkId;
    private String talkTitle;
    private String roomName;
    private Long fromTimeMillis;
    private Long toTimeMillis;

    DotsPageIndicator dotsPageIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //EventBus.getDefault().register(this);

        talkId = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            talkId = bundle.getString("talkId");
            talkTitle = bundle.getString("talkTitle");
            roomName = bundle.getString("roomName");
            fromTimeMillis = bundle.getLong("fromTimeMillis");
            toTimeMillis = bundle.getLong("toTimeMillis");
        }

        //EventBus.getDefault().register(this);

        setContentView(R.layout.talk_activity);

        pager = (GridViewPager) findViewById(R.id.pager);

        talkGridPageAdapter = new TalkGridPageAdapter(this, getFragmentManager(), talkTitle);
        pager.setAdapter(talkGridPageAdapter);

        dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        // TODO: not working
        dotsPageIndicator.setDotColorSelected(R.color.orange);
    }


    @Override
    protected void onResume() {
        super.onResume();


        if (mTalk != null) {
            return;
        }

        if (talkId == null) {
            return;
        }

        // Retrieve the talk
        getTalkFromCache(talkId);
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

        talkGridPageAdapter.unRegister();

        super.onStop();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
        Wearable.MessageApi.addListener(mApiClient, this);
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
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(TALK_PATH + "/" + talkId)) {

                TalkWrapper talkWrapper = new TalkWrapper();

                final Talk talk = talkWrapper.getTalk(event);

                if (talk == null) {
                    return;
                }

                mTalk = talk;

                // add additional information coming from the Slot
                mTalk.setRoomName(roomName);
                mTalk.setFromTimeMillis(fromTimeMillis);
                mTalk.setToTimeMillis(toTimeMillis);

                EventBus.getDefault().postLocal(new TalkEvent(mTalk));


                // retrieve detail of each speaker
                for (Speaker speaker : mTalk.getSpeakers()) {
                    mSpeakers.put(speaker.getUuid(), speaker);
                }

                // EventBus.getDefault().postLocal(new TalkSpeakersListEvent(mSpeakers));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        talkGridPageAdapter.addSpeakers(mSpeakers);
                        talkGridPageAdapter.notifyDataSetChanged();
                    }
                });

                return;
            }


            // Check if we have received some details for a speaker
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(SPEAKER_PATH)) {
                SpeakerDetailWrapper speakerDetailWrapper = new SpeakerDetailWrapper();

                Speaker speaker = speakerDetailWrapper.getSpeakerDetail(event);
                if (speaker == null) {
                    return;
                }

                mSpeakers.put(speaker.getUuid(), speaker);

                EventBus.getDefault().postLocal(new SpeakerDetailEvent(speaker));

                return;
            }


        }

    }


    // Get Talk from the data item repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getTalkFromCache(final String talkId) {

        final String dataPath = TALK_PATH + "/" + talkId;

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                if (dataItems.getCount() == 0) {
                                    // refresh the list of slots from Mobile
                                    sendMessage(TALK_PATH, talkId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve the slots from the cache
                                DataMap dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                if (dataMap == null) {
                                    // unable to fetch data -> refresh the list of slots from Mobile
                                    sendMessage(TALK_PATH, talkId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the talk from the cache
                                TalkWrapper talkWrapper = new TalkWrapper();

                                final Talk talk = talkWrapper.getTalk(dataMap);

                                mTalk = talk;

                                // add additional information coming from the Slot
                                mTalk.setRoomName(roomName);
                                mTalk.setFromTimeMillis(fromTimeMillis);
                                mTalk.setToTimeMillis(toTimeMillis);

                                EventBus.getDefault().postLocal(new TalkEvent(mTalk));


                                // retrieve detail of each speaker
                                for (Speaker speaker : mTalk.getSpeakers()) {
                                    mSpeakers.put(speaker.getUuid(), speaker);
                                }

                                dataItems.release();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        talkGridPageAdapter.addSpeakers(mSpeakers);
                                        talkGridPageAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                );
    }


    // Get Speaker from the data items repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getSpeakerFromCache(final String speakerId) {

        final String dataPath = SPEAKER_PATH + "/" + speakerId;

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                if (dataItems.getCount() == 0) {
                                    // refresh the list of slots from Mobile
                                    sendMessage(SPEAKER_PATH, speakerId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve the slots from the cache
                                DataMap dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                if (dataMap == null) {
                                    // unable to fetch data -> refresh the list of slots from Mobile
                                    sendMessage(SPEAKER_PATH, speakerId);
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
    public void onMessageReceived(MessageEvent messageEvent) {
        final String path = messageEvent.getPath();
        final String message = new String(messageEvent.getData());

        if (path.equals("mobileInfo")) {
            Log.d(TAG, "[WEAR] Message for path [" + path + "]: " + message);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onEvent(ScrollToPageEvent scrollToPageEvent) {

        // Retrieve the page's position
        final Point position = talkGridPageAdapter.getPosition(scrollToPageEvent.getPageName());
        if (position == null) {
            return;
        }

        // Use to move to a specific page
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                pager.setCurrentItem(position.x, position.y, true);
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 100);
    }

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

        EventBus.getDefault().postLocal(new TalkSummaryEvent(mTalk.getTitle(), mTalk.getSummary()));
    }


    public void onEvent(FavoriteRemovedEvent favoriteRemovedEvent) {
        mTalk.setEventId(0L);
        EventBus.getDefault().postLocal(new TalkEvent(mTalk));
    }

    public void onEvent(GetSpeakerEvent getSpeakerEvent) {

        if (getSpeakerEvent == null) {
            return;
        }

        getSpeakerFromCache(getSpeakerEvent.getUuid());
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

        sendMessage("addEvent", dataMap.toByteArray());

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

        sendMessage("removeEvent", dataMap.toByteArray());

    }
}
