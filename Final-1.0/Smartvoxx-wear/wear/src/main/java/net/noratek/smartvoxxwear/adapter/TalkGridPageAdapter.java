package net.noratek.smartvoxxwear.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import net.noratek.smartvoxx.common.model.Speaker;
import net.noratek.smartvoxxwear.R;
import net.noratek.smartvoxxwear.fragment.TalkFragment;
import net.noratek.smartvoxxwear.fragment.TalkSpeakerFragment;
import net.noratek.smartvoxxwear.fragment.TalkSummaryFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 23/08/15.
 */
public class TalkGridPageAdapter extends FragmentGridPagerAdapter {

    private final static String TAG = TalkGridPageAdapter.class.getCanonicalName();

    public final static String TALK_INFO = "talk-info";
    public final static String TALK_SUMMARY = "talk-summary";
    public final static String TALK_SPEAKER_DETAIL = "talk-speaker-detail";


    private final Context mContext;
    private ArrayList<SimpleRow> mPages;

    private SimpleRow mRowSpeakers;

    private LinkedHashMap<String, Speaker> mSpeakersList;

    private static int NO_BACKGROUND = 0;

    private String mTalkTitle;

    // fragments
    TalkFragment mTalkFragment;
    TalkSummaryFragment mTalkSummaryFragment;
    TalkSpeakerFragment mTalkSpeakerFragment;

    HashMap<String, Fragment> mFragments = new HashMap<>();


    public TalkGridPageAdapter(Context context, FragmentManager fm, String talkTitle) {
        super(fm);

        EventBus.getDefault().register(this);

        mContext = context;
        mTalkTitle = talkTitle;
        initPages();

    }

    private void initPages() {
        mPages = new ArrayList();

        SimpleRow row = new SimpleRow();

        row.addPages(new SimplePage(TALK_INFO, "card", mTalkTitle, "", R.drawable.ic_conference, NO_BACKGROUND));
        row.addPages(new SimplePage(TALK_SUMMARY, "card", mTalkTitle, "", R.drawable.ic_conference, NO_BACKGROUND));
        mPages.add(row);

        mRowSpeakers = new SimpleRow();
    }


    public void unRegister() {
        EventBus.getDefault().unregister(this);
    }


    public HashMap<String, Fragment> getFragments() {
        return mFragments;
    }

    @Override
    public Fragment getFragment(int row, int col) {

        SimplePage page = mPages.get(row).getPages(col);

        String pageId = page.getPageId() == null ? page.getPageName() : page.getPageId();

        Bundle bundle = new Bundle();

        bundle.putSerializable("pageInfo", page);


        if (page.getPageName().equalsIgnoreCase(TALK_INFO)) {
            mTalkFragment = new TalkFragment();
            mTalkFragment.setArguments(bundle);

            mFragments.put(pageId, mTalkFragment);

            return mTalkFragment;

        }else if (page.getPageName().equalsIgnoreCase(TALK_SUMMARY)) {
            mTalkSummaryFragment = new TalkSummaryFragment();
            mTalkSummaryFragment.setArguments(bundle);

            mFragments.put(pageId, mTalkSummaryFragment);

            return mTalkSummaryFragment;

        }


        // default is a card for the speaker(s)
        mTalkSpeakerFragment = new TalkSpeakerFragment();
        mTalkSpeakerFragment.setArguments(bundle);

        mFragments.put(pageId, mTalkSpeakerFragment);

        return mTalkSpeakerFragment;
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public Drawable getBackgroundForPage(int row, int col) {
        // getDrawable() is deprecated since API level 22.

        SimplePage page = mPages.get(row).getPages(col);

        if (page.getBackgroundId() == NO_BACKGROUND) {
            // no background
            return BACKGROUND_NONE;
        }

        Drawable drawable = mContext.getResources().getDrawable(page.getBackgroundId());

        return drawable;
    }

    @Override
    public int getRowCount() {
        return mPages.size();
    }

    @Override
    public int getColumnCount(int row) {
        return mPages.get(row).size();
    }


  /*
    @Override
    public void destroyItem(ViewGroup container, int row, int column, Object object) {
        super.destroyItem(container, row, column, object);


        FragmentManager manager = ((Fragment) object).getFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.remove((Fragment) object);
        trans.commit();
    }
*/


    // Events
    public void onEvent(String text){
        /*
        Toast.makeText(mContext,
                "On Resume from Adapter: " + text,
                Toast.LENGTH_SHORT).show();
                */

        //universityPages.addPages(new SimplePage("Title4", "Text4", R.drawable.speakers, R.drawable.debug_background_4));
    }


    /**
     * Get the position of a page based on its name.
     *
     * @param pageName
     * @return null if not found. A Point object if found.
     */
    public Point getPosition(String pageName) {


        if ((pageName == null) || (pageName.trim().isEmpty())) {
            return null;
        }

        for (int rowNum = 0; rowNum < mPages.size(); rowNum++) {

            SimpleRow row = mPages.get(rowNum);
            for (int colNum = 0; colNum < row.size(); colNum++) {
                SimplePage page = row.getPages(colNum);

                if (page.getPageName().equalsIgnoreCase(pageName)) {
                    return new Point(rowNum, colNum);
                }
            }

        }

        return null;
    }


    // Events
    public void addSpeakers(LinkedHashMap<String, Speaker> speakersList) {

        if (speakersList == null) {
            return;
        }

        if (speakersList.size() == 0) {
            return;
        }

        mSpeakersList = speakersList;

        for (Speaker speaker : mSpeakersList.values()) {
            mRowSpeakers.addPages(new SimplePage(speaker.getUuid(), TALK_SPEAKER_DETAIL, "speaker", "Slot", "", R.drawable.ic_conference, NO_BACKGROUND));
        }
        mPages.add(mRowSpeakers);
    }

}
