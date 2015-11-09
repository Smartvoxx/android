package net.noratek.smartvoxxwear.event;


/**
 * Created by eloudsa on 20/09/15.
 */
public class RemoveFavoriteEvent {

    private String talkId;

    public RemoveFavoriteEvent(String talkId) {
        this.talkId = talkId;
    }


    public String getTalkId() {
        return talkId;
    }

    public void setTalkId(String talkId) {
        this.talkId = talkId;
    }
}
