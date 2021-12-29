import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.UUID;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("deprecation")
public class MCPClient {

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		new MCPClient().mcpClientService();

	}

	private void mcpClientService() throws ParseException {
		
		String https_url = "http://10.107.66.41:8081/mcs/ServerSearch";
		HttpPost request = new HttpPost(https_url);
        HttpClient client = HttpClientBuilder.create().build();
        String msg = null;
        int statusCode = 0;
        HttpEntity respEntity = null;

        try {
            // Header
            String tid = UUID.randomUUID().toString();
            Header[] headers = new Header[]{ new BasicHeader("Content-Type", "application/json"),
                    new BasicHeader("TID", tid),
                    new BasicHeader("VIN", "SRCH_LOCAL_TEST1"), new BasicHeader("Language", "2") };

            request.setHeaders(headers);
            
            
            // read body from json file  
            File path = new File("C:/GlobalMCTClient/");
            File[] fileList = path.listFiles();

            if(fileList.length >0){

                int sucessCnt = 0;
                int failCnt = 0;
                long beforeTime = System.currentTimeMillis();

                for(File file : fileList){

                    sucessCnt = 0;
                    failCnt = 0;

                    // json 파일이고, 읽을 수 있으면 실행
                    if(file.getName().endsWith(".json") && file.canRead()){
                    	
                    	// read json file 
                    	Object ob = new JSONParser().parse(new FileReader(file));
                    	JSONObject js = (JSONObject)ob;
                    	
                    	// set body 
                    	//String jsonString = js.toString();
                    	HttpEntity httpEntity = new StringEntity(JSONObject.toJSONString(js), "utf-8");
                    	request.setEntity(httpEntity);
            
                        // response
                    	HttpResponse response = client.execute(request);

                        // 결과 timer
                        Runtime rt = Runtime.getRuntime();

                        
                        statusCode = response.getStatusLine().getStatusCode();
                        
                        if(statusCode != HttpStatus.SC_OK) {
                        	
                        	switch(statusCode) {
	                        	case 404:
	                        		msg = "404 error : [";
	                        		break;
	                        	case 503:
	                        		msg = "503 error : [";
	                        		break;
	                        		
                        	}
                        }
                        else {
                        	respEntity = response.getEntity();
                        	System.out.println("respEntity : " + respEntity);
                        	System.out.println("ReasonPhrase : " + response.getStatusLine().getReasonPhrase());
                        	
                        	String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.name());
                            System.out.println("Response body: " + responseBody);
                        	

                        }
                        // 복호화 decompress

                    }
                }

//                long afterTime = System.currentTimeMillis();
//                long secDiffTime = (afterTime - beforeTime)/1000;
//                System.out.println("시간차이(m) : "+secDiffTime);
//                System.out.println( "번 " + secDiffTime +  " Time(m) 요청 " + fileList.length + "| 응답 " + sucessCnt + " | 실패 "  );
            }
            else{
                System.out.println("No json files... FAIL ");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
