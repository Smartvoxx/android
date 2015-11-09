package net.noratek.smartvoxxwear.event;


import net.noratek.smartvoxx.common.model.Talk;

/**
 * Created by eloudsa on 08/09/15.
 */
public class TalkEvent {

    private Talk talk;


    public TalkEvent(Talk talk) {
        this.talk = talk;
    }

    public Talk getTalk() {
        return talk;
    }

    public void setTalk(Talk talk) {
        this.talk = talk;
    }
}
