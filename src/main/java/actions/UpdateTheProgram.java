package actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class UpdateTheProgram implements Function {
	public static final Logger logger=LoggerFactory.getLogger(UpdateTheProgram.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		JsonObject configuration=parameters.getConfiguration();
		JsonObject body=parameters.getMessage().getBody();
		JsonString startDate=body.getJsonString("startDate");
		JsonString cost=body.getJsonString("cost");
		JsonString note=body.getJsonString("note");
		JsonString name=body.getJsonString("name");
		JsonString description=body.getJsonString("description");
		JsonString programId=body.getJsonString("programId");
		JsonObjectBuilder object = Json.createObjectBuilder();
		StringBuilder reqBody = new StringBuilder();
		
			if(description != null){
				reqBody.append("&description=" + description.toString());
			}
			if(name != null){
				reqBody.append("&name=" + name.toString());
			}
			if(startDate != null){
				object.add("startDate",startDate).build();
				}
			if(cost != null){
				object.add("cost" , cost).build();
			}
			if(note != null){
				object.add("note" , note).build();
			}
			if (programId == null) {
	            throw new IllegalStateException(" programId is required");
	        }
			JsonArray array=Json.createArrayBuilder().add("object").build();
			reqBody.append("&costs="+array.toString());
			String endpoint="/rest/asset/v1/program/"+programId.getString()+".json";
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
