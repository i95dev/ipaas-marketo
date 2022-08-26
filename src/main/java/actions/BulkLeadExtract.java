package actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class BulkLeadExtract implements Function {
	private static final Logger logger=LoggerFactory.getLogger(BulkLeadExtract.class);
	
	public void sendResponse(ExecutionParameters parameters, String output){
		JsonReader Reader = Json.createReader(new StringReader(output));
                JsonObject outputObject = Reader.readObject();
                Reader.close();
                final Message response= new Message.Builder().body(outputObject).build();
		parameters.getEventEmitter().emitData(response);
	}

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString fields=configuration.getJsonString("properties");
		
		JsonString format=body.getJsonString("format");
		JsonString start=body.getJsonString("startAt");
		JsonString end=body.getJsonString("endAt");
		
		JsonObject headersobject = (JsonObject) Json.createObjectBuilder().add("firstName", "First Name").add("lastName", "Last Name")
				.add("id", "Marketo Id").add("email", "Email Address").build();
		JsonObject dateobject = Json.createObjectBuilder().add("startAt", start).add("endAt", end).build();
		JsonObject reobject = Json.createObjectBuilder().add("fields",
                Json.createArrayBuilder().add(fields).build()).add("format", format)
				.add("columnHeaderNames", headersobject).add("filter", Json.createObjectBuilder().add("createdAt", dateobject)).build();
				
		
		
		String endpoint="/bulk/v1/leads/export/create.json";
		HttpPost post=HttpUtils.createPostObjectRequest(configuration, endpoint, reobject);
		try {
			String resp=HttpUtils.sendRequest(post);
			
			JsonReader jsonReader = Json.createReader(new StringReader(resp));
            JsonObject object = jsonReader.readObject();
            jsonReader.close();
            JsonArray JobResponse=object.getJsonArray("result");
            for(int i=0;i<JobResponse.size();i++) {
            	JsonObject jsonObject1 = JobResponse.getJsonObject(i);
            	JsonString exportId=jsonObject1.getJsonString("exportId");
            	
            	String endpoint2="/bulk/v1/leads/export/"+exportId.getString()+"/enqueue.json";
            	HttpPost enqueuereq=HttpUtils.createPostRequest(configuration, endpoint2);
            	String enqueueresponse=HttpUtils.sendRequest(enqueuereq);
            	
            	logger.info("checking the status of the job");
            	String statusEndpoint="/bulk/v1/leads/export/"+exportId.getString()+"/status.json";
            	HttpGet statusReq=HttpUtils.createGetRequest(configuration, statusEndpoint);
            	String StatusResponse=HttpUtils.sendRequest(statusReq);
            	JsonReader jsonReader1 = Json.createReader(new StringReader(StatusResponse));
                JsonObject object1 = jsonReader1.readObject();
                jsonReader1.close();
                
                
                JsonArray arr= object1.getJsonArray("result");
                JsonObject obj= arr.getJsonObject(0);
                JsonValue status1=obj.getJsonString("status");
                JsonString message=obj.getJsonString("message");
                String sts=status1.toString();
            	String status=sts;
                
            	if(status.equals("Failed")){
    				sendResponse(parameters, message.toString());
    				return;
    			}
                
                
                
                while(!status.equals("Completed"))  {
                	Thread.sleep(60000);
                	
                	String statusEndpoint1="/bulk/v1/leads/export/"+exportId.getString()+"/status.json";
                	HttpGet statusReq1=HttpUtils.createGetRequest(configuration, statusEndpoint1);
                	String StatusResponse1=HttpUtils.sendRequest(statusReq1);
                	JsonReader jsonReader2 = Json.createReader(new StringReader(StatusResponse1));
                    JsonObject object2 = jsonReader2.readObject();
                    jsonReader2.close();
                     JsonArray array= object2.getJsonArray("result");
                    JsonObject obj1= array.getJsonObject(0);
                    JsonValue status2=obj.getJsonString("status");
                    String sts1=status1.toString();
                    status=sts1;
                    if(status.equals("Failed")){
    					sendResponse(parameters, message.toString());
    					return;
    				}
                }
                   logger.info("extracting the object");
                    String  query="/bulk/v1/leads/export/"+exportId.getString()+"/file.json";
            		HttpGet get=HttpUtils.createGetRequest(configuration, query);
            		String output=HttpUtils.sendRequest(get);
            		
            		sendResponse(parameters, output);
            		

                    logger.info("Finished execution");
                     }
                
                 
            
            
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		


		
	}

}
