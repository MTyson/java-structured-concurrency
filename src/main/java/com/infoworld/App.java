package com.infoworld;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.*;
import java.util.stream.*;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
    void failFast() throws ExecutionException, InterruptedException {
      int[] planetIds = {1,2,3,-1,4};
      try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        for (int planetId : planetIds) {
	  scope.fork(() -> getPlanet(planetId));
        } 
	scope.join();
      }
    }
    public String getPlanet(int planetId) throws Exception {
      System.out.println("BEGIN getPlanet()");
      String url = "https://swapi.dev/api/planets/" + planetId + "/";
      String ret = "?";

      CloseableHttpClient httpClient = HttpClients.createDefault();
 
      HttpGet request = new HttpGet(url);
      CloseableHttpResponse response = httpClient.execute(request);

      // Check the response status code
      if (response.getStatusLine().getStatusCode() != 200) {
        //System.err.println("Error fetching planet information for ID: " + planetId);
	throw new RuntimeException("Error fetching planet information for ID: " + planetId);
      } else {
      // Parse the JSON response and extract planet information
        ret = EntityUtils.toString(response.getEntity());
        System.out.println("Got a Planet: " + ret);
      }

      // Close the HTTP response and client
      response.close();
      httpClient.close();
      return ret;
    }
    void succeedFast() throws ExecutionException, InterruptedException {
        int[] planetIds = {1,2};
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess()) {
          for (int planetId : planetIds) {
	    scope.fork(() -> getPlanet(planetId));
          } 
	  scope.join();
        }catch (Exception e){
          System.out.println("Error: " + e);
	}

    }    
    void sync() throws Exception {
      int[] planetIds = {1,2,3,4,5};
      for (int planetId : planetIds) {
        getPlanet(planetId);
      }
    }
    void sc() throws Exception {
      int[] planetIds = {1,2,3,4,5};
        try (var scope = new StructuredTaskScope<Object>()) {
          for (int planetId : planetIds) {
	    Subtask<Object> st = scope.fork(() -> getPlanet(planetId));
          } 
	  scope.join();
        }catch (Exception e){
          System.out.println("Error: " + e);
	}

    }
    public static void main(String[] args) {
        var myApp = new App();
	System.out.println("\n\r-- BEGIN Sync");
	try {
    	  myApp.sync();
	} catch (Exception e){
          System.err.println("Error: " + e);
	}
	System.out.println("\n\r-- BEGIN Structured Concurrency");
	try {
          myApp.sc();
	} catch (Exception e){
          System.err.println("Error: " + e);
	}
        System.out.println("\n\r-- BEGIN failFast");
        try {
            myApp.failFast();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("\n\r-- BEGIN succeedFast");
        try {
            myApp.succeedFast();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }      
    }
}
