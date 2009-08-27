/*	
	Minesweeper in SuperCollider3
	Written by Patrick Borgeat <patrick@borgeat.de>
	Last modified: 08/27/2009
*/

Minesweeper
{
	var <>window;
	var <>buttonWidth, <>buttonHeight;
	var <>x, <>y;
	
	var numMines;
	var <>minesInGame;
	
	var <>mineField;
	
	var <>windowWidth = 500;
	var <>windowHeight = 500;
	var <>menuHeight = 50;
	
	var resetButton;
	var statusLabel;
	var modeButton;
	var timerLabel;
	var timerProc;
	
	var timeElapsed;
	
	var <>markMines = false;
	
	var <>isOver = false;
	
	var <>numColors;
	
	*new {|... args|
		^super.new.init(*args);
	}
	
	init {|aX = 20, aY = 20, aNumMines = 60|
	
		this.storeSounds;
		
		x = aX;
		y = aY;
		numMines = aNumMines;
				
		buttonWidth = windowWidth / aX;
		buttonHeight = windowHeight / aY;
		
		numColors = [Color.black, Color.blue, Color.yellow, Color.cyan, Color.magenta, Color.white, Color.black, Color.black, Color.black];
		
		mineField = Array2D(x,y);
		
		window = GUI.window.new("Minesweeper", Rect(400,400,windowWidth,500 + menuHeight), false);
				
		y.do {|iy|
			x.do {|ix|
				mineField.put(ix,iy, MineButton.new(ix, iy, this));
			};
		};
		
		resetButton = GUI.button.new(window, Rect(10,10,80,30));
		resetButton.states_([["Reset", Color.black, Color.grey]]);
		resetButton.action_({this.resetGame;});
		
		modeButton = GUI.button.new(window, Rect(100,10,120,30));
		modeButton.states_([["Mark clear spots", Color.black, Color.red], ["Mark mines", Color.black, Color.green]]);
		modeButton.action_({|... args| this.modeAction(*args);});
		
		statusLabel = GUI.staticText.new(window, Rect(240, 10,200,30));
		timerLabel = GUI.staticText.new(window, Rect(470, 10, 40,30));
		
		this.resetGame();
		
		window.onClose_({this.stopTimer;});
		window.front;
	}
	
	modeAction {|button|
		Synth(\minesweeperClick);
		markMines = button.value == 1;
	}
	
	resetGame {
	
		var rx, ry;
		
		isOver = false;
	
		// reset minefield
		x.do {|ix|
			y.do {|iy|
				mineField.at(ix,iy).reset;
			};
		};
		
		// place mines
		
		minesInGame = 0;
		
		if(numMines > (x*y), {numMines = x*y;});
		
		while({minesInGame < numMines},
		{
			rx = x.rand;
			ry = y.rand;
			if(mineField.at(rx,ry).isMine == false, {
				mineField.at(rx,ry).isMine_(true);
				minesInGame = minesInGame + 1;
			});
		});
		
		// reset timer and restart
				
		this.stopTimer;		
		timeElapsed = 0;
		timerProc = {
			inf.do{
				1.wait;
				timeElapsed = timeElapsed + 1;
				{this.updateTimer}.defer;
			}
		}.fork;
		
		this.refreshMenu;
	}
	
	refreshMenu {
		statusLabel.string_("Not yet marked mines: " ++ minesInGame);
		
		this.updateTimer;
		window.refresh;
	}
	
	updateTimer {
		timerLabel.string_(timeElapsed);
	}
	
	stopTimer {
		if(timerProc.isNil.not, {timerProc.stop; timerProc = nil;});
	}
	
	winGame {
		{
			([0,4,7,12] + 60).do{|item|
				Synth(\minesweeperTone, [freq: item.midicps]);
				0.15.wait;
			};
		}.fork;
		statusLabel.string_("You win! Suave!");
		this.stopTimer;
		isOver = true;
	}
	
	checkForWin {
		var won = true;
		var numPositive = 0;
		mineField.do {|i|
			won = won && (i.isClicked || i.isMarked);
			if(i.isMine && i.isMarked, {numPositive = numPositive + 1;});
		};
		
		if(won || (numPositive == numMines), {this.winGame;});
	}
	
	lostGame {
	
		Synth(\minesweeperExplode);
	
		mineField.do{|i|
			if(i.isMine)
			{
				if(i.isMarked.not, {
					i.button.states_([["X", Color.red, Color.grey]]);
				},{
					i.button.states_([["X", Color.green, Color.grey]]);
				});
			};
			
			if(i.isMarked && i.isMine.not, {i.button.states_([["M", Color.red, Color.grey]]);});
		};
		this.stopTimer;
		statusLabel.string_("You loose! It's a pity.");
		window.refresh;
		isOver = true;
	}
	
	storeSounds {
		Server.default.waitForBoot({
		
			SynthDef(\minesweeperClick, {|out = 0, amp = 0.4|
				
				var env = EnvGen.kr(Env.perc(0.001,0.1), doneAction:2);
				var sig = RLPF.ar(Saw.ar(10), 2000, 0.01) * env * amp;
				Out.ar(out, sig.dup);
				
			}).store;
			// Synth(\minesweeperClick)
			
			SynthDef(\minesweeperTone, {|out = 0, amp = 0.4, freq = 440|
				var env = EnvGen.ar(Env.perc(0.001, 0.2));
				var env2 = EnvGen.ar(Env.perc(0.001,0.3), doneAction:2);
				var mod = SinOsc.ar(freq * 4 + (env * 20), mul:100);
				var sig = SinOsc.ar(freq + mod, mul:amp) * env;
				sig = sig + SinOsc.ar(freq * 4, mul:env2 * amp * 0.5);
				sig = RLPF.ar(sig, 4000, 0.4);
				Out.ar(out, sig.dup);
			}).store;
			// Synth(\minesweeperTone)
			
			SynthDef(\minesweeperExplode, {|out = 0, amp = 0.4|
			
				var env = EnvGen.ar(Env.perc(0.3,4), doneAction:2);
				var densEnv = EnvGen.ar(Env.perc(0.0001,2));
				var sawDecEnv = EnvGen.ar(Env.perc(0.0001,0.2));
				var sig = RLPF.ar(Dust.ar(densEnv*1000), 20 + (env * 4000)) * env;
				sig = sig + (Decay.ar(Saw.ar(20), 0.25) * sawDecEnv);
				
				sig = FreeVerb.ar(sig, 0.5, 0.6) * amp;
				
				Out.ar(out, sig.dup);
			}).store;
			// Synth(\minesweeperExplode)
		
		});
	}
}

