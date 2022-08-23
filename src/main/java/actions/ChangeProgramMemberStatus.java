package actions;

import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.AuthorizationException;
import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class ChangeProgramMemberStatus implements Function {
	private static final Logger logger=LoggerFactory.getLogger(ChangeProgramMemberStatus.class);
	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		
		JsonString statusName=body.getJsonString("statusName");
		JsonString input=body.getJsonString("input");
		
		JsonObject  LeadIdobject =  (JsonObject) Json.createObjectBuilder().add("leadId", input);
		
		
		JsonObject reqobject = (JsonObject) Json.createObjectBuilder().add("statusName", statusName)
				.add("input",Json.createArrayBuilder().add(LeadIdobject).build());
		
		JsonString programId=configuration.getJsonString("programmemberId");
		if (programId == null) {
            throw new IllegalStateException(" programId is required");
        }
		
		String endpoint="/rest/v1/programs/"+programId.getString()+"/members/status.json";
		HttpPost req=HttpUtils.createPostObjectRequest(configuration, endpoint, reqobject);
		req.addHeader(HTTP.CONTENT_TYPE, "application/json");
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
