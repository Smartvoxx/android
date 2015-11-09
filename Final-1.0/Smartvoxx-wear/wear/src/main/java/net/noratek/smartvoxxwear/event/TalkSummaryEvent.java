package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 20/09/15.
 */
public class TalkSummaryEvent {

    private String title;
    private String summary;
    private String talkType;

    public TalkSummaryEvent(String title, String summary, String talkType) {
        this.title = title;
        this.summary = summary;
        this.talkType = talkType;
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

    public String getTalkType() {
        return talkType;
    }

    public void setTalkType(String talkType) {
        this.talkType = talkType;
    }
}
