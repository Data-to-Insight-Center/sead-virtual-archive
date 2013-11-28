package org.seadva.model.builder.api;

import org.dataconservancy.model.builder.InvalidXmlException;
import org.seadva.model.pack.ResearchObject;

import java.io.InputStream;
import java.io.OutputStream;

public interface SeadModelBuilder {

    public ResearchObject buildSip(InputStream in) throws InvalidXmlException;

    public void buildSip(ResearchObject sip, OutputStream sink);


}
