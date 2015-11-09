package net.noratek.smartvoxxwear.calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import net.noratek.smartvoxx.common.model.Talk;
import net.noratek.smartvoxxwear.alarm.AlarmService;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by eloudsa on 20/09/15.
 */
public class CalendarHelper {

    private final static String TAG = CalendarHelper.class.getCanonicalName();

    private Context mContext;
    private String mCalendarURI;


    public CalendarHelper(Context mContext) {
        this.mContext = mContext;

        mCalendarURI = getCalendarUriBase();
    }

    public Long addEvent(Talk talk) {

        if (talk == null) {
            return null;
        }

        // add event only if it's not already added on the calendar (search by title)
        Talk talkEvent = getTalkByTitleAndTime(talk.getTitle(), talk.getFromTimeMillis(), talk.getToTimeMillis());
        if (talkEvent != null) {
            // this talk is already stored in the calendar
            return talkEvent.getEventId();
        }


        ContentValues event = new ContentValues();
        event.put("calendar_id", 1);

        event.put("title", talk.getTitle());

        event.put("description", talk.getSummary());

        event.put("eventLocation", talk.getRoomName());
        event.put("eventTimezone", TimeZone.getDefault().getID());

        // TODO comment these lines before RELEASE
        //Calendar calendar = Calendar.getInstance();
        //calendar.add(Calendar.MINUTE, 11);
        //Date startDate = calendar.getTime();
        //event.put("dtstart", startDate.getTime());

        // TODO uncomment this line before RELEASE
        event.put("dtstart", talk.getFromTimeMillis());
        event.put("dtend", talk.getToTimeMillis());

        event.put("allDay", 0); // 0 for false, 1 for true
        event.put("eventStatus", 1);
        event.put("hasAlarm", 1); // 0 for false, 1 for true

        String eventUriString = mCalendarURI + "events";
        Uri eventUri = mContext.getContentResolver()
                .insert(Uri.parse(eventUriString), event);

        Long eventId = Long.parseLong(eventUri.getLastPathSegment());

        // attach a reminder to the event (10 minutes)
        addReminderAlarm(talk.getId(), eventId, event, 10);

        // you can add additional reminders as show here below
        //addReminderAlarm(talk.getTalkId(), eventId, event, 5);
        //addReminderAlarm(talk.getTalkId(), eventId, event, 15);

        return eventId;

    }


    public int removeEvent(Long eventId) {

        String eventUriString = mCalendarURI + "events";

        Uri eventsUri = Uri.parse(eventUriString);
        Uri eventUri = ContentUris.withAppendedId(eventsUri, eventId);

        int iNumRowsDeleted = mContext.getContentResolver().delete(eventUri, null, null);

        return iNumRowsDeleted;
    }

    private void addReminderAlarm(String talkId, Long eventId, ContentValues event, Integer threshold) {

        if ((threshold == null) || (threshold < 1)) {
            return;
        }

        if (event == null) {
            return;
        }

        // add the alarm manager that will process the notification
        Intent intent = new Intent(mContext, AlarmService.class);

        Random randomValue = new Random();
        int requestCode = randomValue.nextInt(Integer.MAX_VALUE - 1) + 1;

        intent.putExtra("eventId", eventId);
        intent.putExtra("talkId", talkId);
        intent.putExtra("title", event.getAsString("title"));
        intent.putExtra("roomName", event.getAsString("location"));
        intent.putExtra("startTime", event.getAsLong("dtstart"));
        intent.putExtra("endTime", event.getAsLong("dtend"));

        PendingIntent pendingIntent = PendingIntent.getService(mContext, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // set the trigger
        Calendar trigger = Calendar.getInstance();

        trigger.setTime(new Date(event.getAsLong("dtstart")));
        trigger.add(Calendar.MINUTE, -threshold);

        // create and cancel alarms for this pending intent
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(mContext.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, trigger.getTimeInMillis(), pendingIntent);
    }



    public Talk getTalkByEventId(Long eventId) {

        Talk talk = new Talk();

        String eventUriString = mCalendarURI + "events";

        Uri eventsUri = Uri.parse(eventUriString);

        Cursor cursor = mContext.getContentResolver().query(eventsUri, new String[]{"title", "description", "eventLocation", "dtstart", "dtend"}, "_id=" + eventId, null, null);
        if (cursor == null) {
            return null;
        }

        if (cursor.moveToNext() == false) {
            cursor.close();
            return null;
        }

        talk.setTitle(cursor.getString(cursor.getColumnIndex("title")));
        talk.setSummary(cursor.getString(cursor.getColumnIndex("description")));
        talk.setRoomName(cursor.getString(cursor.getColumnIndex("eventLocation")));
        talk.setFromTimeMillis(cursor.getLong(cursor.getColumnIndex("dtstart")));
        talk.setToTimeMillis(cursor.getLong(cursor.getColumnIndex("dtend")));

        cursor.close();

        return talk;
    }



    public Talk getTalkByTitleAndTime(String title, Long startTime, Long endTime) {

        Talk talk = new Talk();

        String eventUriString = mCalendarURI + "events";

        Uri eventsUri = Uri.parse(eventUriString);


        // TODO comment these lines before RELEASE
        //String selection = "((title = ?) AND (dtend = ?))";
        //String[] selectionArgs = new String[]{title, endTime.toString()};

        // TODO uncomment this line before RELEASE
        String selection = "title = ? and dtstart = ? and dtend = ?";
        String[] selectionArgs = new String[]{title, startTime.toString(), endTime.toString()};

        Cursor cursor = mContext.getContentResolver().query(eventsUri, new String[]{"_id", "description", "eventLocation"}, selection, selectionArgs, null);
        if (cursor == null) {
            return null;
        }

        if (cursor.moveToNext() == false) {
            cursor.close();
            return null;
        }

        talk.setEventId(cursor.getLong(cursor.getColumnIndex("_id")));
        talk.setTitle(title);
        talk.setSummary(cursor.getString(cursor.getColumnIndex("description")));
        talk.setRoomName(cursor.getString(cursor.getColumnIndex("eventLocation")));
        talk.setFromTimeMillis(startTime);
        talk.setToTimeMillis(endTime);

        cursor.close();

        return talk;
    }

    private String getCalendarUriBase() {
        String calendarUriBase = null;
        Uri calendars = Uri.parse("content://com.android.calendar/calendars");

        Cursor managedCursor = null;

        try {
            managedCursor = mContext.getContentResolver().query(calendars,
                    null, null, null, null);
        } catch (Exception e) {
        }

        if (managedCursor != null) {
            calendarUriBase = "content://com.android.calendar/";
        } else {
            calendars = Uri.parse("content://calendar/calendars");
            try {
                managedCursor = mContext.getContentResolver().query(calendars,
                        null, null, null, null);
            } catch (Exception e) {
            }
            if (managedCursor != null) {
                calendarUriBase = "content://calendar/";
            }
        }

        managedCursor.close();

        return calendarUriBase;
    }



}
