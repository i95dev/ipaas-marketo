package io.elastic.i95dev.marketo.actions;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elastic.i95dev.marketo.utils.HttpClientUtils;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.Message;

public class CreateCampaign implements Function {
	private static final Logger logger = LoggerFactory.getLogger(CreateCampaign.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString folderId = body.getJsonString("id");
		JsonString folderType = body.getJsonString("type");
		JsonString name = body.getJsonString("name");
		JsonString description = body.getJsonString("description");

		JsonObject object = Json.createObjectBuilder().add("id", folderId).add("type", folderType).build();
		StringBuilder reqBody = new StringBuilder();
		reqBody.append("name=" + name.toString() + "&folder=" + object.toString());
		if (description != null) {
			reqBody.append("&description=" + description);
		}
		String endpoint = "/rest/asset/v1/smartCampaigns.json";
		HttpPost req = HttpClientUtils.uploadfileRequest(configuration, endpoint, reqBody.toString());
		try {
			String res = HttpClientUtils.sendRequest(req);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
			JsonObject object1 = jsonReader.readObject();
			jsonReader.close();

			final Message response = new Message.Builder().body(object1).build();

			parameters.getEventEmitter().emitData(response);

			logger.info("Finished execution");
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
