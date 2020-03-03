InAppUpdater ![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)
===================
AndEx Alert Dialog is a library like ios alert dialog with many more features that optimized for both ltr and rtl languages.


**This library is also available at JitPack.io**

[![](https://jitpack.io/v/rpahade3011/InAppUpdater.svg)](https://jitpack.io/#rpahade3011/InAppUpdater)

## Installation

Add the JitPack repository to your build file.

Add it in your root build.gradle at the end of repositories:

```bash
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Add the dependency

```bash
dependencies {
	        implementation 'com.github.rpahade3011:InAppUpdater:Tag'
	}
```

OR

Add the dependency

```bash
dependencies {
	        implementation 'com.android.rudraksh.inappupdate:InAppUpdater:1.0'
	}
```

## Usage
In your Activity or Fragment include the InAppUpdateManager to use.


```java
import com.android.rudraksh.inappupdate.InAppUpdateManager;
import com.android.rudraksh.inappupdate.UpdateType;
import com.android.rudraksh.inappupdate.UserResponse;

private InAppUpdateManager mInAppUpdateManager;
```

Initalize the InAppUpdateManger

```bash
mInAppUpdateManager = InAppUpdateManager.initialize(this);
```

Starts checking for new update if found.

```bash
mInAppUpdateManager.checkAvailableUpdateIfFound(versionCode -> {
            Log.d(TAG, "Found new version: " + versionCode);
            txtAvailableVersion.setText(String.valueOf(versionCode));
            mInAppUpdateManager.popupDialogForUpdateAvailability(userResponse -> {
                if (userResponse == UserResponse.ACCEPTED) {
                    mInAppUpdateManager.setDefaultUpdateMode(UpdateType.APP_UPDATE_TYPE_FLEXIBLE)
                            .startCheckingForUpdates();
                } else {
                    mInAppUpdateManager.popupSnackbarForUpdateRejection();
                }
            });
        });
```
## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[GNU General Public License v3.0](https://choosealicense.com/licenses/gpl-3.0/)
