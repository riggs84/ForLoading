import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RunnerMock {
    private String baseURL; // stores server address from config file
    private String responseMessage; // stores response message
    private String responseBody;
    private int responseCode; // stores response code
    private HashMap<String, String> credentials; // keeps user id and pass
    private ArrayList<String> queryParams; // keeps query param as buffer. Used for not to create object every time on every request
    private HashMap<String, String> jobGlobalOptions; // for global options

    public RunnerMock(String url){
        baseURL = url;
        credentials = new HashMap<>();
        queryParams = new ArrayList<>();
    }



    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public boolean isResponseContains(String searchStr){
        return responseBody.contains(searchStr);
    }

    private String encodeStr(String str) throws UnsupportedEncodingException {
        // disabled it as during API testing many symbols converted which is not what i need
        return str;
        //return URLEncoder.encode(str, "UTF-8");
    }

    private void storeTextToMap(String respBody, String regExp, HashMap mapObj){
        String[] tokens = respBody.trim().split(regExp);
        for (int i = 0; i < tokens.length -1;){
            mapObj.put(tokens[i++], tokens[i++]);
        }
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getFromCredsByKey(String key){
        return credentials.get(key);
    }

    private void sendQueryAndReadResponseDigestAuth(ArrayList<String> query, String url) {
        // http://literatejava.com/networks/ignore-ssl-certificate-errors-apache-httpclient-4-4/
        HttpClientBuilder b = HttpClientBuilder.create();
        // setup a Trust Strategy that allows all certificates.
        //
        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        b.setSslcontext(sslContext);
        // don't check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        //      -- and create a Registry, to register it.
        //
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();
        // now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        b.setConnectionManager(connMgr);
        CloseableHttpClient httpClient = b.build();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        for(int i = 0; i < query.size();){
            nvps.add(new BasicNameValuePair(query.get(i++),query.get(i++)));
        }
        DigestScheme digestAuth = new DigestScheme();
        digestAuth.overrideParamter("algorithm", "MD5");
        digestAuth.overrideParamter("realm", "JobServer");
        digestAuth.overrideParamter("nonce", "4017397a3051e9d385ec218831e11a17");
        digestAuth.overrideParamter("qop", "auth");
        digestAuth.overrideParamter("opaque", "3188f230b037557f2e6d68fde5c0e561");

        Header auth = null;
        try {
            auth = digestAuth.authenticate(new
                    UsernamePasswordCredentials(getFromCredsByKey("jobrunnerid"), getFromCredsByKey("password")), httpPost);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        /*System.out.println(auth.getName());
        System.out.println(auth.getValue());*/
        httpPost.setHeader(auth);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CloseableHttpResponse response2 = null;
        try {
            response2 = httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //System.out.println(response2.getStatusLine());
            HttpEntity entity2 = response2.getEntity();
            InputStream input = entity2.getContent();
            BufferedReader br;
            StringBuilder sb = new StringBuilder();
            String line;
            br = new BufferedReader(new InputStreamReader(input));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            responseBody = sb.toString();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity2);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response2.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendQueryAndReadResponse(String query, String url) {
        try {
            /*String credentials = credentials.get("jobrunnerid") + ":" + credentials.get("password");
            String encodedCreds = Base64.getEncoder().encodeToString(credentials.getBytes("UTF-8"));*/

            URL myURL = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
            //HttpsURLConnection conn = (HttpsURLConnection) myURL.openConnection();
            //conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-length", String.valueOf(query.length()));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            //conn.setRequestProperty("Authorization", "Basic " + encodedCreds);
            //conn.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); //this is for bad cert problem

            DataOutputStream output = new DataOutputStream(conn.getOutputStream());
            DataInputStream input = null;
            output.writeBytes(query);
            output.close();
            conn.connect();

            input = new DataInputStream(conn.getInputStream());
            StringBuilder strBld = new StringBuilder();
            for(int c = input.read(); c != -1; c = input.read())
                //System.out.print( (char)c );
                strBld.append((char)c);
            input.close();

            // save response data
            responseBody = strBld.toString().trim();
            responseCode = conn.getResponseCode();
            responseMessage = conn.getResponseMessage().trim();

            // place response params into hash map
            storeTextToMap(responseBody,"&|=" , credentials);

            //System.out.println(conn.getContentType());

            //System.out.println("Resp Code:"+conn.getResponseCode());
            //System.out.println("Resp Message:"+ conn.getResponseMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void sendFinishJobQuery(String jobrunnerid, String jobStatus, String jobReturnCode, String terminalErr,
                                   String synced_ok, String synced_err, String synced_conflicts,
                                   String elapsedTime, String speedAverage,String byteProcessed,
                                   String jobRunId){
        String url = baseURL + "/api/finish-job-run.html";
        String query = null;
        try {
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(jobrunnerid));
            queryParams.add("jobstatus");
            queryParams.add(encodeStr(jobStatus));
            queryParams.add("rc_int");
            queryParams.add(encodeStr(jobReturnCode));
            queryParams.add("termerr");
            queryParams.add(encodeStr(terminalErr));
            queryParams.add("synced_ok");
            queryParams.add(encodeStr(synced_ok));
            queryParams.add("synced_err");
            queryParams.add(encodeStr(synced_err));
            queryParams.add("synced_conf");
            queryParams.add(encodeStr(synced_conflicts));
            queryParams.add("elapsed");
            queryParams.add(encodeStr(elapsedTime));
            queryParams.add("speedave");
            queryParams.add(encodeStr(speedAverage));
            queryParams.add("bytesproc");
            queryParams.add(encodeStr(byteProcessed));
            queryParams.add("jobrunid");
            queryParams.add(encodeStr(jobRunId));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    //this is register new runner query
    public void sendNewUserQuery(String companyId, String userName, String computerName, String platform,
                                 String osInfo, String serverOs, String gsver, String pin){
        String url = baseURL + "/api/new-user.html";
        String query = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder
                    .append("companyid=").append(encodeStr(companyId)).append("&")
                    .append("username=").append(encodeStr(userName)).append("&")
                    .append("computername=").append(encodeStr(computerName)).append("&")
                    .append("platform=").append(encodeStr(platform)).append("&")
                    .append("osinfo=").append(encodeStr(osInfo)).append("&")
                    .append("serveros=").append(encodeStr(serverOs)).append("&")
                    .append("gsver=").append(encodeStr(gsver)).append("&")
                    .append("companypin=").append(encodeStr(pin));
            query = builder.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponse(query, url);
    }

    public void sendAuthRunner(String runnerid){
        String url = baseURL + "/test/set-authorize.html";
        try {
            queryParams.add("id");
            queryParams.add(encodeStr(runnerid));
        } catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
            System.out.println("send get jobs");
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendAssignJobToRunner(String runnerid, String companyId, String jobId, String assignType){
        String url = baseURL + "/test/assign-job.html";
        try {
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(runnerid));
            queryParams.add("companyid");
            queryParams.add(encodeStr(companyId));
            queryParams.add("jobid");
            queryParams.add(encodeStr(jobId));
            queryParams.add("assigntype");
            queryParams.add(encodeStr(assignType));
        } catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    //TODO it might require force param which is not implemented
    public void sendGetJobsQuery(String serveros, String gsver, String jobrunnerid){
        String url = baseURL + "/api/get-jobs.html";
        try {
            queryParams.add("serveros");
            queryParams.add(encodeStr(serveros));
            queryParams.add("gsver");
            queryParams.add(encodeStr(gsver));
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(jobrunnerid));
        } catch (UnsupportedEncodingException ex){
            ex.printStackTrace();
            System.out.println("send get jobs");
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendGetRunReqQuery(String jobrunnerid){
        String url = baseURL + "/api/get-run-req.html";
        try {
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(jobrunnerid));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendNewJobRunQuery(String jobrunnerid, String jobid, String when_started, String gsver){
        String url = baseURL + "/api/new-job-run.html";
        try {
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(jobrunnerid));
            queryParams.add("jobid");
            queryParams.add(encodeStr(jobid));
            queryParams.add("when_started");
            queryParams.add(encodeStr(when_started));
            queryParams.add("gsver");
            queryParams.add(encodeStr(gsver));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendUpdateJobRunQuery(String jobstatus, String pct_complete, String elapsed, String speedave,
                                      String speedins, String bytesproc, String timeremsec, String jobrunid, String jobrunnerid){
        String url = baseURL + "/api/update-job-run.html";
        try {
            queryParams.add("jobstatus");
            queryParams.add(encodeStr(jobstatus));
            queryParams.add("pct_complete");
            queryParams.add(encodeStr(pct_complete));
            queryParams.add("elapsed");
            queryParams.add(encodeStr(elapsed));
            queryParams.add("speedave");
            queryParams.add(encodeStr(speedave));
            queryParams.add("speedins");
            queryParams.add(encodeStr(speedins));
            queryParams.add("bytesproc");
            queryParams.add((encodeStr(bytesproc)));
            queryParams.add("timeremsec");
            queryParams.add(encodeStr(timeremsec));
            queryParams.add("jobrunid");
            queryParams.add(encodeStr(jobrunid));
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(jobrunnerid));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendUpdateLog(String logline, String lines, String jobrunid, String jobrunnerid){
        String url = baseURL + "/api/update-lof.html";
        try {
            queryParams.add("logline");
            queryParams.add(encodeStr(logline));
            queryParams.add("lines");
            queryParams.add(encodeStr(lines));
            queryParams.add("jobrunid");
            queryParams.add(encodeStr(jobrunid));
            queryParams.add("jobrunnerid");
            queryParams.add(encodeStr(jobrunnerid));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendUploadAccountsQuery(String adminEmail, String adminPass, String accounts, String accountsPlain,
                                        String foldersPlain, String gsver){
        String url = baseURL + "/api/upload-accounts.html";
        try {
            queryParams.add("adminemail");
            queryParams.add(encodeStr(adminEmail));
            queryParams.add("adminpwd");
            queryParams.add(encodeStr(adminPass));
            queryParams.add("accounts");
            queryParams.add(encodeStr(accounts));
            queryParams.add("acctsplain");
            queryParams.add(encodeStr(accountsPlain));
            queryParams.add("foldersplain");
            queryParams.add(encodeStr(foldersPlain));
            queryParams.add("gsver");
            queryParams.add(encodeStr(gsver));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }

    public void sendUploadJobsQuery(String adminEmail, String adminPass, String jobName, String joblk,
                                    String jobrk, String joblu, String jobru, String jobopt, String gsver){
        String url = baseURL + "/api/upload-job.html";
        try {
            queryParams.add("adminemail");
            queryParams.add(encodeStr(adminEmail));
            queryParams.add("adminpwd");
            queryParams.add(encodeStr(adminPass));
            queryParams.add("jobname");
            queryParams.add(encodeStr(jobName));
            queryParams.add("joblk");
            queryParams.add(encodeStr(joblk));
            queryParams.add("jobrk");
            queryParams.add(encodeStr(jobrk));
            queryParams.add("joblu");
            queryParams.add(encodeStr(joblu));
            queryParams.add("jobru");
            queryParams.add(encodeStr(jobru));
            queryParams.add("jobopt");
            queryParams.add(encodeStr(jobopt));
            queryParams.add("gsver");
            queryParams.add(encodeStr(gsver));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendQueryAndReadResponseDigestAuth(queryParams, url);
        queryParams.clear();
    }


}


