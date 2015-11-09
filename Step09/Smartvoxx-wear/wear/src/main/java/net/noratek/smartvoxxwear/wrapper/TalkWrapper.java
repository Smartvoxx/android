package net.noratek.smartvoxxwear.wrapper;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import net.noratek.smartvoxx.common.model.Speaker;
import net.noratek.smartvoxx.common.model.Talk;
import net.noratek.smartvoxx.common.utils.Constants;

import java.util.List;

/**
 * Created by eloudsa on 08/09/15.
 */
public class TalkWrapper {

    public Talk getTalk(DataEvent dataEvent) {


        if (dataEvent == null) {
            return null;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return null;
        }

        return getTalk(dataMapItem.getDataMap());
    }


    public Talk getTalk(DataMap dataMap) {

        if (dataMap == null) {
            return null;
        }

        DataMap dataTalkMap = dataMap.getDataMap(Constants.DETAIL_PATH);
        if (dataTalkMap == null) {
            return null;
        }

        Talk talk = new Talk();

        talk.setId(dataTalkMap.getString("id"));
        talk.setEventId(dataTalkMap.getLong("eventId"));
        talk.setTalkType(dataTalkMap.getString("talkType"));
        talk.setTrack(dataTalkMap.getString("track"));
        talk.setTrackId(dataTalkMap.getString("trackId"));
        talk.setTitle(dataTalkMap.getString("title"));
        talk.setLang(dataTalkMap.getString("lang"));
        talk.setSummary(dataTalkMap.getString("summary"));


        List<DataMap> speakersDataMap = dataTalkMap.getDataMapArrayList(Constants.SPEAKERS_PATH);
        if (speakersDataMap == null) {
            return talk;
        }

        for (DataMap speakerDataMap : speakersDataMap) {
            // retrieve the speaker's information

            Speaker speaker = new Speaker();

            speaker.setUuid(speakerDataMap.getString("uuid"));
            speaker.setFullName(speakerDataMap.getString("title"));


            talk.addSpeaker(speaker);
        }

        return talk;
    }

}
