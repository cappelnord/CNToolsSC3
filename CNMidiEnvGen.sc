/*
CNMidiEnvGen, a Midi CC Envelope Generator.

Part of CNTools, http://www.cappel-nord.de

Written by Patrick Borgeat <patrick@borgeat.de>, 2008
*/

CNMidiEnvGen
{
	var guiWindow;
	var <> out;
	var <> envSampleSize = 2048;
	var <> chan;
	var <> ctlNum;
	var < lastValue = 0;
	var routine;
	
	*new {|midiOut, chan, ctlNum|
		^super.new.init(midiOut, chan, ctlNum);
	}
	
	init {| midiOut, chan, ctlNum |Ê// Hier Argumente
	
		this.out = midiOut;
		this.chan = chan;
		this.ctlNum = ctlNum;
		
		if(midiOut.class != "MIDIOut",{"Argument is no MIDIOut!".postln;});
	
	}
	
	play {|env, timeScale = 1, min = 0, max = 127|
	
		var envSignal = env.asSignal(envSampleSize) * (max - min) + min;
		var envTime = env.times.sum * timeScale;
		var stepTime = envTime / envSampleSize;
		var undersampleWarning = false;
		
		
		var lastLocalValue, nextWaitTime;
		
		/* "envSignal".postln;
		envSignal.postcs;
		"envTime".postln;
		envTime.postln; */
		
		// Nun ein nicht wirklich hŸbscher CodeBlock. Immerhin soll er selber merken
		// ob der EnvGen mit zu niedriger Samplerate gesampelt worden ist.
		
		lastLocalValue = envSignal[0].floor;
		nextWaitTime = 0;
		
		 routine = fork {
			
			envSignal.do {|val|
						
				val = val.floor;
				nextWaitTime = nextWaitTime + stepTime;
				
				if(val != lastLocalValue,{
				
					// Erstmal schauen ob mšglicherweise Undersampling statgefunden hat, dann warnen!
					if(((val - lastLocalValue).abs > 1) && undersampleWarning.not,{
					
						undersampleWarning = true;
						"Envelope may be undersampled by CCMidiEnvGen. Maybe increase envSampleSize!".postln;
					
					});
					
					nextWaitTime.wait;
					
					out.control(chan,ctlNum,val);
					lastValue = val;
					// val.postln;
					
					nextWaitTime = 0;
				
				});
				
				lastLocalValue = val;
			
			};
		};

	
	}
	
	stop {
		routine.stop;
	}

}
