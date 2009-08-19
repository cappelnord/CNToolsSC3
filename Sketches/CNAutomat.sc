CNAutomat : Object
{
	var matrix;
	var <>changeFunction, <>testFunction, doFunction;
	var wrapAround = false;
	
	var <window;
	
	var <>colors;

	
	conway {
	
	// Initialisiert den CA nach Conway's Game of Life
	// http://en.wikipedia.org/wiki/Conway%27s_Game_of_Life
	
	
	}
	
	colorByState {|state|
	
		colors = Dictionary.new;
		colors.add(\alive, Color(0,0,0,1));
		colors.add(\dead, Color(1,1,1,1));
	
	}
}

CNAutomatCell : Object
{
	var <>state;
	var <button;
	var automat;
	
	countNeighbours {|state|
	
	}

}