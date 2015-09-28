# TopGameDeals
Sample demostrating the use of Android support library 23.0.1 and Android design library to use Material Theme in most Android versions.

###Introduction.
Demostrating ussage of CoordinatorLayout, AppBarLayout, TabLayout to display top deals of digital PC games using CheapShark API v1.0. CheapShark keeps track of game prices on a number of stores such as Steam, GamersGate, and Amazon.
The application takes advantage of Android sync framework and Content Providers to always keep the latest deals.
It also support search and price alert features of CheapShark API.

###Pre-requisites
- Android SDK v23
- Android Build Toolsv23
- Android Support Repository

###Getting Started
The sample uses the Gradle build system. To build this project, import project in Android Studio.
Add KEYSTORE_PASSWORD and KEY_PASSWORD to gradle.properties.
Set app gradle.build key path in storeFile.


###Libraries used
- Android Support Library 23.0.1
- picasso 2.5.2
- okhttp 2.5.0
- gson 2.3.1

###ScreenShoots
![Main Screen](/screenshots/MainScreen.png)
![Detail Screen](/screenshots/Details.png)
![Settings Screen](/screenshots/Settings.png)
![Main Screen Landscape](/screenshots/MainScreen-landscape.png)

###Knonkn issues.
- v7.PreferenceFragmentCompat. Bigger font issue.
- v7.PreferenceFragmentCompat. EditTextPreference accent color is not applied in dialog. 

###License

Copyright 2015 Ricardo Casarez.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
