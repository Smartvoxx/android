package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 20/09/15.
 */
public class TalkSummaryEvent {

    private String title;
    private String summary;

    public TalkSummaryEvent(String title, String summary) {
        this.title = title;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
