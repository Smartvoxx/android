package net.noratek.smartvoxxwear.rest.service;

import net.noratek.smartvoxxwear.rest.model.Schedules;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by eloudsa on 30/10/15.
 */
public interface DevoxxApi {


    @GET("/conferences/{conference}/schedules")
    void getSchedules(@Path("conference") String conference, Callback<Schedules> callback);
}
