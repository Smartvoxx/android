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

import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.adapter.SimplePage;
import net.noratek.smartvoxxwear.event.AddFavoriteEvent;
import net.noratek.smartvoxxwear.event.FavoriteEvent;
import net.noratek.smartvoxxwear.event.GetTalkEvent;
import net.noratek.smartvoxxwear.event.RemoveFavoriteEvent;
import net.noratek.smartvoxxwear.event.TalkEvent;
import net.noratek.smartvoxxwear.model.Talk;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 24/08/15.
 */
public class TalkFragment extends Fragment {

    private final static String TAG = TalkFragment.class.getCanonicalName();


    private View mainView;

    private Talk mTalk;

    private SimplePage pageSettings;

    private static int ELLIPSE_SIZE = 60;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        pageSettings = (SimplePage) (getArguments() != null ? getArguments().getSerializable("pageInfo") : null);


        mainView =  inflater.inflate(R.layout.talk_fragment, container, false);


        WatchViewStub stub = (WatchViewStub) mainView.findViewById(R.id.watch_talk_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                // Set the title if any
                if (pageSettings != null) {
                    String talklTitle = pageSettings.getTitle() != null ? pageSettings.getTitle() : "";
                    ((TextView) mainView.findViewById(R.id.title)).setText(StringUtils.abbreviate(talklTitle, ELLIPSE_SIZE));
                }


                // add event listener
                mainView.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
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

        return mainView;
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

    public void onEvent (final TalkEvent talkEvent) {

        if (talkEvent == null) {
            return;
        }

        mTalk = talkEvent.getTalk();

        displayTalk();


    }


    public void onEvent (final FavoriteEvent favoriteEvent) {
        if (favoriteEvent == null) {
            return;
        }

        Long eventId = favoriteEvent.getEventId();

        mTalk.setEventId(eventId);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((mTalk.getEventId() != null) && (mTalk.getEventId() > 0)) {
                    ((ImageView) mainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_on);
                } else {
                    ((ImageView) mainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_off);
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
                ((TextView) mainView.findViewById(R.id.title)).setText(StringUtils.abbreviate(mTalk.getTitle(), ELLIPSE_SIZE));

                ((TextView) mainView.findViewById(R.id.talkType)).setText(mTalk.getTalkType());

                ((TextView) mainView.findViewById(R.id.roomName)).setText(mTalk.getRoomName());

                if ((mTalk.getEventId() != null) && (mTalk.getEventId() > 0)) {
                    ((ImageView) mainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_on);
                } else {
                    ((ImageView) mainView.findViewById(R.id.favorite)).setImageResource(R.drawable.ic_favorite_off);
                }

                String timeFrom;
                String timeTo;

                ((TextView) mainView.findViewById(R.id.talkTime)).setText("");

                if (mTalk.getFromTimeMillis() != null) {
                    timeFrom = new SimpleDateFormat("HH:mm").format(new Date(mTalk.getFromTimeMillis()));
                } else {
                    return;
                }

                if (mTalk.getToTimeMillis() != null) {
                    timeTo = new SimpleDateFormat("HH:mm").format(new Date(mTalk.getToTimeMillis()));
                } else {
                    return;
                }

                ((TextView) mainView.findViewById(R.id.talkTime)).setText(timeFrom + " - " + timeTo);
            }
        });

    }


}
