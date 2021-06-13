package com.scouter.cruelsun.helper;

import com.scouter.cruelsun.Configs;

public class Time {

    public static String getMoonPhaseString(long worldTicks)
    {
        String phaseString = "null";

        int daysPassed = (int) worldTicks/24000;
        int moonPhase = (daysPassed) % 8;
        switch (moonPhase) {
            case 0:
                phaseString = "full";
                break;
            case 1:
                phaseString = "waning gibbous";
                break;
            case 2:
                phaseString = "third quarter";
                break;
            case 3:
                phaseString = "waning crescent";
                break;
            case 4:
                phaseString = "new";
                break;
            case 5:
                phaseString = "waxing crescent";
                break;
            case 6:
                phaseString = "first quarter";
                break;
            case 7:
                phaseString = "waxing gibbous";
                break;
        }
        if (Configs.CONFIGS.isDebugMode()) System.out.println("Moon phase: " + moonPhase + " " + phaseString);
        return phaseString;
    }

    public static String getApproximateTimeString(long worldTicks)
    {
        String timeOfDayString;
        int time = (int) (worldTicks % 24000);
        if (time >= 0 && time <= 2000)
            timeOfDayString = "morning";
        else if (time > 2000 && time < 6000)
            timeOfDayString = "late morning";
        else if (time > 6000 && time < 9000)
            timeOfDayString = "early afternoon";
        else if (time > 9000 && time < 12000)
            timeOfDayString = "late afternoon";
        else if (time > 12000 && time < 14000)
            timeOfDayString = "evening";
        else if (time > 14000 && time < 22000)
            timeOfDayString = "the middle of the night";
        else
            timeOfDayString = "early morning";

        return timeOfDayString;
    }
}
