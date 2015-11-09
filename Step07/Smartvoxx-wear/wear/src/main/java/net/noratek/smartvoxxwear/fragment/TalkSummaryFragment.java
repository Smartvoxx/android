package net.noratek.smartvoxxwear.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.CardFrame;
import android.support.wearable.view.CardScrollView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.adapter.SimplePage;
import net.noratek.smartvoxxwear.event.GetTalkSummaryEvent;
import net.noratek.smartvoxxwear.event.TalkSummaryEvent;

import org.apache.commons.lang3.StringUtils;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 24/08/15.
 */
public class TalkSummaryFragment extends Fragment {

    private final static String TAG = TalkSummaryFragment.class.getCanonicalName();


    private View mainView;

    private SimplePage pageSettings;

    private String mTitle;
    private String mTalkSummary;

    private Boolean ellipsize = false;

    private final int ELLIPSIS_SIZE = 60;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        pageSettings = (SimplePage) (getArguments() != null ? getArguments().getSerializable("pageInfo") : null);


        mainView =  inflater.inflate(R.layout.talk_summary_fragment, container, false);

        final TextView description = (TextView) mainView.findViewById(R.id.description);

        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTalkSummary == null) {
                    return;
                }

                if (ellipsize) {
                    description.setText(mTalkSummary);
                } else {
                    description.setText(StringUtils.abbreviate(mTalkSummary, ELLIPSIS_SIZE));
                }

                ellipsize = !ellipsize;

                // Force the CardScrollView to reset to its initial position
                mainView.findViewById(R.id.card_scroll_view).setScrollX(0);
                mainView.findViewById(R.id.card_scroll_view).setScrollY(0);

            }
        });

        /*
        // Set the title if any
        if (pageSettings != null) {
            String talklTitle = pageSettings.getTitle() != null ? pageSettings.getTitle() : "";
            ((TextView) mainView.findViewById(R.id.title)).setText(talklTitle);
        }
        */

        // The following is required to allow these features:
        // - scrollable card frame
        // - CardFrame expanded in the full height of the text
        // This required that the layout is not enclosed inside "android.support.wearable.view.BoxInsetLayout"
        // otherwise the scroll will not work
        // From: https://github.com/teshi04/WearViewSample/blob/master/wear/src/main/res/layout/activity_card_scroll_view.xml
        //
        CardScrollView cardScrollView =
                (CardScrollView) mainView.findViewById(R.id.card_scroll_view);
        cardScrollView.setExpansionEnabled(true);
        cardScrollView.setExpansionDirection(CardFrame.EXPAND_DOWN);
        cardScrollView.setExpansionFactor(10.0F);
        cardScrollView.setCardGravity(Gravity.BOTTOM);

        CardFrame cardFrame = (CardFrame) mainView.findViewById(R.id.card_frame);
        cardFrame.setExpansionEnabled(true);
        cardFrame.setExpansionDirection(CardFrame.EXPAND_DOWN);
        cardFrame.setExpansionFactor(10.0F);

        return mainView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mTalkSummary == null) {
            EventBus.getDefault().postLocal(new GetTalkSummaryEvent());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    public void onEvent (final TalkSummaryEvent talkSummaryEvent) {

        if (talkSummaryEvent == null) {
            return;
        }

        mTitle = talkSummaryEvent.getTitle();
        mTalkSummary = talkSummaryEvent.getSummary();

        if (mTalkSummary == null) {
            return;
        }


        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) mainView.findViewById(R.id.title)).setText(mTitle);
                ((TextView) mainView.findViewById(R.id.description)).setText(StringUtils.abbreviate(mTalkSummary, ELLIPSIS_SIZE));
                ellipsize = true;
            }
        });


    }


}
