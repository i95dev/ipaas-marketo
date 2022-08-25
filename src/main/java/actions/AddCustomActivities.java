package actions;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
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

public class AddCustomActivities implements Function {
	private static final Logger logger=LoggerFactory.getLogger(AddCustomActivities.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body=parameters.getMessage().getBody();
		JsonString id=body.getJsonString("leadId");
		JsonString date=body.getJsonString("activityDate");
		JsonString typeId=body.getJsonString("activityTypeId");
		JsonString attribute=body.getJsonString("primaryAttributeValue");
		JsonString name=body.getJsonString("apiName");
		JsonString value=body.getJsonString("value");
		JsonObject attrObj=Json.createObjectBuilder().add("apiName",name).add("value", value).build();
		JsonArray arr=Json.createArrayBuilder().add(attrObj).build();
		JsonObject obj=Json.createObjectBuilder().add("leadId", id).add("activityDate", date).add("activityTypeId", typeId)
				.add("primaryAttributeValue",attribute).add("attributes", arr).build();
         String endpoint="/rest/v1/activities/external.json";
         HttpPost req=HttpUtils.createPostObjectRequest(configuration, endpoint, obj);
         try {
			String res=HttpUtils.sendRequest(req);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
            JsonObject object = jsonReader.readObject();
            jsonReader.close();
            
            
            final Message response= new Message.Builder().body(object).build();

    parameters.getEventEmitter().emitData(response);

    logger.info("Finished execution");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         

		
	}

}
