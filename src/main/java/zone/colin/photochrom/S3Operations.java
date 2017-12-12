package zone.colin.photochrom;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

public class S3Operations {

    private final AmazonS3 amazonS3;

    public S3Operations(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public PhotochromData nextPhotoToTweet() {
        String photochromKey = nextKey();
        S3Object s3Object = amazonS3.getObject("photocromulent", photochromKey);

        return new PhotochromData(photochromKey, s3Object.getObjectContent());
    }

    public void updateLastProcessed(String keyToTweet) {
        amazonS3.putObject("photocromulent", "last-processed.txt", keyToTweet);
    }

    private String nextKey() {
        ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request()
                .withBucketName("photocromulent")
                .withPrefix("photos")
                .withMaxKeys(1)
                .withStartAfter(lastKey());

        ListObjectsV2Result objectListing = amazonS3.listObjectsV2(listObjectsV2Request);
        if (objectListing.getObjectSummaries() == null || objectListing.getObjectSummaries().isEmpty()) {
            throw new RuntimeException("No more photos left to tweet");
        }

        return objectListing.getObjectSummaries().get(0).getKey();
    }

    private String lastKey() {
        S3Object lastProcessedS3Object = amazonS3.getObject("photocromulent", "last-processed.txt");

        try {
            return IOUtils.toString(lastProcessedS3Object.getObjectContent());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
