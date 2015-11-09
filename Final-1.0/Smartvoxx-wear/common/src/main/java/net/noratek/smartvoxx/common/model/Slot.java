package net.noratek.smartvoxx.common.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by eloudsa on 04/10/15.
 */
public class Slot {

    private String slotId;
    private Long scheduleId;
    private Long fromTimeMillis;
    private Long toTimeMillis;
    private String roomName;

    @SerializedName("break")
    private BreakSession breakSession;

    private Talk talk;


    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
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

    public BreakSession getBreakSession() {
        return breakSession;
    }

    public void setBreakSession(BreakSession breakSession) {
        this.breakSession = breakSession;
    }

    public Talk getTalk() {
        return talk;
    }

    public void setTalk(Talk talk) {
        this.talk = talk;
    }
}
