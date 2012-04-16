// =====================================================================
// SuperCollider Workspace
// =====================================================================

a = Synth(\simpleSynth, [\dur, 40.0]);
a.free;
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
}).send(s);

SynthDef(\grains, {arg freq, amp, grain_dur, dur=1, grain_freq=2, gate=1;
	var signal, env, envEnv;
	envEnv = EnvGen.ar(Env.sine(dur,1.0), gate, doneAction:2);
	env = EnvGen.ar(Env.sine(grain_dur, amp), Impulse.ar(Line.ar(grain_freq, grain_freq*1.6, dur)), doneAction:0); 
	signal = SinOsc.ar(freq) * env * envEnv;
	Out.ar(0, signal ! 2);
}).load();

SynthDef(\up_whistle, {arg freqlo, freqhi, dur=0.1, amp=0.5, pan = 0.0;
	var signal, env;
	env = EnvGen.ar(Env.linen(0.1, dur, 0.1, amp), doneAction:2); 

	signal = SinOsc.ar(XLine.ar(freqlo,freqhi, dur)) * env;
	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);
}).load();

SynthDef(\up_trill, {arg freqlo, freqhi, dur=0.1, amp=0.5, pan=0.0;
	var signal, env, envEnv;
	env = EnvGen.ar(Env.linen(0.1, dur, 0.1, amp), doneAction:2); 

	envEnv = SinOsc.ar(25, 1.0.rand, 0.5, 0.5);
	
	signal = SinOsc.ar(XLine.ar(freqlo,freqhi, dur)) * env *envEnv;

	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);

}).load();

 
SynthDef(\ks_guitar, { arg note, pan=0.0, rand, delayTime, noiseType=1;
	var signal, x, y, env, specs, freqs, res, dec;
	env = Env.new(#[0.9, 0.9, 0],#[10, 0.001]);
	// A simple exciter x, with some randomness.
	signal = Decay.ar(Impulse.ar(0, 0, rand), 0.1+rand, WhiteNoise.ar); 
 	signal = CombL.ar(signal, 0.05, note.reciprocal, delayTime);
	freqs = [42, 77, 190, 210, 372, 511, 618 , 805];
	res = 5 * [0.0225, 0.0225, 0.025, 0.0225, 0.0125, 0.015, 0.0175, 0.00175];
	dec = 10 * [1, 1, 1, 1, 1, 1, 1, 1];
	specs = [freqs, res, dec];
	signal = LPF.ar(signal, 10000);
	x = Klank.ar(`specs, signal) * EnvGen.ar(env, doneAction:2);
	//	x = CombC.ar(signal, 0.6, 0.25, 10.0, EnvGen.ar(env, doneAction:2));
	x = Pan2.ar(x, pan);
	
	Out.ar(0, LeakDC.ar(x));
}).store;
 
SynthDef(\sin_grain_random, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1;
	var pan, env, freqdev, freqs, freqIndex;
	var varying_grain_freq, trig, varying_freq;
	f = Drand([ 
		2677.777, 2700.0, 2728.0, 2766.666, 2800.0, 2850.0, 2900.0, 2950.0, 3000.0, 3050.0, 3100.0], inf);
	varying_grain_freq = Line.kr(grain_freq*10.0.rand(),grain_freq*10.0.rand(), dur);
	pan =  WhiteNoise.ar(2.0, -1.0); //SinOsc.kr(0.1, 1.rand, 1);
	env = EnvGen.kr(Env.linen(1.0, dur, 1.0), gate, levelScale: amp, doneAction: 2); 
	trig =  Impulse.kr(varying_grain_freq);
	varying_freq = Demand.kr(trig, 0, f);
	Out.ar(0, GrainSin.ar(2, trig, WhiteNoise.ar(1.0, 2.0) * grain_dur, varying_freq, pan) * env)
}).load();
 
SynthDef(\sin_grain, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1;
var pan, env, freqdev;
pan = SinOsc.kr(0.25);
env = EnvGen.kr(Env.sine(dur, 1.0), gate, levelScale: amp, doneAction: 2); 

Out.ar(0, GrainSin.ar(2, Impulse.ar(grain_freq), grain_dur, freq, pan, -1, 2048) * env)
}).load();
)

a = Synth(\up_whistle, [\freqlo, 2700, \freqhi, 3100, \dur, rrand(0.05, 0.05), \amp, 0.2, \pan, 1]);
a.free;


a = Synth(\up_trill, [\freqlo, 2700, \freqhi, 3100, \dur, rrand(0.05, 0.05), \amp, 0.2, \pan, 1.0]);
a.free;
(
 
g = Task({

	{
		var pan = rrand(-1,1), amp = 0.25 * rrand(0.1, 0.2);
		rand(0.5).wait;
		
		inf.do({

			a = Synth(\up_whistle, [\freqlo, 2600, \freqhi, rrand(2825.0, 2875.0), \dur, rrand(0.1,0.2), \amp, amp, \pan, pan]);
			rrand(1.0, 3.0).wait;
		});
	}.fork;

	{

		var pan = rrand(-1,1), amp = 0.25 * rrand(0.1, 0.2);
		rand(0.5).wait;
		inf.do({

			a = Synth(\up_whistle, [\freqlo, 2875.0, \freqhi, rrand(3050.0, 3150.0), \dur, rrand(0.1,0.2), \amp, amp, \pan, pan]);
			rrand(1.0, 3.0).wait;
		});
	}.fork;

	{
		var amp = 0.25*rrand(0.1,0.2), pan = rrand(-1,1);
		rand(0.5).wait;

		inf.do({
			a = Synth(\up_whistle, [\freqlo, 3100.0, \freqhi, rrand(3225.0, 3275.0), \dur, rrand(0.1,0.2), \amp, amp, \pan, pan]);
			rrand(1.0, 3.0).wait;
		});
	}.fork;

	{
		var amp = 0.25*rrand(0.2,0.4), pan = rrand(-1,1);
		rand(0.5).wait;
		inf.do({
			
			a = Synth(\up_trill, [\freqlo, rrand(2575.0, 2625.0), \freqhi, rrand(3125.0,3175.0), \dur, rrand(0.3,0.4), \amp, amp, \pan, pan]);
			rrand(1.0, 2.0).wait;
		});
	}.fork;
});

)
g.play;
g.pause;
g.free;
   
b = Synth(\sin_grain_random, [\amp, 0.0055, \grain_dur, 0.035, \grain_freq, 40.0, \dur, 3600, \freq, 880]);
b.stop;
b.free;
)

a = Synth(\sin_grain_random, [\amp, 0.2, \grain_dur, 0.15, \grain_freq, 1.0, \dur, 100, \freq, 880]);
a.free;
a = Synth(\sin_grain, [\amp, 0.9, \grain_dur, 0.002, \grain_freq, 300, \dur, 40, \freq, 180]);
a = Synth(\sin_grain, [\amp, 0.9, \grain_dur, 0.002, \grain_freq, 300, \dur, 40, \freq, 200]);

a.set(\gate, 0);
a.free();

a =	Synth(\ks_guitar, [\note, 440.0, \pan, 0, \rand, 0.15, \delayTime, 2+1.0.rand]);

t = TempoClock.new;
 
({
	var freqs, freq;
	freqs = List[150.0, 177.777, 200.0, 228.0, 266.666, 300.0, 355.555, 400.0];
	20.do({
		freq = freqs.at(freqs.size.rand);
		Synth(\ks_guitar, [\note, freq, 
			\pan, 1.0.rand2, 
			\rand, 0.1+0.1.rand, 
			\delayTime, 2+1.0.rand]);
		
		(rrand(0.25, 0.5)).wait;
	});
}.fork;
)

(
t = Task({
	var dur, freq, freqs, waitTime, amp, index, guitarPlayed=0;
	amp = 0.8;
	
	inf.do({
		freqs = List[150.0, 177.777, 200.0, 228.0, 266.666, 300.0, 355.555, 400.0];	
		guitarPlayed = 0;
		4.do({

			// if (guitarPlayed == 0, {
			// 	if (0.125.coin, {
			// 		guitarPlayed = 1;
			// 		{
			// 			20.do({
			// 				freq = freqs.at(freqs.size.rand);
			// 				Synth(\ks_guitar, [\note, freq, 
			// 					\pan, 1.0.rand2, 
			// 					\rand, 0.1+0.1.rand, 
			// 					\delayTime, 2+1.0.rand]);
							
			// 				(rrand(0.25, 0.5)).wait;
			// 			});
			// 		}.fork;
			// 	});
			// });

			if(0.125.coin, {
				var grain_dur, grain_freq;
				dur = 25.0.rand() + 5.0;
				index = freqs.size.rand();
				freq = freqs.at(index);
				grain_dur = rrand(0.0001, 0.002);//0.008);
				grain_freq = rand2(5.0) + 22.0;
				a = Synth(\sin_grain, [\amp, 0.6 * (400.0/freq) * (0.001/grain_dur), \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, dur, \freq, freq]);
			});

			//		if(0.125.coin, {
			//	 	var freq = freqs.at(freqs.size.rand);
			//	 	Synth(\sin_grain_random, [\amp, 0.15, \grain_dur, 0.025, \grain_freq, 25, \dur, 5.0.rand + 5.0, \freq, freq]);

			//	 });

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

			1.0.rand.wait();
		});
		(6.0.rand() + 4.0).wait();
	});
});

t.play();
)

t.play();
t.pause();
t.free();
 
(

f = { arg root, steps;

	var scaleMidi, scalePitches;

	scaleMidi = Array.fill(steps.size, {arg i; steps[i] + root});
	scaleMidi.postln;
	scalePitches = scaleMidi.midicps;

		{
		scalePitches.size.do ({ arg i;
			Synth(\ks_guitar, [\note, scalePitches[i], \pan, 0, \rand, 0.1+0.1.rand, \delayTime, 2]);
			(0.05.rand + 0.01).wait;
		});
		
		}.fork
}

)

(



t = Task({
	var scaleMidi, scalePitches, majorSteps, minorSteps, root;

	majorSteps = [0, 4, 7, 12];
	minorSteps = [0, 3, 7, 12];
	inf.do({
		f.value(48, majorSteps);
		1.0.wait;
		f.value(41, majorSteps);
		1.wait;
		f.value(43, majorSteps);
		1.wait;
		f.value(41, majorSteps);
		1.wait;
	});
});

)

t.play;
t.pause;