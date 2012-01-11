// =====================================================================
// SuperCollider Workspace
// =====================================================================

(
SynthDef(\simpleSynth, {|freq=220.0, amp=0.2, dur=1.0, pan=0.0|
	var signal, env, harmonics;
	harmonics = 2;
	env = EnvGen.ar(Env.triangle(dur, 1.0), doneAction:2);
	signal = Mix.fill(harmonics, {|i| 
				env * SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal/(i+1)); 
			});
	
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).send(s)

SynthDef(\grains, {arg freq, amp, grain_dur, dur=1, grain_freq=2, gate=1;
	var signal, env, envEnv;
	envEnv = EnvGen.ar(Env.triangle(dur,1.0), gate, doneAction:2);
	env = EnvGen.ar(Env.sine(grain_dur, amp), Impulse.ar(grain_freq), doneAction:0); 
	signal = SinOsc.ar(freq) * env * envEnv;
	Out.ar(0, signal ! 2);
}).load();
)

a = Synth(\grains, [\amp, 0.5, \grain_dur, 0.004, \grain_freq, 20, \dur, 5, \freq, 251]);
a.gate=0;
a.free();

(

t = Task({
	var dur, freq, freqs, waitTime, amp, index;
	amp = 0.5;
	inf.do({
	freqs = List[50.0, 66.667, 75.0, 100.0, 114.0, 133.333, 150.0, 
		177.777, 200.0, 228.0, 266.666, 300.0, 355.555, 400.0];
		4.do({
			if(1.0.rand < 0.5, {
				var grain_dur, grain_freq;
				dur = 10.0.rand() + 5.0;
				index = freqs.size.rand();
				freq = freqs.at(index);
				grain_dur = rrand(0.00001, 0.008);
				grain_freq = rrand(-5.0,5.0) + 25;
				a = Synth(\grains, [\amp, 0.5, \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, dur, \freq, freq]);
			});

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