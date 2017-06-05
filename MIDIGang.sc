/*
MandelClock

BIG CRAPWARE
*/

MIDIGang {
	classvar ext2intDict;
	classvar int2extDict;
	classvar int2valueDict;
	classvar int2modeDict;
	
	classvar hasMidi = false;
		
	*start {
		
		CCResponder.removeAll();
		
		ext2intDict = Dictionary.new;
		int2extDict = Dictionary.new;
		int2valueDict = Dictionary.new;
		int2modeDict = Dictionary.new;
	}
	
	*recall {
		var midiOut;
		
		hasMidi.if {
			midiOut = MIDIOut(1);
			midiOut.latency = 0;
		
			ext2intDict.keys.doÊ{|extern|
				extern.isNumber.if {
					midiOut.control(0, extern, 127 * int2valueDict.at(ext2intDict.at(extern)));
				}; // otherwise it's OSC
			};
		};
	}
	
	*flush {
		// TO IMPROVE
		
		CCResponder.removeAll();
		this.start();
	}
	
	*add {|intern, extern, mode='ok'|
		var c = currentEnvironment;
		
		hasMidi = true;
		
		(c.class != ProxySpace).if {
			"currentEnvironment s not a ProxySpace".warn;
			^nil;
		};
		
		intern = intern.asInt;
		extern = extern.asInt;
				
		ext2intDict.put(extern, intern);
		int2extDict.put(intern, extern);
		int2valueDict.put(intern, 0);
		int2modeDict.put(intern, mode);
		
		this.createKrProxy(intern);
		
		CCResponder({|src, chan, num, value|
			// num.postln;
			this.setIntern(intern, value.linlin(0,127,0,1));
		}, nil, nil, extern, nil, true, true);	
	}
	
	*addOSC {|intern, extern, mode='osc'|
		var c = currentEnvironment;
		
		(c.class != ProxySpace).if {
			"currentEnvironment s not a ProxySpace".warn;
			^nil;
		};
		
		intern = intern.asInt;
		extern = extern.asString;
		
		ext2intDict.put(extern, intern);
		int2extDict.put(intern, extern);
		int2valueDict.put(intern, 0);
		int2modeDict.put(intern, mode);
		
		this.createKrProxy(intern);

		OSCresponder(nil, extern, {|time, theResponder, message, addr|
			this.setIntern(intern, message[1]);
		}).add;
	}
	
	*get {|intern|
		var ret = int2valueDict.at(intern);
		
		ret.isNil.if ({
			("Internal Key " ++ intern.asString ++ " is not in MIDIGang").warn;
			^0.0;
		}, {
			^ret;
		});
	}
	
	*setExtern {|extern, value|
		this.setIntern(ext2intDict.at(extern), value);
	}
	
	*setIntern {|intern, value|
		
		var mode = int2modeDict.at(intern);
		
		(mode == \binary).if {
			(value > 0).if {
				value = 1.0;
			};	
		};
		
		(mode != \osc).if {
			// value.postln;
			// intern.postln;
		};
		
		
		int2valueDict.put(intern, value);
		this.setKrProxy(intern, value); 
	}
	
	*symbForID {|intern|
		^("cc" ++ intern).asSymbol;
	}
	
	*createKrProxy {|intern|
		var c = currentEnvironment;
		var proxy;
		
		(c.class == ProxySpace)	.if ({
			proxy = NodeProxy.control(c.server, 1);
			c.envir.put(this.symbForID(intern), proxy);
			proxy.put(0, {|value=0.0| Lag2.kr(value, 0.01)}, 0, [\value, 0.0]);
		}, {
			"currentEnvironment is not a ProxySpace".warn;
		});
	}
	
	*setKrProxy {|intern, value|
		var c = currentEnvironment;
		
		(c.class == ProxySpace)	.if ({
			c.envir[this.symbForID(intern)].set(\value, value);
		}, {
			"currentEnvironment is not a ProxySpace".warn;
		});
	}
}

Pcc : Pattern {

	var <>key;
	
	*new {|key|
		^super.new.key_(key);
	}
	
	embedInStream {|event|
	var ret;		
		while {true} {
			ret = MIDIGang.get(key);
			ret.isNil.if({
				("Pcc: " ++ key.asString ++ " yields nil. Bad!").warn;
				0.yield;
			},{
				ret.yield;
			});
		};
		^event;
	}
}