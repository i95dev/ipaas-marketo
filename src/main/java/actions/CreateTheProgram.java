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

public class CreateTheProgram implements Function {
	public static final Logger logger=LoggerFactory.getLogger(CreateTheProgram.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		JsonObject configuration=parameters.getConfiguration();
		JsonObject body=parameters.getMessage().getBody();
		JsonString name=body.getJsonString("programName");
		JsonString channel=body.getJsonString("channel");
		JsonString type=body.getJsonString("type");
		JsonString folderType=body.getJsonString("folderType");
		JsonString folderId=body.getJsonString("folderId");
		JsonString description=body.getJsonString("description");
		JsonObject object = Json.createObjectBuilder().add("id",folderId).add("type", folderType).build();
		StringBuilder reqBody = new StringBuilder();
		reqBody.append("name=" + name + "&folder=" + object.toString()+"&type="+type+"&channel="+channel());
			if(description != null){
				reqBody.append("&description=" + description);
			}
			String endpoint="/rest/asset/v1/programs.json";
			HttpPost req=HttpUtils.uploadfileRequest(configuration, endpoint, reqBody.toString());
			try {
				String res=HttpUtils.sendRequest(req);
				JsonReader jsonReader = Json.createReader(new StringReader(res));
	            JsonObject object1= jsonReader.readObject();
	            jsonReader.close();
	            
	            final Message response= new Message.Builder().body(object1).build();

	            parameters.getEventEmitter().emitData(response);

	            logger.info("Finished execution");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		
	}
			

}
