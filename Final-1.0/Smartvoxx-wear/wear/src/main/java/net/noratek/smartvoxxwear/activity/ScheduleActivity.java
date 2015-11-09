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

import net.noratek.smartvoxx.common.model.Schedule;
import net.noratek.smartvoxx.common.utils.Constants;
import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.wrapper.SchedulesListWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleActivity extends Activity implements WearableListView.ClickListener, GoogleApiClient.ConnectionCallbacks, DataApi.DataListener {

    private final static String TAG = ScheduleActivity.class.getCanonicalName();

    // Google Play Services
    private GoogleApiClient mApiClient;

    // Layout widgets and adapters
    private WearableListView mListView;
    private ListViewAdapter mListViewAdapter;

    // Avoid double tap
    private Boolean mClicked = false;

    //create a counter to count the number of instances of this activity
    public static AtomicInteger mActivitiesLaunched = new AtomicInteger(0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // if launching will create more than one instance of this activity, bail out
        if (mActivitiesLaunched.incrementAndGet() > 1) {
            finish();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                // Listview component
                mListView = (WearableListView) findViewById(R.id.wearable_list);

                // Assign the adapter
                mListViewAdapter = new ListViewAdapter(ScheduleActivity.this, new ArrayList<Schedule>());
                mListView.setAdapter(mListViewAdapter);

                // Set the click listener
                mListView.setClickListener(ScheduleActivity.this);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        mClicked = false;

        // Retrieve and display the list of schedules
        getSchedulesFromCache(Constants.SCHEDULES_PATH);
    }

    @Override
    protected void onDestroy() {
        //remove this activity from the counter
        mActivitiesLaunched.getAndDecrement();

        super.onDestroy();
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
        if ((mApiClient != null) && (mApiClient.isConnected())) {
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
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {

            // Check if we have received our schedules
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.SCHEDULES_PATH)) {

                SchedulesListWrapper schedulesListWrapper = new SchedulesListWrapper();

                final List<Schedule> schedulesList = schedulesListWrapper.getSchedulesList(event);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // hide the progress bar
                        findViewById(R.id.progressBar).setVisibility(View.GONE);

                        mListViewAdapter.refresh(schedulesList);
                    }
                });

                return;
            }
        }

    }


    // Get Schedules from the data items repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getSchedulesFromCache(final String pathToContent) {
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
                                    // refresh the list of schedules from Mobile
                                    sendMessage(Constants.SCHEDULES_PATH, "get list of schedules");
                                    dataItems.release();
                                    return;
                                }

                                // retrieve the schedule from the cache
                                DataMap dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                if (dataMap == null) {
                                    // unable to fetch data -> refresh the list of schedules from Mobile
                                    sendMessage(Constants.SCHEDULES_PATH, "get list of schedules");
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the schedule from the cache
                                SchedulesListWrapper schedulesListWrapper = new SchedulesListWrapper();

                                final List<Schedule> schedulesList = schedulesListWrapper.getSchedulesList(dataMap);

                                dataItems.release();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // hide the progress bar
                                        findViewById(R.id.progressBar).setVisibility(View.GONE);

                                        mListViewAdapter.refresh(schedulesList);
                                    }
                                });
                            }
                        }
                );
    }


    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {

        // Avoid double tap
        if (mClicked) {
            return;
        }

        Schedule schedule = (Schedule) viewHolder.itemView.getTag();
        if (schedule == null) {
            return;
        }

        mClicked = true;

        // display slots for this schedule
        Intent scheduleIntent = new Intent(ScheduleActivity.this, SlotActivity.class);

        Bundle b = new Bundle();
        b.putString("dayOfWeek", schedule.getDay());
        scheduleIntent.putExtras(b);

        ScheduleActivity.this.startActivity(scheduleIntent);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }


    // Inner class providing the WearableListview's adapter
    public class ListViewAdapter extends WearableListView.Adapter {
        private List<Schedule> mDataset;
        private final Context mContext;

        // Provide a suitable constructor (depends on the kind of dataset)
        public ListViewAdapter(Context context, List<Schedule> schedulesList) {
            mContext = context;
            this.mDataset = schedulesList;
        }

        // Provide a reference to the type of views we're using
        public class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView textView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                textView = (TextView) itemView.findViewById(R.id.description);
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

            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            TextView view = itemHolder.textView;

            // retrieve, transform and display the schedule's day
            Schedule schedule = mDataset.get(position);
            String scheduleDay = schedule.getTitle().replace(",", "\n");
            view.setText(scheduleDay);

            // replace list item's metadata
            holder.itemView.setTag(schedule);
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

        public void refresh(List<Schedule> schedulesList) {
            mDataset = schedulesList;

            // reload the listview
            notifyDataSetChanged();
        }


        // Static nested class used to animate the listview's item
        public final class SettingsItemView extends FrameLayout implements WearableListView.OnCenterProximityListener {

            private TextView description;

            public SettingsItemView(Context context) {
                super(context);
                View.inflate(context, R.layout.schedule_row_activity, this);

                description = (TextView) findViewById(R.id.description);
            }

            @Override
            public void onCenterPosition(boolean b) {
                description.animate().scaleX(1f).scaleY(1f).alpha(1);
            }

            @Override
            public void onNonCenterPosition(boolean b) {
                description.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
            }
        }

    }


}
