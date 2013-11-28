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
package org.dataconservancy.model.builder.xstream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <code>TeeInputStream</code> contains an input stream and an output stream (the sink).  As bytes are read from the
 * input stream, they are copied to the sink.  Clients of this class can later "replay" the original input stream
 * by reading the bytes from the sink.  Closing this input stream will close the sink, and no more bytes will be
 * able to be written.
 */
class TeeInputStream extends FilterInputStream {
    private final OutputStream sink;

    TeeInputStream(InputStream toTee, OutputStream sink) {
        super(toTee);
        this.sink = sink;
    }

    /**
     * Return the sink.
     *
     * @return the sink
     */
    public OutputStream getSink() {
        return sink;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            sink.write(b);
        }
        return b;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int bytesRead = in.read(bytes);
        if (bytesRead != -1) {
            sink.write(bytes, 0, bytesRead);
        }
        return bytesRead;

    }

    @Override
    public int read(byte[] bytes, int offset, int len) throws IOException {
        int bytesRead = in.read(bytes, offset, len);
        if (bytesRead != -1) {
            sink.write(bytes, offset, bytesRead);
        }
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        in.close();
        sink.close();
    }
}
