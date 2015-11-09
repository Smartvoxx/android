package net.noratek.smartvoxxwear.event;

import net.noratek.smartvoxx.common.model.Speaker;

/**
 * Created by eloudsa on 13/09/15.
 */
public class DisplaySpeakerEvent {


    private String pageName;
    private Speaker speaker;


    public DisplaySpeakerEvent(Speaker speaker) {
        this.speaker = speaker;
    }

    public DisplaySpeakerEvent(String pageName, Speaker speaker) {
        this.pageName = pageName;
        this.speaker = speaker;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
