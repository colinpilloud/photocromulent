package zone.colin.photochrom;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

@RunWith(MockitoJUnitRunner.class)
public class PhotochromLambdaHandlerTest {

    @InjectMocks
    private PhotochromLambdaHandler photochromLambdaHandler;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private S3Operations s3Operations;

    @Mock
    private PictureTweeter pictureTweeter;

    @Mock
    private PhotochromRequest photochromRequest;

    @Mock
    private Context lambdaContext;

    @Test
    public void tweetNextPhoto_doesNothingWithRequestOrContext() {
        photochromLambdaHandler.tweetNextPhoto(photochromRequest, lambdaContext);

        Mockito.verifyZeroInteractions(photochromRequest, lambdaContext);
    }

    @Test
    public void tweetNextPhoto_sequencesCallsCorrectly() {
        PhotochromData photochromData = new PhotochromData("key", Mockito.mock(InputStream.class));
        Mockito.when(s3Operations.nextPhotoToTweet()).thenReturn(photochromData);

        photochromLambdaHandler.tweetNextPhoto(photochromRequest, lambdaContext);

        InOrder inOrder = Mockito.inOrder(pictureTweeter, s3Operations);
        inOrder.verify(pictureTweeter).tweetPicture(photochromData);
        inOrder.verify(s3Operations).updateLastProcessed("key");
    }
}
