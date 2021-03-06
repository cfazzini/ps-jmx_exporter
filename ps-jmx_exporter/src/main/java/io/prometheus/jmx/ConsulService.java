package io.prometheus.jmx;

// import java.util.Arrays;
import java.util.List;
// import java.util.Collections;
// import java.util.List;
import java.util.Map;
// import java.util.Optional;
import java.net.InetAddress;
// import java.util.HashMap;
// import java.util.Map;
import java.util.UUID;
import java.lang.Integer;
// import org.json.JSONException;
import org.json.JSONObject;
// import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataOutputStream;

public class ConsulService { 
  
  private String consulHost;
  private String consulPort;

  public ConsulService(String consulHost, String consulPort ) {
    this.consulHost = consulHost;
    this.consulPort = consulPort;
  }

  private void putJsontoExternalConsul (JSONObject json){
    try{
      URL url = new URL("http://" + consulHost + ":" + consulPort + "/v1/catalog/register");
      HttpURLConnection httpConnection  = (HttpURLConnection) url.openConnection();
      httpConnection.setDoOutput(true);
      httpConnection.setRequestMethod("PUT");
      httpConnection.setRequestProperty("Content-Type", "application/json");

      DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
      wr.write(json.toString().getBytes());
      Integer responseCode = httpConnection.getResponseCode();
      System.err.println("Registering External Service. Consul response code: " + responseCode);
    }
    catch (Exception e) {
      System.err.println("Registering External Service. Consul registration failed!" + e.getMessage());
    }

  }

  private void putJsontoInternalConsul (JSONObject json){
    try{
      URL url = new URL("http://" + consulHost + ":" + consulPort + "/v1/agent/service/register");
      HttpURLConnection httpConnection  = (HttpURLConnection) url.openConnection();
      httpConnection.setDoOutput(true);
      httpConnection.setRequestMethod("PUT");
      httpConnection.setRequestProperty("Content-Type", "application/json");

      DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
      wr.write(json.toString().getBytes());
      Integer responseCode = httpConnection.getResponseCode();
      System.err.println("Registering Internal Service. Consul response code: " + responseCode);
    }
    catch (Exception e) {
      System.err.println("Registering Internal Service. Consul registration failed!" + e.getMessage());
    }

  }

  public void registerInternalService(String serviceName, int servicePort, List<String> tags){
    try {
      String uuidSeed = (String)InetAddress.getLocalHost().getHostName()+"-"+serviceName+servicePort;
      final String serviceId = serviceName + "-" + UUID.nameUUIDFromBytes(uuidSeed.getBytes()).toString();
      String fqdnHostName = InetAddress.getLocalHost().getCanonicalHostName();
      String hostName = InetAddress.getLocalHost().getHostName();
      JSONObject internalSvcJsonObj = new JSONObject();
      // Map labelsMap = new LinkedHashMap();
      // Map checksMap = new LinkedHashMap();
      // Map checkDefMap = new LinkedHashMap();
      internalSvcJsonObj.put("id",serviceId);
      internalSvcJsonObj.put("name",serviceName);
      internalSvcJsonObj.put("port",servicePort);
      internalSvcJsonObj.put("tags", tags);
      // if (healthCheck){
      //   checksMap.put("name","http-check");
      //   checksMap.put("status","passing");
      //   checkDefMap.put("http", "http://"+hostName+":"+servicePort+"/metrics");
      //   checkDefMap.put("interval", "30s");
      //   checksMap.put("Definition", checkDefMap);
      //   internalSvcJsonObj.put("check",checksMap);
      // }

      // internalSvcJsonObj.put()

      putJsontoInternalConsul(internalSvcJsonObj);
    }
    catch (Exception e) {
      System.err.println("Consul registration failed!" + e.getMessage());
    }
  }

  public void registerExternalService(String serviceName, int servicePort, List<String> tags) {
    try {
      String uuidSeed = (String)InetAddress.getLocalHost().getHostName()+"-"+serviceName+servicePort;
      final String serviceId = serviceName + "-" + UUID.nameUUIDFromBytes(uuidSeed.getBytes()).toString();
      String fqdnHostName = InetAddress.getLocalHost().getCanonicalHostName();
      String hostName = InetAddress.getLocalHost().getHostName();
      // create json object
      JSONObject externalSvcJsonObj = new JSONObject();
      Map labelsMap = new LinkedHashMap();
      Map nodeMetaMap = new LinkedHashMap();
      Map serviceMap = new LinkedHashMap();
      Map checksMap = new LinkedHashMap();
      Map checkDefMap = new LinkedHashMap();
      serviceMap.put("ID",serviceId);
      serviceMap.put("Service", serviceName);
      serviceMap.put("Port", servicePort);
      serviceMap.put("Tags", tags);
      serviceMap.put("Address", hostName);
      nodeMetaMap.put("external-node","true");
      nodeMetaMap.put("external-probe", "true");
      externalSvcJsonObj.put("Node",fqdnHostName);
      externalSvcJsonObj.put("Address",fqdnHostName);
      externalSvcJsonObj.put("NodeMeta", nodeMetaMap);
      externalSvcJsonObj.put("Service",serviceMap);
      // if (healthCheck){
      //   checksMap.put("Name","http-check");
      //   checksMap.put("status","passing");
      //   checkDefMap.put("http", "http://"+hostName+":"+servicePort+"/metrics");
      //   checkDefMap.put("interval", "30s");
      //   checksMap.put("Definition", checkDefMap);
      //   externalSvcJsonObj.put("Check",checksMap);
      // }

      putJsontoExternalConsul(externalSvcJsonObj);
    }
    catch (Exception e) {
      System.err.println("External registration failed: " + e.getMessage());
    }
  }


}