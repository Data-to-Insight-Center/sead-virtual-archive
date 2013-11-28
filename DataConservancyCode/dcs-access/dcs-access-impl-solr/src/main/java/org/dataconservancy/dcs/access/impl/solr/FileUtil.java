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
package org.dataconservancy.dcs.access.impl.solr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * See {@link org.dataconservancy.dcs.index.dcpsolr.FileUtil}
 */

@Deprecated
public class FileUtil {
    public static File createTempDir(String prefix) throws IOException {
        File dir = File.createTempFile(prefix, null);

        dir.delete();
        dir.mkdir();

        return dir;
    }

    public static File join(File dir, String ... els) {
    	StringBuilder sb = new StringBuilder(dir.getPath());
    	
    	for (String el: els) {
    		sb.append(File.separatorChar);
    		sb.append(el);
    	}
    	
    	return new File(sb.toString());
    }
    
    public static FileChannel channel(File file, boolean writeable)
            throws IOException {
        String opts = writeable ? "rw" : "r";
        RandomAccessFile fd = new RandomAccessFile(file, opts);
        FileChannel chan = fd.getChannel();

        return chan;
    }

    public static MappedByteBuffer mmap(File file, long offset, long length,
            boolean writeable) throws IOException {
        FileChannel chan = channel(file, writeable);
        FileChannel.MapMode mode = writeable ? FileChannel.MapMode.READ_WRITE
            : FileChannel.MapMode.READ_ONLY;
        MappedByteBuffer buf = chan.map(mode, offset, length);
        chan.close();

        return buf;
    }

    /**
     * Read length bytes from offset in file into a buffer.
     * 
     * @param file
     * @param offset
     * @param length
     * @return buffer with position set to 0
     * @throws IOException
     */
    public static ByteBuffer read(File file, long offset, int length)
            throws IOException {
        FileChannel chan = channel(file, false);

        ByteBuffer buf = ByteBuffer.allocate(length);
        chan.position(offset);

        while (buf.remaining() > 0) {
            if (chan.read(buf) <= 0) {
                throw new IOException("Failed to read from channel.");
            }
        }

        buf.rewind();
        chan.close();

        return buf;
    }

    /**
     * Use unlock to release lock and avoid channel leaks.
     * 
     * @param file
     * @param shared
     * @return
     * @throws IOException
     */
    public static FileLock lock(File file, boolean shared) throws IOException {
        FileChannel chan = new RandomAccessFile(file, "rw").getChannel();
        return chan.lock(0, Long.MAX_VALUE, shared);
    }

    /**
     * Use unlock to release lock and avoid channel leaks.
     * 
     * @param file
     * @param shared
     * @return lock or null if lock is being held by someone else
     * @throws IOException
     */
    public static FileLock trylock(File file, boolean shared) throws IOException {
        FileChannel chan = new RandomAccessFile(file, "rw").getChannel();
        return chan.tryLock(0, Long.MAX_VALUE, shared);
    }
    
    public static void unlock(FileLock lock) throws IOException {
        lock.channel().close();
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4 * 1024];

        for (;;) {
            int n = in.read(buf);
            
            if (n == -1) {
                break;
            }
            
            out.write(buf, 0, n);
        }
    }
    
    private static void copyfile(File infile, File outfile) throws IOException {
        FileChannel in = channel(infile, false);
        FileChannel out = channel(outfile, true);

        for (long offset = 0; offset < in.size();) {
            long n = in.transferTo(offset, in.size() - offset, out);

            if (n <= 0) {
                throw new IOException("Failed transfer to channel.");
            }

            offset += n;
        }

        out.truncate(out.position());

        in.close();
        out.close();
    }

    /**
     * Delete files and recursively delete directories.
     * 
     * @param file
     * @return success
     */
    public static boolean delete(File file) {
        if (file.isFile()) {
            return file.delete();
        } else {
            for (File f : file.listFiles()) {
                delete(f);
            }

            return file.delete();
        }
    }

    /**
     * Return size of a file or size of all files contained by a directory.
     */
    public static long size(File file) {
        if (file.isFile()) {
            return file.length();
        } else {
            long size = 0;
            
            for (File f : file.listFiles()) {
                size += size(f);
            }

            return size;
        }
    }
    
    /**
     * Copies files and directories just like the unix cp -r command.
     * 
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copy(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (to.exists()) {
                to = new File(to, from.getName());
                to.mkdir();
            } else {
                to.mkdir();
            }

            for (File f : from.listFiles()) {
                copy(f, to);
            }
        } else {
            if (to.isDirectory()) {
                copyfile(from, new File(to, from.getName()));
            } else {
                copyfile(from, to);
            }
        }
    }
}
