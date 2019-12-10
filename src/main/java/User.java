import java.util.Random;

public class User extends Thread{
    private RunnerMock runner;
    String runnerId;
    String jobid;
    String compid;

    public User(String url, String companyID, String jobID){
        this.runner = new RunnerMock(url);
        this.jobid = jobID;
        this.compid = companyID;
    }

    private void registerRunner(){
        runner.sendNewUserQuery(compid, getSaltString(), getSaltString(), "1", "1", "0",
                "10.10.10", "");
        runnerId = runner.getFromCredsByKey("jobrunnerid");

    }

    private void startNewJobRun(){
        for (;;) {
            // we create new job run and then go to inner cycle to simulate job update
            runner.sendNewJobRunQuery(runnerId, jobid, "1", "10.10.10");
            System.out.println("new job run request is sent in thread: " + Thread.currentThread().getName());
            //System.out.println(runner.getResponseBody());
            String jobrunid;
            try {
                jobrunid = runner.getResponseBody().split("=")[1]; // getting job run id
            } catch (Exception ex) {
                throw new IllegalArgumentException("jobrunid is empty");
            }
            for (;;){
                if (jobrunid.isEmpty()) {
                    throw new IllegalStateException("jobrunid is empty");
                }
                runner.sendUpdateJobRunQuery("1", "1", "1", "1", "1",
                        "1", "1", jobrunid, runnerId);
                System.out.println("job run update request sent in thread: " + Thread.currentThread().getName());
                int ranSec = (int)(Math.random() * ((1500 - 200) + 1) + 200);
                try {
                    // 200 mil to 1500 mil
                    Thread.sleep(ranSec);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
                if ((new Random().nextInt(15 - 1) + 1) == 5) {
                    sendGetJobs();
                }
                if ((new Random().nextInt(20 - 1) + 1) == 5) {
                    runner.sendNewUserQuery(compid, getSaltString(), getSaltString(), "2", "2", "0",
                            "10.10.10", "");
                }
                if ((new Random().nextInt(20 - 1) + 1) == 5) {
                    runner.sendNewUserQuery(compid, getSaltString(), getSaltString(), "2", "2", "0",
                            "10.6.1.7", "");
                    runner.sendGetJobsQuery("1", "10.10.10", "11111111");
                }

            }
        }

    }

    private void assignJobToRunner(){
        runner.sendAssignJobToRunner(runnerId, compid, jobid, "1");
        System.out.println("job is assigned to runner in thread: " + Thread.currentThread().getName());
    }

    private void sendGetJobs(){
        for (;;){
            runner.sendGetJobsQuery("0", "10.10.10", runnerId);
            System.out.println("Get jobs request sent in thread: " + Thread.currentThread().getName());
            runner.sendGetRunReqQuery(runnerId);
            System.out.println("Run req request sent in thread: " + Thread.currentThread().getName());
            //int ranSec = (int)(Math.random() * ((5000 - 1000) + 1) + 1000);
            int ranSec = (int)(Math.random() * ((1500 - 200) + 1) + 200);
            try {
                Thread.sleep(ranSec);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    // main method
    public void run() {
        System.out.println("Thread started: " + Thread.currentThread().getName());
        registerRunner();
        runner.sendAuthRunner(runnerId);
        System.out.println("auth runner request is sent in thread: " + Thread.currentThread().getName());
        assignJobToRunner();
        startNewJobRun();
        }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
}

