TNode.dumpClassSubtree;"";

a = 4
GUI
a.class.asString.dump

b = TNode.new(nil, "Testknoten")
b.gui

"Hallo" + b

(
a = TNode(
	DumpTNode(
		FunctionTNode(
			TNode(nil, "Endpunkt"),
			"Post-Shit-Knoten",
			{|value| "SHIT!".postln;value;}
		)
	)
);
)

a.trace // Zeigt den Knotenweg.

a.target.triggerPost_("Lol!"); // Sagt dem Knoten a dass er Lol! ausgeben soll
a.trigger("Dieser String wird gedumpt vom DumpTNode Knoten");


(
b = TNode(
		DelayTNode(
			DumpTNode(),
			delayTime: 2
		)
	);
	
b.triggerPost_("Start!");
)

(freq: 440).play
b.trace
	
b.trigger("blub")

( // Feedback Schleife; Chromatische Tonleiter Ÿber 1 Oktaven.
  // Das zu tracen bringt im Moment noch den Absturz :>

	~startNode = TNode();
	~playNode = FunctionTNode(function: {|value| (freq: value.midicps, amp: 0.01, sustain: 0.1).play; value;});
	~delayNode = DelayTNode(delayTime: 0.5);
	~gateNode = BooleanTNode(function: {|value| value <= (60+12)});
	~plusNode = FunctionTNode(function: {|value| value + 1});
	
	// Hier werden einzelnen Knoten Ziele zugewiesen. Man kann auch einen Array als Argument nehmen.
	~startNode.target_(~playNode);
	~playNode.target_(~delayNode);
	~delayNode.target_(~plusNode);
	~plusNode.target_(~gateNode);
	~gateNode.target_(~playNode);
	
	~startNode.triggerPost_("Los geht's!");
	
	~startNode.trigger(60); // Gibt 60 in das System
)

