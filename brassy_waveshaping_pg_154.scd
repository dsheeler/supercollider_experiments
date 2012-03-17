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
SynthDef("brassy_waveshaping_pg_154", { arg freq=440, gate; 
	var osc, ampenv, ampenvctl, tfuncenv, buf, tfuncstream, f1, f1envctl;

	b = Buffer.alloc(s,1024,1);
	//b.cheby([0.4,0.8, 1, 0.7, 0.5, 0.3, 0.2, 0.1, 0.05, 0, 0.02], true, false, true );
	//b.cheby([0,0.8, 0, 0, 0, 0, 0, 0, 0, 0, 0], true, true, true );


	//	b = Buffer.alloc(s, 512,1);
	b.cheby([0.4,0.8, 1, 0.7, 0.5, 0.3, 0.2, 0.1, 0.05, 0, 0.02] );

	f1 = Env.new([0, 0.49, 0.7, 0.5, 1], [0.001, 0.039, 0.03, 0.15], releaseNode: 2);
	ampenv = Env.adsr(0.03,0.01, 0.7,0.15, 1.0, 'lin');

	osc = EnvGen.kr(f1,gate)*Shaper.ar(b, SinOsc.ar(freq, 0, EnvGen.ar(ampenv, gate, doneAction: 2)));
	Out.ar(0, osc) 
}).writeDefFile;
Server.local.sendMsg("/d_load", SynthDef.synthDefDir ++ "brassy_waveshaping_pg_154.scsyndef");
)


a = Synth("brassy_waveshaping_pg_154");
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