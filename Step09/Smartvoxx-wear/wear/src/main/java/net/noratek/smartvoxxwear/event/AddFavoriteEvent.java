package net.noratek.smartvoxxwear.event;


import net.noratek.smartvoxx.common.model.Talk;

/**
 * Created by eloudsa on 20/09/15.
 */
public class AddFavoriteEvent {

    private Talk talk;

    public AddFavoriteEvent(Talk talkCalendar) {
        this.talk = talkCalendar;
    }

    public Talk getTalk() {
        return talk;
    }
}
