package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 15/10/15.
 */
public class FavoriteRemovedEvent {

    private String talkId;


    public FavoriteRemovedEvent() {
    }

    public FavoriteRemovedEvent(String talkId) {
        this.talkId = talkId;
    }

    public String getTalkId() {
        return talkId;
    }

    public void setTalkId(String talkId) {
        this.talkId = talkId;
    }
}
