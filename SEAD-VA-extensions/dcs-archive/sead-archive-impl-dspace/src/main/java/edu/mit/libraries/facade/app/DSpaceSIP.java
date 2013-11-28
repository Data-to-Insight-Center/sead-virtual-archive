/*
 * Copyright (c) 2008, Hewlett-Packard Company and Massachusetts
 * Institute of Technology.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Neither the name of the Hewlett-Packard Company nor the name of the
 * Massachusetts Institute of Technology nor the names of their
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */


package edu.mit.libraries.facade.app;

import edu.harvard.hul.ois.mets.*;
import edu.harvard.hul.ois.mets.helper.*;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.io.File;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <pre>Prepare a METS SIP (Submission Information Package) to ingest
 * into DSpace, creating a new Item.
 *
 * This is a utility to help another application prepare a DSpace
 * SIP with as little effort as possible.  All it has to do is:
 *  - Create a SIP
 *  - Add Bitstreams
 *  - Add Descriptive Metadata
 *  - Write the SIP, to either a file or OutputStream.
 * It works in conjunction with the simple LNI client to upload a SIP
 * directly to the LNI.
 *
 * It does not rely on *any* DSpace code.  It only requires JDOM (for XML)
 * and the Harvard METS toolkit.
 *
 * Requires Sun Java JRE 5 and these libraries:
 *
 *  - Harvard METS Java toolkit, version 1.5
 *     http://hul.harvard.edu/mets/
 *
 *  - JDOM 1.0
 *     http://jdom.org/
 * </pre>
 * @author Larry Stone
 * @version $Revision: 2108 $
 */
public class DSpaceSIP
{
    // Describes the DSpace SIP version implemented here
    private static final String METS_PROFILE = "DSpace METS SIP Profile 1.0";

    // default value for validate
    private static final boolean VALIDATE_DEFAULT = true;

    // Filename of manifest, relative to package toplevel
    private static final String MANIFEST_FILE = "mets.xml";

    // PREMIS XML namespace; URI and JDOM namespace object
    private static final String PREMIS_NS_URI = "http://www.loc.gov/standards/premis";
    private static final Namespace PREMIS_NS = Namespace.getNamespace("premis", PREMIS_NS_URI);

    // JDOM xml output writer - indented format for readability.
    private static XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

    // counter for gensym()
    private int idCounter = 1;

    // Put all DMD sections for the Item into one group with this ID
    private String dmdGroupID = gensym("dmd_group");

    // attempt to validate the METS manifest before writing SIP
    private boolean validate = VALIDATE_DEFAULT;

    // Zip file compression level
    private int compression = 0;

    /**
     * Table of files to add to package, such as mdRef'd metadata.
     * Key is relative pathname of file, value a record of associated paths.
     */
    private Map<String,PackageFile> zipFiles = new HashMap<String,PackageFile>();

    // map of bundle name to list of relative-file-paths in that bundle.
    private Map<String,List> bundles = new HashMap<String,List>();

    // relative path of Primary Bitstream (PBS) if any
    private String primaryBitstream = null;

    // METS manifest object
    private Mets manifest = null;

    // DMDs to refer back to from structmap
    private List<String> dmdIDs = new ArrayList<String>();

    // Simple record, holds the data about each file in this package.
    class PackageFile
    {
        String relPath = null;     // relative path
        File absPath = null;       // absolute path on disk
        String zipPath = null;     // filename within the Zip archive

        PackageFile(String r, File a)
        {
            relPath = r;
            absPath = a;
            zipPath = gensym("pkgfile");
        }
    }

