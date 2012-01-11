// =====================================================================
// SuperCollider Workspace
// =====================================================================

(
SynthDef(\simpleSynth, {|freq=220.0, amp=0.2, dur=1.0, pan=0.0|
	var signal, env, harmonics;
	harmonics = 10;
	env = EnvGen.ar(Env.triangle(dur, 1.0), doneAction:2);
	signal = Mix.fill(harmonics, {|i| 
				env * SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal/(i+1)); 
			});
	
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).send(s)
)

a = Synth(\simpleSynth, [\amp, 0.5, \dur, 10]);

(

t = Task({
	var dur, freq, freqs, waitTime, amp, index;
	amp = 0.5;
	inf.do({
	freqs = List[50.0, 66.667, 75.0, 100.0, 114.0, 133.333, 150.0, 
		177.777, 200.0, 228.0, 266.666, 300.0, 355.555, 400.0];
		4.do({
			dur = (1.0.rand() * 20.0 ) + 5.0;
			index = freqs.size.rand();
			freq = freqs.removeAt(index);
			"freq %; dur %\n".postf(freq, dur);
			Synth(\simpleSynth, [\freq, freq, \pan, -1.0, \dur, dur, \amp, (amp*50.0/freq)]);
			1.0.rand.wait();
			dur = dur + 1.0.rand();
			freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			"freq %; dur %\n".postf(freq, dur);
			Synth(\simpleSynth, [\freq, freq, \pan, 1.0, \dur, dur, \amp, (amp*50.0/freq)]);
			1.0.rand.wait();
			dur = dur + 1.0.rand();
			freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			"freq %; dur %\n".postf(freq, dur);
			Synth(\simpleSynth, [\freq, freq, \pan, 0.6, \dur, dur, \amp, (amp*50.0/freq)]);
	1.0.rand.wait();
			dur = dur + 1.0.rand();
			freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			"freq %; dur %\n".postf(freq, dur);
			Synth(\simpleSynth, [\freq, freq, \pan, -0.6, \dur, dur, \amp, (amp*50.0/freq)]);

		});
		(6.0.rand() + 4.0).wait();
	});
});

t.play();
)

t.play();
t.pause();
t.free();