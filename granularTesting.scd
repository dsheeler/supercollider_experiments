// =====================================================================
// SuperCollider Workspace
// =====================================================================

s.boot;
s.quit;

(
var winenv;
// a custom envelope 
winenv = Env([0, 1, 0], [0.5, 0.5], [8, -8]);
z = Buffer.sendCollection(s, winenv.discretize, 1);
(
SynthDef("grain", {arg gate = 1, amp = 1, envbuf;
var pan, env, input;
// use mouse x to control panning
pan = MouseX.kr(-1, 1);
env = EnvGen.kr(
Env([0, 1, 0], [1, 1], \sin, 1),
gate,
levelScale: amp,
doneAction: 2);
input = In.ar;
Out.ar(0,
GrainIn.ar(2, Impulse.kr(32), 1, input, pan, envbuf) * env)
}).send(s);

)

// use built-in env
x = Synth(\grain, [\envbuf, -1])

// switch to the custom env
x.set(\envbuf, z)
x.set(\envbuf, -1);

x.set(\gate, 0);

(
SynthDef("in_out", {
Out.ar([0,1], In.ar(0,2));
}).send(s);
)

x = Synth(\in_out);

(
SynthDef("grain", { arg gate=1, amp=1, envbuf; 
	var pan, env;
	pan = 0;
	env = EnvGen.kr(Env([0, 1, 0], [1, 1], \sin, 1),gate,levelScale: amp,doneAction: 2);
	Out.ar(0,GrainIn.ar(2, Impulse.kr(32), 1, In.ar(0,2), pan, envbuf) * env);
}).send(s);
)

x.free;
x = Synth("grain", [\envbuf, -1])
a = Synth("grain");
x.set(\gate, 1);

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