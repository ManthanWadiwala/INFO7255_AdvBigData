package com.info7255.demo.controller;


import com.info7255.demo.service.JsonValidator;
import com.info7255.demo.service.PlanService;

import org.everit.json.schema.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/v1/plan")
public class PlanController {

	JsonValidator jsonValidator;
	PlanService planService;
	
	@Autowired
	public PlanController(PlanService planService, JsonValidator jsonValidator) {

        this.planService = planService;
        this.jsonValidator = jsonValidator;

    }
	
// ########## GET #################
	
    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public ResponseEntity getPlan(@PathVariable String id,
                                        @RequestHeader HttpHeaders requestHeaders){

    	String objectType="plan";
        String key = objectType + ":" + id;
        if(!this.planService.checkIfKeyExists(key)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Error", "ObjectId does not exist!").toMap());
        } else {

                Map<String, Object> plan = this.planService.getPlan(key);
                String originalETag = this.planService.getEtag(key);
                String eTag = requestHeaders.getFirst("If-None-Match");
                if (eTag != null && eTag.equals(originalETag)) {
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(originalETag).build();
                } else {
  
                	return ResponseEntity.ok().eTag(originalETag).body(new JSONObject(plan).toMap());
                }

        }
    }
   
 // ########## POST #################
	
	@RequestMapping(method = RequestMethod.POST)
	    public ResponseEntity createPlan(@RequestBody(required = false) String jsonData,
	                                     @RequestHeader(required=false) HttpHeaders requestHeaders) throws Exception {

	        

	        if (jsonData == null || jsonData.isEmpty()){
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
	                    body(new JSONObject().put("Error", "Request body is empty.").toMap());
	        }

	        JSONObject jsonPlan = new JSONObject(jsonData);

	        try {
	            jsonValidator.validateJSON(jsonPlan);
	        } catch(ValidationException ex){
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
	                    body(new JSONObject().put("Error",ex.getAllMessages()).toMap());
	        }

	        String key = jsonPlan.get("objectType") + ":" + jsonPlan.get("objectId");
	        String objectId = (String)jsonPlan.get("objectId");
	        String etag = this.planService.savePlan(jsonPlan, key);

	        JSONObject response = new JSONObject();
	        response.put("objectId", jsonPlan.get("objectId"));


	        return ResponseEntity.created(new URI("/plan/" + key)).eTag(etag).body(response.toMap());

	    }
	
// ########## DELETE #################
	
    @RequestMapping(method =  RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deletePlan(@RequestHeader HttpHeaders requestHeaders,
                                        @PathVariable String id){

    	String objectType="plan";
        String key = objectType + ":" + id;
        if(!this.planService.checkIfKeyExists(key)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new JSONObject().put("Error", "ObjectID does not exist!").toMap());
        }

        this.planService.deletePlan(key);
        return ResponseEntity.noContent().build();
    }
	
}
