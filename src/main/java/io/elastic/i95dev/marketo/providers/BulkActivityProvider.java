package io.elastic.i95dev.marketo.providers;

import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.elastic.api.DynamicMetadataProvider;
import io.elastic.i95dev.marketo.utils.AuthorizationException;
import io.elastic.i95dev.marketo.utils.HttpUtils;


	public class BulkActivityProvider implements DynamicMetadataProvider  {
		private static final Logger logger=LoggerFactory.getLogger(BulkActivityProvider.class);

		@Override
		public JsonObject getMetaModel(JsonObject configuration) {
			JsonObjectBuilder result = Json.createObjectBuilder();
		
			JsonObjectBuilder response = Json.createObjectBuilder();
			   
			   
			   String Endpoint="/rest/v1/activities/types.json";
			   
			   HttpGet request=HttpUtils.createGetRequest(configuration, Endpoint);
			   try {
				String res=HttpUtils.sendRequest(request);
				JsonReader jsonReader = Json.createReader(new StringReader(res));
	            JsonObject object = jsonReader.readObject();
	            jsonReader.close();
            JsonArray array = object.getJsonArray("result");
	            	                           
	            
	        for (int i = 0; i < array.size(); i++) {
	                JsonObject entityObject =array.getJsonObject(i);
	              JsonString id= entityObject.getJsonString("id");
	               String property=id.toString();
	                
	                result.add(property, property);
	                
	            }
	            
	            
			} catch (Exception  e) {
				
				e.printStackTrace();
			}
			   
			  return result.build();

	}
	}



	
	
