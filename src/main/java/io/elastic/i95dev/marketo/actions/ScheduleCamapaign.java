package io.elastic.i95dev.marketo.actions;

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

import io.elastic.i95dev.marketo.utils.HttpClientUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class ScheduleCamapaign implements Function {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleCamapaign.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString id = body.getJsonString("ID");
		if (id == null) {
			throw new IllegalStateException(" Id is required");
		}
		JsonString name = body.getJsonString("name");
		JsonString value = body.getJsonString("value");

		JsonString runAt = body.getJsonString("runAt");

		JsonObject dateobject = Json.createObjectBuilder().add("name", name).add("value", value).build();

		JsonArray tokens1 = Json.createArrayBuilder().add(dateobject).build();

		JsonObject requestObj = Json.createObjectBuilder()
				.add("input", Json.createObjectBuilder().add("runAt", runAt).add("tokens", tokens1)).build();

		String endpoint = "/rest/asset/v1/smartCampaign/" + id.getString() + ".json";
		HttpPost req = HttpClientUtils.createPostObjectRequest(configuration, endpoint, requestObj);
		try {
			String res = HttpClientUtils.sendRequest(req);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();

			final Message response = new Message.Builder().body(object).build();

			parameters.getEventEmitter().emitData(response);

			logger.info("Finished execution");
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}