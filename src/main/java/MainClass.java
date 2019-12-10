import java.util.*;

public class MainClass {
    public static RunnerMock runner;
    public static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

    public void main(String[] args){
        Counters.resetCounters();
        DateGenerator.reset();
        int repeats = 700;
        runner = new RunnerMock("http://testgscc.com");
        runner.sendNewUserQuery("1714", "blabla", "blabla", "1", "1", "0", "11", "");
        ArrayList<Thread> threadsPull = new ArrayList<>();
        for (int i = 0; i < repeats; i++){
            Thread thread = new User2();
            threadsPull.add(thread);
        }
        for (int i = 0; i < repeats; i++){
            threadsPull.get(i).start();
        }
        Counters.printResult();
        //String runnerid = runner.getFromCredsByKey("jobrunnerid");
        /*String[] str = new String[repeats];
        Map<String, String> mapa = new HashMap<>();

        for (int i = 0 ; i < repeats; i++){
            GregorianCalendar gc = new GregorianCalendar();
            int year = randBetween(1900, 2010);
            gc.set(gc.YEAR, year);
            int dayOfYear = randBetween(1, gc.getActualMaximum(gc.DAY_OF_YEAR));
            gc.set(gc.DAY_OF_YEAR, dayOfYear);
            StringBuilder strBld = new StringBuilder();
            strBld.append(gc.get(gc.YEAR)).append("-").append("12").append("-").append(gc.get(gc.DATE))
                    .append(" ");

            String date = strBld.toString() + "06:03:01";
            runner.sendNewJobRunQuery(runnerid, "4848", date, "33");
            System.out.println(" interation: " + i);
            mapa.put(runner.getResponseBody().split("=")[1], runner.getResponseBody().split("=")[1]);
            str[i] = runner.getResponseBody().split("=")[1];
        }

        for (int i = 0; i < repeats; i++){
            int random = new Random().nextInt((1 + 1 - 0) + 0);
            String str1 = null;
            if(random == 1){
                Counters.err++;
                str1 = "0";
            } else {
                Counters.ok++;
                str1 = "1";
            }
            runner.sendFinishJobQuery(runnerid, "7", Integer.toString(random), "0", str1, "0",
                    "0", "1", "1", "1", str[i]);
            System.out.println("finish iteration: " + i);
        }
        Counters.printResult();

        System.out.println("Map size: " + mapa.size());

        /*Counters.resetCounters();
        ArrayList<User2> threads = new ArrayList<>();
        for (int i = 0; i < 100; i++){
            threads.add(new User2());
            threads.get(i).assign();
        }
        System.out.println("Assign runners....");
        try {
            Thread.sleep(500000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Time out is out...");
        for (int i = 0; i < 100; i++){
            threads.get(i).run();
        }
        Counters.printResult();*/





        //User user = new User(args[0]);
        //User user = new User("http://oldsql.com");
        //user.run(args[1], args[2]);
        //user.run("1687", "4546");
        /*for (int i=0; i<=20; i++) {
            user.run("1599", "3577");
        }*/
        //user.run("1599", "3577");

    }
}