MineButton
{
	var <>button;
	var <>posX, <>posY;
	var <>isMine = false;
	var <>isClicked = false;
	var <>isMarked = false;
	var <>game;
	
	*new {|... args|
		^super.new.init(*args);
	}
	
	init{|aPosX = 0, aPosY = 0, aGame|
	
		posX = aPosX;
		posY = aPosY;
		
		game = aGame;
		
		button = GUI.button.new(game.window, Rect(posX * game.buttonWidth, posY * game.buttonHeight + game.menuHeight, game.buttonWidth, game.buttonHeight));
		button.action_({|... args| this.clickAction(*args);});
	}
	
	clickAction {
		
		if((isClicked == false) && (game.isOver == false), {
			
			if(game.markMines, {
			
				this.mark;
			
			},{
			
				this.tryField(true);
			
			});
		});
		
		// for not overwritting the "loose"-message
		if(game.isOver.not)
		{
			game.refreshMenu;
			game.checkForWin;
		};
	}
	
	reset {
		button.states_([[" ", Color.black, Color.grey]]);
		isMine = false;
		isClicked = false;
		isMarked = false;
	}
	
	mark {
		Synth(\minesweeperClick);
		if(isMarked.not, {
			isMarked = true;
			button.states_([["M", Color.green, Color.grey]]);
			game.minesInGame_(game.minesInGame - 1);
		},{
			isMarked = false;
			button.states_([[" ", Color.black, Color.grey]]);
			game.minesInGame_(game.minesInGame + 1);
		});
	}
	
	// recursions are called with sound=false --> only the origin will make a sound
	tryField {|sound=false|
		var numNearMines;
		
		isMarked = false;
	
		if(isMine, {
			game.lostGame;
			button.states_([["X", Color.red, Color.grey]]);
			isClicked = true;
		},{
			numNearMines = this.getNearMines;
			if(numNearMines == 0, {
			
				if(sound, {{
					([60,60,60,67] + 24).do{|item|
					Synth(\minesweeperTone, [freq: item.midicps]);
					0.15.wait;
					};
				}.fork;});
			
				isClicked = true;
				button.states_([[" ", Color.black, Color.(0.3,0.3,0.3)]]);
				this.neighborFields.do{|i|
					if(i.isClicked.not && i.isMarked.not, {i.tryField;});
				};
			},{ 
				button.states_([[numNearMines.asString, game.numColors[numNearMines], Color.grey]]);
				if(sound, {Synth(\minesweeperTone, [freq: ([-1,0,3,5,7,9,12,15,17,20] + 60).at(numNearMines).midicps]);});
				isClicked = true;
			});
		});
	}
	
	neighborFields
	{
		var ret = List.new;
		
		[-1,0,1].do{|dX|
			[-1,0,1].do{|dY|
				if(((dX == 0) && (dY == 0)).not && (posX + dX >= 0) && (posY + dY >= 0) && (posX + dX < game.x) && (posY + dY < game.y))
				{
					if(game.mineField.at(this.posX + dX, this.posY + dY).isNil.not, {ret.add(game.mineField.at(this.posX + dX, this.posY + dY));});
				}
			};
		};
		^ret;
	}
	
	getNearMines{
		var ret = 0;
		this.neighborFields.do{|i|
			if(i.isMine, {ret = ret + 1;});
		}
		^ret;
	}
}

