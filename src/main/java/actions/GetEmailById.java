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

public class GetEmailById implements Function{
	private static final Logger logger=LoggerFactory.getLogger(GetEmailById.class);
	

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final Message config=parameters.getMessage();
		final JsonObject body=config.getBody();
		
		JsonString id=body.getJsonString("ID");
		
		if (id == null) {
            throw new IllegalStateException(" Id is required");
        }
		
		 final String endpoint ="/rest/asset/v1/email/"+id.getString()+".json";
		 try {
		 HttpGet get=HttpUtils.createGetRequest(configuration, endpoint);
		 
			String res = HttpUtils.sendRequest(get);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
            JsonObject object = jsonReader.readObject();
            jsonReader.close();
            
            
            final Message response= new Message.Builder().body(object).build();

    parameters.getEventEmitter().emitData(response);

    logger.info("Finished execution");
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		 
	
	
		
		
	}


}
