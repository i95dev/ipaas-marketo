package io.elastic.i95dev.marketo.actions;
import java.io.StringReader;

	import javax.json.Json;
	import javax.json.JsonArray;
	import javax.json.JsonObject;
	import javax.json.JsonObjectBuilder;
	import javax.json.JsonReader;
	import javax.json.JsonString;
	import javax.json.JsonValue;

	import org.apache.http.client.methods.HttpGet;
	import org.apache.http.client.methods.HttpPost;
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import io.elastic.api.ExecutionParameters;
	import io.elastic.api.Function;
	import io.elastic.api.Message;
	import io.elastic.i95dev.marketo.utils.HttpUtils;

	public class BulkActivityExtract implements Function {
		private static final Logger logger=LoggerFactory.getLogger(BulkActivityExtract.class);
		
		public void sendResponse(ExecutionParameters parameters, String output){
			JsonReader Reader = Json.createReader(new StringReader(output));
	                JsonObject outputObject = Reader.readObject();
	                Reader.close();
	                final Message response= new Message.Builder().body(outputObject).build();
			parameters.getEventEmitter().emitData(response);
		}

		@Override
		public void execute(ExecutionParameters parameters) {
			JsonObjectBuilder Status = Json.createObjectBuilder();
					
			final JsonObject configuration = parameters.getConfiguration();
			final JsonObject body = parameters.getMessage().getBody();
			
			String endpoint="/bulk/v1/activities/export/create.json";
			HttpPost post=HttpUtils.createPostObjectRequest(configuration, endpoint, body);
			try {
				String resp=HttpUtils.sendRequest(post);
				
				JsonReader jsonReader = Json.createReader(new StringReader(resp));
	            JsonObject object = jsonReader.readObject();
	            jsonReader.close();
	            JsonArray JobResponse=object.getJsonArray("result");
	            for(int i=0;i<JobResponse.size();i++) {
	            	JsonObject jsonObject1 = JobResponse.getJsonObject(i);
	            	JsonString exportId=jsonObject1.getJsonString("exportId");
	            	
	            	String endpoint2="/bulk/v1/activities/export/"+exportId.getString()+"/enqueue.json";
	            	HttpPost enqueuereq=HttpUtils.createPostRequest(configuration, endpoint2);
	            	String enqueueresponse=HttpUtils.sendRequest(enqueuereq);
	            	
	            
	            	String statusEndpoint="/bulk/v1/activities/export/"+exportId.getString()+"/status.json";
	            	HttpGet statusReq=HttpUtils.createGetRequest(configuration, statusEndpoint);
	            	String StatusResponse=HttpUtils.sendRequest(statusReq);
	            	JsonReader jsonReader1 = Json.createReader(new StringReader(StatusResponse));
	                JsonObject object1 = jsonReader1.readObject();
	                jsonReader1.close();
	                
	                
	                JsonArray arr= object1.getJsonArray("result");
	                JsonObject obj= arr.getJsonObject(0);
	                JsonValue status1=obj.getJsonString("status");
	                String sts=status1.toString();
	            	String status=sts;
			    
			if(status.equals("Failed")){
				sendResponse(parameters, "Export Job status is Failed in Marketo");
				return;
			}
	                
	                
	                
	                
	                while(!status.equals("Completed"))  {
	                	Thread.sleep(60000);
				
				
	                	
	                	String statusEndpoint1="/bulk/v1/activities/export/"+exportId.getString()+"/status.json";
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
				// failed
				if(status.equals("Failed")){
					sendResponse(parameters, "Export Job status is Failed in Marketo");
					return;
				}
	                   }
	                    
			    String  query="/bulk/v1/activities/export/"+exportId.getString()+"/file.json";
	            		HttpGet get=HttpUtils.createGetRequest(configuration, query);
	            		String output=HttpUtils.sendRequest(get);
			  
			    // success
			    sendResponse(parameters, output);
	            		
	            	

	                    logger.info("Finished execution");
	                     }
	                
	                 } catch (Exception e) {
				
				e.printStackTrace();
			}
			


			
		}

	}


