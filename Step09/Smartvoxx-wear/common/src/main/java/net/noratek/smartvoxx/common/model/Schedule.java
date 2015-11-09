package net.noratek.smartvoxx.common.model;

/**
 * Created by eloudsa on 01/11/15.
 */
public class Schedule {

    private String day;
    private String title;

    public Schedule() {
    }

    public Schedule(String day, String title) {
        this.day = day;
        this.title = title;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
