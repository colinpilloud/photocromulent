package zone.colin.photochrom;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class PictureTweeter {

    private final Twitter twitter;

    public PictureTweeter(Twitter twitter) {
        this.twitter = twitter;
    }

    public void tweetPicture(PhotochromData photochromData) {
        StatusUpdate statusUpdate = new StatusUpdate("");
        statusUpdate.setMedia(photochromData.getKey(), photochromData.getInputStream());

        try {
            twitter.updateStatus(statusUpdate);
        } catch (TwitterException ex) {
            throw new RuntimeException(ex);
        }
    }
}
