import org.w3c.dom.css.Counter;

import java.util.Random;

public class User2 extends Thread {
    private int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }
    String jobid = "4672";
    String compid = "1714";
    String jobrunnerid;

    public User2(){
        super("My Thread");
        start();
    }

    public void run(){
        jobrunnerid = MainClass.runner.getFromCredsByKey("jobrunnerid");
        for (;;){
            MainClass.runner.sendNewJobRunQuery(jobrunnerid, jobid, DateGenerator.getNextDate(), "0.0.0.0");
            String jobrunid = MainClass.runner.getResponseBody().trim().split("=")[1];
            int random = new Random().nextInt((1 + 1 - 0) + 0);
            String str1 = null;
            if(random == 1){
                Counters.err++;
                str1 = "0";
            } else {
                Counters.ok++;
                str1 = "1";
            }
            MainClass.runner.sendFinishJobQuery(jobrunnerid, "7", Integer.toString(random), str1, "", "",
                    "", "", "", "", jobrunid);
            Counters.printResult();
            System.out.println("Thread #:" + Thread.currentThread().getId());
        }

    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    private String generateDate(){
        String year = Integer.toString(randBetween(1900, 2018));
        int month = randBetween(1, 12);
        String monthStr;
        if(month < 10){
            monthStr = "0" + Integer.toString(month);
        } else {
            monthStr = Integer.toString(month);
        }
        int day = randBetween(1, 30);
        String dayStr;
        if (day < 10){
            dayStr = "0" + Integer.toString(day);
        } else {
            dayStr = Integer.toString(day);
        }
        int hour = randBetween(00, 23);
        String hourStr;
        if(hour < 10){
            hourStr = "0" + Integer.toString(hour);
        } else {
            hourStr = Integer.toString(hour);
        }
        int minutes = randBetween(00, 60);
        String minutesStr;
        if (minutes < 10){
            minutesStr = "0" + Integer.toString(minutes);
        } else {
            minutesStr = Integer.toString(minutes);
        }
        int seconds = randBetween(00, 60);
        String secondsStr;
        if(seconds < 10){
            secondsStr = "0" + Integer.toString(seconds);
        } else {
            secondsStr = Integer.toString(seconds);
        }

        return year + "-" + monthStr + "-" + dayStr + " " + hourStr + ":" + minutesStr + ":" + secondsStr;
    }



}
