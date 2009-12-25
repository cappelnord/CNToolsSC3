/*
	CNMidiEnvGen, a MIDI CC Envelope Generator
	(c) 2008 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
	
	Part of CNToolsSC3
	http://github.com/cappelnord/CNToolsSC3
*/

CNMidiEnvGen
{
	var <> out;
	var <> envSampleSize = 2048;
	var <> chan;
	var <> ctlNum;
	var < lastValue = 0;
	var routine;
	
	*new {|midiOut, chan, ctlNum|
		^super.new.init(midiOut, chan, ctlNum);
	}
	
	init {| midiOut, chan, ctlNum |
	
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
		
		// The following block of code isn't that pretty. It should find out
		// for itself, if the EnvGen was sampled with a too low sample rate.
		
		lastLocalValue = envSignal[0].floor;
		nextWaitTime = 0;
		
		 routine = fork {
			
			envSignal.do {|val|
						
				val = val.floor;
				nextWaitTime = nextWaitTime + stepTime;
				
				if(val != lastLocalValue,{
				
					// let's see if there might be some undersampling going on, then warn
					if(((val - lastLocalValue).abs > 1) && undersampleWarning.not,{
					
						undersampleWarning = true;
						"Envelope may be undersampled by CCMidiEnvGen. Maybe increase envSampleSize!".postln;
					
					});
					
					nextWaitTime.wait;
					
					out.control(chan,ctlNum,val);
					lastValue = val;
					
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
