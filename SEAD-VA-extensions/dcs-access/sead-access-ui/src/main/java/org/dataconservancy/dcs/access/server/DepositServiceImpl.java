/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.dcs.access.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.BasicScheme;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;

import org.dataconservancy.dcs.access.client.SeadApp;
import org.dataconservancy.dcs.access.client.upload.DepositConfig;
import org.dataconservancy.dcs.access.client.upload.RPCException;
import org.dataconservancy.dcs.access.client.upload.model.Package;
import org.dataconservancy.dcs.access.server.util.ByteArray;
import org.dataconservancy.dcs.access.server.util.PackageUtil;
import org.dataconservancy.dcs.access.server.util.StatusReader;
import org.dataconservancy.dcs.access.server.util.StatusReader.Status;
import org.dataconservancy.dcs.access.shared.Constants;
import org.dataconservancy.dcs.access.shared.Event;
import org.dataconservancy.dcs.access.client.api.DepositService;
import org.dataconservancy.dcs.access.client.model.FileNode;
import org.dataconservancy.dcs.access.client.presenter.MediciIngestPresenter;
import org.dataconservancy.dcs.ingest.Events;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.InvalidXmlException;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;
import org.dataconservancy.model.dcs.DcsDeliverableUnit;
import org.dataconservancy.model.dcs.DcsResourceIdentifier;
import org.seadva.model.builder.xstream.SeadXstreamStaxModelBuilder;
import org.seadva.model.pack.ResearchObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

