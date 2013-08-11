// =====================================================================
// SuperCollider Workspace
// =====================================================================

s.boot;
s.quit;

(
SynthDef("risset_clarinet_pg_153", { arg freq=440, amp=0.2, gate; 
	var osc, ampenv, ampenvctl, tfuncenv, buf, tfuncstream;

	tfuncenv = Env.new([-0.8, -0.8, -0.5, 0.5, 0.8, 0.8], [0.39, 0, 0.22, 0, 0.39]);

	buf = Buffer.alloc(s,1024,1);

	t = Signal.fill(512, {arg i; tfuncenv.at(i/512.0)});
	//t.plot;
	buf.loadCollection(t.asWavetable);

	ampenv = Env.newClear(3);
	ampenvctl = Control.names([\ampenv]).kr( ampenv.asArray );

	osc = amp * Shaper.ar(buf, SinOsc.ar(freq, 0, EnvGen.kr(ampenvctl, gate)));

	osc.scope;
	Out.ar(0, osc) 
}).writeDefFile;
Server.local.sendMsg("/d_load", SynthDef.synthDefDir ++ "risset_clarinet_pg_153.scsyndef");
)
(
var dur, level;

dur = 2;
level = 0.1;
e = Env.linen(2,2.2-(1+0.64), 1, 0.8);

a = Synth("risset_clarinet_pg_153");
a.setn(\ampenv, e.asArray);
a.play(s);
)
(
a.set(\gate, 0);
a.set(\gate, 1);

a.set(\freq, 30);
a.set(\gate, 0);
a.set(\gate, 1);
a.set(\amp, 0.8);
a.set(\freq, 220);
a.set(\gate, 0);
a.set(\gate, 1);
)
a.free;

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


w = GUI.window.new("FM", Rect(512, 64, 360, 130));
w.view.decorator = d = FlowLayout(w.view.bounds);

server.sendMsg("/d_load", "synthdefs/FM.scsyndef");



b2 = GUI.button.new(w, Rect(0,0, 80, 24));
b2.states = [ ["Play", Color.black, Color.green] ];
b2.action = { arg view; 
	var id = server.nextNodeID;
	msg = ["/s_new", "FM", id, 0, 0];
	values.keysValuesDo({ arg key, value; 
		msg = msg.addAll([key, value]); 
	});
	msg.postln;
	server.performList(\sendMsg, msg); 

};

b2.enabled = server.serverRunning;
d.nextLine;

f.value(\dur,[0, 50, \lin]);
f.value(\fc, [20, 500, \lin]);
f.value(\fm, [0,500, \lin]);
f.value(\amp, \amp, \db);
f.value(\imax,  [0,500,\lin]);

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