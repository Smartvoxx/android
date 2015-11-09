package net.noratek.smartvoxxwear.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxx.common.model.Slot;
import net.noratek.smartvoxx.common.utils.Constants;
import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.wrapper.SlotsWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by eloudsa on 01/11/15.
 */
public class SlotActivity extends Activity implements WearableListView.ClickListener, GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {

    private final static String TAG = SlotActivity.class.getCanonicalName();

    // Data path
    private String mDataPath = Constants.SLOTS_PATH;

    // Google Play Services
    private GoogleApiClient mApiClient;

    // Layout widgets and adapters
    private TextView mSlotTitleView;
    private WearableListView mListView;
    private ListViewAdapter mListViewAdapter;

    // Avoid double tap
    private Boolean mClicked = false;

    // Schedule's day
    private String mDayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Retrieve the schedule's link
        mDayOfWeek = "monday";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mDayOfWeek = bundle.getString("dayOfWeek");
        }

        // Compose the data path
        mDataPath = Constants.SLOTS_PATH + "/" + mDayOfWeek;


        setContentView(R.layout.slot_activity);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                mSlotTitleView = (TextView) findViewById(R.id.title);
                mSlotTitleView.setText(mDayOfWeek);

                // Get the list component from the layout of the activity
                mListView = (WearableListView) findViewById(R.id.wearable_list);

                // Assign an adapter to the list
                mListViewAdapter = new ListViewAdapter(SlotActivity.this, new ArrayList<Slot>());
                mListView.setAdapter(mListViewAdapter);

                // Set a click listener
                mListView.setClickListener(SlotActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mClicked = false;

        // Retrieve and display the list of slots for the selected day
        getSlotsFromCache(mDataPath);
    }



    @Override
    protected void onStart() {
        super.onStart();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (null != mApiClient && mApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mApiClient, this);
            mApiClient.disconnect();
        }

        super.onStop();
    }


    private void sendMessage(final String path, final String message) {
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
    public void onClick(WearableListView.ViewHolder viewHolder) {

        if (mClicked) {
            return;
        }

        Slot slot = (Slot) viewHolder.itemView.getTag();
        if (slot == null) {
            return;
        }

        if ((slot.getTalk() == null) || (slot.getTalk().getId() == null)) {
            return;
        }

        mClicked = true;


        // display detail for this slot
        Intent slotIntent = new Intent(SlotActivity.this, TalkActivity.class);

        Bundle b = new Bundle();
        b.putString("talkId", slot.getTalk().getId());
        b.putString("talkTitle", slot.getTalk().getTitle());
        b.putString("roomName", slot.getRoomName());
        b.putLong("fromTimeMillis", slot.getFromTimeMillis());
        b.putLong("toTimeMillis", slot.getToTimeMillis());
        slotIntent.putExtras(b);

        SlotActivity.this.startActivity(slotIntent);

    }


    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            // Check if we have received our slot
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(mDataPath)) {

                SlotsWrapper slotsWrapper = new SlotsWrapper();

                final List<Slot> slotList = slotsWrapper.getSlotsList(event);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);

                        // set the schedule's date
                        mSlotTitleView.setText(mDayOfWeek);

                        mListViewAdapter.refresh(slotList);
                    }
                });

                return;
            }

            // Event received when a change occurred in a favorite
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.FAVORITE_PATH)) {

                // favorite has changed -> refresh the list
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressBar).setVisibility(View.GONE);

                        // set the schedule's date
                        mSlotTitleView.setText(mDayOfWeek);

                        mListViewAdapter.refresh();
                    }
                });

                return;
            }

        }
    }


    // Get Slots from the data items repository (cache).
    // If not available, we get the data from the Mobile device.
    //
    private void getSlotsFromCache(final String pathToContent) {
        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(pathToContent)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                if (dataItems.getCount() == 0) {
                                    // refresh the list of slots from Mobile
                                    sendMessage(Constants.SLOTS_PATH, mDayOfWeek);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve the slots from the cache
                                DataMap dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                if (dataMap == null) {
                                    // unable to fetch data -> refresh the list of slots from Mobile
                                    sendMessage(Constants.SLOTS_PATH, mDayOfWeek);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the slots from the cache
                                SlotsWrapper slotsWrapper = new SlotsWrapper();

                                final List<Slot> slotList = slotsWrapper.getSlotsList(dataMap);

                                dataItems.release();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // hide the progress bar
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);

                                        mListViewAdapter.refresh(slotList);
                                    }
                                });
                            }
                        }
                );
    }


    // Inner class providing the WearableListview's adapter
    public class ListViewAdapter extends WearableListView.Adapter {
        private List<Slot> mDataset;
        private final Context mContext;

        // Provide a suitable constructor (depends on the kind of dataset)
        public ListViewAdapter(Context context, List<Slot> slotList) {
            mContext = context;
            this.mDataset = slotList;
        }

        // Provide a reference to the type of views you're using
        public class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView mDescriptionView;
            private TextView mRoomNameView;
            private TextView mTalkTimeView;
            private LinearLayout mTrackColorLinearLayout;
            private ImageView mFavoriteImage;


            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                mDescriptionView = (TextView) itemView.findViewById(R.id.description);
                mRoomNameView = (TextView) itemView.findViewById(R.id.roomName);
                mTalkTimeView = (TextView) itemView.findViewById(R.id.talkTime);
                mTrackColorLinearLayout = (LinearLayout) itemView.findViewById(R.id.trackColor);
                mFavoriteImage = (ImageView) itemView.findViewById(R.id.favorite);
            }
        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
            // Inflate our custom layout for list items
            return new ItemViewHolder(new SettingsItemView(mContext));
        }

        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder,
                                     int position) {

            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            TextView descriptionView = itemHolder.mDescriptionView;
            TextView roomNameView = itemHolder.mRoomNameView;
            TextView talkTimeView = itemHolder.mTalkTimeView;
            LinearLayout trackColorLinearLayout = itemHolder.mTrackColorLinearLayout;
            final ImageView favoriteImage = itemHolder.mFavoriteImage;

            Slot slot = mDataset.get(position);

            String slotName = "Unknown talk";

            if (slot.getBreakSession() != null) {
                slotName = slot.getBreakSession().getNameEN();
            } else if (slot.getTalk() != null) {
                slotName = slot.getTalk().getTitle();
                trackColorLinearLayout.setBackground(getDrawable(getTrackColor(slot.getTalk().getTrackId())));

                /*
                if ((slot.getTalk().getEventId() != null) && (slot.getTalk().getEventId() > 0L)) {
                    favoriteImage.setVisibility(View.VISIBLE);
                } else {
                    favoriteImage.setVisibility(View.GONE);
                }
                */

                favoriteImage.setVisibility(View.GONE);

                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(Constants.FAVORITE_PATH + "/" + slot.getTalk().getId())
                        .build();

                Wearable.DataApi.getDataItems(mApiClient, uri)
                        .setResultCallback(
                                new ResultCallback<DataItemBuffer>() {
                                    @Override
                                    public void onResult(DataItemBuffer dataItems) {

                                        if (dataItems.getCount() == 0) {
                                            dataItems.release();
                                            return;
                                        }

                                        DataMap dataMap = DataMap.fromByteArray(dataItems.get(0).getData());

                                        DataMap favoriteMap = dataMap.getDataMap(Constants.DETAIL_PATH);
                                        if (favoriteMap == null) {
                                            dataItems.release();
                                            return;
                                        }

                                        Long eventId = favoriteMap.getLong("eventId");
                                        if (eventId > 0L) {
                                            favoriteImage.setVisibility(View.VISIBLE);
                                        }

                                        dataItems.release();
                                    }
                                }
                        );
            }

            // display information
            descriptionView.setText(slotName);
            roomNameView.setText(slot.getRoomName());

            String talkTime = new SimpleDateFormat("HH:mm").format(new Date(slot.getFromTimeMillis()));
            talkTime += "-" + new SimpleDateFormat("HH:mm").format(new Date(slot.getToTimeMillis()));
            talkTimeView.setText(talkTime);

            // replace list item's metadata
            holder.itemView.setTag(slot);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            if (mDataset == null) {
                return 0;
            }
            return mDataset.size();
        }

        // refresh the list with a new dataset
        public void refresh(List<Slot> slotList) {
            mDataset = slotList;

            refresh();
        }

        // Refresh without impact the dataset
        public void refresh() {
            // refresh the listview
            notifyDataSetChanged();
        }


        // Static nested class used to animate the listview's item
        public final class SettingsItemView extends FrameLayout implements WearableListView.OnCenterProximityListener {
            private TextView roomName;
            private TextView talkTime;
            private TextView description;
            private LinearLayout trackColor;
            private ImageView favoriteImage;

            public SettingsItemView(Context context) {
                super(context);
                View.inflate(context, R.layout.slot_row_activity, this);

                description = (TextView) findViewById(R.id.description);
                roomName = (TextView) findViewById(R.id.roomName);
                talkTime = (TextView) findViewById(R.id.talkTime);
                trackColor = (LinearLayout) findViewById(R.id.trackColor);
                favoriteImage = (ImageView) findViewById(R.id.favorite);
            }

            @Override
            public void onCenterPosition(boolean b) {
                description.animate().scaleX(1f).scaleY(1f).alpha(1);
                roomName.animate().scaleX(1f).scaleY(1f).alpha(1);
                talkTime.animate().scaleX(1f).scaleY(1f).alpha(1);
                trackColor.animate().scaleX(1f).scaleY(1f).alpha(1);
                favoriteImage.animate().scaleX(1f).scaleY(1f).alpha(1);

            }

            @Override
            public void onNonCenterPosition(boolean b) {
                description.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.4f);
                roomName.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.4f);
                talkTime.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.4f);
                trackColor.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.4f);
                favoriteImage.animate().scaleX(0.7f).scaleY(0.7f).alpha(0.4f);
            }
        }



        // we display a color related to the type of track
        private int getTrackColor(String trackId) {

            if (trackId == null) {
                return R.color.none_color;
            }

            if (trackId.equalsIgnoreCase("startup")) {
                return R.color.startup_color;
            }

            if (trackId.equalsIgnoreCase("ssj")) {
                return R.color.ssj_color;
            }

            if (trackId.equalsIgnoreCase("java")) {
                return R.color.java_color;
            }

            if (trackId.equalsIgnoreCase("mobile")) {
                return R.color.mobile_color;
            }

            if (trackId.equalsIgnoreCase("archisec")) {
                return R.color.archisec_color;
            }

            if (trackId.equalsIgnoreCase("methodevops")) {
                return R.color.methodevops_color;
            }

            if (trackId.equalsIgnoreCase("future")) {
                return R.color.future_color;
            }

            if (trackId.equalsIgnoreCase("lang")) {
                return R.color.lang_color;
            }

            if (trackId.equalsIgnoreCase("cloud")) {
                return R.color.cloud_color;
            }

            if (trackId.equalsIgnoreCase("web")) {
                return R.color.web_color;
            }

            return R.color.none_color;

        }

    }
}
