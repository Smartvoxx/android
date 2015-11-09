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
import net.noratek.smartvoxxwear.event.GetTalkSummaryEvent;
import net.noratek.smartvoxxwear.event.TalkSummaryEvent;

import org.apache.commons.lang3.StringUtils;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 24/08/15.
 */
public class TalkSummaryFragment extends Fragment {

    private final static String TAG = TalkSummaryFragment.class.getCanonicalName();


    private View mMainView;

    private String mTitle;
    private String mTalkSummary;
    private String mTalkType;

    private Boolean mEllipsize = false;

    private final int ELLIPSIS_SIZE = 60;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        EventBus.getDefault().register(this);

        mMainView =  inflater.inflate(R.layout.talk_summary_fragment, container, false);

        final TextView description = (TextView) mMainView.findViewById(R.id.description);

        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTalkSummary == null) {
                    return;
                }

                if (mEllipsize) {
                    description.setText(mTalkSummary);
                    mEllipsize = false;
                } else {
                    description.setText(StringUtils.abbreviate(mTalkSummary, ELLIPSIS_SIZE));
                    mEllipsize = true;
                }

                // Force the CardScrollView to reset to its initial position
                mMainView.findViewById(R.id.card_scroll_view).setScrollX(0);
                mMainView.findViewById(R.id.card_scroll_view).setScrollY(0);

            }
        });


        // The following is required to allow these features:
        // - scrollable card frame
        // - CardFrame expanded in the full height of the text
        // This required that the layout is not enclosed inside "android.support.wearable.view.BoxInsetLayout"
        // otherwise the scroll will not work
        // From: https://github.com/teshi04/WearViewSample/blob/master/wear/src/main/res/layout/activity_card_scroll_view.xml
        //
        CardScrollView cardScrollView =
                (CardScrollView) mMainView.findViewById(R.id.card_scroll_view);
        cardScrollView.setExpansionEnabled(true);
        cardScrollView.setExpansionDirection(CardFrame.EXPAND_DOWN);
        cardScrollView.setExpansionFactor(10.0F);
        cardScrollView.setCardGravity(Gravity.BOTTOM);

        CardFrame cardFrame = (CardFrame) mMainView.findViewById(R.id.card_frame);
        cardFrame.setExpansionEnabled(true);
        cardFrame.setExpansionDirection(CardFrame.EXPAND_DOWN);
        cardFrame.setExpansionFactor(10.0F);

        return mMainView;
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
        mTalkType = talkSummaryEvent.getTalkType();

        if (mTalkSummary == null) {
            return;
        }


        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) mMainView.findViewById(R.id.title)).setText(mTitle);
                ((TextView) mMainView.findViewById(R.id.talkType)).setText(mTalkType);
                ((TextView) mMainView.findViewById(R.id.description)).setText(StringUtils.abbreviate(mTalkSummary, ELLIPSIS_SIZE));
                mEllipsize = true;
            }
        });


    }


}
