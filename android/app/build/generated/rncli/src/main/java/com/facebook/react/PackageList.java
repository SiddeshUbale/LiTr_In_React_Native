
package com.facebook.react;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import java.util.Arrays;
import java.util.ArrayList;

import com.linkedin.android.litr.filter.BuildConfig;
import com.linkedin.android.litr.filter.R;

// react-native-compressor
import com.reactnativecompressor.CompressorPackage;
// react-native-fs
import com.rnfs.RNFSPackage;
// react-native-image-picker
import com.imagepicker.ImagePickerPackage;
// react-native-video
import com.brentvatne.react.ReactVideoPackage;
// react-native-video-processing
import com.shahenlibrary.RNVideoProcessingPackage;
// rn-fetch-blob
import com.RNFetchBlob.RNFetchBlobPackage;

public class PackageList {
  private Application application;
  private ReactNativeHost reactNativeHost;
  public PackageList(ReactNativeHost reactNativeHost) {
    this.reactNativeHost = reactNativeHost;
  }

  public PackageList(Application application) {
    this.reactNativeHost = null;
    this.application = application;
  }

  private ReactNativeHost getReactNativeHost() {
    return this.reactNativeHost;
  }

  private Resources getResources() {
    return this.getApplication().getResources();
  }

  private Application getApplication() {
    if (this.reactNativeHost == null) return this.application;
    return this.reactNativeHost.getApplication();
  }

  private Context getApplicationContext() {
    return this.getApplication().getApplicationContext();
  }

  public ArrayList<ReactPackage> getPackages() {
    return new ArrayList<>(Arrays.<ReactPackage>asList(
      new MainReactPackage(),
      new CompressorPackage(),
      new RNFSPackage(),
      new ImagePickerPackage(),
      new ReactVideoPackage(),
      new RNVideoProcessingPackage(),
      new RNFetchBlobPackage()
    ));
  }
}
