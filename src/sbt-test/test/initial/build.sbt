seq(ProguardPlugin.proguardSettings :_*)

name := "default"

proguardOptions += keepMain("Test")
