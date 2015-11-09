package net.noratek.smartvoxxwear.rest.service;


import net.noratek.smartvoxx.common.model.Schedules;
import net.noratek.smartvoxx.common.model.SlotList;
import net.noratek.smartvoxx.common.model.Speaker;
import net.noratek.smartvoxx.common.model.Talk;

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

    @GET("/conferences/{conference}/talks/{talkid}")
    void getTalk(@Path("conference") String conference, @Path("talkid") String uuid, Callback<Talk> callback);

    @GET("/conferences/{conference}/speakers/{uuid}")
    void getSpeaker(@Path("conference") String conference, @Path("uuid") String uuid, Callback<Speaker> callback);


}
