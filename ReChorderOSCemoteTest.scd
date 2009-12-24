~grainChorder = GrainReChorder.new(s);
~grainChorder.amp_(0.2);
~grainChorder.bufferLength_(44100);

~recordStopOSC = OSCresponder(nil, "/button/A1", {|time, theResponder, message, addr|
if(message[1] == 1, {~grainChorder.record;},{~grainChorder.stopRecording;~grainChorder.play;});

"A1".postln;
}).add;

~freeOSC = OSCresponder(nil, "/button/E1", {|time, theResponder, message, addr|
if(message[1] == 1, {~grainChorder.free});
}).add;

~rateOSC = OSCresponder(nil, "/button/B1", {|time, theResponder, message, addr|
if(message[1] == 1, {~grainChorder.set(\rate, 4.0.rand + 0.5);});
}).add;

~rateOSC = OSCresponder(nil, "/button/B2", {|time, theResponder, message, addr|
if(message[1] == 1, {~grainChorder.set(\rate, 1);});
}).add;