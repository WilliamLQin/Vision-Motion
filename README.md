[![release](https://img.shields.io/badge/release-v1.0-blue.svg)](https://raw.githubusercontent.com/WilliamLQin/Vision-Motion/master/VisionMotion.apk)
[![OpenCV](https://img.shields.io/badge/OpenCV-3.0.0-red.svg)](https://opencv.org/releases.html)
[![Android-Studio](https://img.shields.io/badge/Android%20Studio-3.0.0-brightgreen.svg)](https://developer.android.com/studio/index.html)
[![API](https://img.shields.io/badge/API-21+-green.svg)](https://developer.android.com/about/versions/android-5.0.html)

# Vision Motion

![alt icon](https://raw.githubusercontent.com/WilliamLQin/MotionSensor/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png)

Vision Motion is a mobile app that uses the camera to track an object and graph position, velocity, and acceleration. <br>

<br>

Working in Android Studio 3.0. <br>
Running on OpenCV 3.3.0 Android pack release. <br>

## Getting Started

Vision Motion is a project that has been directly built in Android Studio. <br>
Download the project and open it in Android Studio! <br>

## Built With

* [Android Studio](https://developer.android.com/studio/index.html) - IDE for compiling Android apps
* [OpenCV](https://opencv.org/) - Computer vision for finding objects
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) - Used to create graphs

## Troubleshooting

Some devices may display the camera feed in BGR instead of RGB.
Please modify the code in MainActivity.java lines 573-574 to change the output:
```
//mRgbaMat = mBgrMat;
mRgbaMat = inMat;
```
Try commenting either one of them and see which one works.

If you are having any issues with the OpenCV library, try reinstalling the android pack at the [OpenCV website](https://opencv.org/releases.html). <br>
You can follow this [tutorial](https://www.learn2crack.com/2016/03/setup-opencv-sdk-android-studio.html) to setup the OpenCV library again. <br>
<br>
If you are still running on Android Studio 2.3.3 you will have to modify the gradle files to back to your version of certain libraries.

