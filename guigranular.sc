// =====================================================================
// SuperCollider Workspace
// =====================================================================
(
SynthDef(\grains, {arg freq, amp, dur=1, grain_freq=2;
	var signal, env;
	env = EnvGen.ar(Env.sine(dur, amp), Impulse.ar(grain_freq), doneAction:0); 
	signal = SinOsc.ar(freq) * env;
	Out.ar(0, signal ! 2);
}).load();
)

(
var w, f, d, values, server, id, isOn = false;
var b2, s,  theSynth;

values = IdentityDictionary.new;
server = Server.local;

f = { arg name, spec = \lin, guispec;
	var height = 20, v, s, n;
	guispec = guispec ? spec;
	spec = spec.asSpec;
	guispec = guispec.asSpec;

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
		if (isOn, { theSynth.set(name, val);});
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

id = 2001;
w = GUI.window.new("granular synth", Rect(512, 64, 360, 130));
w.view.decorator = d = FlowLayout(w.view.bounds);


b2 = GUI.button.new(w, Rect(0,0, 80, 24));
b2.states = [
	["Play", Color.black, Color.green],
	["Stop", Color.white, Color.red],
];
b2.action = { arg view; 
	var msg;
	if (view.value == 1, {
		isOn = true;
		theSynth =  Synth(\grains, values.getPairs());
	},{
		isOn = false;
		theSynth.free();
	});
};
b2.enabled = server.serverRunning;
d.nextLine;

f.value(\dur, \amp, \db);
f.value(\grain_freq, \lofreq);
f.value(\freq, \freq);
f.value(\amp, \amp, \db);

a = SimpleController(server);
f = { arg server; 
	b2.enabled = server.serverRunning;
	if (server.serverRunning.not, { b2.value = 0 });
};
a.put(\serverRunning, f);
w.onClose = { 
	if (isOn, { server.sendMsg("/n_free", id) });
 	a.remove;
};

w.front;
)



(
var winenv;

b = Buffer.read(s, "mono_recording_of_my_voice_for_a_few_seconds.aiff");
// a custom envelope 
winenv = Env([0, 1, 0], [0.5, 0.5], [8, -8]);
z = Buffer.sendCollection(s, winenv.discretize, 1);

SynthDef(\buf_grain_test, {arg gate = 1, amp = 1, sndbuf, envbuf;
var pan, env, freqdev;
// use mouse x to control panning
pan = MouseX.kr(-1, 1);
env = EnvGen.kr(
Env([0, 1, 0], [1, 1], \sin, 1),
gate,
levelScale: amp,
doneAction: 2);
Out.ar(0,
GrainBuf.ar(2, Impulse.kr(100), 0.1, sndbuf, LFNoise1.kr.range(0.5, 2),
LFNoise2.kr(0.1).range(0, 1), 2, pan, envbuf) * env)
}).send(s);

)

// use built-in env
x = Synth(\buf_grain_test, [\sndbuf, b, \envbuf, -1])

// switch to the custom env
x.set(\envbuf, z)
x.set(\envbuf, -1);

x.set(\gate, 0);

