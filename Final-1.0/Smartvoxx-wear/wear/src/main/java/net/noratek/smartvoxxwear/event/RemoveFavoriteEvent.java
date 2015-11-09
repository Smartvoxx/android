package net.noratek.smartvoxxwear.event;


/**
 * Created by eloudsa on 20/09/15.
 */
public class RemoveFavoriteEvent {

    private String talkId;
    private Long eventId;


    public RemoveFavoriteEvent(String talkId, Long eventId) {
        this.talkId = talkId;
        this.eventId = eventId;
    }

    public String getTalkId() {
        return talkId;
    }

    public void setTalkId(String talkId) {
        this.talkId = talkId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
