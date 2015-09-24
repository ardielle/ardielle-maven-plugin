//Copyright 2016 Yahoo Inc.
//Licensed under the terms of the Apache version 2.0 license. See LICENSE file for terms.
package com.yahoo.rdl.maven;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;

@Singleton
public class ProcessRunner {

    public String run(List<String> command) throws IOException {
        return run(command, new ProcessBuilder(command));
    }

    public String run(List<String> command, ProcessBuilder processBuilder) throws IOException {
        Process process = processBuilder.start();
        try (StreamConsumer stdout = new StreamConsumer(process.getInputStream()).start()) {
            try (StreamConsumer stderr = new StreamConsumer(process.getErrorStream()).start()) {
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    throw new IOException("Process took longer than 10 seconds to execute: " + command);
                }
                if (process.exitValue() != 0) {
                    String s = stderr.getContents();
                    throw new IOException("command '" + StringUtils.join(command, " ") + "' produced error: " + s);
                }
            }
            return stdout.getContents();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    // Consumes a stream on a separate thread, so that process.waitFor won't block if the operating system
    // buffers fill up.
    private static class StreamConsumer implements Closeable {

        private static final ExecutorService executor = Executors.newCachedThreadPool();

        private InputStream inputStream;
        private Future<String> future;

        private StreamConsumer(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        private StreamConsumer start() {
            future = executor.submit(() -> IOUtils.toString(inputStream, "UTF-8"));
            return this;
        }

        private String getContents() throws IOException {
            try {
                return future.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) {
                    throw (IOException)cause;
                } else {
                    throw new RuntimeException(cause);
                }
            } catch (TimeoutException e) {
                throw new IOException("stream not closed", e);
            }
        }

        @Override
        public void close() throws IOException {
            inputStream.close();
        }
    }


}
