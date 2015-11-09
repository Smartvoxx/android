package net.noratek.smartvoxxwear.wrapper;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import net.noratek.smartvoxx.common.model.Schedule;
import net.noratek.smartvoxx.common.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eloudsa on 03/09/15.
 */
public class SchedulesListWrapper {

    public List<Schedule> getSchedulesList(DataEvent dataEvent) {

        List<Schedule> schedulesList = new ArrayList<>();

        if (dataEvent == null) {
            return schedulesList;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return schedulesList;
        }


        return getSchedulesList(dataMapItem.getDataMap());
    }


    public List<Schedule> getSchedulesList(DataMap dataMap) {

        List<Schedule> schedulesList = new ArrayList<>();

        if (dataMap == null) {
            return schedulesList;
        }

        List<DataMap> schedulesDataMap = dataMap.getDataMapArrayList(Constants.LIST_PATH);
        if (schedulesDataMap == null) {
            return schedulesList;
        }

        for (DataMap scheduleDataMap : schedulesDataMap) {
            // retrieve the speaker's information

            schedulesList.add(new Schedule(
                    scheduleDataMap.getString("day"),
                    scheduleDataMap.getString("title").replaceAll("Schedule for ", "")));
        }

        return schedulesList;

    }

}
