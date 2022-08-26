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

public class CreateTheFolder implements Function {
	private static final Logger logger=LoggerFactory.getLogger(CreateTheFolder.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		String endpoint ="/rest/asset/v1/folders.json";
		JsonString folderName=body.getJsonString("folderName");
		JsonString folderType=body.getJsonString("folderType");
		JsonString folderId=body.getJsonString("folderId");
		JsonString description=body.getJsonString("description");
		JsonObject jsonobject = Json.createObjectBuilder().add("id",folderId).add("type", folderType).build();
		StringBuilder reqBody = new StringBuilder();
		reqBody.append( "parent=" + jsonobject.toString()+"&name="+folderName.toString());
			if(description != null){
				reqBody.append("&description=" + description.toString());
			}
		HttpPost req= HttpUtils.uploadfileRequest(configuration, endpoint, reqBody.toString());
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
