package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 30/08/15.
 */
public class ScrollToPageEvent {

    String pageName;


    public ScrollToPageEvent(String pageName) {
        this.pageName = pageName;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
