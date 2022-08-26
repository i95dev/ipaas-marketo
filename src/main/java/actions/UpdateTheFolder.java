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

public class UpdateTheFolder implements Function {
	private static final Logger logger=LoggerFactory.getLogger(UpdateTheFolder.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		
		JsonString folderid=body.getJsonString("id");
		JsonString folderType=body.getJsonString("type");
		JsonString foldername=body.getJsonString("name");
		JsonString description=body.getJsonString("description");
		
		StringBuilder reqBody = new StringBuilder();
		reqBody.append( "type=" + folderType.toString());
		if(description != null){
			reqBody.append("&description=" + description.toString());
		}
		if(foldername != null){
			reqBody.append("&name=" + foldername.toString());
		}
		if(folderid==null) {
			throw new IllegalStateException(" folder Id is required");
		}
			
			String endpoint="/rest/asset/v1/folder/"+folderid.getString()+".json";
		
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
