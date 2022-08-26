package actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class GetLeadActivities implements Function {
	private static final Logger logger=LoggerFactory.getLogger(GetLeadActivities.class);
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
		final Message config=parameters.getMessage();
		final JsonObject body=config.getBody();
		JsonString date=configuration.getJsonString("sinceDatetime");
		JsonString leadId=configuration.getJsonString("LeadId");
		if(date ==null && leadId==null) {
			throw new IllegalStateException(" leadId and date are required ");
		}
		
		String pagingTokenendpoint ="/rest/v1/activities/pagingtoken.json?sinceDatetime="+date.getString();
		HttpGet pagingTokenreq=HttpUtils.createGetRequest(configuration, pagingTokenendpoint);
		try {
		 String pagingTokenresp=HttpUtils.sendRequest(pagingTokenreq);
		 JsonReader jsonReader1 = Json.createReader(new StringReader(pagingTokenresp));
         JsonObject pagingTokenobject = jsonReader1.readObject();
         jsonReader1.close();
         JsonString pageToken=pagingTokenobject.getJsonString("nextPageToken");
         final String endpoint ="/rest/v1/activities.json?nextPageToken="+pageToken.getString()+"&leadIds="+leadId.getString();
		 
		 HttpGet get=HttpUtils.createGetRequest(configuration, endpoint);
		 
			String res = HttpUtils.sendRequest(get);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
            JsonObject object = jsonReader.readObject();
            jsonReader.close();
            logger.info("Receiving the response");
            sendResponse(parameters, res);
            JsonString moreResult=object.getJsonString("moreResult");
            String result=moreResult.toString();
         
            while(result.equalsIgnoreCase("true")) {
            	final String endpoint1 ="/rest/v1/activities.json?nextPageToken="+pageToken.getString()+"&leadIds="+leadId.getString();
       		 
       		 HttpGet get1=HttpUtils.createGetRequest(configuration, endpoint1);
       		 
       			String res1 = HttpUtils.sendRequest(get1);
       			JsonReader Reader1 = Json.createReader(new StringReader(res1));
                   JsonObject object1 = Reader1.readObject();
                   jsonReader.close();
                   sendResponse(parameters, res1);
                   JsonString moreResult1=object1.getJsonString("moreResult");
                   result=moreResult1.toString();
            }
            logger.info("Finished execution");
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		 
	
	
		
		
	}

}
