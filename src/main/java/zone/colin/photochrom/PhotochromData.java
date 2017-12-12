package zone.colin.photochrom;

import java.io.InputStream;

public class PhotochromData {

    private final String key;
    private final InputStream inputStream;

    public PhotochromData(String key, InputStream inputStream) {
        this.inputStream = inputStream;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
