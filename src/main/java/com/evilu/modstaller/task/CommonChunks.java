package com.evilu.modstaller.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.evilu.modstaller.task.TaskChunk.TaskChunkStatusCallback;

/**
 * CommonChunks
 */
public interface CommonChunks {

    
    public static void copy(final long size, final InputStream is, final OutputStream os, final int bufferSize, final TaskChunkStatusCallback statusCallback) throws IOException {

        final byte[] buffer = new byte[bufferSize];
        statusCallback.setProgress(0d);
        int bytesRead = 0;
        long totalBytesRead = 0;
        while ((bytesRead = is.read(buffer, 0, buffer.length)) > 0) {
            os.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;

            if (statusCallback.shouldCancel()) return;

            statusCallback.setProgress((double) totalBytesRead / (double) size);
        }
    }
}
