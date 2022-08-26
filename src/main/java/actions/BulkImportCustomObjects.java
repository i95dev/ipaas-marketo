package actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class BulkImportCustomObjects implements Function{
	private static final Logger logger=LoggerFactory.getLogger(BulkImportCustomObjects.class);
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
		JsonString path=body.getJsonString("filepath");
		JsonString format=body.getJsonString("format");
		JsonString apiName=body.getJsonString("apiName");
		if (apiName == null) {
            throw new IllegalStateException("apiName is required");
        }
		String endpoint="/bulk/v1/customobjects/"+apiName.getString()+"/import.json?format="+format.getString();
		HttpPost jobreq=HttpUtils.uploadRequest(configuration, endpoint, path.toString());
		//jobreq.addHeader(HTTP.CONTENT_TYPE, "multipart/form-data");
		try {
			String jobres=HttpUtils.sendRequest(jobreq);
			JsonReader jsonReader = Json.createReader(new StringReader(jobres));
            JsonObject object= jsonReader.readObject();
            jsonReader.close();
            JsonArray arr= object.getJsonArray("result");
            JsonObject obj= arr.getJsonObject(0);
            JsonString batchId=obj.getJsonString("batchId");
            
            String statusEndpoint="/bulk/v1/customobjects/"+apiName.getString()+"/import/"+batchId.getString()+"/status.json" ;
            HttpGet Statusreq=HttpUtils.createGetRequest(configuration, statusEndpoint);
            String Statusres=HttpUtils.sendRequest(Statusreq);
            JsonReader jsonReader1 = Json.createReader(new StringReader(Statusres));
            JsonObject object1= jsonReader1.readObject();
            jsonReader1.close();
            //sendResponse(parameters, Statusres);
            logger.info("received status info",jsonReader1);
            
            JsonArray resultarr= object1.getJsonArray("result");
            JsonObject obj1= resultarr.getJsonObject(0);
            JsonString status=obj1.getJsonString("status");
            JsonString message=obj1.getJsonString("message");
            int fail=obj1.getInt("numOfRowsFailed");
            int warning=obj1.getInt("numOfRowsWithWarning");
            
            String sts1=status.toString();
            if(sts1.equals("Failed")){
				sendResponse(parameters, message.toString());
				return;
			}
            
            while(!sts1.equals("Completed"))  {
            	Thread.sleep(60000);
            	String statusEndpoint1="/bulk/v1/customobjects/"+apiName.getString()+"/import/"+batchId.getString()+"/status.json";
                HttpGet Statusreq1=HttpUtils.createGetRequest(configuration, statusEndpoint1);
                String Statusres1=HttpUtils.sendRequest(Statusreq1);
                JsonReader jsonReader2 = Json.createReader(new StringReader(Statusres1));
                JsonObject object2= jsonReader2.readObject();
                jsonReader2.close();
                JsonArray resultarry= object2.getJsonArray("result");
                JsonObject obj2= resultarry.getJsonObject(0);
                JsonString status1=obj2.getJsonString("status");
                
                String sts2=status1.toString();
                sts1=sts2;
                if(sts1.equals("Failed")){
    				sendResponse(parameters, message.toString());
    				return;
    			}
                }
            if(sts1.equals("Completed")){
            if(fail>0){
            	logger.info("cheking for the no.of rows failed");
            	String failendpoint="/bulk/v1/customobjects/"+apiName.getString()+"/import/"+batchId.getString()+"/failures.json";
            	HttpGet failReq=HttpUtils.createGetRequest(configuration, failendpoint);
            	String failRes=HttpUtils.sendRequest(failReq);
				sendResponse(parameters, failRes);
				
			}
            if(warning>0){
            	logger.info("cheking for the no.of rows with warning");
            	String warningendpoint="/bulk/v1/customobjects/"+apiName.getString()+"/import/"+batchId.getString()+"/warnings.json";
            	HttpGet warningReq=HttpUtils.createGetRequest(configuration, warningendpoint);
            	String warningRes=HttpUtils.sendRequest(warningReq);
				sendResponse(parameters, warningRes);
				
			}
            }
            logger.info("excuted successfully");
            
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		
	}


}
