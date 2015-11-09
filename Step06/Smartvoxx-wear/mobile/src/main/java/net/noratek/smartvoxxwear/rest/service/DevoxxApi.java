package net.noratek.smartvoxxwear.rest.service;

import net.noratek.smartvoxxwear.rest.model.Schedules;
import net.noratek.smartvoxxwear.rest.model.SlotList;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by eloudsa on 30/10/15.
 */
public interface DevoxxApi {


    @GET("/conferences/{conference}/schedules")
    void getSchedules(@Path("conference") String conference, Callback<Schedules> callback);

    @GET("/conferences/{conference}/schedules/{day}")
    void getSchedule(@Path("conference") String conference, @Path("day") String day, Callback<SlotList> callback);


}
