// =====================================================================
// SuperCollider Workspace
// =====================================================================

(
SynthDef(\simpleSynth, {|freq=220.0, amp=0.2, dur=1.0, pan=0.0|
	var signal, env, harmonics;
	harmonics = 3;
	env = EnvGen.ar(Env.triangle(dur, 1.0), doneAction:2);
	signal = Mix.fill(harmonics, {|i| 
				env * SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal/(i+1)); 
			});
	
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).send(s);

SynthDef(\grains, {arg freq, amp, grain_dur, dur=1, grain_freq=2, gate=1;
	var signal, env, envEnv;
	envEnv = EnvGen.ar(Env.sine(dur,1.0), gate, doneAction:2);
	env = EnvGen.ar(Env.sine(grain_dur, amp), Impulse.ar(Line.ar(grain_freq, grain_freq*1.6, dur)), doneAction:0); 
	signal = SinOsc.ar(freq) * env * envEnv;
	Out.ar(0, signal ! 2);
}).load();
)

(

SynthDef(\sin_grain_random, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1;
	var pan, env, freqdev, freqs, freqIndex;
	var varying_grain_freq, trig, varying_freq;
	f = Drand([150.0, 
		177.777, 200.0, 228.0, 266.666, 300.0, 355.555, 400.0], inf);
	varying_grain_freq = Line.kr(grain_freq*40.0.rand(),grain_freq*40.0.rand(), dur);
	pan = SinOsc.kr(0.1);
	env = EnvGen.kr(Env.sine(dur, 1.0), gate, levelScale: amp, doneAction: 2); 
	trig =  Impulse.kr(varying_grain_freq);
	varying_freq = Demand.kr(trig, 0, f);
	Out.ar(0, GrainSin.ar(2, trig, WhiteNoise.ar(1.0, 2.0) * grain_dur, varying_freq, pan) * env)
}).load();

SynthDef(\sin_grain, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1, en;
var pan, env, freqdev;
pan = SinOsc.kr(0.25);
env = EnvGen.kr(Env.sine(dur, 1.0), gate, levelScale: amp, doneAction: 2); 

Out.ar(0, GrainSin.ar(2, Impulse.ar(grain_freq + (grain_freq * SinOsc.kr(0.1*grain_freq, 0, 0.1*grain_freq))), grain_dur, freq, pan, -1, 2048) * env)
}).load();
)


a = Synth(\sin_grain_random, [\amp, 0.25, \grain_dur, 0.025, \grain_freq, 25.0, \dur, 10, \freq, 880]);
a = Synth(\sin_grain, [\amp, 0.9, \grain_dur, 0.095, \grain_freq, 30, \dur, 15, \freq, 320]);
a.set(\gate, 0);
a.free();

(

t = Task({
	var dur, freq, freqs, waitTime, amp, index;
	amp = 0.8;
	inf.do({
	freqs = List[150.0, 
		177.777, 200.0, 228.0, 266.666, 300.0, 355.555, 400.0];
		4.do({
			if(1.0.rand < 0.5, {
				var grain_dur, grain_freq;
				dur = 10.0.rand() + 5.0;
				index = freqs.size.rand();
				freq = freqs.at(index);
				grain_dur = rrand(0.001, 0.02);//0.008);
				grain_freq = rand2(40.0) + 60;
				a = Synth(\sin_grain, [\amp, 125.0/freq, \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, dur, \freq, freq]);
			});

			if(1.0.rand < 10.125, {
			 	var freq = freqs.at(freqs.size.rand);
			 	a = Synth(\sin_grain_random, [\amp, 0.4, \grain_dur, 0.08, \grain_freq, 0.1, \dur, 5.0.rand + 5.0]);

			 });

			dur = (1.0.rand() * 20.0 ) + 5.0;
			index = freqs.size.rand();
			freq = freqs.removeAt(index);
			"freq %; dur %\n".postf(freq, dur);
			amp = 1.0;
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