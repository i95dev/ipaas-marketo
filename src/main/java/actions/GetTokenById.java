package actions;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Utils.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class GetTokenById implements Function{
private static final Logger logger=LoggerFactory.getLogger(GetTokenById.class);
	

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body=parameters.getMessage().getBody();
		JsonString Folder=configuration.getJsonString("folderName");
		JsonString id=configuration.getJsonString("Id");
		if (id == null) {
            throw new IllegalStateException(" Id is required");
        }
		String endpoint="/rest/asset/v1/folder/"+id.getString()+"/tokens.json?folderType="+Folder.getString();
		HttpGet req=HttpUtils.createGetRequest(configuration, endpoint);
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
