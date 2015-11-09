package net.noratek.smartvoxxwear.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.noratek.smartvoxx.common.model.Talk;
import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.adapter.SimplePage;
import net.noratek.smartvoxxwear.event.AddFavoriteEvent;
import net.noratek.smartvoxxwear.event.FavoriteEvent;
import net.noratek.smartvoxxwear.event.GetTalkEvent;
import net.noratek.smartvoxxwear.event.RemoveFavoriteEvent;
import net.noratek.smartvoxxwear.event.TalkEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 24/08/15.
 */
public class TalkFragment extends Fragment {

    private final static String TAG = TalkFragment.class.getCanonicalName();


    private View mMainView;

    private Talk mTalk;

    private SimplePage mPageSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        mPageSettings = (SimplePage) (getArguments() != null ? getArguments().getSerializable("pageInfo") : null);


        mMainView = inflater.inflate(R.layout.talk_fragment, container, false);


        WatchViewStub stub = (WatchViewStub) mMainView.findViewById(R.id.watch_talk_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // Set the title if any
                if (mPageSettings != null) {
                    String talklTitle = mPageSettings.getTitle() != null ? mPageSettings.getTitle() : "";
                    ((TextView) mMainView.findViewById(R.id.title)).setText(talklTitle);
                }

                // add event listener
                mMainView.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTalk == null) {
                            return;
                        }

                        String confirmationMessage = "";
                        if ((mTalk.getEventId() != null) && (mTalk.getEventId() > 0)) {
                            // remove from my favorites
                            confirmationMessage = getString(R.string.remove_favorites);
                            EventBus.getDefault().postLocal(new RemoveFavoriteEvent(mTalk.getId(), mTalk.getEventId()));
                        } else {
                            // add to my favorites
                            confirmationMessage = getString(R.string.add_favorites);
                            EventBus.getDefault().postLocal(new AddFavoriteEvent(mTalk));
                        }

                        startConfirmationActivity(ConfirmationActivity.SUCCESS_ANIMATION, confirmationMessage);
                    }
                });
            }
        });

        return mMainView;
    }

    private void startConfirmationActivity(int animationType, String message) {
        Intent confirmationActivity = new Intent(getActivity(), ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);

        getActivity().startActivity(confirmationActivity);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mTalk == null) {
            EventBus.getDefault().postLocal(new GetTalkEvent());

        } else {
            displayTalk();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(final TalkEvent talkEvent) {

        if (talkEvent == null) {
            return;
        }

        mTalk = talkEvent.getTalk();

        displayTalk();


    }


    public void onEvent(final FavoriteEvent favoriteEvent) {
        if (favoriteEvent == null) {
            return;
        }

        Long eventId = favoriteEvent.getEventId();

        mTalk.setEventId(eventId);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mTalk.getEventId() != null) && (mTalk.getEventId() > 0)) {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_on);
                } else {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_off);
                }
            }
        });
    }


    private void displayTalk() {

        if (mTalk == null) {
            return;
        }

        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) mMainView.findViewById(R.id.title)).setText(mTalk.getTitle());

                ((TextView) mMainView.findViewById(R.id.roomName)).setText(mTalk.getRoomName());

                if ((mTalk.getEventId() != null) && (mTalk.getEventId() > 0)) {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_on);
                } else {
                    ((ImageView) mMainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_off);
                }

                String timeFrom;
                String timeTo;
                String dayOfWeek;

                ((TextView) mMainView.findViewById(R.id.talkTime)).setText("");

                if (mTalk.getFromTimeMillis() != null) {
                    timeFrom = new SimpleDateFormat("HH:mm").format(new Date(mTalk.getFromTimeMillis()));
                    dayOfWeek = new SimpleDateFormat("EEEE").format(new Date(mTalk.getFromTimeMillis()));

                } else {
                    return;
                }

                if (mTalk.getToTimeMillis() != null) {
                    timeTo = new SimpleDateFormat("HH:mm").format(new Date(mTalk.getToTimeMillis()));
                } else {
                    return;
                }

                ((TextView) mMainView.findViewById(R.id.dayOfWeek)).setText(dayOfWeek);

                ((TextView) mMainView.findViewById(R.id.talkTime)).setText(timeFrom + " - " + timeTo);
            }
        });

    }


}
