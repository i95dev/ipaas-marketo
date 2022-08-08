package actions;

import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.AuthorizationException;
import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class AddLeadToList implements Function{
	private static final Logger logger=LoggerFactory.getLogger(AddLeadToList.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString Id=body.getJsonString("ListID");
		JsonString LeadId=body.getJsonString("LeadId");
		if(Id ==null && LeadId==null) {
			throw new IllegalStateException(" Id is required");
		}
		
		
	    final String endpoint="lists/"+Id.getString()+"/leads.json?id="+LeadId.getString();
	    HttpPost req= HttpUtils.createPostObjectRequest(configuration, endpoint, body);
	    try {
			String res=HttpUtils.sendRequest(req);
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
