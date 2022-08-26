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

public class SendSampleEmail implements Function {
	public static final Logger logger=LoggerFactory.getLogger(SendSampleEmail.class);
	@Override
	public void execute(ExecutionParameters parameters) {
		JsonObject configuration=parameters.getConfiguration();
		JsonObject body=parameters.getMessage().getBody();
		JsonString emailAddress=body.getJsonString("emailAddress");
		JsonString textOnly=body.getJsonString("textOnly");
		JsonString id=body.getJsonString("EmailId");
		if (id == null) {
            throw new IllegalStateException(" Email Id is required");
        }
		StringBuilder reqBody = new StringBuilder();
		reqBody.append("emailAddress=" + emailAddress);
		if (textOnly!=null) {
			reqBody.append("&textOnly="+textOnly);
            
        }
		String endpoint="/rest/asset/v1/email/"+id.getString()+"/sendSample.json";
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
