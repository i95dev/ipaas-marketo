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

public class TriggerCampaign implements Function {
	private static final Logger logger = LoggerFactory.getLogger(TriggerCampaign.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString CampaignId = body.getJsonString("ID");
		if (CampaignId == null) {
			throw new IllegalStateException("Campaign Id is required");
		}
		JsonString name = body.getJsonString("name");
		JsonString value = body.getJsonString("value");

		JsonString id = body.getJsonString("id");

		JsonObject dateobject = Json.createObjectBuilder().add("id", id).build();
		JsonArray leads1 = Json.createArrayBuilder().add(dateobject).build();
		JsonObject tokens = Json.createObjectBuilder().add("name", name).add("value", value).build();
		JsonArray leads11 = Json.createArrayBuilder().add(tokens).build();

		JsonObject requestObj = (JsonObject) Json.createObjectBuilder()
				.add("input", Json.createObjectBuilder().add("leads", leads1).add("tokens", leads11)).build();

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