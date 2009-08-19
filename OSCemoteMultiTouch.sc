OSCemoteMultiTouch : Object
{
	var <>startAction, <>setAction, <>stopAction;
	var <fseq;
	var fingerIDs;
	var <fingers;
	var <>dumpMessages = false;
	var responder;
	
	*new {
		^super.new.init();
	}
	
	init {
	
		// Ugly Initialization :->
		fingers = nil.dup(5);
		fingerIDs = nil.dup(5);
		fseq = 0;
		
		startAction = {};
		stopAction = {};
		setAction = {};
		
		responder = OSCresponder(nil, "/tuio/2Dcur", {|time, theResponder, message, addr|
		
			var data, command;
			var fingerIndex;
			var fingerTest = false;
			var i = 0;
			
			var toDelete;
			var indexToDelete;

		
			// This Function runs quite often. Optimization could be useful!
			
			if(dumpMessages, {
				message.postln;
			});
		
			command = message[1];
			data = message.copyRange(2,message.size-1); // dumb?
						
			command.switch(
				'source', {
					// let's ignore this for now...
				},
				'fseq', {
					// just to do something with this message ...
				
					if(data[0] != (fseq + 1), {
						"Warning: Sequence of Messages doesn't match expectations!".postln;
					});
					
					fseq = data[0];
				
				},
				'set', {
				
				
					// first check, if finger is set yet. if not then cast one, if yes then update it.
					/* data Array:
						0 --> ID
						1 --> posX
						2 --> posY
						3 --> vectorX
						4 --> vectorY
						5 --> motion
					*/
					
					fingerIndex = fingerIDs.indexOf(data[0]);
					
					if(fingerIndex == nil, {
						
						// new finger
						
						while({(i < 5) && (fingerTest == false)},
						{
							fingerTest = fingerIDs[i] == nil;
							i = i + 1;
						});
						
						i = i - 1; // duumb!

						if(fingerTest == false, {
							// no free finger slot, no finger!
							"Warning: Somehow there seem to be more than 5 fingers ...".postln;
						},{
							// let's make some finger
							fingerIDs[i] = data[0];
							
							fingers[i] = OSCemoteMultiTouchFinger.new(data[0], data[1], data[2], data[3], data[4], data[5]);
							
							// let's do something with our finger
							startAction.value(fingers[i], i);
						
						});
						
						
					},
					{
						// old finger
						
						fingers[fingerIndex].update(data[1], data[2], data[3], data[4], data[5]);
						setAction.value(fingers[fingerIndex], fingerIndex);
					
					});
					
					
					
				},
				'alive', {
				
					toDelete = fingerIDs.copy;
					
					data.do{|item|
						toDelete[fingerIDs.indexOf(item)] = nil;
					};
					
					toDelete.do{|item|
					
						(item.notNil).if({
						
							indexToDelete = fingerIDs.indexOf(item);
						
							stopAction.value(fingers[indexToDelete], indexToDelete);
							fingerIDs[indexToDelete] = nil;
							fingers[indexToDelete] = nil;
			
						});
					}
				}
			);
		});
		
		responder.add;
	}
	
}

OSCemoteMultiTouchFinger : Object
{
	var <>posX, <>posY, <>vectorX, <>vectorY, <>motion;
	var <id;
	
	*new {|id, posX, posY, vectorX, vectorY, motion|
		^super.new.init(id, posX, posY, vectorX, vectorY, motion);
	}
	
	init {|aid, aposX, aposY, avectorX, avectorY, amotion|
	
		id = aid;
		
		posX = aposX;
		posY = aposY;
		vectorX = avectorX;
		vectorY = avectorY;
		motion = amotion;
	}
	
	update {|aposX, aposY, avectorX, avectorY, amotion|
	
		posX = aposX;
		posY = aposY;
		vectorX = avectorX;
		vectorY = avectorY;
		motion = amotion;
	}
}