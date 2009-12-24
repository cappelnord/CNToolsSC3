// Midi Device 1 is Apple Internal MIDI on my machine

MIDIClient.init;
m = MIDIOut(0,MIDIClient.destinations[0].uid);

a = CNMidiEnvGen(m);

e = Env.perc(0.1,1) // You can use any envelope you desire
f = Env.adsr(0.3,0.3,0.6,0.2).plot

a.play(env: e,chan: 1, ctlNum: 2, timeScale:4, max:127, min:0);
a.play(env: f, chan:1, ctlNum: 3, timeScale:8, max:127, min:0);
// arguments: Envelope, MIDI channel, Midi controll, timeScale, mul and add.
// if you start 2 envelopes on the same CNMIDIEnvGen object you can only stop the last one.

a.stop // stops the envelope, otherwise it would stop itself when it's finished.