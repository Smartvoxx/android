package net.noratek.smartvoxx.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eloudsa on 06/09/15.
 */
public class Talk {

    private String id;
    private Long eventId = 0L;
    private String talkType;
    private String track;
    private String trackId;
    private String title;
    private String lang;
    private String summary;
    private String roomName;
    private Long fromTimeMillis;
    private Long toTimeMillis;

    List<Speaker> speakers;

    public Talk() {
    }

    public Talk(String title, String summary, String roomName, Long fromTimeMillis, Long toTimeMillis) {
        this.title = title;
        this.summary = summary;
        this.roomName = roomName;
        this.fromTimeMillis = fromTimeMillis;
        this.toTimeMillis = toTimeMillis;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTalkType() {
        return talkType;
    }

    public void setTalkType(String talkType) {
        this.talkType = talkType;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Speaker> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Speaker> speakers) {
        this.speakers = speakers;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    public void addSpeaker(Speaker speaker) {

        if (speaker == null) {
            return;
        }

        if (speakers == null) {
            speakers = new ArrayList<>();
        }

        speakers.add(speaker);
    }


}
