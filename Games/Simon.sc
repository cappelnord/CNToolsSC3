/*
	Simon
	(c) 2009 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
	
	Part of CNToolsSC3
	http://github.com/cappelnord/CNToolsSC3
*/


Simon
{
	var window, buttons;
	var synthDefs, synthArgs;
	var noteArray, playHead;
	var turnLabel, statusLabel;
	
	var <userTurn = false;
	var <lost = false;
	var <gameIsRunning = false;
	var <playHead = 0;
	var windowOpen = true;
	
	var <> waitTime = 0.4;
	var <> pauseTime = 1.2;
	var <> waitDecrease = 0.01;
	


	*new {
		^super.new.init;
	}
	
	*says { // Maaaagic :)
		^Simon.new;	
	}
	
	init {
	
		var buttonFont, bigFont;
		
		Server.default.waitForBoot({
			SynthDef(\simonDefault, {|out = 0, freq = 440, attack = 0.1, release = 0.7, pan = 0|
		
				var env = EnvGen.kr(Env.perc(attack, release, 0.5), doneAction:2);
				Out.ar(out, Pan2.ar(Blip.ar(freq,15,env),pan));
		
			}).store;
		});
		
		synthDefs = Dictionary.new;
		
		synthDefs[\top] = \simonDefault;
		synthDefs[\bottom] = \simonDefault;
		synthDefs[\left] = \simonDefault;
		synthDefs[\right] = \simonDefault;
		
		synthArgs = Dictionary.new;
		
		synthArgs[\bottom] = [\freq, 60.midicps];
		synthArgs[\left] = [\freq, 64.midicps, \pan, -0.4];
		synthArgs[\top] = [\freq, 67.midicps];
		synthArgs[\right] = [\freq, 72.midicps, \pan, 0.4];

	
		buttons = Dictionary.new;
		
		window = GUI.window.new("Simon", Rect(200,200,500,500), false)
				.onClose_({windowOpen = false;});
		
		buttonFont = GUI.font.new("Arial", 60);
		bigFont = GUI.font.new("Arial", 32);
		
		
		buttons[\top] = GUI.button.new(window, Rect(110, 10, 280, 100))
			.font_(buttonFont)
			.states_([
					["W", Color.black, Color.new255(80,0,0)],
					["W", Color.black, Color.new255(255,0,0)]
				    ])
			.action_({ this.buttonAction(\top);});
			
		buttons[\bottom] = GUI.button.new(window, Rect(110, 390, 280, 100))
			.font_(buttonFont)
			.states_([
					["S", Color.black, Color.new255(80,80,0)],
					["S", Color.black, Color.new255(255,255,0)]
				    ])
			.action_({ this.buttonAction(\bottom);});
			
		buttons[\left] = GUI.button.new(window, Rect(10, 110, 100, 280))
			.font_(buttonFont)
			.states_([
					["A", Color.black, Color.new255(0,0,80)],
					["A", Color.black, Color.new255(0,0,255)]
				    ])
			.action_({ this.buttonAction(\left);});
			
		buttons[\right] = GUI.button.new(window, Rect(390, 110, 100, 280))
		    	.font_(buttonFont)
			.states_([
					["D", Color.black, Color.new255(0,80,0)],
					["D", Color.black, Color.new255(0,255,0)]
				    ])
			.action_({ this.buttonAction(\right);});
			
		GUI.button.new(window, Rect(200,330,100,50))
			.states_([["Start/Restart", Color.black, Color.grey]])
			.action_({ this.restart;});
			
		turnLabel = GUI.staticText.new(window, Rect(120, 140, 250,50))
			.font_(bigFont)
			.string_("Press start ...");
			
		statusLabel = GUI.staticText.new(window, Rect(120, 180, 250,50))
			.font_(bigFont)
			.string_("... and have fun :)");
			
		window.view.keyDownAction_({ |view,char,modifiers,unicode,keycode| this.keyAction(keycode); });	    	
		window.front;
	
	}
	
	buttonAction { |buttonSymbol, override = false|
	
	
		if(lost == false, {
			if( ((gameIsRunning == true) && (userTurn == true)) || (override == true), {
	
				this.blinkButton(buttonSymbol);
				this.soundButton(buttonSymbol);
		
				this.updateLabels;
		
			}, 
			{
				buttons[buttonSymbol].value_(0);
		
			});
		
			if(userTurn == true, { this.usersTurn(buttonSymbol) });
		},
		{
			this.blinkButton(noteArray[playHead]);
		});
	
	}
	
	keyAction { |keycode|
	
		// keycode.postln;
		
		if(keycode == 0, {this.buttonAction(\left);});
		if(keycode == 1, {this.buttonAction(\bottom);});
		if(keycode == 2, {this.buttonAction(\right);});
		if(keycode == 13, {this.buttonAction(\top);});
	
	}
	
	blinkButton { |buttonSymbol|
	
		buttons[buttonSymbol].value_(1);
		{ if(windowOpen == true, {buttons[buttonSymbol].value_(0);}); }.defer(0.2);	
	}
	
	soundButton { |buttonSymbol|
	
		Synth.grain(synthDefs[buttonSymbol], synthArgs[buttonSymbol]);
	
	}
	
	updateLabels {
	
		if(userTurn == true, 
		{
			turnLabel.string_("Your turn");
		},
		{
			turnLabel.string_("Simon's turn");
		});
		
		if(lost == true,
		{
			turnLabel.string_("You lost!");
		});
	
		statusLabel.string_(playHead + "/" + noteArray.size);
	
	}
	
	restart {
	
		userTurn = false;
		playHead = 0;
		noteArray = List.new;
		lost = false;
		
		gameIsRunning = true;
		this.updateLabels;
		
		this.simonsTurn;
	}
	
	simonsTurn {
	
		if(playHead < noteArray.size,
		{
			this.buttonAction(noteArray[playHead], true);
			playHead = playHead + 1;
			{if(windowOpen == true, {this.simonsTurn;});}.defer(this.calcSimonsWait);
		},
		{
			noteArray.add([\top, \bottom, \left, \right].choose);
			// noteArray.dump;
			this.buttonAction(noteArray[playHead], true);
			
			userTurn = true;
			playHead = 0;	
		});
		
		this.updateLabels;
	}
	
	usersTurn {|buttonSymbol|
	
		if(buttonSymbol == noteArray[playHead],
		{
			playHead = playHead + 1;
		},
		{
			gameIsRunning = false;
			lost = true;
			this.blinkButton(noteArray[playHead]);
		});
		
		if(playHead >= noteArray.size,
		{
			userTurn = false;
			playHead = 0;
			{if(windowOpen == true, {this.simonsTurn;});}.defer(pauseTime);
		});
		
		this.updateLabels;
	
	}
	
	calcSimonsWait {
	
		var result = waitTime - (noteArray.size * waitDecrease);
		
		if(result < 0.1, {result = 0.1});
		
		^result;
	
	}
	
	setSynthDef { |button, synth|
	
		synthDefs[button] = synth;
	}
	
	setSynthArgs { |button, args|
		
		synthArgs[button] = args;
	}
	
	setAllSynthDefs {|synth|
	
		[\top, \bottom, \left, \right].do {|item|
			synthDefs[item] = synth;
		};
	}

}



