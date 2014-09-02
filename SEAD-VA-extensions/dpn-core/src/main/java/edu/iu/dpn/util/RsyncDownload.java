package edu.iu.dpn.util;

import java.io.IOException;
import java.nio.file.Path;

public class RsyncDownload {
    String USER;

    public RsyncDownload(String USER){
        this.USER = USER;
    }

    public String downloadFile(String URI, String hostname, String local) throws Exception {
        System.out.println("URI: "+URI+" hostname: "+hostname+" local: "+local);
        //String[] cmd = new String[]{"rsync", USER + "@"+hostname+":" + URI, local};
        String[] cmd = new String[]{"rsync", URI, local};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = null;
        int val;
        try{
            p = pb.start();
            val = p.waitFor();
        } catch(IOException e){
            throw new Exception("IOException in Rsync Download", e);
        } catch(InterruptedException e){
            throw new Exception("Rsync Download Interrupted", e);
        }
        if (val != 0) {
            throw new Exception("Exception during RSync; return code = " + val);
        }
        String fileName = URI.substring(URI.lastIndexOf("/")+1);
        local =  local+"/"+fileName;
        return local;
    }
}
