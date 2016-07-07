Pulsator4Android
===========
[![Build Status](https://travis-ci.org/booncol/Pulsator4Droid.svg?branch=master)](https://travis-ci.org/booncol/Pulsator4Droid)
[ ![Download](https://api.bintray.com/packages/booncol/maven/pulsator4droid/images/download.svg) ](https://bintray.com/booncol/maven/pulsator4droid/_latestVersion)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.bclogic/pulsator4droid/badge.svg)](https://maven-badges.herokuapp.com/maven-central/pl.bclogic/pulsator4droid)
[![License](http://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat)](http://mit-license.org)
[![Twitter](https://img.shields.io/badge/twitter-@booncol-blue.svg?style=flat)](http://twitter.com/booncol)

Pulse animation for Android.

![](demo.gif)

##Maven

```
<dependency>
  <groupId>pl.bclogic</groupId>
  <artifactId>pulsator4droid</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

##Gradle

```
compile 'pl.bclogic:pulsator4droid:1.0.0'
```

##Usage
###In layout

```xml
<pl.bclogic.pulsator4droid.library.PulsatorLayout
	android:id="@+id/pulsator"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	app:pulse_count="4"
	app:pulse_duration="7000"
	app:pulse_repeat="0"
	app:pulse_color="@color/colorAccent"
	app:pulse_startFromScratch="false">
</pl.bclogic.pulsator4droid.library.PulsatorLayout>
```

Use following properties

- `pulse_count` : Number of pulse circles
- `pulse_duration` : Duration in milliseconds of single pulse
- `pulse_repeat` : Number of pulse repeats. Zero means `INFINITE`
- `pulse_color` : ARGB pulse color
- `pulse_startFromScratch` : Set to true if animation should start from the beginning

###In activity

```java
PulsatorLayout pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
pulsator.start();
```

##Demo

Try the demo app and change the count and duration parameters in real time.

##Author

**Lukasz Majda**


##License

```
The MIT License (MIT)

Copyright (c) 2016 Lukasz Majda

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```