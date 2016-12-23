Setup Instructions:
1. Install Libx toolbar (Firefox version) from: http://libx.org/
2. Add the "emu.js" file to libx.
   Windows: 
    Put the "emu.js" file under the Firefox profile folder. Specifically, the path should look like the following:
	%APPDATA%\Mozilla\Firefox\Profiles\xxxxxxxx.default\extensions\{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}\chrome\libx\content\libx
   Mac:
    Find libx extension file (*.xpi), rename it to *.zip and extract archive. Put "emu.js" file under content/libx folder. 
3. Add the following line in both "libxprefs.xul" and "libx.xul" files:
	<script type="application/x-javascript" src="chrome://libx/content/emu.js" />,
below the following lines in both files:
	<script type="application/x-javascript" src="chrome://libx/content/xxxxx.js" />
[3.1. for Mac users]:
   Zip the content of extension directory (not the directory itself) and rename back to *.xpi.
4. Setup an HTTP server (e.g., at http://www.someplace.org) and update the following line in "emu.js" to match your web server:
	var _getUrl = "http://www.someplace.org";
5. Put the "savePage.cgi" on the HTTP server, and update the following line in "emu.js" file accordingly:
	var _postUrl = "http://www.someplace.org/savePage.cgi?";
6. Open Firefox, search Google, browse Web pages(the tracking code should start capturing your behavior). If lines containing "EMU.0.7" appear in "access_log" file on the HTTP server, the setup is successfully completed.

Notes:
1. We are still working on improving the code, please let us know if you find any bugs.
2. We only used the code in MS windows environment.
