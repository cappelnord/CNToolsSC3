// Vorher Server Booten für Sound

(
	w = GUI.window.new("Test the TNode Shit!", Rect(200,200,420,340));
	
	// Nodes
	~playNode = FunctionTNode(nil, "Ding!", function: {|value|(freq: value.midicps).play;value;});
	
	~dumpNode = DumpTNode.new();

	~startNode = ButtonTNode(nil, "Click");
	~randomFreq = FunctionTNode(nil, "Random Note", function: {|value| (48..72).choose});
	~delayNode = DelayTNode(~playNode, "Delay um 1s", delayTime: 1);
	~delay2Node = DelayTNode(nil, "Delay um 0.5s", delayTime: 0.5);
	~machQuinte = FunctionTNode(~playNode, "Quinte", function: {|value| value + 7;});
	~machTerz = FunctionTNode(~playNode, "Terz", function: {|value| value + 4;});
	
	// Verkabelung, beim erstellen der Nodes wurden schon einige Targets gesetzt
	~startNode.target_(~randomFreq);
	~randomFreq.target_([~delayNode, ~delay2Node, ~machTerz, ~dumpNode]);
	~delay2Node.target_(~machQuinte);
	
	~startNode.gui(w, Rect(160,20,100,40));
	~randomFreq.gui(w, Rect(160,80,100,40));
	~delayNode.blink(w, Rect(20,140,100,40)); 	// Delay wird noch ein eigenes GUI kriegen.
										// blink ist allerdings immer verfügbar.
     ~delay2Node.blink(w, Rect(160,140,100,40));
     ~machQuinte.blink(w, Rect(160,200,100,40));
     ~machTerz.blink(w, Rect(300,200,100,40));
	~playNode.gui(w, Rect(160, 260, 100,40));
	
	w.front;
)

