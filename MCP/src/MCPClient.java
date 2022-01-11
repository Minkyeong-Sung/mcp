import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.swing.text.AbstractDocument.Content;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
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
public class MCPClient extends Thread{
	
	private static String wt = "mcs/weather/point/ver2";
	private static String search = "mcs/ServerSearch";
	private static String inc = "mcs/incident";
	private static String trf ="mcs/traffic";
	
	private static String wt_file = "weather";
	private static String search_file = "ServerSearch";
	private static String incident_file = "incident";
	private static String traffic_file = "traffic";
	
	// multi thread func() 
	CloseableHttpClient httpClient;
    HttpPost httppost;
    String id;
	
    public MCPClient(CloseableHttpClient httpClient, HttpPost httppost, String id) {
    	this.httpClient = httpClient;
    	this.httppost = httppost;
    	this.id = id;
    }
    
   @Override
   public void run() {
      try{
         //Executing the request
         CloseableHttpResponse httpresponse = httpClient.execute(httppost);

         //Displaying the status of the request.
         System.out.println("status of thread "+id+":"+httpresponse.getStatusLine());

         //Retrieving the HttpEntity and displaying the no.of bytes read
         HttpEntity entity = httpresponse.getEntity();
         if (entity != null) {
            System.out.println("Bytes read by thread thread "+id+": "+EntityUtils.toByteArray(entity).length);
         }
      }catch(Exception e) {
         System.out.println(e.getMessage());
      }
   }

	public static void main(String[] args) throws ParseException, InterruptedException, FileNotFoundException, IOException {
		
		//Creating the Client Connection Pool Manager by instantiating the PoolingHttpClientConnectionManager class.
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

	    //Set the maximum number of connections in the pool
	    connManager.setMaxTotal(100);

	    //Create a ClientBuilder Object by setting the connection manager
	    HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);
	 
	    //Build the CloseableHttpClient object using the build() method.
	    CloseableHttpClient httpclient = clientbuilder.build();

	    //Creating the HttpGet requests
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
	    HttpPost httpget1 = new HttpPost(https_url);
	    
	    // 반복문 실행 
	    // 폴더 내 존재하는 파일 크기만큼 반복문 실행 
	 // read body from json file  
        File path = new File(filePath);
        File[] fileList = path.listFiles();
        
        int len = fileList.length;
        MCPClient[] thread = new MCPClient[len];
        int idx = 0;
        
        if(fileList.length >0){
        	
        	for(File file : fileList){
            	// json 파일이고, 읽을 수 있으면 실행
                if(file.getName().endsWith(".json") && file.canRead()){
                	
                	HttpClient client = HttpClientBuilder.create().build();
                	
                	// set Header info
                    String tid = UUID.randomUUID().toString();
                    Header[] headers = new Header[]{ new BasicHeader("Content-Type", "application/json; charset=UTF-8"),
                            new BasicHeader("TID", tid),
                            new BasicHeader("VIN", vinNumber), new BasicHeader("Language", "2") };
                    
                    httpget1.setHeaders(headers);
                	
                    // read json file 
                	Object ob = new JSONParser().parse(new FileReader(file));
                	JSONObject js = (JSONObject)ob;
                	
                	// set body 
                	//String jsonString = js.toString();
                	HttpEntity httpEntity = new StringEntity(JSONObject.toJSONString(js), "UTF-8");
              
                	httpget1.setEntity(httpEntity);
                	
                	thread[idx] = new MCPClient(httpclient, httpget1, file.getName());
        
                    // response
                	//HttpResponse response = client.execute(request);
                	idx ++;
                }
            }
        	
        	//Starting all the threads
            for(int i =0; i<len; i++) {
            	thread[i].start();
            }
            
            for(int i =0; i<len; i++) {
            	thread[i].join();
            }
        }
        else{
        	System.out.println("=================================");
            System.out.println("No json files... FAIL ");
        }

