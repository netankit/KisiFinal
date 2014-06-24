package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

public class CreateGatewayCall extends LocatableCall {

	private JSONObject blinkUpResponse;

	public CreateGatewayCall(JSONObject blinkUpResponse) {
		super("gateways", HTTPMethod.POST);
		
		this.blinkUpResponse = blinkUpResponse;
		this.handler = new JsonHttpResponseHandler(){
			//TODO: Implement a proper handler
		};
		
		createJson();
	}
	
	private void createJson() {
		String agentUrl = null;
		String impeeId = null;
		String planId = null;
		try {
			agentUrl = blinkUpResponse.getString("agent_url");
			impeeId = blinkUpResponse.getString("impee_id");
			planId = blinkUpResponse.getString("plan_id");
		
			//impeeId contains white spaces in the end, remove them
	        if (impeeId != null) 
	            impeeId = impeeId.trim();

	    	JSONObject gateway = new JSONObject();
		
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("blinked_up", true);
			gateway.put("ei_impee_id", impeeId);


			json.put("gateway", gateway);
			json.put("ei_plan_id", planId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
