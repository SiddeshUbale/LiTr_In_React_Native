import React, { Component } from "react";
import { StyleSheet, Text, View, Image, Button } from "react-native";
import ImagePicker from "react-native-image-picker";
import RNFetchBlob from "rn-fetch-blob";
import { ProcessingManager } from "react-native-video-processing";
import VideoPlayer from "react-native-video-controls";
import Video from "react-native-video";
var RNFS = require("react-native-fs");
const shortid = require("shortid");

import ABC from "./CustomModule";

export default class App extends Component {
  state = {
    pickedImage: null,
    originalSize: "0",
    compressedSize: "0",
    initialPath: null,
    fileToRead: null,
    imageWidth: "0",
    imageHeight: "0",
  };

  reset = () => {
    this.setState({
      pickedImage: null,
      originalSize: "0",
      compressedSize: "0",
      initialPath: null,
      fileToRead: null,
      imageWidth: "0",
      imageHeight: "0",
    });
  };

  /**
   * The first arg is the options object for customization (it can also be null or omitted for default options),
   * The second arg is the callback which sends object: response (more info below in README)
   */

  pickImageHandler = () => {
    ImagePicker.launchImageLibrary({ mediaType: "video" }, async (res) => {
      // ImagePicker.showImagePicker(
      //   { title: "Pick an Image", maxWidth: 800, maxHeight: 600 },
      //   (res) => {
      if (res.didCancel) {
        console.log("User cancelled!");
      } else if (res.error) {
        console.log("Error", res.error);
      } else {
        this.setState({
          pickedImage: { uri: res.uri },
          initialPath: res.path,
        });
        console.log("Resource:", res);
        await Image.getSize(res.uri, (width, height) => {
          console.log("Width : ", width, " Height : ", height);
          this.setState({
            imageWidth: width,
            imageHeight: height,
          });
        });
        RNFetchBlob.fs
          .stat(res.path)
          .then((stats) => {
            console.log("Stats For Original :- ", stats);
            this.setState({
              originalSize: stats.size,
            });
          })
          .catch((err) => {});
      }
    });
  };

  resetHandler = () => {
    console.log("Image Cleared");
    this.reset();
  };

  //Compression code Below//
  compressionHandler = () => {

    console.log(">>>>>>>>>>>>>>>> !!!!! Will do compression here !!!!! <<<<<<<<<<<<<<<<<<");

    let URL = this.state.pickedImage;
    const source = URL.uri;
    console.log("##################### Path is : -", source);

    const destPath =
              `${RNFS.TemporaryDirectoryPath}/${shortid.generate()}` + ".mp4";
            console.log("destPath: ", destPath);

    ABC.compressVid(source, destPath, Number.parseInt(this.state.imageWidth), Number.parseInt(this.state.imageHeight));

    // console.log(
    //   "Reduced Width : ",
    //   Number.parseInt(this.state.imageWidth / 4, 10)
    // );
    // console.log(
    //   "Reduced Height : ",
    //   Number.parseInt(this.state.imageHeight / 4, 10)
    // );
    // let URL = this.state.pickedImage;
    // const source = URL.uri;
    // console.log("##################### Path is : -", source);
    // const options = {
    //   width: this.state.imageWidth / 4, //Number.parseInt((this.state.imageWidth / 4, 10)),
    //   height: this.state.imageHeight / 4, //Number.parseInt((this.state.imageHeight / 4, 10)),
    //   // bitrateMultiplier: 100,
    //   // minimumBitrate: 3000,
    //   removeAudio: false, // default is false
    //   path: source,
    // };
    // ProcessingManager.compress(this.state.initialPath, options) // like VideoPlayer compress options
    //   .then(async (data) => {
    //     console.log("Compressed data :- ", data);
    //     RNFetchBlob.fs
    //       .stat(data.source)
    //       .then(async (stats) => {
    //         console.log("Stats For Compressed :- ", stats);
    //         this.setState({
    //           compressedSize: stats.size,
    //         });
    //         const destPath =
    //           `${RNFS.TemporaryDirectoryPath}/${shortid.generate()}` + ".mp4";
    //         console.log("destPath: ", destPath);
    //         RNFS.copyFile(stats.path, destPath);
    //         this.setState({
    //           fileToRead: stats.path,
    //         });
    //         console.log(stats.path);
    //       })
    //       .catch((err) => {
    //         console.log("error: ", err);
    //       });
    //   });
  };

  readHandler = () => {
    console.log("Reading File at ", this.state.fileToRead, " location");

    // Linking.getInitialURL()
    //   .then((url) => {
    //     if (url) {
    //       console.log(url);
    //     }
    //   })
    //   .catch((err) => console.error("An error occurred", err));
  };

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.textStyle}>Pick Video From Gallery </Text>

        <View style={styles.vidView}>
          <Video
            style={styles.compressedVid}
            resizeMode={"cover"}
            repeat={true}
            source={this.state.pickedImage} //{{ uri: this.state.fileToRead }} // Can be a URL or a local file.
            ref={(ref) => {
              this.player = ref;
            }} // Store reference
            onBuffer={this.onBuffer} // Callback when remote video is buffering
            onError={this.videoError} // Callback when video cannot be loaded
          />
        </View>

        <View style={styles.button}>
          <Button title="Pick Video" onPress={this.pickImageHandler} />
          <Button title="Compress" onPress={this.compressionHandler} />
        </View>

        <View style={styles.compressButton}>
          <Button title="Reset" onPress={this.resetHandler} />
          <Button title="Read" onPress={this.readHandler} />
        </View>

        <View>
          <Text style={styles.textStyle}>
            Original Size : {this.state.originalSize / 1000000} MB
          </Text>
          <Text style={styles.textStyle}>
            Compressed Size : {this.state.compressedSize / 1000000} MB
          </Text>
          <Text style={styles.textStyle}>
            Compression Ratio :
            {Number.parseInt(
              (this.state.compressedSize / this.state.originalSize) * 100,
              10
            )}
            %
          </Text>
        </View>

        {/* <View style={styles.vidView}>
          <Video
            style={styles.compressedVid}
            resizeMode={"cover"}
            repeat={true}
            source={{ uri: this.state.fileToRead }} //{{ uri: this.state.fileToRead }} // Can be a URL or a local file.
            ref={(ref) => {
              this.player = ref;
            }} // Store reference
            onBuffer={this.onBuffer} // Callback when remote video is buffering
            onError={this.videoError} // Callback when video cannot be loaded
          />
        </View> */}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    alignItems: "center",
  },
  textStyle: {
    fontWeight: "bold",
    fontSize: 20,
    textAlign: "center",
    color: "#1a8cff",
    marginTop: 20,
  },
  placeholder: {
    borderWidth: 1,
    width: "100%",
    height: 200,
  },
  button: {
    width: "80%",
    marginTop: 20,
    flexDirection: "row",
    justifyContent: "space-around",
  },
  compressButton: {
    width: "80%",
    marginTop: 20,
    flexDirection: "row",
    justifyContent: "space-around",
  },
  previewImage: {
    width: "100%",
    height: "100%",
  },
  vidView: {
    borderWidth: 1,
    width: "100%",
    height: 250,
  },
  compressedVid: {
    width: "100%",
    height: "100%",
  },
});
