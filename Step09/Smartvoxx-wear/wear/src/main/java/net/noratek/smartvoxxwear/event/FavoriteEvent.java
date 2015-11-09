package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 03/11/15.
 */
public class FavoriteEvent {

    private Long eventId;

    public FavoriteEvent(Long eventId) {
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
