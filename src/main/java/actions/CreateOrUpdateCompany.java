package actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class CreateOrUpdateCompany implements Function {
private static final Logger logger=LoggerFactory.getLogger(CreateOrUpdateCompany.class);
	

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final Message config=parameters.getMessage();
		final JsonObject body=config.getBody();
		JsonString id=body.getJsonString("externalCompanyId");
		JsonString name=body.getJsonString("company");
		final String endpoint ="/rest/v1/companies.json";
		JsonObject obj = Json.createObjectBuilder().add("externalCompanyId",id).add("company", name).build();
		JsonObject bodyobject = Json.createObjectBuilder().add("input",Json.createArrayBuilder().add(obj)).build();
		 try {
		 HttpPost post=HttpUtils.createPostObjectRequest(configuration, endpoint, bodyobject);
		    post.addHeader("Content-Type", "application/json");
			String res = HttpUtils.sendRequest(post);
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
