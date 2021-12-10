package com.newproject;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TrackTransform;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.TransformationOptions;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;
import com.linkedin.android.litr.codec.MediaCodecDecoder;
import com.linkedin.android.litr.codec.MediaCodecEncoder;
import com.linkedin.android.litr.exception.MediaTransformationException;
import com.linkedin.android.litr.filter.GlFilter;
import com.linkedin.android.litr.io.MediaExtractorMediaSource;
import com.linkedin.android.litr.io.MediaMuxerMediaTarget;
import com.linkedin.android.litr.io.MediaRange;
import com.linkedin.android.litr.io.MediaSource;
import com.linkedin.android.litr.io.MediaTarget;
import com.linkedin.android.litr.render.GlVideoRenderer;
import com.newproject.utils.TransformationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class CustomModule extends ReactContextBaseJavaModule {

    private static final String TAG = CustomModule.class.getSimpleName();

    //private final MediaTransformer mediaTransformer;

    private static ReactApplicationContext reactContext;

    CustomModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    String uniqueID = UUID.randomUUID().toString();

    private static final String KEY_ROTATION = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            ? MediaFormat.KEY_ROTATION
            : "rotation-degrees";

    /* This Function is what we call from frontend */
    @ReactMethod
    public void compressVid(String pathValue, String destPath, int imageWidth, int imageHeight) {

        //FFmpegMediaMetadataRetriever mFFmpegMediaMetadataRetriever = new FFmpegMediaMetadataRetriever();
        //mFFmpegMediaMetadataRetriever.setDataSource(pathValue);

        //String FRAME_RATE = mFFmpegMediaMetadataRetriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE);

        Toast.makeText(reactContext, pathValue, Toast.LENGTH_LONG).show();

        MediaTransformer mediaTransformer = new MediaTransformer(reactContext);

        TransformationListener listener = new TransformationListener() {
            @Override
            public void onStarted(@NonNull String id) {

            }

            @Override
            public void onProgress(@NonNull String id, float progress) {

            }

            @Override
            public void onCompleted(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {

            }

            @Override
            public void onCancelled(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {

            }

            @Override
            public void onError(@NonNull String id, @Nullable Throwable cause, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {

            }
        };

        Log.d(">>>>>>>>>>>>>Received Path:-", pathValue);
        Log.d(">>>>>>>>>>>>>Destination Path:-", destPath);
        Log.d(">>>>>>>>>>>>>Received Width:-", String.valueOf(imageWidth));
        Log.d(">>>>>>>>>>>>>Received Height:-", String.valueOf(imageHeight));
        Log.d(">>>>>>>>>>>>>Job ID:-", uniqueID);
        Log.d(">>>>>>>>>>>>>Transformation Listener:-", String.valueOf(listener));
        //Log.d(">>>>>>>>>>>>>Frame Rate:-", FRAME_RATE);

        mediaTransformer.transform(uniqueID,
                Uri.parse(pathValue),
                destPath,
                MediaFormat.createVideoFormat("video/mp4v-es", imageWidth , imageHeight),
                MediaFormat.createAudioFormat("audio/mp4a-latm" ,48000 , 2),
                listener,
                null);
        Toast.makeText(reactContext, ">>>>> Doind Something <<<<<", Toast.LENGTH_LONG).show();
        //startTransformation(Uri.parse(pathValue),
        //        destPath,
        //        );
    }

    /* his Method is copied from LiTr as it is which is used for Transcode functionality */
    public void startTransformation(@NonNull SourceMedia sourceMedia,
                                    @NonNull TargetMedia targetMedia,
                                    @NonNull TrimConfig trimConfig,
                                    @NonNull TransformationState transformationState) {
        if (targetMedia.getIncludedTrackCount() < 1) {
            return;
        }

        if (targetMedia.targetFile.exists()) {
            targetMedia.targetFile.delete();
        }

        transformationState.requestId = UUID.randomUUID().toString();
        MediaTransformationListener transformationListener = new MediaTransformationListener(reactContext,
                transformationState.requestId,
                transformationState,
                targetMedia);

        try {
            int videoRotation = 0;
            for (MediaTrackFormat trackFormat : sourceMedia.tracks) {
                if (trackFormat.mimeType.startsWith("video")) {
                    videoRotation = ((VideoTrackFormat) trackFormat).rotation;
                    break;
                }
            }
            MediaTarget mediaTarget = new MediaMuxerMediaTarget(targetMedia.targetFile.getPath(),
                    targetMedia.getIncludedTrackCount(),
                    videoRotation,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            List<TrackTransform> trackTransforms = new ArrayList<>(targetMedia.tracks.size());


            MediaRange mediaRange = trimConfig.enabled
                    ? new MediaRange(
                    TimeUnit.MILLISECONDS.toMicros((long) (trimConfig.range.get(0) * 1000)),
                    TimeUnit.MILLISECONDS.toMicros((long) (trimConfig.range.get(1) * 1000)))
                    : new MediaRange(0, Long.MAX_VALUE);
            MediaSource mediaSource = new MediaExtractorMediaSource(reactContext, sourceMedia.uri, mediaRange);

            for (TargetTrack targetTrack : targetMedia.tracks) {
                if (!targetTrack.shouldInclude) {
                    continue;
                }
                TrackTransform.Builder trackTransformBuilder = new TrackTransform.Builder(mediaSource,
                        targetTrack.sourceTrackIndex,
                        mediaTarget)
                        .setTargetTrack(trackTransforms.size())
                        .setTargetFormat(targetTrack.shouldTranscode ? createMediaFormat(targetTrack) : null)
                        .setEncoder(new MediaCodecEncoder())
                        .setDecoder(new MediaCodecDecoder());
                if (targetTrack.format instanceof VideoTrackFormat) {
                    trackTransformBuilder.setRenderer(new GlVideoRenderer(createGlFilters(sourceMedia,
                            (TargetVideoTrack) targetTrack,
                            0.56f,
                            new PointF(0.6f, 0.4f),
                            30)));
                }

                trackTransforms.add(trackTransformBuilder.build());
            }

            MediaTransformer mediaTransformer = new MediaTransformer(reactContext);

            mediaTransformer.transform(transformationState.requestId,
                    trackTransforms,
                    transformationListener,
                    MediaTransformer.GRANULARITY_DEFAULT);
        } catch (MediaTransformationException ex) {
            Log.e(TAG, "Exception when trying to perform track operation", ex);
        }
    }

    @Nullable
    private MediaFormat createMediaFormat(@Nullable TargetTrack targetTrack) {
        MediaFormat mediaFormat = null;
        if (targetTrack != null && targetTrack.format != null) {
            mediaFormat = new MediaFormat();
            if (targetTrack.format.mimeType.startsWith("video")) {
                mediaFormat = createVideoMediaFormat((VideoTrackFormat) targetTrack.format);
            } else if (targetTrack.format.mimeType.startsWith("audio")) {
                mediaFormat = createAudioMediaFormat((AudioTrackFormat) targetTrack.format);
            }
        }

        return mediaFormat;
    }

    @NonNull
    private MediaFormat createVideoMediaFormat(@NonNull VideoTrackFormat trackFormat) {
        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString(MediaFormat.KEY_MIME, trackFormat.mimeType);
        mediaFormat.setInteger(MediaFormat.KEY_WIDTH, trackFormat.width);
        mediaFormat.setInteger(MediaFormat.KEY_HEIGHT, trackFormat.height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, trackFormat.bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, trackFormat.keyFrameInterval);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, trackFormat.frameRate);
        mediaFormat.setLong(MediaFormat.KEY_DURATION, trackFormat.duration);
        mediaFormat.setInteger(KEY_ROTATION, trackFormat.rotation);

        return mediaFormat;
    }

    @NonNull
    private MediaFormat createAudioMediaFormat(@NonNull AudioTrackFormat trackFormat) {
        MediaFormat mediaFormat = new MediaFormat();
        mediaFormat.setString(MediaFormat.KEY_MIME, trackFormat.mimeType);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, trackFormat.channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, trackFormat.samplingRate);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, trackFormat.bitrate);
        mediaFormat.setLong(MediaFormat.KEY_DURATION, trackFormat.duration);

        return mediaFormat;
    }

    @Nullable
    private List<GlFilter> createGlFilters(@NonNull SourceMedia sourceMedia,
                                           @Nullable TargetVideoTrack targetTrack,
                                           float overlayWidth,
                                           @NonNull PointF position,
                                           float rotation) {
        List<GlFilter> glFilters = null;
        if (targetTrack != null && targetTrack.overlay != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(reactContext.getContentResolver().openInputStream(targetTrack.overlay));
                if (bitmap != null) {
                    float overlayHeight;
                    VideoTrackFormat sourceVideoTrackFormat = (VideoTrackFormat) sourceMedia.tracks.get(targetTrack.sourceTrackIndex);
                    if (sourceVideoTrackFormat.rotation == 90 || sourceVideoTrackFormat.rotation == 270) {
                        float overlayWidthPixels = overlayWidth * sourceVideoTrackFormat.height;
                        float overlayHeightPixels = overlayWidthPixels * bitmap.getHeight() / bitmap.getWidth();
                        overlayHeight = overlayHeightPixels / sourceVideoTrackFormat.width;
                    } else {
                        float overlayWidthPixels = overlayWidth * sourceVideoTrackFormat.width;
                        float overlayHeightPixels = overlayWidthPixels * bitmap.getHeight() / bitmap.getWidth();
                        overlayHeight = overlayHeightPixels / sourceVideoTrackFormat.height;
                    }

                    PointF size = new PointF(overlayWidth, overlayHeight);

                    GlFilter filter = TransformationUtil.createGlFilter(reactContext,
                            targetTrack.overlay,
                            size,
                            position,
                            rotation);
                    if (filter != null) {
                        glFilters = new ArrayList<>();
                        glFilters.add(filter);
                    }
                }
            } catch (IOException ex) {
                Log.e(TAG, "Failed to extract audio track metadata: " + ex);
            }
        }

        return glFilters;
    }

    /* Use if required */
    public void stopCompression(){
        MediaTransformer mediaTransformer = new MediaTransformer(reactContext);

        mediaTransformer.cancel(uniqueID);
    }

    @NonNull
    @Override
    public String getName() {
        return "ABC";
    }
}
