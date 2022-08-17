package io.elastic.i95dev.marketo.providers;

import java.io.StringReader;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elastic.i95dev.marketo.utils.HttpClientUtils;
import io.elastic.api.DynamicMetadataProvider;

public class LeadMetaDataProvider implements DynamicMetadataProvider {
	private static final Logger logger = LoggerFactory.getLogger(LeadMetaDataProvider.class);

	@Override
	public JsonObject getMetaModel(JsonObject configuration) {
		JsonObjectBuilder result = Json.createObjectBuilder();

		JsonObjectBuilder response = Json.createObjectBuilder();

		String Endpoint = "leads/describe.json";

		HttpGet request = HttpClientUtils.createGetRequest(configuration, Endpoint);
		try {
			String res = HttpClientUtils.sendRequest(request);
			JsonReader jsonReader = Json.createReader(new StringReader(res));
			JsonObject object = jsonReader.readObject();
			jsonReader.close();
			JsonArray entityArray = object.getJsonArray("result");
			for (int i = 0; i < entityArray.size(); i++) {
				JsonObject entityObject = entityArray.getJsonObject(i);
				Set<String> keys = entityObject.keySet();
				String property = keys.toString();

				result.add(property, property);

			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		return result.build();

	}
}