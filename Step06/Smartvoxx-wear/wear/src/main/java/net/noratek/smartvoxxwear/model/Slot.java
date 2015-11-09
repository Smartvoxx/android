package net.noratek.smartvoxxwear.model;

/**
 * Created by eloudsa on 05/09/15.
 */
public class Slot {

    private String slotId;
    private Long fromTimeMillis;
    private Long toTimeMillis;
    private String roomName;

    private BreakSession breakSession;
    private Talk talk;
    

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public Long getFromTimeMillis() {
        return fromTimeMillis;
    }

    public void setFromTimeMillis(Long fromTimeMillis) {
        this.fromTimeMillis = fromTimeMillis;
    }

    public Long getToTimeMillis() {
        return toTimeMillis;
    }

    public void setToTimeMillis(Long toTimeMillis) {
        this.toTimeMillis = toTimeMillis;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public BreakSession getBreak() {
        return breakSession;
    }

    public void setBreak(BreakSession breakSession) {
        this.breakSession = breakSession;
    }


    public Talk getTalk() {
        return talk;
    }

    public void setTalk(Talk talk) {
        this.talk = talk;
    }

}
