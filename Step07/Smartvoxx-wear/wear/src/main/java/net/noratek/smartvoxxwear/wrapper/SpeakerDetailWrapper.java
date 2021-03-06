package net.noratek.smartvoxxwear.wrapper;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import net.noratek.smartvoxxwear.model.Speaker;

/**
 * Created by eloudsa on 29/08/15.
 */
public class SpeakerDetailWrapper {


    public Speaker getSpeakerDetail(DataEvent dataEvent) {


        if (dataEvent == null) {
            return null;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return null;
        }


        return getSpeakerDetail(dataMapItem.getDataMap());
    }

    public Speaker getSpeakerDetail(DataMap dataMap) {

        DataMap speakerDataMap = dataMap.getDataMap("/detail");
        if (speakerDataMap == null) {
            return null;
        }

        // retrieve the speaker's information
        Speaker speaker = new Speaker(
                speakerDataMap.getString("uuid"),
                speakerDataMap.getString("lastName"),
                speakerDataMap.getString("firstName"),
                speakerDataMap.getString("blog"),
                speakerDataMap.getString("twitter"),
                speakerDataMap.getString("company"),
                speakerDataMap.getString("bio"),
                speakerDataMap.getString("avatarURL"),
                speakerDataMap.getString("avatarImage"));

        return speaker;


    }

}