@SuppressWarnings("serial")
public class DepositServiceImpl
        extends RemoteServiceServlet
        implements DepositService {
	
	

    private Abdera abdera;

    private DcsModelBuilder dcpbuilder;

    public void init() throws ServletException {
        this.abdera = new Abdera();
        this.dcpbuilder = new DcsXstreamStaxModelBuilder();
    }

    private AbderaClient getClient(String endpoint, String user, String pass)
            throws MalformedURLException, URISyntaxException {
    	
    	AbderaClient   client = new AbderaClient(abdera);

        AbderaClient.registerTrustManager(); // needed for SSL
        AbderaClient.registerScheme(AuthPolicy.BASIC, BasicScheme.class);
        
        client.setAuthenticationSchemePriority(AuthPolicy.BASIC);
        
        
        client.usePreemptiveAuthentication(false);
        client.addCredentials(endpoint,
                              null,
                              "basic",
                              new UsernamePasswordCredentials(user, pass));
        //set credentials (for database & google oauth authentication)
              return client;
        
    }

 
    public String submitSIP(String endpoint,
                            String user,
                            String pass,
                            Package pkg) throws RPCException {
        AbderaClient client = null;

        try {
        
            client = getClient(endpoint, user, pass);

            RequestOptions opts = new RequestOptions();
            opts.setContentType("application/xml");
            opts.setHeader("X-Packaging",
                           "http://dataconservancy.org/schemas/dcp/1.0");
            opts.setHeader("X-Verbose", "true");


            Dcp dcp;

            try {
                dcp = PackageUtil.constructDcp(pkg);
            } catch (IllegalArgumentException e) {
                //e.printStackTrace();
                throw new RPCException("Malformed SIP: " + e.getMessage());
            }

            ByteArray buf = new ByteArray(8 * 1024);
            dcpbuilder.buildSip(dcp, buf.asOutputStream());
            ClientResponse resp =
                    client.post(endpoint, buf.asInputStream(), opts);

            int status = resp.getStatus();

            StringWriter result = new StringWriter();
            resp.getDocument().writeTo(result);


            if (status == 200 || status == 201 || status == 202) {
                return result.toString();
            } else {
            	throw new RPCException("Package deposit failed: " + result);
            }
        } catch (IOException e) {
            throw new RPCException(e.getMessage());
        } catch (URISyntaxException e) {
            throw new RPCException(e.getMessage());
        } finally {
            if (client != null) {
                client.teardown();
            }
        }
    }

    int timeout = 10*60;//5 second intervals//timeout is now 50 minutes
    StatusReader reader;
    int fivSec = 5*1000;
    public String checkStatus(String process, String statusUrl, int expectedCount){
    	try
    	{
    		Thread.sleep(2*1000);
	    	int time =0;
	    	reader = new StatusReader();
	    	Status status = Status.Pending;
	    	while(true){
	    		status = reader.getStatus(process, statusUrl, expectedCount);
	    		if(status.equals(Status.Pending)){
	    			time +=5*1000;
	    			if(time>=timeout*fivSec)
	    				return status.Timeout.getText();
	    			Thread.sleep(fivSec);
	    			continue;
	    		}
	    		else
	    			break;
	    	}
	    	return status.getText();
	    }
    	catch( InterruptedException e){
    		e.printStackTrace();
    		return e.getMessage();
    	}
    }
    

    public String getStatusDetails(String process, String statusUrl, int expectedCount){
    	try
    	{
    		Thread.sleep(2*1000);
	    	int time =0;
	    	reader = new StatusReader();
	    	String status = null;
	    	while(true){
	    		status = reader.getDetails(process, statusUrl, expectedCount);
	    		if(status==null){
	    			time +=5*1000;
	    			if(time>=timeout*fivSec)
	    				return status;
	    			Thread.sleep(fivSec);
	    			continue;
	    		}
	    		else
	    			break;
	    	}
	    	return status;
	    }
    	catch( InterruptedException e){
    		e.printStackTrace();
    		return e.getMessage();
    	}
    }
   
	@Override
	public DepositConfig getDepositConfig(String endpoint) {
		return new DepositConfig(endpoint + "file", endpoint + "sip");
	}

	@Override
	public boolean checkDownload(String url) {
		URL oracle;
		try {
			oracle = new URL(url);
			HttpURLConnection yc = (HttpURLConnection)oracle.openConnection();
			int code =yc.getResponseCode();
		//	System.out.println(code);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		return true;
		
	}
	
	@Override
	 public String getLinks(String urlStr) {
	      URL url;
	      HttpURLConnection conn;
	      BufferedReader rd;
	      String line;
	      String result = "";
	      try {
	         url = new URL(urlStr);
	         conn = (HttpURLConnection) url.openConnection();
	         conn.setRequestMethod("GET");
	         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	         result = rd.readLine();
	         while ((line = rd.readLine()) != null) {
	            result += "\n" + line;
	         }
	         rd.close();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	      return result;
	}

	
	@Override
	public Map<Date,List<Event>> statusUpdate(String statusUrl, Date latestDate){
		
		reader = new StatusReader();
		Map<Date, List<Event>> events = null;
		try {
			events = reader.getEvents(statusUrl, latestDate);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return events;
	}
	

	@Override
	public void loadDuIds(List<String> statusUrl){
		if(statusUrl==null)
			return;
		if(statusUrl.size()==0)
			return;
		Iterator<String> iterator = statusUrl.iterator();
		while(iterator.hasNext()){
			URL status;
			try {
				status = new URL(iterator.next());
				InputStream inputStream =
						status.openStream();
				   ResearchObject sip = new SeadXstreamStaxModelBuilder().buildSip(inputStream);
				   for(DcsDeliverableUnit du:sip.getDeliverableUnits()){
					   for(DcsResourceIdentifier id: du.getAlternateIds()){
						  if(id.getTypeId().equalsIgnoreCase("medici")||
							  id.getTypeId().equalsIgnoreCase("lowermississipppi"))
							  if(!Constants.duIds.containsKey(id.getIdValue()))
							  {
						  			Constants.duIds.put(id.getIdValue(), du.getId()); 
						  			break;
							  }
					   }
					   
				   }
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;//It is enough to just load the first one
		}
		
	}

	@Override
	public boolean deleteCollection(String id, String endpoint) {
		String urlStr = endpoint+"del/?id="+id.replace(":", "%3A");
		HttpMethod httpget = new GetMethod(urlStr);
        HttpClient client = new HttpClient();
        try {
            int x = client.executeMethod(httpget);
            if(x==200)
            	return true;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
	}
	
}
