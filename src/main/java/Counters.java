public class Counters {
    public static int ok = 0;
    public static int err = 0;


    public static void resetCounters(){
        ok = 0;
        err = 0;
    }

    public static void printResult(){
        System.out.println("ok: " + ok);
        System.out.println("Err: " + err);
    }
}
