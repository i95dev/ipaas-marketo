package actions;

import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.json.Json;
import javax.json.JsonObject;
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

public class CloneTheProgram implements Function {
	private static final Logger logger=LoggerFactory.getLogger(CloneTheProgram.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString name=body.getJsonString("programName");
		JsonString description=body.getJsonString("description");
		JsonString type=body.getJsonString("folderType");
		JsonString id=body.getJsonString("folderId");
		JsonString Programid=body.getJsonString("programId");
		JsonObject object = Json.createObjectBuilder().add("type",type).add("id", id).build();
		StringBuilder reqBody = new StringBuilder();
		reqBody.append("name=" + name.toString() + "&folders=" + object.toString());
			if(description != null){
				reqBody.append("&description=" + description.toString());
			}
		if(id ==null ) {
			throw new IllegalStateException(" program Id is required");
		}
		final String endpoint ="/rest/asset/v1/program/"+Programid.getString()+"/clone.json";
		HttpPost request=HttpUtils.uploadfileRequest(configuration, endpoint, reqBody.toString());
		try {
			String res=HttpUtils.sendRequest(request);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
            JsonObject object1= jsonReader.readObject();
            jsonReader.close();
            
            final Message response= new Message.Builder().body(object1).build();

            parameters.getEventEmitter().emitData(response);

            logger.info("Finished execution");
			
		} catch (KeyManagementException | NoSuchAlgorithmException | AuthorizationException e) {
			
			e.printStackTrace();
		}
		
		
	}

}