        System.out.println("=================================");
        System.out.println("finished program...");
	    
	    
	    //Thread 생성 
//	    MCPClient thread1 = new MCPClient(httpclient, httpget1, file.getName());
//	    MCPClient thread4 = new MCPClient(httpclient, httpget1, file.getName());
	    
	    
	}

	
	
//	private void mcpClientService() throws ParseException {
//		
//		HttpPost request = new HttpPost(https_url);
//        String msg = null;
//        int statusCode = 0;
//        HttpEntity respEntity = null;
//        
//        // read body from json file  
//        File path = new File(filePath);
//        File[] fileList = path.listFiles();
//
//        if(fileList.length >0){
//        	
//        	try {
//                for(File file : fileList){
//
//                    HttpClient client = HttpClientBuilder.create().build();
//                	// Header
//                    String tid = UUID.randomUUID().toString();
//                    Header[] headers = new Header[]{ new BasicHeader("Content-Type", "application/json; charset=UTF-8"),
//                            new BasicHeader("TID", tid),
//                            new BasicHeader("VIN", vinNumber), new BasicHeader("Language", "2") };
//                    
//                	request.setHeaders(headers);
//
//                    // json 파일이고, 읽을 수 있으면 실행
//                    if(file.getName().endsWith(".json") && file.canRead()){
//                    	
//                    	System.out.println("=================================");
//                    	System.out.println("test file : " + file.getName());
//                    	
//                    	// start time 
//                    	long start = System.currentTimeMillis();
//
//                    	
//                    	// read json file 
//                    	Object ob = new JSONParser().parse(new FileReader(file));
//                    	JSONObject js = (JSONObject)ob;
//                    	
//                    	// set body 
//                    	//String jsonString = js.toString();
//                    	HttpEntity httpEntity = new StringEntity(JSONObject.toJSONString(js), "UTF-8");
//                  
//                    	request.setEntity(httpEntity);
//                    	
//            
//                        // response
//                    	HttpResponse response = client.execute(request);
//                    			
//                    	response.addHeader("Content-Type", "application/json; charset=UTF-8");
//                    	
//                    	
//                    	//HttpEntity entity = response.getEntity();
//                    	HttpEntity entity = MultipartEntityBuilder
//                    			.create()
//                    			.setCharset(StandardCharsets.UTF_8)
//                    			.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//                    			.addBinaryBody("file", JSONObject.toJSONString(js).getBytes(StandardCharsets.UTF_8),
//                    					ContentType.create(ContentType.TEXT_PLAIN.getMimeType(), StandardCharsets.UTF_8),file.getName()).build(); 
//                    	
//                    	String responseString = EntityUtils.toString(entity, "UTF-8");
////                    	System.out.println(">>> "+responseString);
//                    	
//                        statusCode = response.getStatusLine().getStatusCode();
//                        
//                        response.setEntity(httpEntity);
//                        // end time 
//                        long end = System.currentTimeMillis();
//                        
//                        
//                        if(statusCode != HttpStatus.SC_OK) {
//                        	
//                        	System.out.println("status : " + statusCode  + " error | " );
//
//                        }
//                        else {
//                        	
//                        	System.out.println("status : " + statusCode + " | " );
//                        	
////                        	response.setHeader("Content-Type", "text/plain; charset=UTF-8");
//                        	respEntity = response.getEntity();
////                        	System.out.println("entity.getContent : " + entity.getContent());
////                        	System.out.println("response.getEntity().getContent : " + response.getEntity().getContent().toString() );
//                        	String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
////                            System.out.println("Response body: " + responseBody);
//                       }
//                        
//                    	System.out.println(" time : " + (end-start) );
//
//                        // 복호화 decompress
//
//                    }
//                }
//	        } catch (IOException e) {
//	        	System.out.println("try-catch error :" + e.getMessage());
//	            e.printStackTrace();
//	            
//	        }
//
//        }
//        else{
//        	System.out.println("=================================");
//            System.out.println("No json files... FAIL ");
//        }
//
//        System.out.println("=================================");
//        System.out.println("finished program...");
//    }
}
