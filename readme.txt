* This is FFmpeg4Android simplified interface demo.

* It will run an ffmpeg command on your Android device using the ffmpeg4android simplified interface.

* To run the demo make sure you create on your device /sdcard/videokit folder,
 and copy in.mp4 from this project assets to this folder.
 
* After running the demo, the out.mp4 will be created in the /sdcard/videokit folder.
  You can use any player to play the output file.


* You can change the default working directory location by setting your working directory in the run method
  (replace the default /sdcard/videokit):
  vk.run(utilConvertToComplex(commandStr), "/sdcard/videokit", getApplicationContext());
  
  also make sure the demo video location is set correctly:
  final String demoVideoPath = "/sdcard/videokit/in.mp4";
  
* Supported devices: this will work on ARMv7 and above devices (most devices today are ARMv7 or above).
  If you use an emulator, make sure you select devices that support ARMv7 or above (e.g Nexus 7).
  