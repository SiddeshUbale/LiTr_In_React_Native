package com.newproject;


import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.TransformationOptions;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;
import com.linkedin.android.litr.utils.MediaFormatUtils;
import com.linkedin.android.litr.utils.TranscoderUtils;
import com.newproject.utils.TransformationUtil;
import com.newproject.SourceMedia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//import wseemann.media.FFmpegMediaMetadataRetriever;

public class CustomModule extends ReactContextBaseJavaModule implements MediaPickerListener{

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

        //Toast.makeText(reactContext, pathValue, Toast.LENGTH_LONG).show();

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

        TransformationOptions transformationOptions;

        Log.d(">>>>>>>>>>>>>Received Path:-", pathValue);
        Log.d(">>>>>>>>>>>>>Destination Path:-", destPath);
        Log.d(">>>>>>>>>>>>>Received Width:-", String.valueOf(imageWidth));
        Log.d(">>>>>>>>>>>>>Received Height:-", String.valueOf(imageHeight));
        Log.d(">>>>>>>>>>>>>Job ID:-", uniqueID);
        Log.d(">>>>>>>>>>>>>Transformation Listener:-", String.valueOf(listener));
        //Log.d(">>>>>>>>>>>>>Frame Rate:-", FRAME_RATE);

        //mediaTransformer.transform(uniqueID,
        //        Uri.parse(pathValue),
        //        destPath,
        //        MediaFormat.createVideoFormat("video/mp4v-es", imageWidth , imageHeight),
        //        MediaFormat.createAudioFormat("audio/mp4a-latm" ,48000 , 2),
        //        listener,
        //        null);

        //Toast.makeText(reactContext, ">>>>> Doing Compression Now <<<<<", Toast.LENGTH_LONG).show();

        TransformationPresenter presenter = new TransformationPresenter(reactContext, mediaTransformer);
        SourceMedia sourceMedia = new SourceMedia();
        TargetMedia targetMedia = new TargetMedia();
        TrimConfig trimConfig = new TrimConfig();
        TransformationState transformationState = new TransformationState();



        sourceMedia.uri = Uri.parse(pathValue);
        sourceMedia.size = TranscoderUtils.getSize(reactContext, Uri.parse(pathValue));
        sourceMedia.duration = getMediaDuration(Uri.parse(pathValue)) / 1000f;

        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(reactContext, Uri.parse(pathValue), null);
            sourceMedia.tracks = new ArrayList<>(mediaExtractor.getTrackCount());

            for (int track = 0; track < mediaExtractor.getTrackCount(); track++) {
                MediaFormat mediaFormat = mediaExtractor.getTrackFormat(track);
                String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (mimeType == null) {
                    continue;
                }

                if (mimeType.startsWith("video")) {
                    VideoTrackFormat videoTrack = new VideoTrackFormat(track, mimeType);
                    videoTrack.width = getInt(mediaFormat, MediaFormat.KEY_WIDTH);
                    videoTrack.height = getInt(mediaFormat, MediaFormat.KEY_HEIGHT);
                    videoTrack.duration = getLong(mediaFormat, MediaFormat.KEY_DURATION);
                    videoTrack.frameRate = MediaFormatUtils.getFrameRate(mediaFormat, -1).intValue();
                    videoTrack.keyFrameInterval = MediaFormatUtils.getIFrameInterval(mediaFormat, -1).intValue();
                    videoTrack.rotation = getInt(mediaFormat, KEY_ROTATION, 0);
                    videoTrack.bitrate = getInt(mediaFormat, MediaFormat.KEY_BIT_RATE);
                    sourceMedia.tracks.add(videoTrack);
                } else if (mimeType.startsWith("audio")) {
                    AudioTrackFormat audioTrack = new AudioTrackFormat(track, mimeType);
                    audioTrack.channelCount = getInt(mediaFormat, MediaFormat.KEY_CHANNEL_COUNT);
                    audioTrack.samplingRate = getInt(mediaFormat, MediaFormat.KEY_SAMPLE_RATE);
                    audioTrack.duration = getLong(mediaFormat, MediaFormat.KEY_DURATION);
                    audioTrack.bitrate = getInt(mediaFormat, MediaFormat.KEY_BIT_RATE);
                    sourceMedia.tracks.add(audioTrack);
                } else {
                    sourceMedia.tracks.add(new GenericTrackFormat(track, mimeType));
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Failed to extract sourceMedia", ex);
        }

        System.out.println("SourceMedia >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ sourceMedia.uri.toString());
        System.out.println("SourceMedia >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ sourceMedia.duration);
        System.out.println("SourceMedia >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ sourceMedia.size);
        System.out.println("SourceMedia >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ sourceMedia.tracks.toString());

        updateTrimConfig(trimConfig, sourceMedia);

        System.out.println("TrimConfig >>>>>>>>>>>>>>>>>>>>>>>>>>>>"+ trimConfig.range.toString());


        File targetFile = new File(TransformationUtil.getTargetFileDirectory(reactContext.getApplicationContext()),
                "transcoded_" + TransformationUtil.getDisplayName(reactContext, sourceMedia.uri));

        targetMedia.setTargetFile(targetFile);
        targetMedia.setTracks(sourceMedia.tracks);

        presenter.startTransformation(
                sourceMedia,
                targetMedia,
                trimConfig,
                transformationState
        );
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

    @Override
    public void onMediaPicked(@NonNull Uri uri) {

        SourceMedia sourceMedia = null;
        
        sourceMedia.uri = uri;
        sourceMedia.size = TranscoderUtils.getSize(reactContext, uri);
        sourceMedia.duration = getMediaDuration(uri) / 1000f;

    }

    private long getMediaDuration(@NonNull Uri uri) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(reactContext, uri);
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Long.parseLong(durationStr);
    }

    private int getInt(@NonNull MediaFormat mediaFormat, @NonNull String key) {
        return getInt(mediaFormat, key, -1);
    }

    private int getInt(@NonNull MediaFormat mediaFormat, @NonNull String key, int defaultValue) {
        if (mediaFormat.containsKey(key)) {
            return mediaFormat.getInteger(key);
        }
        return defaultValue;
    }

    private long getLong(@NonNull MediaFormat mediaFormat, @NonNull String key) {
        if (mediaFormat.containsKey(key)) {
            return mediaFormat.getLong(key);
        }
        return -1;
    }

    protected void updateTrimConfig(@NonNull TrimConfig trimConfig, @NonNull SourceMedia sourceMedia) {
        trimConfig.setTrimEnd(sourceMedia.duration);
    }
}
