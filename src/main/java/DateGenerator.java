public class DateGenerator {
    private static int year = 1900;
    private static int month = 1;
    private static int day = 1;
    private static int hour = 0;
    private static int minute = 0;
    private static int sec = 0;

    public static void reset(){
        year = 1900;
        month = 1;
        day = 1;
        hour = 0;
        minute = 0;
        sec = 0;
    }

    public static String getNextDate(){
        StringBuilder strBld = new StringBuilder();
        if (sec >= 60){
            sec = 0;
            minute++;
            if (minute >= 60){
                minute = 0;
                hour++;
                if (hour >= 23){
                    hour = 0;
                    day++;
                    if(day >= 30){
                        day = 1;
                        month++;
                        if(month >= 12){
                            month = 1;
                            year++;
                        }
                    }
                }
            }
        } else {
            sec++;
        }
        strBld.append(Integer.toString(year)).append("-");
        if(month < 10){
            strBld.append("0").append(Integer.toString(month)).append("-");
        } else {
            strBld.append(Integer.toString(month)).append("-");
        }
        if(day < 10){
            strBld.append("0").append(Integer.toString(day)).append(" ");
        } else {
            strBld.append(Integer.toString(day)).append(" ");
        }
        if(hour < 10){
            strBld.append("0").append(Integer.toString(hour)).append(":");
        } else {
            strBld.append(Integer.toString(hour)).append(":");
        }
        if(minute < 10){
            strBld.append("0").append(Integer.toString(minute)).append(":");
        } else {
            strBld.append(Integer.toString(minute)).append(":");
        }
        if(sec < 10){
            strBld.append("0").append(Integer.toString(sec));
        } else {
            strBld.append(Integer.toString(sec));
        }
        return strBld.toString();
        }
    }
