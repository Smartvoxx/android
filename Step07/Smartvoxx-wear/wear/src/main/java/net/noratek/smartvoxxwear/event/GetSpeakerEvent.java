package net.noratek.smartvoxxwear.event;

/**
 * Created by eloudsa on 29/08/15.
 */
public class GetSpeakerEvent {

    private String uuid;

    public GetSpeakerEvent(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
