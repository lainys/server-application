package arifmetic.server;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable {
    private static final int MAX_BUFFER_SIZE = 1024;

    public static final int DOWNLOADING = 0;
    public static final int COMPLETE = 1;

    private URL url;
    private int size;
    private int downloaded;
    private int status;

    /**
     * @param url
     */
    public Download(URL url) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;

        download();
    }

    /**
     *
     */
    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * @param url
     * @return
     */
    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    /**
     *
     */
    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Range",
                    downloaded + "-");

            connection.connect();

            if (connection.getResponseCode() / 100 != 2) {
                //error();
            }

            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                //error();
            }

            if (size == -1) {
                size = contentLength;
                stateChanged();
            }

            file = new RandomAccessFile(getFileName(url), "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                int read = stream.read(buffer);
                if (read == -1)
                    break;

                file.write(buffer, 0, read);
                downloaded += read;
                stateChanged();
            }

            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        } catch (Exception e) {
            //error();
        } finally {
            if (file != null)
                try {
                    file.close();
                } catch (Exception e) {
                    // ...
                }

            if (stream != null)
                try {
                    stream.close();
                } catch (Exception e) {
                    // ...
                }
        }
    }

    // Notify observers that this download's status has changed.
    private void stateChanged() {
        // ...
    }
}