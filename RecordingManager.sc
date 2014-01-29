/*
	RecordingManager, a GUI for post-processing audiofiles recorded in SuperCollider
	(c) 2009 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
	
	Part of CNToolsSC3
	http://github.com/cappelnord/CNToolsSC3
*/

RecordingManager : Object
{
	var <>path, <title;
	var items;
	
	var <window, <itemView, refreshButton;
	var <compressedPath, <processedPath;
	var sampleMenu, compressionMenu, bitrateMenu, <statusText;
	var doNormalizeButton, doTrimButton, doFadeButton;
	var <doNormalize, <doTrim, <doFade;
	var <bitrate, <compressionCommand, <compressionExtension, <sampleFormat;
	var <>lock = false;
	var compressionIndex;
		
	classvar sampleOptions = #["int16", "int24", "float"];
	
	// TODO: Find a Way to specify compressors with more sense. 
	
	classvar allCompressionOptions = #[	["Core Audio AAC", "afconvert", "-f m4af -d aac -b {BR}000 {IN} {OUT}", "m4a"], 
							   		["Lame MP3 (Ports)", "/opt/local/bin/lame", "-b {BR} {IN} {OUT}", "mp3"],
							   		["Lame MP3", "/usr/local/bin/lame", "-b {BR} {IN} {OUT}", "mp3"]];
							 
	classvar bitrateOptions = #["32", "64", "96", "128", "160" ,"192", "256", "320"];

	*new {|path, title, compressionType|
		path = path ? thisProcess.platform.recordingsDir;
		title = title ? "Recording Manager";
		^super.new.init(path, title, compressionType);
	}
	
	init {|a_path, a_title, a_compressionType|
	
		var layout, compressionOptions;
		
		path = a_path;
		title = a_title;
		
		compressedPath = path ++ "/compressed";
		processedPath = path ++ "/processed";
		
		items = List.new; // just in case;
		
		this.createDirs;
		
		window = Window("RecordingManager", Rect(200,200,460,510), false);
		layout = window.addFlowLayout(10@10, 3@3);
		
		StaticText.new(window, 130@20).string_("Output Sample Format:");
		
		sampleMenu = PopUpMenu(window, 130@20);
		sampleMenu.items_(sampleOptions);
		sampleMenu.action_({sampleFormat = sampleOptions[sampleMenu.value];});
		sampleFormat = sampleOptions[0]; // init
		
		StaticText.new(window, 10@20); // SPACER ???
		doNormalizeButton = Button(window, 20@20);
		StaticText.new(window, 130@20).string_("Normalize Audio");
		doNormalizeButton.states_([["X", Color.black, Color.gray], [" ", Color.black, Color.gray]]);
		doNormalizeButton.action_({|button| doNormalize = button.value == 0;});
		doNormalize = true;
		
		layout.nextLine;
		
		// to check which compressors are available on the system. 
		compressionOptions = allCompressionOptions.select({|item| item[1].systemCmd <= 512;});
		
		// possibility to specify selected compression type.
		// RecordingManager.new(compressionType: "mp3").	
		if (a_compressionType.notNil){
			compressionIndex = compressionOptions.detectIndex{ |item, i|
				//e.g either "Lame MP3" or "mp3"
				(item[0].asString == a_compressionType) || (item[3].asString == a_compressionType)		
			};
			if (compressionIndex == nil){
				compressionIndex = 0;
				"The defined compression type is not installed!".warn	
			};
		}{
			compressionIndex = 0;
		};
		
		StaticText.new(window, 130@20).string_("Compression Type:");

		compressionMenu = PopUpMenu(window, 130@20);
		compressionMenu.items_(compressionOptions.collect({|item| item[0]}));
		compressionMenu.value_(compressionIndex).action_({
			compressionCommand = compressionOptions.at(compressionMenu.value).at(1) + compressionOptions.at(compressionMenu.value).at(2);
			compressionExtension = compressionOptions[compressionMenu.value].at(3);
			this.refreshFiles; // because extension may have changed... baaad!
		});
		compressionCommand = compressionOptions.at(compressionIndex).at(1) + compressionOptions.at(compressionIndex).at(2); // init
		compressionExtension = compressionOptions.at(compressionIndex).at(3); // init
		
		StaticText.new(window, 10@20); // SPACER ???
		doTrimButton = Button(window, 20@20);
		StaticText.new(window, 130@20).string_("Trim Silence");
		doTrimButton.states_([["X", Color.black, Color.gray], [" ", Color.black, Color.gray]]);
		doTrimButton.action_({|button| doTrim = button.value == 0;});
		doTrim = true;
		
		layout.nextLine;
		
		StaticText.new(window, 130@20).string_("Compression Bitrate:");
		
		bitrateMenu = PopUpMenu(window, 130@20);
		bitrateMenu.items_(bitrateOptions);
		bitrateMenu.action_({bitrate = bitrateOptions[bitrateMenu.value];});
		bitrateMenu.valueAction_(5); // init
		
		StaticText.new(window, 10@20); // SPACER ???
		doFadeButton = Button(window, 20@20);
		StaticText.new(window, 130@20).string_("128 Frames Fades");
		doFadeButton.states_([["X", Color.black, Color.gray], [" ", Color.black, Color.gray]]);
		doFadeButton.action_({|button| doFade = button.value == 0;});
		doFade = true;
		
		layout.nextLine;
		
		refreshButton = Button(window, 263@20);
		refreshButton.states_([["Refresh Files", Color.black, Color.grey]]);
		refreshButton.action_({this.lock.not.if {this.refreshFiles;};});
		layout.nextLine;			

		// ScrollView
		itemView = ScrollView(window, 440@380);
		itemView.hasBorder_(true);
		itemView.hasVerticalScroller_(true);
		itemView.resize = 5;
		itemView.visibleOrigin_(440@380);
		itemView.decorator = FlowLayout(Rect(0,0,440,380), 5@5,5@5);
		layout.nextLine;
		

		statusText = StaticText.new(window,440@20).string_("Warning: Processing may block your Machine.");
		
		this.populate;
		
		window.front;		
	}
	
	refreshFiles {
		this.removeItemViews;
		this.populate;
		itemView.refresh;

	}
	
	createDirs {
		[path, compressedPath, processedPath].do {|item|
			(File.exists(item).not).if {
				systemCmd("mkdir" + item.quote);
			};
		};
	}
	
	removeItemViews {
		items.do {|item|
			item.removeView;
		};
		itemView.decorator = FlowLayout(Rect(0,0,440,380), 5@5,5@5);	}
	
	populate {
		var files = (path ++ "/*.aiff").pathMatch;
		items = List.new;
		files.reverse.do {|item|
			items.add(RecordingManagerItem(this, item));
			itemView.decorator.nextLine;
		};
	}		
}

