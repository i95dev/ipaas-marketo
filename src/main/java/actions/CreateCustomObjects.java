package actions;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class CreateCustomObjects implements Function{
	private static final Logger logger=LoggerFactory.getLogger(CreateLead.class);
	

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString name=configuration.getJsonString("customObjectName");
		JsonObject bodyobject =  (JsonObject) Json.createObjectBuilder().add("action", "craeteOnly")
				.add("dedupeBy", "dedupeFields").add("input",Json.createArrayBuilder().add(body)).build();
		
		String endpoint="/rest/v1/customobjects/"+name.getString()+".json";
		HttpPost req=HttpUtils.createPostObjectRequest(configuration, endpoint, bodyobject);
		try {
			String res=HttpUtils.sendRequest(req);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
            JsonObject object = jsonReader.readObject();
            jsonReader.close();
            
            final Message response= new Message.Builder().body(object).build();

            parameters.getEventEmitter().emitData(response);

            logger.info("Finished execution");
		} catch (Exception  e) {
			
			e.printStackTrace();
		}
		
		
	}
	
}