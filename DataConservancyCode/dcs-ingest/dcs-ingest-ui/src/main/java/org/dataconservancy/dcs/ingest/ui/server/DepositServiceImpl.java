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
package org.dataconservancy.dcs.ingest.ui.server;

import java.io.IOException;
import java.io.StringWriter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.BasicScheme;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;

import org.dataconservancy.dcs.ingest.ui.client.DepositConfig;
import org.dataconservancy.dcs.ingest.ui.client.DepositService;
import org.dataconservancy.dcs.ingest.ui.client.RPCException;
import org.dataconservancy.dcs.ingest.ui.client.model.Package;
import org.dataconservancy.model.builder.DcsModelBuilder;
import org.dataconservancy.model.builder.xstream.DcsXstreamStaxModelBuilder;
import org.dataconservancy.model.dcp.Dcp;

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
        AbderaClient client = new AbderaClient(abdera);

        AbderaClient.registerTrustManager(); // needed for SSL
        AbderaClient.registerScheme(AuthPolicy.BASIC, BasicScheme.class);
        client.setAuthenticationSchemePriority(AuthPolicy.BASIC);
        client.usePreemptiveAuthentication(false);
        client.addCredentials(endpoint,
                              null,
                              "basic",
                              new UsernamePasswordCredentials(user, pass));

        return client;
    }

    public DepositConfig login(String endpoint, String user, String pass)
            throws RPCException {
        // TODO work around for bad urls in service document
        //        AbderaClient client = null;
        //
        //        String depsip = null;
        //        String upfile = null;
        //
        //        try {
        //            client = getClient(endpoint, user, pass);
        //
        //            ClientResponse resp = client.get(endpoint);
        //
        //            if (resp.getStatus() != 200) {
        //                throw new RPCException("Failed to retrieve service document "
        //                        + endpoint + ": " + resp.getStatusText());
        //            }
        //
        //            Document<Service> doc = resp.getDocument();
        //            Service service = doc.getRoot();
        //
        //            for (Workspace ws : service.getWorkspaces()) {
        //                for (Collection col : ws.getCollections()) {
        //                    if (col.getTitle().trim().equals("SIP deposit")) {
        //                        depsip = col.getResolvedHref().toURL().toString();
        //                    } else if (col.getTitle().trim().equals("File Upload")) {
        //                        upfile = col.getResolvedHref().toURL().toString();
        //                    }
        //                }
        //            }
        //        } catch (IOException e) {
        //            throw new RPCException(e.getMessage());
        //        } catch (URISyntaxException e) {
        //            throw new RPCException(e.getMessage());
        //        } finally {
        //            if (client != null) {
        //                client.teardown();
        //            }
        //        }
        //
        //        if (upfile == null || depsip == null) {
        //            return null;
        //        }
        //
        //        return new DepositConfig(upfile, depsip);

        return new DepositConfig(endpoint + "file", endpoint + "sip");
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

            //System.out.println("submitting to " + endpoint);

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

            //            System.out.println(new String(buf.array, 0, buf.length));
            //            System.out.println(result.toString());

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
}
