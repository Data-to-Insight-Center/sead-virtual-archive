package edu.iu.dpn.util;

import java.io.IOException;
import java.nio.file.Path;

public class RsyncDownload {
    String USER;

    public RsyncDownload(String USER){
        this.USER = USER;
    }

    public Path downloadFile(String URI, Path local) throws Exception {
        String[] cmd = new String[]{"rsync", "-r", USER + "@" + URI, local.toString()};
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
        return local;
    }
}
