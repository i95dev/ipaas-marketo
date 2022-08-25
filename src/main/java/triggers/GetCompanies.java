package triggers;

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

public class GetCompanies implements Function  {
private static final Logger logger=LoggerFactory.getLogger(GetCompanies.class);
	

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final Message config=parameters.getMessage();
		final JsonObject body=config.getBody();
		JsonString type=configuration.getJsonString("type");
		JsonString value=configuration.getJsonString("value");
		final String endpoint ="/rest/v1/companies.json?filterType="+type.getString()+"&filterValues="+value.getString();
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
