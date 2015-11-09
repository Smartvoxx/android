package net.noratek.smartvoxxwear.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import net.noratek.smartvoxxwear.R;

public class ScheduleActivity extends Activity {

    private final static String TAG = ScheduleActivity.class.getCanonicalName();

    // Messages path
    private final String SCHEDULES_PATH = "/schedules";


    // Google Play Services
    private GoogleApiClient mApiClient;

    // Layout widgets
    private TextView mTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_activity);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);

                stub.findViewById(R.id.getSchedules).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage(SCHEDULES_PATH, "dummy");
                    }
                });
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mApiClient.connect();
    }


    @Override
    protected void onStop() {

        if ((mApiClient != null) && (mApiClient.isConnected())) {
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

}