    /**
     * Default constructor.
     */
    public DSpaceSIP()
            throws MetsException
    {
        super();
        init(VALIDATE_DEFAULT, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * Detailed constructor.
     * @param validate whether or not to validate the resulting METS
     * @param compression level of compression (0-9) to use in Zipfile.
     */
    public DSpaceSIP(boolean validate, int compression)
            throws MetsException
    {
        super();
        init(validate, compression);
    }

    private void init(boolean validate, int compression)
    {
        this.validate = validate;
        this.compression = compression;

        // Initialize manifest -- Create the METS manifest structure
        manifest = new Mets();

        // Top-level stuff
        manifest.setID(gensym("mets"));
        manifest.setLABEL("DSpace Item");
        manifest.setPROFILE(METS_PROFILE);

        // MetsHdr
        MetsHdr metsHdr = new MetsHdr();
        metsHdr.setCREATEDATE(new Date()); // FIXME: CREATEDATE is now:
        // maybe should be item create
        // date?
        manifest.getContent().add(metsHdr);
    }

    /**
     * Set the OBJID attribute in the METS manifest
     * @param o new value for OBJID
     */
    public void setOBJID(String o)
    {
        if (o != null)
            manifest.setOBJID(o);
    }

    /**
     * Adds a a Bitstream to this Item, using contents of a File in the filesystem.
     * @param path the File containing the data of this Bitstream.
     * @param name logical pathname within the Item (DSpace Bitstream's "name" attribute)
     * @param isPrimaryBitstream true if this is the Item' Primary Bitstream, i.e. index page of a website.
     */
    public void addBitstream(File path, String name, String bundle, boolean isPrimaryBitstream)
    {
        zipFiles.put(name, new PackageFile(name, path));
        if (bundles.containsKey(bundle))
            bundles.get(bundle).add(name);
        else
        {
            List<String> a = new ArrayList<String>();
            a.add(name);
            bundles.put(bundle,a);
        }
        if (isPrimaryBitstream)
            primaryBitstream = name;
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on the contents of a File in the filesystem.  The file is
     * added to the SIP and referenced from the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param mdFile the File containing metadata
     * @param mimeType internet media type (MIME type) of contents of mdFile
     */
    public void addDescriptiveMD(String type, File mdFile, String mimeType)
    {
        String locfile = gensym("mdfile");
        zipFiles.put(locfile, new PackageFile(locfile, mdFile));

        DmdSec dmdSec = new DmdSec();
        String dmdID = gensym("dmd");
        dmdSec.setID(dmdID);
        dmdIDs.add(dmdID);
        dmdSec.setGROUPID(dmdGroupID);
        MdRef ref = new MdRef();
        ref.setMIMETYPE(mimeType);
        setMdType(ref, type);
        ref.setLOCTYPE(Loctype.URL);
        ref.setXlinkHref(locfile);
        dmdSec.getContent().add(ref);
        manifest.getContent().add(dmdSec);
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on a list of JDOM elements.  The elements become the
     * contents of the dmdSec in the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param md list of JDOM elements containing the DMD
     */
    public void addDescriptiveMD(String type, List<Element> md)
            throws MetsException
    {
        Element first = md.get(0);
        XmlData xd = addDescriptiveMDInternal(type, outputter.outputString(first), first.getNamespace());
        for (Element e : md.subList(1, md.size()))
            addToXmlData(xd, outputter.outputString(e), e.getNamespace());
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on a JDOM element.  The element will be the
     * contents of the dmdSec in the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param md JDOM element containing the DMD
     */
    public void addDescriptiveMD(String type, Element md)
            throws MetsException
    {
        addDescriptiveMDInternal(type, outputter.outputString(md), md.getNamespace());
    }

    /**
     * Adds a section of descriptive metadata (DMD) to the METS document,
     * based on String containing serialized XML.  The string is expected
     * to contain one element, which becomes the
     * contents of the dmdSec in the METS document.
     * Note that all DMD sections apply to the entire Item in the SIP.
     * @param type METS metadata type name (e.g. "MODS")
     * @param md serialized XML metadata
     */
    public void addDescriptiveMD(String type, String md)
            throws MetsException
    {
        addDescriptiveMDInternal(type, md, null);
    }


    /**
     * Add serialized XML to the METS toolkit's XmlData object's contents.
     * This is a separate function to facilitate experimenting with
     * namespace handling and parsing techniques during development.
     */
    private void addToXmlData(XmlData xd, String md, Namespace ns)
            throws MetsException
    {
        xd.getContent().add(Any.reader(new MetsReader(new ByteArrayInputStream(md.getBytes()))));

        // XXX FIXME?: Adding namespaces here results in multiple copies of xmlns attributes.
        // if (ns != null)
        //     xd.setSchema(ns.getPrefix(), ns.getURI());
    }

    /**
     * Adds a dmdSec to the METS manifest containing serialized XML metadata.
     * Returns the xmlData element within that dmdSec.
     */
    private XmlData addDescriptiveMDInternal(String type, String md, Namespace ns)
            throws MetsException
    {
        XmlData xmlData = new XmlData();

        // XXX FIXME? should add schemaLocation here too if available
        if (ns != null)
            xmlData.setSchema(ns.getPrefix(), ns.getURI());

        // read from serialized XML directly into XmlData's contents.
        addToXmlData(xmlData, md, ns);

        DmdSec dmdSec = new DmdSec();
        String dmdID = gensym("dmd");
        dmdSec.setID(dmdID);
        dmdIDs.add(dmdID);
        dmdSec.setGROUPID(dmdGroupID);
        MdWrap mdWrap = new MdWrap();
        setMdType(mdWrap, type);
        mdWrap.getContent().add(xmlData);
        dmdSec.getContent().add(mdWrap);
        manifest.getContent().add(dmdSec);
        return xmlData;
    }

    /**
     * Adds Agent element to the METS header.
     * @param role one of the acceptable METS roles, e.g. "CUSTODIAN"
     * @param type one of the acceptable METS types e.g. "ORGANIZATION"
     * @param aname proper name of the agent
     */
    public void addAgent(String role, String type, String aname)
    {
        Agent agent = new Agent();
        try
        {
            agent.setROLE(Role.parse(role.toUpperCase()));
        }
        catch (MetsException e)
        {
            agent.setROLE(Role.OTHER);
            agent.setOTHERROLE(role);
        }
        try
        {
            agent.setTYPE(Type.parse(type.toUpperCase()));
        }
        catch (MetsException e)
        {
            agent.setTYPE(Type.OTHER);
            agent.setOTHERTYPE(type);
        }
        Name name = new Name();
        name.getContent().add(new PCData(aname));
        agent.getContent().add(name);

        // find the header
        for (Object o : manifest.getContent())
        {
            if (o instanceof MetsHdr)
            {
                ((MetsHdr) o).getContent().add(agent);
                break;
            }
        }
    }

    /**
     * Make a new unique ID with specified prefix.
     * @param prefix the prefix of the identifier, constrained to XML ID schema
     * @return a new string identifier unique in this session (instance).
     */
    private String gensym(String prefix)
    {
        return prefix + "_" + String.valueOf(idCounter++);
    }

    // Create fileSec and structMap, add them to METS manifest.
    // The structMap just lists Bitstreams, and identifies PBS if there is one.
    private void finishManifest(OutputStream out)
            throws MetsException,
            UnsupportedEncodingException
    {
        // fileSec - all non-metadata bundles go into fileGrp,
        // and each bitstream therein into a file.
        // Create the bitstream-level techMd and div's for structmap
        // at the same time so we can connec the IDREFs to IDs.
        FileSec fileSec = new FileSec();

        // log the primary bitstream for structmap
        String primaryBitstreamFileID = null;

        // accumulate content DIV items to put in structMap later.
        List<Div> contentDivs = new ArrayList<Div>();

        for (Map.Entry<String,List> e : bundles.entrySet())
        {
            List<String> bitstreams = (List<String>)e.getValue();

            // Create a fileGrp
            FileGrp fileGrp = new FileGrp();

            // Bundle name for USE attribute
            fileGrp.setUSE(e.getKey());

            for (String bitstream : bitstreams)
            {
                edu.harvard.hul.ois.mets.File file = new edu.harvard.hul.ois.mets.File();
                String fileID = gensym("bitstream");
                file.setID(fileID);

                // log primary bitstream for later (structMap)
                if (primaryBitstream != null && primaryBitstream.equals(bitstream))
                    primaryBitstreamFileID = fileID;

                // if this is content, add to structmap too:
                Div div = new Div();
                div.setID(gensym("div"));
                div.setTYPE("DSpace Content Bitstream");
                Fptr fptr = new Fptr();
                fptr.setFILEID(fileID);
                div.getContent().add(fptr);
                contentDivs.add(div);

                // FIXME: CREATED: no date

                // length of file on disk
                file.setSIZE(zipFiles.get(bitstream).absPath.length());

                /*** XXX FIXME: add checksums?  not essential, and
                 XXX FIXME: package submission protocols are reliable
                 XXX FIXME: enough that is should not be necessary..

                 // translate checksum and type, if available, to METS.
                 String csType = bitstreams[bits].getChecksumAlgorithm();
                 String cs = bitstreams[bits].getChecksum();
                 if (auth && cs != null && csType != null)
                 {
                 try
                 {
                 file.setCHECKSUMTYPE(Checksumtype.parse(csType));
                 file.setCHECKSUM(cs);
                 }
                 catch (MetsException e)
                 {
                 log.warn("Cannot set bitstream checksum type="+csType+" in METS.");
                 }
                 }
                 ****/

                // locat is relative Zip path
                FLocat flocat = new FLocat();
                flocat.setLOCTYPE(Loctype.URL);
                flocat.setXlinkHref(zipFiles.get(bitstream).zipPath);

                // Make bitstream techMD metadata, add to file.
                String techID = gensym("techMd_for_bitstream_");
                AmdSec fAmdSec = new AmdSec();


                fAmdSec.setID(techID);

                TechMD techMd = new TechMD();
                techMd.setID(gensym("tech"));
                MdWrap mdWrap = new MdWrap();
                setMdType(mdWrap, "PREMIS");
                mdWrap.getContent().add(makeFilePREMIS(bitstream));

                techMd.getContent().add(mdWrap);
                fAmdSec.getContent().add(techMd);

//                RightsMD rightsMD = new RightsMD();
//                rightsMD.setID(gensym("rights"));
//                MdWrap mdWrap2 = new MdWrap();
//                mdWrap2.setMDTYPE(Mdtype.OTHER);
//                mdWrap2.setOTHERMDTYPE("METSRIGHTS");
//                mdWrap.getContent().add(makeFilePREMIS(bitstream));
//
//                fAmdSec.getContent().add(rightsMD);

                manifest.getContent().add(fAmdSec);

                file.setADMID(techID);

                // Add FLocat to File, and File to FileGrp
                file.getContent().add(flocat);
                fileGrp.getContent().add(file);
            }

            // Add fileGrp to fileSec
            fileSec.getContent().add(fileGrp);
        }

        // Add fileSec only if it has contents
        // XXX NOTE: METS schema *allows* fileSec to be left out,
        //  but as of 1.5, DSpace ingester does not..
        if (!fileSec.getContent().isEmpty())
            manifest.getContent().add(fileSec);

        // Create simple structMap: initial div represents the Item,
        // and user-visible content bitstreams are in its child divs.
        StringBuffer dmdIDstr = new StringBuffer();
        for (String dmdID : dmdIDs)
            dmdIDstr.append(" "+dmdID);
        StructMap structMap = new StructMap();
        structMap.setID(gensym("struct"));
        structMap.setTYPE("LOGICAL");
        structMap.setLABEL("DSpace");
        Div div0 = new Div();
        div0.setID(gensym("div"));
        div0.setTYPE("DSpace Item");
        div0.setDMDID(dmdIDstr.substring(1));

        // if there is a primary bitstream, add FPTR to it.
        if (primaryBitstreamFileID != null)
        {
            Fptr fptr = new Fptr();
            fptr.setFILEID(primaryBitstreamFileID);
            div0.getContent().add(fptr);
        }

        // add DIV for each content bitstream
        div0.getContent().addAll(contentDivs);
        structMap.getContent().add(div0);
        manifest.getContent().add(structMap);

        if (validate)
            manifest.validate(new MetsValidator());
        manifest.write(new MetsWriter(out));
    }


    /**
     *
     * Construct minimal PREMIS for a bitstream:
     *   object/objectIdentifier = URL, name
     *   object/originalName = name
     *   object/objectCategory = "File"
     *   object/objectCharacteristics/size = len
     *   object/fixity/messageDigestAlgorithm (OPT)
     *   object/fixity/messageDigest (OPT)
     */
    private XmlData makeFilePREMIS(String bitstream)
            throws UnsupportedEncodingException
    {
        Element premis = new Element("premis", PREMIS_NS);
        Element object = new Element("object", PREMIS_NS);
        premis.addContent(object);

        // objectIdentifier is required
        Element oid = new Element("objectIdentifier", PREMIS_NS);
        Element oit = new Element("objectIdentifierType", PREMIS_NS);
        oit.setText("URL");
        oid.addContent(oit);
        Element oiv = new Element("objectIdentifierValue", PREMIS_NS);
        oiv.setText(URLEncoder.encode(bitstream, "UTF-8"));
        oid.addContent(oiv);
        object.addContent(oid);

        // objectCategory is fixed value, "File".
        Element oc = new Element("objectCategory", PREMIS_NS);
        oc.setText("File");
        object.addContent(oc);

        Element ochar = new Element("objectCharacteristics", PREMIS_NS);
        object.addContent(ochar);

        // size
        Element size = new Element("size", PREMIS_NS);
        size.setText(String.valueOf(zipFiles.get(bitstream).absPath.length()));
        ochar.addContent(size);

        // originalName <- name (or source if none)
        Element on = new Element("originalName", PREMIS_NS);
        on.setText(bitstream);
        object.addContent(on);

        XmlData xmlData = new XmlData();
        xmlData.setSchema(PREMIS_NS.getPrefix(), PREMIS_NS.getURI());
        xmlData.getContent().add(new PreformedXML(outputter.outputString(premis)));
        return xmlData;
    }

    // Set a METS metadata type of a mdWrap - if Mdtype.parse() gets exception,
    // that means it's not in the MDTYPE vocabulary, so use OTHER.
    private void setMdType(MdWrap mdWrap, String mdtype)
    {
        try
        {
            mdWrap.setMDTYPE(Mdtype.parse(mdtype));
        }
        catch (MetsException e)
        {
            mdWrap.setMDTYPE(Mdtype.OTHER);
            mdWrap.setOTHERMDTYPE(mdtype);
        }
    }

    // Set a METS metadata type of a mdRef - if Mdtype.parse() gets exception,
    // that means it's not in the MDTYPE vocabulary, so use OTHER.
    private void setMdType(MdRef mdWrap, String mdtype)
    {
        try
        {
            mdWrap.setMDTYPE(Mdtype.parse(mdtype));
        }
        catch (MetsException e)
        {
            mdWrap.setMDTYPE(Mdtype.OTHER);
            mdWrap.setOTHERMDTYPE(mdtype);
        }
    }

    // copy from one stream to another
    private static void copyStream(final InputStream input, final OutputStream output)
            throws IOException
    {
        final int BUFFER_SIZE = 1024 * 4;
        final byte[] buffer = new byte[BUFFER_SIZE];

        while (true)
        {
            final int count = input.read(buffer, 0, BUFFER_SIZE);
            if (-1 == count)
                break;
            output.write(buffer, 0, count);
        }
    }

    /**
     * Write out the package to filesystem at the designated path.
     * @param path the File to which to write this SIP.
     */
    public void write(File path)
            throws IOException, MetsException
    {
        write (new FileOutputStream(path));
    }

    /**
     * Write out the package to a stream.
     * @param out OutputStream to which it is written.
     */
    public void write(OutputStream out)
            throws IOException, MetsException,
            UnsupportedEncodingException
    {
        ZipOutputStream zip = new ZipOutputStream(out);
        zip.setComment("METS archive created by DSpaceSIP");

        // NOTE: Never set method to ZipOutputStream.STORED since that mode
        // demands setting size of each entry, we don't know it for manifest.
        // Oddly, this works even when compression is NO_COMPRESSION.
        zip.setLevel(compression);
        zip.setMethod(ZipOutputStream.DEFLATED);

        // write manifest first.
        ZipEntry me = new ZipEntry(MANIFEST_FILE);
        //me.setTime(lmTime);
        zip.putNextEntry(me);
        finishManifest(zip);
        zip.closeEntry();

        // copy all files, incl. bitstreams, into zip
        for (Map.Entry<String,PackageFile> e : zipFiles.entrySet())
        {
            PackageFile pf = e.getValue();
            ZipEntry ze = new ZipEntry(pf.zipPath);
            ze.setTime(pf.absPath.lastModified());
            zip.putNextEntry(ze);
            copyStream(new FileInputStream(pf.absPath), zip);
            zip.closeEntry();
        }
        zip.close();
        zipFiles = null;
    }
}