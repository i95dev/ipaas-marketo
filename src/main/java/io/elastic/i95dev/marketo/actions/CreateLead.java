package io.elastic.i95dev.marketo.actions;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elastic.i95dev.marketo.utils.HttpClientUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class CreateLead implements Function{
	private static final Logger logger=LoggerFactory.getLogger(CreateLead.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		String endpoint ="leads.json";
		HttpPost req= HttpClientUtils.createPostObjectRequest(configuration, endpoint, body);
		try {
			String res=HttpClientUtils.sendRequest(req);
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