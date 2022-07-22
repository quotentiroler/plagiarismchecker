package com.example.plagcheck.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.plagcheck.ProcessJSON.ExcludeFingerprints;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonpCharacterEscapes;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@RestController
@RequestMapping(path = "/excludefp/")
public class APIController {

    @GetMapping("/")
    public String getDemo() {
        return "POST 2 json files to /excludefp/ like this: [json1,json2]. json1 will be removed from json2, result comes in response body";
    }

    @RequestMapping(value = "/", produces = "application/json", method = {
            RequestMethod.POST })
    public ResponseEntity<String> getResults(@RequestBody(required = true) String fp)
            throws JsonMappingException, JsonProcessingException {
        System.out.println(fp);
        JsonElement je = JsonParser.parseString(fp);
        JsonArray f = je.getAsJsonArray();
        JSONObject l = new JSONObject(f.get(0).getAsJsonObject().toString());
        JSONObject r = new JSONObject(f.get(1).getAsJsonObject().toString());
        ExcludeFingerprints ef = new ExcludeFingerprints(l, r);
        return new ResponseEntity<String>(ef.getResult().toString(), HttpStatus.OK);

    }

}