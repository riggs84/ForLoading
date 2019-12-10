import java.util.Scanner;

public class GetJobMainExecutor {

    public static void main(String[] args){
        /*RunnerMock runner = new RunnerMock("http://newui.com");
        for(int i =1; i <= 23; i++){
            String inter = Integer.toString(i);
            runner.sendNewUserQuery("1717", inter, inter, "1", "1", "0", "10.10.10.1", "");
        }*/


        int threadsRequired = 100;
        for(int i = 0; i < threadsRequired; i++){
            new GetJobs().start();
            //new RunOnce().start();
        }
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("we are waiting...");
            scanner.nextLine();
        }
    }
}
