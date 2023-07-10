package com.yeastar.linkus.demo.utils;

import android.content.Context;
import android.os.SystemClock;
import android.widget.Chronometer;


import com.yeastar.linkus.demo.R;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    public static boolean isEarly(int days, long time) {
        return (currentTimeMillis() - time) > (days * 24 * 3600 * 1000);
    }

    public static int currentTimeSecond() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long[] getTsTimes() {
        long[] times = new long[2];

        Calendar calendar = Calendar.getInstance();

        times[0] = calendar.getTimeInMillis() / 1000;

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        times[1] = calendar.getTimeInMillis() / 1000;

        return times;
    }

    public static String getFormatDatetime(int year, int month, int day) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new GregorianCalendar(year, month, day).getTime());
    }

    public static Date getDateFromFormatString(String formatDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(formatDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getNowDatetime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return (formatter.format(new Date()));
    }

    public static String dateToStringTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
        return (formatter.format(date));
    }

    public static int getNow() {
        return (int) ((new Date()).getTime() / 1000);
    }

    public static String getNowDateTime(String format) {
        Date date = new Date();

        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
        return df.format(date);
    }

    public static String getDateString(long milliseconds) {
        return getDateTimeString(milliseconds, "yyyyMMdd");
    }

    public static String getTimeString(long milliseconds) {
        return getDateTimeString(milliseconds, "HH:mm");
    }

    public static String getBeijingNowTimeString(String format) {
        TimeZone timezone = TimeZone.getTimeZone("Asia/Shanghai");

        Date date = new Date(currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(timezone);

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeZone(timezone);
        String prefix = gregorianCalendar.get(Calendar.AM_PM) == Calendar.AM ? "上午" : "下午";

        return prefix + formatter.format(date);
    }

    public static String getBeijingNowTime(String format) {
        TimeZone timezone = TimeZone.getTimeZone("Asia/Shanghai");

        Date date = new Date(currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(timezone);

        return formatter.format(date);
    }

    public static String getDateTimeString(long milliseconds, String format) {
        Date date = new Date(milliseconds);
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter.format(date);
    }


    public static String getFavoriteCollectTime(long milliseconds) {
        String showDataString = "";
        Date today = new Date();
        Date date = new Date(milliseconds);
        Date firstDateThisYear = new Date(today.getYear(), 0, 0);
        if (!date.before(firstDateThisYear)) {
            SimpleDateFormat dateformatter = new SimpleDateFormat("MM-dd", Locale.getDefault());
            showDataString = dateformatter.format(date);
        } else {
            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            showDataString = dateformatter.format(date);
        }
        return showDataString;
    }

    /**
     * im时间显示规则
     *
     * @param context　上下文对象
     * @param milliseconds 时间毫秒
     * @param abbreviate   是否最近联系人列表
     */
    public static String getTimeShowString(Context context, long milliseconds, boolean abbreviate) {
        /*
            1.一分钟之内显示刚刚表示
            2.1分钟<显示时间<1小时，用多少分钟前表示，如：29分30秒前接收的消息，则显示30分钟前。
            3.一小时<显示时间<今天，用准确的时间表示，如：现在是10:30分，则60分钟前接收的消息，则显示：9:30
            4.今天之前<显示时间<两天，用昨天表示
            ….依次类推
            5.2天<显示时间<1星期，用星期几星期几表示
            …依次类推
            6.1星期<显示时间，用日期表示。如：5/6
            7.超过一年，则显示年月日。如：17/5/6

            未考虑会话列表及十二小时和二十四小时
            当天:
                1分钟前 刚刚
                大于1分钟小于一小时 mm分钟前
                一小时前两小时内 一个小时前
                上午/下午hh:mm
            昨天:
                大于当日24时小于昨日24时是昨天上午/下午hh:mm
            一周内:
                大于昨日24时倒推一周 MM 上午/下午hh:mm
            剩下的是当年的 mm月dd日 上午/下午hh:mm
            超过今年的就是yyyy年mm月dd日 上午/下午hh:mm(十二小时制)
        */
        String dataString;

        Date showTime = new Date(milliseconds);
        Date currentTime = new Date();
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        Date todayBegin = todayStart.getTime();
        Date oneMinute = new Date(currentTime.getTime() - 60 * 1000);//一分钟前
        Date oneHour = new Date(currentTime.getTime() - 3600 * 1000);//一小时前
//        Date twoHour = new Date(currentTime.getTime() - 2 * 3600 * 1000);//两小时内
        Date yesterdayBegin = new Date(todayBegin.getTime() - 3600 * 24 * 1000);//昨天
        Date lastWeekBegin = new Date(todayBegin.getTime() - 7 * 3600 * 24 * 1000);//上周的今天

        Calendar currentYearStart = Calendar.getInstance();
        currentYearStart.set(Calendar.DAY_OF_YEAR, 1);
        currentYearStart.set(Calendar.HOUR_OF_DAY, 0);
        currentYearStart.set(Calendar.MINUTE, 0);
        currentYearStart.set(Calendar.SECOND, 0);
        currentYearStart.set(Calendar.MILLISECOND, 0);
        Date currentYearBegin = currentYearStart.getTime();
//        if (abbreviate) {//最近会话列表
//            if (!showTime.before(oneMinute)) {
//                return context.getString(R.string.im_just);
//            } else if (!showTime.before(oneHour)) {
//                long l = (currentTime.getTime() - showTime.getTime()) / (60 * 1000);
//                if (l == 1) {
//                    return l + context.getString(R.string.im_min_ago);
//                }
//                return l + context.getString(R.string.im_mins_ago);
//            }
//        }
        if (!showTime.before(todayBegin)) {
            return getTodayTimeBucket(context, showTime);
        } else if (!showTime.before(yesterdayBegin)) {
            dataString = context.getString(R.string.public_yesterday);
        } else if (!showTime.before(lastWeekBegin)) {//周几+时分
            dataString = getWeekOfDate(context, showTime);
        } else if (!showTime.before(currentYearBegin)) {//MM月dd日 HH：mm
            SimpleDateFormat dataFormatter;
            if (Locale.getDefault().getLanguage().equals(new Locale("zh").getLanguage())) {
                if (abbreviate) {
                    dataFormatter = new SimpleDateFormat("M/d", Locale.getDefault());
                } else {
                    dataFormatter = new SimpleDateFormat("M月d日", Locale.getDefault());
                }
            } else {
                dataFormatter = new SimpleDateFormat("MMM dd", Locale.getDefault());
            }
            dataString = dataFormatter.format(showTime);
        } else {
            SimpleDateFormat dataFormatter;
            if (Locale.getDefault().getLanguage().equals(new Locale("zh").getLanguage())) {
                if (abbreviate) {
                    dataFormatter = new SimpleDateFormat("yy/M/d", Locale.getDefault());
                } else {
                    dataFormatter = new SimpleDateFormat("yyyy年M月d日", Locale.getDefault());
                }
            } else {
                dataFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            }
            dataString = dataFormatter.format(showTime);
        }
        if (abbreviate) {
            return dataString;
        } else {
            return dataString + " " + getTodayTimeBucket(context, showTime);
        }
    }

    /**
     * 根据不同时间段，显示不同时间
     *
     * @param date 日志
     * @return s
     */
    public static String getTodayTimeBucket(Context context, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat timeFormatter1to12 = new SimpleDateFormat("hh:mm", Locale.getDefault());
//        if (get24HourMode(ImCache.getContext())) {
//            SimpleDateFormat timeFormatter1to24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            return timeFormatter1to24.format(date);
//        } else {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 0 && hour < 12) {
                return context.getString(R.string.public_am, timeFormatter1to12.format(date));
            } else if (24 > hour && hour >= 12) {
                return context.getString(R.string.public_pm, timeFormatter1to12.format(date));
            }
//        }
        return "";
    }

    /**
     * 根据不同时间制式，获取所需时间
     */
    public static String getStringTimeFor12Or24(Context context, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy.MM.dd ", Locale.getDefault());
        SimpleDateFormat timeFormatter1to12 = new SimpleDateFormat("hh:mm", Locale.getDefault());
        if (get24HourMode(context)) {
            SimpleDateFormat timeFormatter1to24 = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
            return timeFormatter1to24.format(date);
        } else {
            String format = timeFormatter.format(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 0 && hour < 12) {
                return format + context.getString(R.string.public_am, timeFormatter1to12.format(date));
            } else if (24 > hour && hour >= 12) {
                return format + context.getString(R.string.public_pm, timeFormatter1to12.format(date));
            }
        }
        return "";
    }

    /**
     * 根据日期获得星期
     *
     * @param date
     * @return
     */
    public static String getWeekOfDate(Context context, Date date) {
        int[] weekDaysName = {R.string.public_sun, R.string.public_mon, R.string.public_tue, R.string.public_wed, R.string.public_thu, R.string.public_fri, R.string.public_sat};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return context.getString(weekDaysName[intWeek]);
    }

    public static boolean isSameDay(long time1, long time2) {
        return isSameDay(new Date(time1), new Date(time2));
    }

    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        return sameDay;
    }

    /**
     * 判断两个日期是否在同一周
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameWeekDates(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (1 == subYear && 11 == cal2.get(Calendar.MONTH)) {
            // 如果12月的最后一周横跨来年第一周的话则最后一周即算做来年的第一周
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        } else if (-1 == subYear && 11 == cal1.get(Calendar.MONTH)) {
            return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
        }
        return false;
    }

    public static long getSecondsByMilliseconds(long milliseconds) {
        long seconds = new BigDecimal(((float) milliseconds / (float) 1000)).setScale(0,
                BigDecimal.ROUND_HALF_UP).intValue();
        // if (seconds == 0) {
        // seconds = 1;
        // }
        return seconds;
    }

    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + i;
        else retStr = "" + i;
        return retStr;
    }

    public static String getElapseTimeForShow(int milliseconds) {
        StringBuilder sb = new StringBuilder();
        int seconds = milliseconds / 1000;
        if (seconds < 1)
            seconds = 1;
        int hour = seconds / (60 * 60);
        if (hour != 0) {
            sb.append(hour).append("小时");
        }
        int minute = (seconds - 60 * 60 * hour) / 60;
        if (minute != 0) {
            sb.append(minute).append("分");
        }
        int second = (seconds - 60 * 60 * hour - 60 * minute);
        if (second != 0) {
            sb.append(second).append("秒");
        }
        return sb.toString();
    }

    public static boolean get24HourMode(final Context context) {
        if (context != null) {
            return android.text.format.DateFormat.is24HourFormat(context);
        }
        return true;
    }

    public static String getStringTime(Date date) {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm ", Locale.getDefault());
        return timeFormatter.format(date);
    }

    /**
     * 判断两个时间是否相隔一天
     */
    public static boolean isOverOneDay(long localtime, long current) {

        return Math.abs(current - localtime) > 24 * 60 * 60 * 1000;
    }

    /**
     * 设置计时器样式
     * 规则:
     *     小于1小时样式 00:00
     *     大于1小时样式 00:00:00
     * @param chronometer 计时器
     */
    public static void setChronometerFormat(Chronometer chronometer) {
        int hour = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60 / 60);
        int minute = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60) % 60;
        int second = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000) % 60;
        if (hour < 1) {
            if (minute == 59 && second == 59) {
                chronometer.setFormat("0" + "%s");
            } else {
                chronometer.setFormat("%s");
            }
        } else if (hour < 10) {
            chronometer.setFormat("0" + "%s");
        } else {
            chronometer.setFormat("%s");
        }
    }

    public static String getSimpleDateStr(Context context, Date date) {
        Calendar nowCalendar = Calendar.getInstance(Locale.getDefault());
        Calendar cdrCalendar = Calendar.getInstance(Locale.getDefault());
        cdrCalendar.setTime(date);
        boolean sameYear = cdrCalendar.get(Calendar.YEAR) == nowCalendar.get(Calendar.YEAR);
        boolean sameDay = cdrCalendar.get(Calendar.DAY_OF_YEAR) == nowCalendar.get(Calendar.DAY_OF_YEAR);
        //同一天显示时间
        if (sameYear && sameDay) {
            return getTodayTimeBucket2(context, date);
        } else {//其他日期显示格式统一(区分12/24小时制):2001-12-5 下午01:15 / 2001-12-5 13:15
            String prefix = DateToStr(date, "yyyy-MM-dd ");
            String suffix = getTodayTimeBucket2(context, date);
            return prefix + suffix;
        }
    }

    /**
     * 日期转换成字符串
     * @param date 日期
     * @param formatPattern 字符串日期模板
     * @return str
     */
    public static String DateToStr(Date date, String formatPattern) {

        SimpleDateFormat format = new SimpleDateFormat(formatPattern, Locale.getDefault());
        return format.format(date);
    }

    /**
     * 根据不同时间段，显示不同时间(精确到秒)
     *
     * @param date 日期
     * @return 获取12小时或24小时制的时分如: 下午01:24 / 13:24
     */
    public static String getTodayTimeBucket2(Context context, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        SimpleDateFormat timeFormatter1to12 = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
        if (is24HourMode(context)) {
            SimpleDateFormat timeFormatter1to24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            return timeFormatter1to24.format(date);
        } else {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 0 && hour < 12) {
                return context.getString(R.string.public_am, timeFormatter1to12.format(date));
            } else if (24 > hour && hour >= 12) {
                return context.getString(R.string.public_pm, timeFormatter1to12.format(date));
            }
        }
        return "";
    }

    /**
     * @param context c
     * @return 是否24小时制
     */
    public static boolean is24HourMode(final Context context) {
        if (context != null) {
            return android.text.format.DateFormat.is24HourFormat(context);
        }
        return true;
    }
}