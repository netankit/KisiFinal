package de.kisi.android.api.calls;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import de.kisi.android.api.KisiAPI;

public class CreateGatewayCall extends LocatableCall {

	
	
	private JSONObject blinkUpResponse;

	public CreateGatewayCall(JSONObject blinkUpResponse) {
		super("gateways", HTTPMethod.POST);
		
		this.blinkUpResponse = blinkUpResponse;
		this.handler = new JsonHttpResponseHandler(){
			//TODO: Implement a proper handler
		};
		
		
	}
	
	@Override
	protected void createJson() {
		String agentUrl = null;
		String impeeId = null;
		String planId = null;
		try {
			agentUrl = blinkUpResponse.getString("agent_url");
			impeeId = blinkUpResponse.getString("impee_id");
			planId = blinkUpResponse.getString("plan_id");
		} catch (JSONException e2) {
			e2.printStackTrace();
		} 
		//impeeId contains white spaces in the end, remove them
        if (impeeId != null) 
            impeeId = impeeId.trim();

        JSONObject location = generateJSONLocation();
    	JSONObject gateway = new JSONObject();
		try {
			gateway.put("name", "Gateway");
			gateway.put("uri", agentUrl);
			gateway.put("blinked_up", true);
			gateway.put("ei_impee_id", impeeId);
			gateway.put("location", location);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
		this.json = new JSONObject();
		
		try {
			json.put("gateway", gateway);
			json.put("ei_plan_id", planId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
