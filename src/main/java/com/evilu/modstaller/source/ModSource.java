package com.evilu.modstaller.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.core.TranslationService;
import com.evilu.modstaller.model.Mod;
import com.evilu.modstaller.model.TaskFailedException;
import com.evilu.modstaller.task.TaskBuilder;
import com.evilu.modstaller.task.TaskChunk;
import com.evilu.modstaller.task.TaskBuilder.TaskStep;
import com.evilu.modstaller.ui.util.BindingUtil;

import javafx.beans.value.ObservableValue;


/**
 * ModSource
 */
public interface ModSource {

    public InputStream getStream() throws IOException;

    public long getSize() throws IOException;

    public static ModSource fromFile(final File file) {
        return new ModSource() {

          @Override
          public InputStream getStream() throws IOException {
              return new FileInputStream(file);
          }

          @Override
          public long getSize() {
              return file.length();
          }

        };
    }

    public static ModSource fromURL(final URL url) {
        return new ModSource() {

          @Override
          public InputStream getStream() throws IOException {
              return url.openStream();
          }

          @Override
          public long getSize() throws IOException {
              final HttpURLConnection con = (HttpURLConnection) url.openConnection();
              con.setRequestMethod("HEAD");

              return con.getContentLengthLong();
          }
        };
    }

    @Deprecated(forRemoval = true)
    default TaskStep<File, File, File> writeToFile(final Mod mod) {
        final TranslationService ts = ApplicationContext.get().getTranslationService();

        return TaskBuilder.build((file, sc) -> {
                final byte[] buffer = new byte[8192];
                sc.setProgress(0d, 1d);
                sc.setMessage(ts.translateOnce("task.writeModToFile", mod.getDisplayName()));

                try {
                    final long size = getSize();
                    int offset = 0;
                    final InputStream is = getStream();
                    final OutputStream os = new FileOutputStream(file);

                    while (offset < size) {
                        final int bytesRead = is.read(buffer);
                        offset += bytesRead;

                        os.write(buffer, 0, bytesRead);

                        sc.setProgress((double) offset / (double) size, 1d);
                    }

                    os.close();
                } catch (final Throwable t) {
                    sc.setMessage(ts.translateOnce("task.failed"));
                    throw new RuntimeException(String.format("Failed copying mod: %s (%s)", mod.getName(), mod.getVersion().toString()), t);
                }

                sc.setMessage(ts.translateOnce("task.done"));
                return file;
        });
    }

    default TaskChunk<File> writeToFile(final ObservableValue<Mod> modValue, final ObservableValue<File> fileValue) {
      final TranslationService ts = ApplicationContext.get().getTranslationService();

      return TaskChunk.create(ts.translatedExpression("task.writeModToFile.msg", BindingUtil.mapString(modValue, Mod::getDisplayName)), statusCallback -> {
                final byte[] buffer = new byte[8192];
                statusCallback.setProgress(0d);

                try {
                    final long size = getSize();
                    int offset = 0;
                    final InputStream is = getStream();
                    final OutputStream os = new FileOutputStream(fileValue.getValue());

                    while (offset < size) {
                        final int bytesRead = is.read(buffer);
                        if (bytesRead < 1) {
                            throw new RuntimeException("Expected at least one more byte");
                        }
                        offset += bytesRead;

                        os.write(buffer, 0, bytesRead);

                        statusCallback.setProgress((double) offset / (double) size);
                    }

                    os.close();
                } catch (final Throwable t) {
                    final Mod mod = modValue.getValue();
                    throw new TaskFailedException(ts.translated("task.writeModToFile.failed", mod.getName(), t.getLocalizedMessage()), t);
                }

                return fileValue.getValue();
      });
    }
}
