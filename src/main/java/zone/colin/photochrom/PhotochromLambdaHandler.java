package zone.colin.photochrom;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import twitter4j.TwitterFactory;

public class PhotochromLambdaHandler {

    private final S3Operations s3Operations;
    private final PictureTweeter pictureTweeter;

    public PhotochromLambdaHandler() {
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_WEST_2)
                .build();
        this.s3Operations = new S3Operations(amazonS3);
        this.pictureTweeter = new PictureTweeter(TwitterFactory.getSingleton());
    }

    public PhotochromLambdaHandler(S3Operations s3Operations, PictureTweeter pictureTweeter) {
        this.s3Operations = s3Operations;
        this.pictureTweeter = pictureTweeter;
    }

    public void tweetNextPhoto(PhotochromRequest request, Context lambdaContext) {
        PhotochromData photochromData = s3Operations.nextPhotoToTweet();

        pictureTweeter.tweetPicture(photochromData);

        s3Operations.updateLastProcessed(photochromData.getKey());
    }
}
