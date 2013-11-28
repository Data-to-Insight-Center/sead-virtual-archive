/*
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.dcs.access.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.dataconservancy.dcs.access.client.api.TransformerService;
import org.dataconservancy.dcs.access.client.model.SchemaType;
import org.dataconservancy.dcs.access.server.util.ServerConstants;
import org.dataconservancy.dcs.access.shared.Constants;
import org.w3c.dom.Document;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TransformerServiceImpl extends RemoteServiceServlet
  implements TransformerService
{
  
  public TransformerServiceImpl()
  {
  }

	@Override
	public String xslTransform(SchemaType.Name inputSchema, SchemaType.Name outputSchema, String metadataXml) throws TransformerException{
		
		TransformerFactory factory = TransformerFactory.newInstance();
		Source xslt = new StreamSource(new File(getServletContext().getContextPath()+"xml/"+ inputSchema.name() +"to"+outputSchema.name()+ ".xsl"));
        Transformer transformer = factory.newTransformer(xslt);

        StringReader reader = new StringReader(metadataXml);
        StringWriter writer = new StringWriter();
        Source text = new StreamSource(reader);
        transformer.transform(text, new StreamResult(writer));
        return writer.toString();
	}
	
	@Override
	public SchemaType.Name validateXML(String inputXml, String schemaURI) {
		 
		if(schemaURI== null)
			return null;
		for(SchemaType.Name schemaName: SchemaType.Name.values()){
			if(Pattern.compile(Pattern.quote(schemaName.nameValue()), Pattern.CASE_INSENSITIVE).matcher(schemaURI).find()) {
				 DocumentBuilder parser;
				    Document document;
					try {

						parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						
						document = parser.parse(new StringBufferInputStream(inputXml));

						SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);  
			       
			        	Source schemaFile = new StreamSource(new File(
			        			getServletContext().getContextPath()+"xml/"+ 
			        			schemaName.name()+".xsd"));  
				        Schema schema = factory.newSchema(schemaFile);  
				  
				        Validator validator = schema.newValidator();  
			            validator.validate(new DOMSource(document));
			            return schemaName;
			        } catch (Exception e) {  
			            e.printStackTrace();  
			          
			        }         
			       
			}	
		}
		return null;
		
	}
String homeDir = "/home/kavchand/tmp/";
	@Override
	public String fgdcToHtml(String inputUrl, String format) {
		
		if(format.contains("fgdc")){
		 TransformerFactory factory = TransformerFactory.newInstance();
	        Source xslt = new StreamSource(new File(
	        		homeDir+"queryFgdcResult.xsl"
	        		));
	        Transformer transformer;
			try {
				transformer = factory.newTransformer(xslt);
				String inputPath =
				homeDir+UUID.randomUUID().toString()+"fgdcinput.xml";
				saveUrl(inputPath, inputUrl);
				Source text = new StreamSource(new File(
						inputPath
						));
				String outputPath = 
						homeDir+UUID.randomUUID().toString()+"fgdcoutput.html";
				File outputFile = new File(
		        		outputPath
		        		);
		        transformer.transform(text, new StreamResult(outputFile));
		        FileInputStream stream = new FileInputStream(new File(outputPath));
		        try {
		          FileChannel fc = stream.getChannel();
		          MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		          /* Instead of using default, pass in a decoder. */
		          return Charset.defaultCharset().decode(bb).toString();
		        }
		        finally {
		          stream.close();
		        }
		        
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			try{
			String inputPath =  
					//getServletContext().getContextPath()+"/xml/"+
					homeDir+UUID.randomUUID().toString()+"fgdcinput.xml";
			saveUrl(inputPath, inputUrl);
			Source text = new StreamSource(new File(
					//"/home/kavchand/Desktop/fgdc.xml"
					inputPath
					));
			FileInputStream stream = new FileInputStream(new File(inputPath));

	          FileChannel fc = stream.getChannel();
	          MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
	          /* Instead of using default, pass in a decoder. */
	          return Charset.defaultCharset().decode(bb).toString();
			}
			 catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	        
		return null;
	}
	
	public void saveUrl(String filename, String urlString) throws MalformedURLException, IOException
    {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try
        {
            in = new BufferedInputStream(new URL(urlString).openStream());
            fout = new FileOutputStream(filename);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1)
            {
                fout.write(data, 0, count);
            }
        }
        finally
        {
            if (in != null)
                in.close();
            if (fout != null)
                fout.close();
        }
    }
	
	@Override
	public String dateToString(Date date){
		return ServerConstants.dateFormat.format(date);
	}

}