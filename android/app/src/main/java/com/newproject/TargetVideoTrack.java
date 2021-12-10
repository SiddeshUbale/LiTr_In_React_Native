package com.newproject;

import android.net.Uri;

public class TargetVideoTrack extends TargetTrack {

    public boolean shouldApplyOverlay;
    public Uri overlay;

    public TargetVideoTrack(int sourceTrackIndex,
                            boolean shouldInclude,
                            boolean shouldTranscode,
                            VideoTrackFormat format) {
        super(sourceTrackIndex, shouldInclude, shouldTranscode, format);
    }

    public VideoTrackFormat getTrackFormat() {
        return (VideoTrackFormat) format;
    }
}
