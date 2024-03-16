package com.fcb.coupon.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 唐陆军
 * @Description TODO
 * @createTime 2021年06月09日 19:32:00
 */
public class DateUtils {

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期转换字符
     * @param date      日期
     * @param format    转换后格式
     * @return
     */
    public static String parseDateToString(Date date, String format){
        if (date == null){
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }


    public static Date getDelayTime(Date initialTime, Integer delayDay) {
        Calendar myDate = Calendar.getInstance();
        myDate.setTime(initialTime);
        myDate.add(Calendar.DAY_OF_MONTH, delayDay);
        myDate.set(Calendar.HOUR_OF_DAY, 23);
        myDate.set(Calendar.MINUTE, 59);
        myDate.set(Calendar.SECOND, 59);
        myDate.set(Calendar.MILLISECOND, 59);
        return myDate.getTime();
    }

    public static Date getDelayMonth(Date initialTime, Integer delayMonth) {
        Calendar myDate = Calendar.getInstance();
        myDate.setTime(initialTime);
        myDate.add(Calendar.MONTH, delayMonth);
        myDate.set(Calendar.HOUR_OF_DAY, 23);
        myDate.set(Calendar.MINUTE, 59);
        myDate.set(Calendar.SECOND, 59);
        myDate.set(Calendar.MILLISECOND, 59);
        return myDate.getTime();
    }

    public static Date getTodayStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime();
    }

    public static Date getTodayEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }

    public static Date getMonthStartTime() {
        Calendar today = Calendar.getInstance();
        //设置为1号,当前日期既为本月第一天
        today.set(Calendar.DAY_OF_MONTH, today.getActualMinimum(Calendar.DAY_OF_MONTH));
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTime();
    }

    public static Date getMonthEndTime() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.DAY_OF_MONTH, today.getActualMaximum(Calendar.DAY_OF_MONTH));
        today.set(Calendar.HOUR, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        today.set(Calendar.MILLISECOND, 999);
        return today.getTime();
    }


    /*
     * @description 同一天
     * @author 唐陆军
     * @param: d1
     * @param: d2
     * @date 2021-7-15 15:45
     * @return: boolean
     */
    public static boolean isTheSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                && (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH))
                && (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH));
    }

    /*
     * @description 同一月
     * @author 唐陆军

     * @param: d1
     * @param: d2
     * @date 2021-7-15 15:45

     * @return: boolean
     */
    public static boolean isTheSameMonth(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                && (c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH));
    }

}
