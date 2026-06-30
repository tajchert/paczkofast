package pl.tajchert.paczko.fast

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// =============================================================================
// APPLICATION CLASS
// =============================================================================
// The @HiltAndroidApp annotation triggers Hilt's code generation.
//
// ## What Happens Here
//
// 1. Hilt generates a base class that extends Application
// 2. This class is used as the application container
// 3. All Hilt dependency injection starts from here
//
// ## Manifest Registration
//
// Don't forget to register this in AndroidManifest.xml:
// ```xml
// <application android:name=".PaczkofastApplication" ...>
// ```
// =============================================================================

@HiltAndroidApp
class PaczkofastApplication : Application()
