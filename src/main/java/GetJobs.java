import java.util.Random;

public class GetJobs extends Thread {
    private RunnerMock runner = new RunnerMock("http://newui.com");
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
        String jobId = "5594";
        String jobrunid = "";
        System.out.println("The thread " + Thread.currentThread().getId() + " is in run method");
        for(;;){
            if(newUser){
                runner.sendNewUserQuery("2189", "LOAD2", getSaltString(), "1", "Viktor OS", "0",
                        "12.12.12.12", "");
                System.out.println("new user sent in thread " + Thread.currentThread().getId());
                newUser = false;
            }
                {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runner.sendGetJobsQuery("0", "12.12.12.12", runner.getFromCredsByKey("jobrunnerid"));

                }
            try {
                Thread.sleep(300);
                runner.sendNewJobRunQuery(runner.getFromCredsByKey("jobrunnerid"), jobId, DateGenerator.getNextDate(), "13.13.13");
                try {
                    jobrunid = runner.getResponseBody().trim().split("=")[1];
                    System.out.println("jobRunId: " + jobrunid + " -:- " + Thread.currentThread().getId());
                } catch (Exception ex){
                    System.err.println("jobrunid fail");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < 3; i++){
                try {
                    Thread.sleep(300);
                    runner
                            .sendUpdateJobRunQuery("1",
                                    "10", "1", "1", "1", "1", "1111",
                                    jobrunid, runner.getFromCredsByKey("jobrunnerid"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runner.sendFinishJobQuery(runner.getFromCredsByKey("jobrunnerid"), "4", "1", "0",
                    "1", "0", "0", "0", "0", "0", jobrunid);

            System.out.println("Thread cycle is finished " + Thread.currentThread().getId());
        }
    }
}