RecordingManagerItem : Object
{
	var manager;
	
	var <path, <compressedPath, <processedPath, <fileName;
	var <hasProcessed, <hasCompressed;
	var deleteButton, processButton, compressButton, mailButton;
	var compressedFileFormat;
	
	var view;
	
	*new {|manager, path|
		^super.new.init(manager,path);
	}
	
	init {|a_manager, a_path|
		var nextColor;
		
		manager = a_manager;
		this.path_(a_path); // to get everything else set
		
		view = FlowView(manager.itemView, 410@25);
		
		deleteButton = Button(view, 50@20);
		deleteButton.states_([["Delete", Color.black, Color.red]]);
		deleteButton.action_({manager.lock.not.if {this.delete;manager.refreshFiles;};});
		
		processButton = Button(view, 70@20);
				
		hasProcessed.if({
			nextColor = Color.yellow;
		},{
			nextColor = Color.gray;
			
			processButton.action_({
				manager.lock.not.if {
					{
						manager.lock_(true);
						this.process;
						manager.lock_(false);
						{manager.refreshFiles;}.defer;
					}.fork;
				};
			});
		});
		
		processButton.states_([["Process", Color.black, nextColor]]);
		
		
		compressButton = Button(view, 70@20);
		
		hasCompressed.if({
			nextColor = Color.yellow;
		},{
			nextColor = Color.gray;
			compressButton.action_({
				manager.lock.not.if {
					{
						manager.lock_(true);
						hasProcessed.not.if {
							this.process;
						};
						this.compress;
						manager.lock_(false);
						{manager.refreshFiles;}.defer;
					}.fork;
				};
			});
		});
		
		compressButton.states_([["Compress", Color.black, nextColor]]);
		
		// Mail Buttons only make sense on OSX
		
		(thisProcess.platform.name == \osx).if {
			mailButton = Button(view, 40@20);
			
			hasCompressed.if({
				nextColor = Color.green;
				mailButton.action_({this.mail});
			},{
				nextColor = Color.gray;
			});
				
			mailButton.states_([["Mail", Color.black, nextColor]]);
		};
		
		StaticText.new(view, 160@20).string_(fileName);
	}
	
	path_ {|a_path|
		var fileNameWithoutExtension;
		path = a_path;
		fileName = path.basename;
		
		// this looks so like garbage
		fileNameWithoutExtension = fileName.split($.);
		fileNameWithoutExtension.removeAt(fileNameWithoutExtension.size-1);
		fileNameWithoutExtension = fileNameWithoutExtension.join(".");
		
		processedPath = manager.processedPath ++ "/" ++ fileName;
		compressedPath = manager.compressedPath ++ "/" ++ fileNameWithoutExtension ++ "." ++ manager.compressionExtension;
		
		hasProcessed = File.exists(processedPath);
		hasCompressed = File.exists(compressedPath);
	}
	
	removeView {
		view.remove;
	}
	
	delete {
		File.delete(path);
		hasProcessed.if {File.delete(processedPath);};
		manager.statusText.string_("Only sourcefile got deleted.");
	}
	
	
	process {
		var sf, outSf, data, dataOut, inPoint, outPoint, numSamples, numChannels, foundSound, fadePointer, peakAmp, ampMul, err;
		
		{manager.statusText.string_("Processing" + fileName ++ "...");}.defer;
		
		err = {
			sf = SoundFile.new;
			if(sf.openRead(path).not) {"ERROR: Could not open file.".throw;};
			
			numChannels = sf.numChannels;
			numSamples = sf.numChannels * sf.numFrames;
			data = FloatArray.newClear(numSamples);
			sf.readData(data);
			
			inPoint = 0;
			outPoint = numSamples - 1;
			
			0.0001.wait;
			manager.doTrim.if {
				// detecting silence: moving inPoint and outPoint towards each other.
				foundSound = false;
				while({(foundSound == false) && (inPoint < numSamples)}) {
					numChannels.do {
						foundSound = foundSound || (data[inPoint] != 0.0);
						inPoint = inPoint +1;
					};
				};
				
				if(inPoint == numSamples) {"ERROR: File is silent. Processing is stupid!".throw;};
				
				inPoint = inPoint - numChannels; // go back one sample
				if(inPoint < 0) {inPoint = 0}; // may be senseless ...
				
				("Will trim " ++ (inPoint / numChannels).floor ++ " frames of silence at the beginning.").postln;
				
				foundSound = false;
				while({(foundSound == false) && (outPoint > 0)}) {
					numChannels.do {
						foundSound = foundSound || (data[outPoint] != 0.0);
						outPoint = outPoint - 1;
					};
				};
				
				// no need to check for complete silence here.
				
				outPoint = outPoint + numChannels; // go forward one sample;
				if(outPoint > (numSamples - 1)) {outPoint = numSamples - 1;};
				
				("Will trim " ++ ((numSamples - outPoint) / numChannels).floor ++ " frames of silence at the end.").postln;
				
					
			};
			
			0.0001.wait;
			manager.doFade.if {
				fadePointer = 0;
				128.do {|i|
					numChannels.do {
						data[inPoint + fadePointer] = data[inPoint + fadePointer] * (i / 128);
						data[outPoint - fadePointer] = data[outPoint - fadePointer] * (i / 128);
						fadePointer = fadePointer + 1;
					};
				};
			};
			
			ampMul = 1.0;
			
			0.0001.wait;
			manager.doNormalize.if {
				peakAmp = 0.0;
				(outPoint - inPoint).do {|i|
					if(data[inPoint + i].abs > peakAmp) {peakAmp = data[inPoint + i].abs;}
				};
				("Peak Amplitude: " ++ peakAmp).postln;
				
				(peakAmp == 0.0).if {"ERROR: File is silent. Processing is stupid!".throw;};
				
				ampMul = 0.9999 / peakAmp;
			};
			
			// copy to new FloatArray and multiplicate by amplitude (if needed)
			
			dataOut = FloatArray.newClear(outPoint - inPoint + 1);
						
			(outPoint - inPoint + 1).do {|i|
						dataOut[i] = data[inPoint + i] * ampMul;
			};
			
			0.0001.wait;
			outSf = SoundFile.new;
			outSf.numChannels_(numChannels);
			outSf.headerFormat_("AIFF");
			outSf.sampleFormat_(manager.sampleFormat);
			outSf.openWrite(processedPath);
			
			outSf.writeData(dataOut);
			
			outSf.close;
			sf.close;
			
			{manager.statusText.string_("Finished Processing to processed/" ++ fileName);}.defer;
			
			// SoundFile.normalize(path, processedPath, "AIFF", manager.sampleFormat);
		}.try;
		
		if(err.class == String) {{manager.statusText.string_(err);}.defer;};
	}
	
	compress {
		var stringToExecute, res;
		{manager.statusText.string_("Compressing File " ++ fileName);}.defer;

		stringToExecute = manager.compressionCommand;
		
		stringToExecute = stringToExecute.replace("{BR}", manager.bitrate.asString);
		stringToExecute = stringToExecute.replace("{IN}", processedPath.quote);
		stringToExecute = stringToExecute.replace("{OUT}", compressedPath.quote);
		
		stringToExecute.postln;
		res = stringToExecute.systemCmd;
		
		{manager.statusText.string_("Compression Result: " ++ res);}.defer;
	}
	
	mail { // OSX Only
		("osascript \""++ RecordingManager.filenameSymbol.asString.dirname ++ "/SendAttachmentViaMail.applescript\" \"" ++ compressedPath.basename  ++ "\" \"" ++ compressedPath ++ "\"").systemCmd;
		manager.statusText.string_("Mail should have opened with a new message.");
	}
}