package net.noratek.smartvoxxwear.event;


import net.noratek.smartvoxx.common.model.Speaker;

/**
 * Created by eloudsa on 30/08/15.
 */
public class SpeakerDetailEvent {

    private Speaker speaker;


    public SpeakerDetailEvent(Speaker speaker) {
        this.speaker = speaker;
    }

    public Speaker getSpeaker() {
        return speaker;
    }

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }
}
