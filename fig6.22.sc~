// =====================================================================
// SuperCollider Workspace
// =====================================================================

s.boot;
s.quit;

(
SynthDef("rustling", { arg dur=0.2, peakbw=200, amp=0.1, gate=1; 
	var osc, bwenv;
	bwenv= Env.new([0.000000001,1.0,0.00000000001],[0.5, 0.5], [-2, 2]);
	bwenv.plot;

	osc =  LPF.ar( WhiteNoise.ar(1), peakbw*EnvGen.ar(bwenv,gate,timeScale: dur, doneAction:2), amp) ;

	Out.ar([0,1], osc); 
}).load(s);
)

a = Synth("rustling");
a.play();
(
var w, f, d, values, server, id, isOn = false, msg;
var b1, b2, s;

values = IdentityDictionary.new;
server = Server.local;

f = { arg name, spec = \lin, guispec;
	var height = 20, v, s, n;
	guispec = guispec ? spec;
	spec = spec.asSpec;
	guispec = guispec.asSpec;
	spec.class.postln;
	v = GUI.staticText.new(w, Rect(0, 0, 72, height));
	v.font = GUI.font.new("Helvetica", 12);
	v.stringColor = Color.black;
	v.align = \right;
	
	s = GUI.slider.new(w, Rect(0, 0, 182, height));
	s.resize = 2;
	s.action = { 
		var val, guival, step;
		val = spec.map(s.value);
		values.put(name, val);
		//		server.sendMsg("/n_set", id, name, val);
		guival = guispec.map(s.value);
		step = pow(10, floor(min(0, guival.abs.log10 - 2)));
		v.string = guival.round(step).asString ++ guispec.units;
	};
	s.value = spec.unmap(spec.default);
	s.action.value;
	
	n = GUI.staticText.new(w, Rect(0, 0, 72, height));	n.string = name;
	n.stringColor = Color.black;
	n.font = GUI.font.new("Helvetica", 12);
	n.resize = 3;
	
	w.view.decorator.nextLine;
};


w = GUI.window.new("Rustling", Rect(512, 64, 360, 130));
w.view.decorator = d = FlowLayout(w.view.bounds);

b2 = GUI.button.new(w, Rect(0,0, 80, 24));
b2.states = [ ["Play", Color.black, Color.green] ];
b2.action = { arg view; 
	var id = server.nextNodeID;
	msg = ["/s_new", "rustling", id, 0, 0];
	values.put("/gate", 1);
	values.keysValuesDo({ arg key, value; 
		msg = msg.addAll([key, value]); 
	});
	msg.postln;
	server.performList(\sendMsg, msg); 

};

b2.enabled = server.serverRunning;
d.nextLine;

f.value(\dur,[0, 2, \lin]);
f.value(\peakbw, [1, 20000, \exp]);
f.value(\amp, \amp, \db);

a = SimpleController(server);
f = { arg server; 
	b2.enabled = server.serverRunning;
	if (server.serverRunning.not, { b2.value = 0 });
};
a.put(\serverRunning, f);
w.onClose = { 
 	a.remove;
};

w.front;
)
////////////////////////////////////////////////////////////////////////

a = Synth("FM");
a.free;o