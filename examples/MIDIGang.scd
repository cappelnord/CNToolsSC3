// ProxySpace must have been pushed

MIDIGang.start();
((1..8).do {|i| MIDIGang.add(i,i);})

// these two lines connect MIDI CC 1-8 to ~cc1-8.kr. The pattern Pcc(1-8) also yields the control values. A third argument could be the symbol \binary, which would yield only 1 if x > 0. The first argument is the internal number (used in kr NodeProxies and as argument for PCC), the second is the actuall CC number.
