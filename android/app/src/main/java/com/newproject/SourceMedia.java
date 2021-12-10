package com.newproject;

import android.net.Uri;
import androidx.databinding.BaseObservable;

import java.util.ArrayList;
import java.util.List;

public class SourceMedia extends BaseObservable {

    public Uri uri;
    public long size;
    public float duration;

    public List<MediaTrackFormat> tracks = new ArrayList<>();
}
