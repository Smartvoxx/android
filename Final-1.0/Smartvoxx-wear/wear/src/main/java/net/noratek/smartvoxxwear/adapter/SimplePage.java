package net.noratek.smartvoxxwear.adapter;

import java.io.Serializable;

/**
 * Created by eloudsa on 24/08/15.
 */
public class SimplePage implements Serializable {

    private static final long serialVersionUID = -4816792608001679309L;

    private String mPageId;
    private String mPageName;
    private String mPageType;
    private String mTitle;
    private String mDescription;
    private int mIconId;
    private int mBackgroundId;


    public SimplePage(String title, String description, int iconId, int backgroundId) {
        this.mTitle = title;
        this.mDescription = description;
        this.mIconId = iconId;
        this.mBackgroundId = backgroundId;
    }


    public SimplePage(String pageId, String pageName, String pageType, String title, String description, int iconId, int backgroundId) {
        this.mPageId = pageId;
        this.mPageName = pageName;
        this.mPageType = pageType;
        this.mTitle = title;
        this.mDescription = description;
        this.mIconId = iconId;
        this.mBackgroundId = backgroundId;
    }

    public SimplePage(String pageName, String pageType, String title, String description, int iconId, int backgroundIdd) {
        this.mPageName = pageName;
        this.mPageType = pageType;
        this.mTitle = title;
        this.mDescription = description;
        this.mIconId = iconId;
        this.mBackgroundId = backgroundIdd;
    }

    public String getPageId() {
        return mPageId;
    }

    public void setPageId(String mPageId) {
        this.mPageId = mPageId;
    }

    public String getPageName() {
        return mPageName;
    }

    public void setPageName(String pageName) {
        this.mPageName = pageName;
    }

    public String getPageType() {
        return mPageType;
    }

    public void setPageType(String mPageType) {
        this.mPageType = mPageType;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        this.mDescription = description;
    }

    public int getIconId() {
        return mIconId;
    }

    public void setIconId(int mIconId) {
        this.mIconId = mIconId;
    }

    public int getBackgroundId() {
        return mBackgroundId;
    }

    public void setBackgroundId(int mBackgroundId) {
        this.mBackgroundId = mBackgroundId;
    }
}
