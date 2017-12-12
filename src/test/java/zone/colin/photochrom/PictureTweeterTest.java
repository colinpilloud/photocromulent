package zone.colin.photochrom;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PictureTweeterTest {

    @InjectMocks
    private PictureTweeter pictureTweeter;

    @Mock
    private Twitter twitter;

    @Mock
    private InputStream inputStream;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void tweetPicture_statusFormulatedAsExpected() throws Exception {
        PhotochromData photochromData = new PhotochromData("key", inputStream);

        pictureTweeter.tweetPicture(photochromData);

        ArgumentCaptor<StatusUpdate> statusUpdateCaptor = ArgumentCaptor.forClass(StatusUpdate.class);
        Mockito.verify(twitter).updateStatus(statusUpdateCaptor.capture());

        StatusUpdate statusUpdate = statusUpdateCaptor.getValue();
        assertThat(statusUpdate.getStatus(), is(""));
        // XXX no getter for media, which is cheesy
    }

    @Test
    public void tweetPicture_twitterExceptionThrown_caughtAndWrapped() throws Exception {
        PhotochromData photochromData = new PhotochromData("key", inputStream);

        TwitterException twitterException = new TwitterException("my cool twitter exception message");
        Mockito.when(twitter.updateStatus(Mockito.any(StatusUpdate.class))).thenThrow(twitterException);

        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(is(twitterException));

        pictureTweeter.tweetPicture(photochromData);
    }
}
