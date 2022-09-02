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

public class CreateOpportunityRoles implements Function {
	private static final Logger logger = LoggerFactory.getLogger(CreateOpportunityRoles.class);

	@Override
	public void execute(ExecutionParameters parameters) {
		final JsonObject configuration = parameters.getConfiguration();
		final JsonObject body = parameters.getMessage().getBody();
		JsonString externalOpportunityId = body.getJsonString("externalOpportunityId"); // 19UYA31581L000000
		JsonString leadId = body.getJsonString("leadId");

		JsonString role = body.getJsonString("role");
		JsonString isPrimary = body.getJsonString("isPrimary");
		JsonString action = body.getJsonString("action");
		JsonString dedupeBy = body.getJsonString("dedupeBy");

		JsonObject dateobject = Json.createObjectBuilder().add("externalOpportunityId", externalOpportunityId)
				.add("leadId", leadId).add("role", role).add("isPrimary", isPrimary).build();
		JsonArray input = Json.createArrayBuilder().add(dateobject).build();

		JsonObject requestObj = Json.createObjectBuilder().add("dedupeBy", dedupeBy).add("action", action)
				.add("input", input).build();

		String endpoint = "/rest/v1/opportunities/roles.json";
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