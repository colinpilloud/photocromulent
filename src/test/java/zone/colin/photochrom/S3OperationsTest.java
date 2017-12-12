package zone.colin.photochrom;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringInputStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class S3OperationsTest {

    @InjectMocks
    private S3Operations s3Operations;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AmazonS3 amazonS3;

    @Mock
    private S3Object s3Object;

    @Before
    public void setUp() {
        Mockito.when(amazonS3.getObject("photocromulent", "last-processed.txt"))
                .thenReturn(s3ObjectWithTextContent("prior key"));
    }

    @Test
    public void nextPhotoToTweet_listObjectsRequest_formulatedCorrectly() {
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setKey("current key");

        ListObjectsV2Result listObjectsV2Result = new ListObjectsV2Result();
        listObjectsV2Result.getObjectSummaries().add(s3ObjectSummary);

        ArgumentCaptor<ListObjectsV2Request> listObjectsRequestCaptor =
                ArgumentCaptor.forClass(ListObjectsV2Request.class);
        Mockito.when(amazonS3.listObjectsV2(listObjectsRequestCaptor.capture())).thenReturn(listObjectsV2Result);

        s3Operations.nextPhotoToTweet();

        ListObjectsV2Request listObjectsRequest = listObjectsRequestCaptor.getValue();
        assertThat(listObjectsRequest.getBucketName(), is("photocromulent"));
        assertThat(listObjectsRequest.getPrefix(), is("photos"));
        assertThat(listObjectsRequest.getMaxKeys(), is(1));
        assertThat(listObjectsRequest.getStartAfter(), is("prior key"));
    }

    @Test
    public void nextPhotoToTweet_returnsWrapperWithKeyAndInputStreamToProcess() {
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        s3ObjectSummary.setKey("current key");

        ListObjectsV2Result listObjectsV2Result = new ListObjectsV2Result();
        listObjectsV2Result.getObjectSummaries().add(s3ObjectSummary);

        Mockito.when(amazonS3.listObjectsV2(Mockito.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Result);
        Mockito.when(amazonS3.getObject("photocromulent", "current key")).thenReturn(s3Object);

        PhotochromData photochromData = s3Operations.nextPhotoToTweet();

        assertThat(photochromData.getKey(), is("current key"));
        assertThat(photochromData.getInputStream(), is(s3Object.getObjectContent()));
    }

    @Test(expected = RuntimeException.class)
    public void nextPhotoToTweet_noMorePhotosToTweet_throwsException() {
        ListObjectsV2Result listObjectsV2Result = new ListObjectsV2Result();

        Mockito.when(amazonS3.listObjectsV2(Mockito.any(ListObjectsV2Request.class))).thenReturn(listObjectsV2Result);
        Mockito.when(amazonS3.getObject("photocromulent", "current key")).thenReturn(s3Object);

        s3Operations.nextPhotoToTweet();
    }

    @Test
    public void updateLastProcessed_delegatesToAmazonS3Client() {
        s3Operations.updateLastProcessed("current key");

        Mockito.verify(amazonS3).putObject("photocromulent", "last-processed.txt", "current key");
    }

    private S3Object s3ObjectWithTextContent(String text) {
        S3Object s3Object = new S3Object();
        try {
            s3Object.setObjectContent(new StringInputStream(text));
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Unsupported encoding", ex);
        }
        return s3Object;
    }

}
