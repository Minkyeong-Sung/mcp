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
	
	private String wt = "mcs/weather/point/ver2";
	private String search = "mcs/ServerSearch";
	private String inc = "mcs/incident";
	private String trf ="mcs/traffic";
	
	private String wt_file = "weather";
	private String search_file = "ServerSearch";
	private String incident_file = "incident";
	private String traffic_file = "traffic";
	

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		

		new MCPClient().mcpClientService();

	}

	private void mcpClientService() throws ParseException {
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Please enter number");
		System.out.println("(1) weather (2)serverSearch (3)incident (4)traffic");
		System.out.print(">>>> ");
		int number = sc.nextInt();
		String vinNumber = "LOCAL_TEST_";
		
		String https_url = "http://10.107.66.41:8081/";
		String filePath = "C:/GlobalMCTClient/";
		
		switch(number) {
			case 1:
				https_url += wt;
				filePath += wt_file;
				vinNumber += wt_file;
				break;
			case 2:
				https_url += search;
				filePath += search_file;
				vinNumber += search_file;
				break;
			case 3:
				https_url += inc;
				filePath += incident_file;
				vinNumber += incident_file;
				break;
			case 4:
				https_url += trf;
				filePath += traffic_file;
				vinNumber += traffic_file;
				break;
		}
		
		
		HttpPost request = new HttpPost(https_url);
        String msg = null;
        int statusCode = 0;
        HttpEntity respEntity = null;
        
        // read body from json file  
        File path = new File(filePath);
        File[] fileList = path.listFiles();

        if(fileList.length >0){
        	
        	try {
                for(File file : fileList){

                    HttpClient client = HttpClientBuilder.create().build();
                	// Header
                    String tid = UUID.randomUUID().toString();
                    Header[] headers = new Header[]{ new BasicHeader("Content-Type", "application/json"),
                            new BasicHeader("TID", tid),
                            new BasicHeader("VIN", vinNumber), new BasicHeader("Language", "2") };

                	request.setHeaders(headers);

                    // json �����̰�, ���� �� ������ ����
                    if(file.getName().endsWith(".json") && file.canRead()){
                    	
                    	System.out.println("=================================");
                    	System.out.println("test file : " + file.getName());
                    	
                    	// start time 
                    	long start = System.currentTimeMillis();

                    	
                    	// read json file 
                    	Object ob = new JSONParser().parse(new FileReader(file));
                    	JSONObject js = (JSONObject)ob;
                    	
                    	// set body 
                    	//String jsonString = js.toString();
                    	HttpEntity httpEntity = new StringEntity(JSONObject.toJSONString(js), "utf-8");
                    	request.setEntity(httpEntity);
            
                        // response
                    	HttpResponse response = client.execute(request);
                        
                        statusCode = response.getStatusLine().getStatusCode();
                        
                        // end time 
                        long end = System.currentTimeMillis();
                        
                        
                        if(statusCode != HttpStatus.SC_OK) {
                        	
                        	System.out.print("status : " + statusCode  + " error | " );
                        	
//                            	switch(statusCode) {
//    	                        	case 404:
//    	                        		msg = "404 error : [";
//    	                        		break;
//    	                        	case 503:
//    	                        		msg = "503 error : [";
//    	                        		break;	
//                            	}
                        }
                        else {
                        	
                        	System.out.print("status : " + statusCode + " | " );
                        	
                        	respEntity = response.getEntity();

//                            	System.out.println("respEntity : " + respEntity);
//                            	System.out.println("ReasonPhrase : " + response.getStatusLine().getReasonPhrase());
//                            	
//                            	String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//                                System.out.println("Response body: " + responseBody);
                       }
                        
                    	System.out.println(" time : " + (end-start) );

                        // ��ȣȭ decompress

                    }
                }
	        } catch (IOException e) {
	        	System.out.println("try-catch error :" + e.getMessage());
	            e.printStackTrace();
	            
	        }

        }
        else{
            System.out.println("No json files... FAIL ");
        }

    }
}
