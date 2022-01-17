import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
	
	static long startTime = 0;
	
	// multi thread func() 
	CloseableHttpClient httpClient;
    HttpPost httppost;
    String id;
    
    Socket socket = null;

	
    public MCPClient(CloseableHttpClient httpClient, HttpPost httppost, String id) {
    	this.httpClient = httpClient;
    	this.httppost = httppost;
    	this.id = id;
    }
    
   @Override
   public void run() {
	   
	  try{
    	 //Executing the request
    	  startTime = System.currentTimeMillis();
         CloseableHttpResponse httpresponse = httpClient.execute(httppost);
         System.out.println("=======================");
         //Displaying the status of the request.
         System.out.println("get name : "+  getName() + " startTime : " +  startTime );
         System.out.println("status of thread "+id+":"+httpresponse.getStatusLine());

         HttpEntity entity = httpresponse.getEntity();
         if (entity != null) {
            System.out.println("Bytes read by thread thread "+id+": "+EntityUtils.toByteArray(entity).length);
         }
         
         System.out.println(getName() + " 소요 시간 : " + (System.currentTimeMillis() - startTime) );
    	  
      }catch(Exception e) {
    	  System.out.println(e.getMessage());
      }
      
      
   }

	public static void main(String[] args) throws ParseException, InterruptedException, FileNotFoundException, IOException {
		
		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		
	    //Set the maximum number of connections in the pool
	    connManager.setMaxTotal(100);

	    HttpClientBuilder clientbuilder = HttpClients.custom().setConnectionManager(connManager);
	    CloseableHttpClient httpclient = clientbuilder.build();

	    //Creating the HttpGet requests
	    Scanner sc = new Scanner(System.in);
		
		System.out.println("Please enter number");
		System.out.println("(1) weather (2)serverSearch (3)incident (4)traffic");
		System.out.print(">>>> ");
		int number = sc.nextInt();
		String vinNumber = "local_test_";
		
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
	    
	    // 폴더 내 존재하는 파일 크기만큼 반복문 실행 
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
                	
                	vinNumber += idx;
                	
                	// set Header info
                    String tid = UUID.randomUUID().toString();
                    Header[] headers = new Header[]{ new BasicHeader("Content-Type", "application/json; charset=UTF-8"),
                            new BasicHeader("TID", tid),
                            new BasicHeader("VIN", vinNumber), new BasicHeader("Language", "2"),
                            new BasicHeader("Accept", "*/*"),
                            new BasicHeader("Version", "4.4.3"),
                            new BasicHeader("Offset", "8"),
                            new BasicHeader("Telecom" , "Veriizon"),
                            new BasicHeader("Type" , "0"),
                            new BasicHeader("Timeout-Callback", "/timeout/noti"),
                            new BasicHeader("Brand", "H"),
                            new BasicHeader("To", "MCS"),
                            new BasicHeader("Authorization", "Basic dG1zOmhtYWdlbjI="),
                            new BasicHeader("From", "TMU")
                            };
                    
                    
                    httpget1.setHeaders(headers);
                	
                    // read json file 
                	Object ob = new JSONParser().parse(new FileReader(file));
                	JSONObject js = (JSONObject)ob;
                	
                	// set body 
                	//String jsonString = js.toString();
                	HttpEntity httpEntity = new StringEntity(JSONObject.toJSONString(js), "UTF-8");
              
                	httpget1.setEntity(httpEntity);
                
                	thread[idx++] = new MCPClient(httpclient, httpget1, file.getName());
                }
            }
        	
        	//Starting all the threads
            for(int i =0; i<len; i++) {
            	Thread.sleep(10);
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
	}
}
