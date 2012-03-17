// =====================================================================
// SuperCollider Workspace
// =====================================================================


s.quit;
s.boot;
b = Buffer.alloc(s, 512, 1, {arg buf; buf.chebyMsg([0.4,0.8, 1, 0.7, 0.5, 0.3, 0.2, 0.1, 0.05, 0, 0.02])});//[1,0,1,1,0,1])});
b.plot;
(

x = play({ 

Shaper.ar(

b, 

SinOsc.ar(300, 0, Line.kr(0,1,6)),

0.5

) 

});

)

x.free;
 b.free;

s.boot;
s.quit;

(
SynthDef("waveshaping_ringmod_drum", { arg freq=440, amp=0.9, gate; 
	var osc, ampenv, ampenvctl, tfuncenv, buf, tfuncstream, f1, f2, t, f1envctl;

	b = Buffer.alloc(s,1024,1);
	t = Signal.fill(512, { arg i; var j = i / 511.0;
		1 + (0.841 * j) - (0.707 * (j**2)) - (0.595 * (j**3)) + (0.5 * (j**4)) +( 0.42 * (j**5)) - (0.354 * (j**6)) - (0.297 * (j**7)) + (0.25 * (j**8)) + (0.21*(j**9))});
	//t.plot;
	b.loadCollection(t.asWavetable);
	b.plot;

	f1 = Env.new([1, 0, 0], [0.04, 0.16]);
	f2 = Env.new([0, 0.9, 1, 0.9, 0], [0.01, 0.01, 0.01, 0.17], [-1, -3, 3, -5]);
	//	f1.plot;
	//	f2.plot;

	osc = amp * SinOsc.ar(freq, 0, EnvGen.kr(f2,gate)) * Shaper.ar(b, SinOsc.ar(0.7071*freq, 0, EnvGen.kr(f1, gate, doneAction: 2)));
	Out.ar(0, osc) 
}).load(s);
)


a = Synth("waveshaping_ringmod_drum", [\freq, 60]);
a.set(\gate, 1);
a.set(\gate, 0);


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