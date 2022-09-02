package io.elastic.i95dev.marketo.triggers;

import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elastic.i95dev.marketo.utils.HttpClientUtils;
import io.elastic.sailor.impl.HttpUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class GetCampaignsByID implements Function {
	private static final Logger logger = LoggerFactory.getLogger(GetCampaignsByID.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final Message config = parameters.getMessage();
		final JsonObject body = config.getBody();

		JsonString id = configuration.getJsonString("id");

		if (id == null) {
			throw new IllegalStateException(" id is required");
		}

		final String endpoint = "/rest/asset/v1/smartCampaign/" + id.getString() + ".json";
		try {
			HttpGet get = HttpClientUtils.createGetRequest(configuration, endpoint);

			String res = HttpClientUtils.sendRequest(get);
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