import java.util.ArrayList;
import java.util.Random;

public class RunOnce extends Thread {
    RunnerMock runner = new RunnerMock("http://newui.com");
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

    @Override
    public void run(){
        boolean newUser = true;
        for (;;){
            if (newUser){
                runner.sendNewUserQuery("2189", "123", getSaltString(), "1", "1", "0",
                        "14.14.14.14", "");
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ArrayList<String> responseArr = new ArrayList<>();
            System.out.println("Wait to assign run once started for thread: " + Thread.currentThread().getId());
                try {
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            while (true){
                System.out.println("Get Run Once request has been sent for thread:" + Thread.currentThread().getId());
                runner.sendGetRunReqQuery(runner.getFromCredsByKey("jobrunnerid"));
                String response = runner.getResponseBody();
                if (response.trim().isEmpty()){
                    break;
                } else {
                    String resp = runner.getResponseBody().trim().split(" ")[1];
                    responseArr.add(resp.trim().split(":")[0]);
                    // parse response here and save into responseArr
                }
            }
            for (int i = 0; i < responseArr.size(); i++){
                String jobrunid;
                runner.sendNewJobRunQuery(runner.getFromCredsByKey("jobrunnerid"), responseArr.get(i), DateGenerator.getNextDate(),
                        "14.14.14.14");
                System.out.println("New job Run query has sent for thread: " + Thread.currentThread().getId());
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    jobrunid = runner.getResponseBody().trim().split("=")[1];
                } catch (Exception ex){
                    System.err.println("jobrunid fail");
                }
            }
            for(;;){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Get job has sent for thread: " + Thread.currentThread().getId());
                runner.sendGetJobsQuery("1", "14.14.14.14", runner.getFromCredsByKey("jobrunnerid"));
            }
        }
    }
}
