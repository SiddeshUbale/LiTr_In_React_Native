package com.newproject;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;

public class TargetTrack extends BaseObservable {
    public int sourceTrackIndex;
    public boolean shouldInclude;
    public boolean shouldTranscode;
    public MediaTrackFormat format;

    public TargetTrack(int sourceTrackIndex, boolean shouldInclude, boolean shouldTranscode, @NonNull MediaTrackFormat format) {
        this.sourceTrackIndex = sourceTrackIndex;
        this.shouldInclude = shouldInclude;
        this.shouldTranscode = shouldTranscode;
        this.format = format;
    }
}
