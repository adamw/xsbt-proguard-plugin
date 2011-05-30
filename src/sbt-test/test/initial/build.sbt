seq(ProguardPlugin.proguardSettings :_*)

proguardOptions += keepMain("Test")
