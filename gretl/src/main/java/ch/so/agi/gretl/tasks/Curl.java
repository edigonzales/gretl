package ch.so.agi.gretl.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;


// README.md eventuell
// abstract class geht erst mit 5.6 oder so. Nicht mit 5.1.1
// Dann kommen aber viele Warnungen von anderen Tasks wegen fehlendem Getter o.ae.
// Publisher-Ansatz geht nicht, weil dann wird wirklich ein Objekt vom Typ Property erwartet.
// Das ist fuer Anwender doof.

public class Curl extends DefaultTask {
    protected GretlLogger log;

    @Internal
    public String serverUrl;
    
    @Internal
    public MethodType method;
    
    @Internal
    public int expectedStatusCode;
    
    @Internal
    @Optional
    public String expectedBody;
    
    @Internal
    @Optional
    public Map<String,Object> formData; // curl [URL] -F key1=value1 -F file1=@my_file.xtf 
    
    @Internal
    @Optional
    public String data; // curl [URL] -d "key1=value1&key2=value2"
    
    @Internal
    @Optional
    public File outputFile; // curl [URL] -o
    
    @Internal
    @Optional
    public File dataBinary; // curl [URL] --data-binary
    
    @Internal
    @Optional
    public Map<String,String> headers; // curl [URL] -H ... -H ...
    
    @Internal
    @Optional
    public String user;
    
    @Internal
    @Optional
    public String password;
       
    @TaskAction
    public void request() throws ClientProtocolException, IOException {
        log = LogEnvironment.getLogger(Curl.class);

        System.out.println("*********"+serverUrl+"***********");
        System.out.println("*********"+method+"***********");
        System.out.println("*********"+formData+"***********");
        
        RequestBuilder requestBuilder;
        if (method.equals(MethodType.GET)) {
            requestBuilder = RequestBuilder.get();
        } else {
            requestBuilder = RequestBuilder.post();
        }
        
        if (user != null && password != null) {
            Header header = new BasicHeader("Authorization", "Basic "+ Base64.getEncoder().encodeToString((user+":"+password).getBytes()));
            requestBuilder.addHeader(header);
        }

        if (formData != null) {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                if (entry.getValue() instanceof String) {
                    entityBuilder.addTextBody(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof File) {
                    entityBuilder.addBinaryBody(entry.getKey(), (File) entry.getValue());   
                }
            }
            HttpEntity entity = entityBuilder.build();
            requestBuilder.setEntity(entity);
        }

        requestBuilder.setUri(serverUrl);
        
        int responseStatusCode;
        String responseContent;
        HttpUriRequest request = requestBuilder.build();
        try (CloseableHttpClient httpClient = HttpClients.createDefault(); 
                CloseableHttpResponse httpResponse = httpClient.execute(request)) {
          
            responseStatusCode = httpResponse.getStatusLine().getStatusCode();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            responseContent = response.toString();
        } 
        
        if (responseStatusCode != expectedStatusCode) {
            throw new GradleException("Wrong status code returned: " + String.valueOf(responseStatusCode));
        }
        
        if (!responseContent.contains(expectedBody)) {
            throw new GradleException("Response body does not contain expected string: " + responseContent);
        }

    }
    
    private static enum MethodType {
        GET, POST
    }    
}
