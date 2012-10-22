// =====================================================================
// SuperCollider Workspace
// =====================================================================

a = Synth(\simpleSynth, [\dur, 0.8, \amp, 0.09, \freq, 666]);
a.free;
(
SynthDef(\simpleSynth, {|freq=220.0, amp=0.2, dur=1.0, pan=0.0|
	var signal, env, harmonics;
	harmonics = 6;
	env = EnvGen.ar(Env.sine(dur, 1.0), doneAction:2);
	signal = Mix.fill(harmonics, {|i| 
				env * SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal * harmonics.reciprocal/(i+1)); 
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
	env = EnvGen.ar(Env.perc(0.1, dur), doneAction:2); 

	envEnv = SinOsc.ar(25, 1.0.rand, 0.5, 0.5);
	
	signal = SinOsc.ar(XLine.ar(freqlo,freqhi, dur)) * amp* env *envEnv;

	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);

}).load();

SynthDef(\cricket, {arg freq, freqtrill=30, dur=10.0, amp=0.1, pan=0.0;
	var signal, env, envEnv;
	env = EnvGen.ar(Env.linen(0.001, dur-0.002, 0.001), doneAction:2); 

	envEnv = SinOsc.ar(freqtrill, 1.0.rand, 0.5, 0.5);
	
	signal = SinOsc.ar(freq, 0, amp) * env *envEnv;

	signal = Pan2.ar(signal, pan);
	Out.ar(0, signal);

}).load();

 
SynthDef(\ks_guitar, { arg note, pan=0.0, rand, delayTime, noiseType=1;
	var signal, x, y, env, specs, freqs, res, dec;
	//env = Env.new(#[1.9, 1.9, 0],#[10, 0.001]);
	// A simple exciter x, with some randomness.
	signal = Decay.ar(Impulse.ar(0, 0, rand), 0.1+rand, WhiteNoise.ar); 
 	signal = CombL.ar(signal, 0.05, note.reciprocal, delayTime);
	freqs = [163, 121, 257, 326, 383, 431, 369, 504];
	res = 0.1*[0.0005, 0.0005, 0.0002, 0.0005, 0.0001, 0.0005, 0.00010, 0.00010];
	dec = delayTime * [1, 1, 1, 1, 1, 1, 1, 1];
	specs = [freqs, res, dec];
	signal = LPF.ar(signal, 10000);
	x = Klank.ar(`specs, signal) * EnvGen.ar(Env.perc, doneAction:2);
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
	pan =  1;//WhiteNoise.ar(2.0, -1.0); //SinOsc.kr(0.1, 1.rand, 1);
	env = EnvGen.kr(Env.linen(1.0, dur, 1.0), gate, levelScale: amp, doneAction: 2); 
	trig =  Impulse.kr(varying_grain_freq);
	varying_freq = Demand.kr(trig, 0, f);
	Out.ar(0, GrainSin.ar(2, trig, WhiteNoise.ar(1.0, 2.0) * grain_dur, varying_freq, pan) * env)
}).load();
 
SynthDef(\sin_grain, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1;
var pan, env, freqdev;
pan = SinOsc.kr(rrand(0.8,1.8), rand(2*3.14));
env = EnvGen.kr(Env.sine(dur, amp), gate, doneAction: 2); 

Out.ar(0, GrainSin.ar(2, Impulse.ar(grain_freq), grain_dur, freq, pan, -1, 2048) * env)
}).load();

SynthDef(\chimes, { arg dur=6.0, amp=1.0, dens=1.0;
	var signal, env;
	var chime, freqSpecs, burst, harmonics = 10;
	var burstEnv, burstLength = 0.075;

	env = EnvGen.ar(Env.sine(dur,amp), gate:1, doneAction:2);
	freqSpecs = `[
		{rrand(1000.0, 6000.0)}.dup(harmonics), //freq array
		{rrand(0.3, 1.0)}.dup(harmonics).normalizeSum, //amp array
		{rrand(2.0, 4.0)}.dup(harmonics)]; //decay rate array

	burstEnv = Env.sine(0.01, burstLength); //envelope times
	burst = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Dust.kr(dens))*0.15); //Noise burst

	signal = env * Klank.ar(freqSpecs, burst);
	Out.ar(0, signal ! 2);
}).store;

SynthDef(\wind, { arg amp=1.0, dur=6.0, freqlo=400, freqhi=880;
	var freqenv, sig, env;
	env = EnvGen.ar(Env.sine(dur,amp), gate:1, doneAction:2);
	freqenv = Env.new([freqlo, freqhi, freqlo], [0.5*dur, 0.5*dur], \exponential); 
	sig = env * BPF.ar(WhiteNoise.ar(0.4), EnvGen.ar(freqenv), 0.1);
	Out.ar(0, sig ! 2);
}).store;

)



(
{
	inf.do({
		a = Synth(\cricket,  [\freq, exprand(3000, 3800), \freqtrill, rrand(19.0,28.0), \dur, rand(30.0), \amp, rrand(0.02, 0.06), \pan, rrand(-1,1)]);
		rand(10.0).wait;
	});
}.fork;
)

(
{
	inf.do({
		a = Synth(\up_trill, [\freqlo, exprand(2800.0, 2900.0), \freqhi, exprand(3100.0, 3300.0), \dur, rrand(0.15, 0.22), \amp, rrand(0.2, 0.6), \pan, rrand(-1,1)]);
		rand(1.0).wait;
	});

}.fork;
)

c = Synth(\chimes, [\amp, 44.9125, \dens, 6.6, \dur, 10.0]);
w = Synth(\wind, [\dur, 40.0, \freqlo, 500.0, \freqhi, 500.0, \amp, 10]);
a =	Synth(\ks_guitar, [\note, 82.0, \pan, 0, \rand, 80, \delayTime, 7.5]);
w.free;
a = Synth(\up_whistle, [\freqlo, 2700, \freqhi, 3100, \dur, rrand(0.05, 0.05), \amp, 0.2, \pan, 1]);
a.free;


a = Synth(\up_trill, [\freqlo, 2166, \freqhi, 2666, \dur, 0.18, \amp, 0.9, \pan, 0]);
a.free;
(
 
g = Task({

	{
		//var pan = rrand(-1,-0.25), 
		var pan = rrand(-1,1);
		var amp = 0.25 * rrand(0.1, 0.2);
		
		rand(1).wait;
		
		inf.do({

			a = Synth(\up_whistle, [\freqlo, 2600, \freqhi, rrand(2825.0, 2875.0), \dur, rrand(0.1,0.2), \amp, amp, \pan, pan]);
			rrand(1.0, 3.0).wait;
		});
	}.fork;

	{

		//		var pan = rrand(-1,-0.35), 
		var amp = 0.25 * rrand(0.1, 0.2);
		var pan = rrand(-1,1);
		rand(2).wait;
		inf.do({

			a = Synth(\up_trill, [\freqlo, 2875.0, \freqhi, rrand(3050.0, 3150.0), \dur, rrand(0.1,0.2), \amp, amp, \pan, pan]);
			rrand(1.0, 5.0).wait;
		});
	}.fork;

	{
		var amp = 0.25*rrand(0.1,0.2);//, pan = rrand(-1,-0.35);
		var pan = rrand(-1,1);
		rand(2).wait;

		inf.do({
			a = Synth(\up_trill, [\freqlo, 2500.0, \freqhi, 2550.0, \dur, rrand(7.0,40), \amp, amp, \pan, pan]);
			rrand(1.0, 5.0).wait;
		});
	}.fork;

(
t = Task({
	var dur, freq, freqs, waitTime, amp, index, guitarPlayed=0;
	inf.do({(
		6.do({
			if (0.4.coin, {
				var dur =  rrand(10.0, 30.0);
				var amp = rrand (1.0, 2.0);
				var freqhi = rrand(200.0, 800.0);
				var freqlo = rrand(200.0, 800.0);

				Synth(\wind, [\dur, dur, \amp, amp, \freqlo, freqlo, \freqhi, freqhi]);
				if (amp > 1.0, {
					Synth(\chimes, [\dur, dur, \amp, 2.0*log(freqhi), \dens, 0.1*log(freqhi)]);
				});
			});

			1.0.rand.wait();
		});
		(6.0.rand() + 4.0).wait();
	)});
});

t.play();
)

t.play();
t.pause();

	{
		var amp = 0.25*rrand(0.2,0.4);//, pan = rrand(-1,-0.35);
		var pan = rrand(-1,1);
		rand(2).wait;
		inf.do({
			
			a = Synth(\up_trill, [\freqlo, rrand(2575.0, 2625.0), \freqhi, rrand(3125.0,3175.0), \dur, rrand(0.3,0.4), \amp, amp, \pan, pan]);
			rrand(1.0, 5.0).wait;
		});
	}.fork;
});

)
g.play;
g.pause;
g.free;
   
b = Synth(\sin_grain_random, [\amp, 0.01, \grain_dur, 0.05, \grain_freq, 120.0, \dur, 100, \freq, 880]);
b.stop;
b.free;
)

a = Synth(\sin_grain_random, [\amp, 0.2, \grain_dur, 0.15, \grain_freq, 1.0, \dur, 100, \freq, 880]);
a.free;
a = Synth(\sin_grain, [\amp, 0.9, \grain_dur, 0.002, \grain_freq, 300, \dur, 40, \freq, 180]);
a = Synth(\sin_grain, [\amp, 0.9, \grain_dur, 0.002, \grain_freq, 300, \dur, 40, \freq, 200]);

a.set(\gate, 0);
a.free();


t = TempoClock.new;
 
(
{
	var freqs, freq;
	freqs = List[150.0,  200.0, 250, 300.0];
	inf.do({
		freq = freqs.at(freqs.size.rand);
		Synth(\ks_guitar, [\note, freq, 
			\pan, 0.0, 
			\rand, 0.1+0.1.rand, 
			\delayTime, 0.1+1.0.rand]);
		
		(rrand(0.112, 0.112)).wait;
	});
}.fork;
)

















(
t = Task({
	var dur, freq, freqs, waitTime, amp, index, guitarPlayed=0;
	amp = 0.8;
	
	inf.do({(


		freqs = List[41.203, 55, 69.296, 82.407];//, 110, 138.59];//, 177.777, 200.0, 228.0, 266.666, 330.0, 355.555, 440.0, 666];	
		guitarPlayed = 0;
		6.do({
			if (0.4.coin, {
				var dur =  rrand(10.0, 30.0);
				var amp = rrand (1.0, 2.0);
				var freqhi = rrand(200.0, 800.0);
				var freqlo = rrand(200.0, 800.0);

				Synth(\wind, [\dur, dur, \amp, amp, \freqlo, freqlo, \freqhi, freqhi]);
				if (amp > 1.0, {
					Synth(\chimes, [\dur, dur, \amp, 0.8*log(freqhi), \dens, 0.1*log(freqhi)]);
				});
			});
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

		
			// (
			
			// 	(
			// 	{
			 
			// 		var grain_dur, grain_freq, sin_freq, sin_dur, env_amp;
			// 		rrand(0,5).sleep();
			// 		sin_freq = exprand(22, 666);
			// 		sin_dur = rrand(10,25);//25.0.rand() + 5.0;
			// 		grain_dur = exprand(0.001, 0.01); // rrand(0.0001, 0.002);//0.008);
			// 		grain_freq = rrand(2,52);// exprand(66,66);// rand2(5.0) + 22.0;
			// 		env_amp = 0.0002 / (grain_dur ** (2/3) * log(sin_freq) ** 3);
			// 		"amp % freq % grain_dur % grain_freq % dur %\n".postf(env_amp, sin_freq, grain_dur, grain_freq, sin_dur);
			// 		a = Synth(\sin_grain, [\amp,  env_amp, \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, sin_dur, \freq, sin_freq]);
			// 	}.fork();
			// 	); 
			
			// //		a = Synth(\sin_grain, [\amp,  1000, \grain_dur, 0.0001, \grain_freq, 10, \dur, 15, \freq, 22]);
			// 	//				a = Synth(\sin_grain, [\amp,  0.0879323, \grain_dur, 0.0035532, \grain_freq, 651.32759, \dur, 15, \freq, 446.551]);
			// 	//a = Synth(\sin_grain, [\amp,  2.00079323, \grain_dur, 0.001532, \grain_freq, 51.32759, \dur, 55, \freq, 666.551]);
			// if(0.5.coin(), {
			// 	(
			// 	{
			// 		var grain_dur, grain_freq, freq, dur, amp;
			// 		rrand(0,5).sleep();
			// 		freq = exprand(20, 666);
			// 		dur = rrand(10,25);//25.0.rand() + 5.0;
			// 		grain_dur = exprand(0.001, 0.00666); // rrand(0.0001, 0.002);//0.008);
			// 		grain_freq = exprand(1,5);// rand2(5.0) + 22.0;
			// 		amp = 0.05 / (log(grain_freq)  * grain_dur ** (2/3) * log(freq) ** (4/5));//;0.5;//50/(grain_freq * freq);
			// 		"amp % freq % grain_dur % grain_freq % dur %\n".postf(amp, freq, grain_dur, grain_freq, dur);
			// 		("a = Synth(\'sin_grain\', [\'amp\',  %, \'grain_dur\', %, \'grain_freq\', %, \'dur\', %, \'freq\', %])").postf(amp, grain_dur, grain_freq, dur, freq);
			// 		a = Synth(\sin_grain, [\amp,  amp, \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, dur, \freq, freq]);
			// 	}.fork();
			// 	)
			// });
			// );

			//		if(0.125.coin, {
			//	 	var freq = freqs.at(freqs.size.rand);
			//	 	Synth(\sin_grain_random, [\amp, 0.15, \grain_dur, 0.025, \grain_freq, 25, \dur, 5.0.rand + 5.0, \freq, freq]);

			//	 });
		
		// 	dur = (1.0.rand() * 20.0 ) + 5.0;
	// 		index = freqs.size.rand();
	// 		freq = freqs.removeAt(index);
			
	// 		amp = 0.666;
	// 		Synth(\simpleSynth, [\freq, freq, \pan, -1.0, \dur, dur, \amp, (amp*50.0/freq)]);
	// 		1.0.rand.wait();
	// 		dur = dur + 1.0.rand();
	// 		freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			
	// 		Synth(\simpleSynth, [\freq, freq, \pan, 1.0, \dur, dur, \amp, (amp*50.0/freq)]);
	// 		1.0.rand.wait();
	// 		dur = dur + 1.0.rand();
	// 		freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			
	// 		Synth(\simpleSynth, [\freq, freq, \pan, 0.6, \dur, dur, \amp, (amp*50.0/freq)]);
	// 1.0.rand.wait();
	// 		dur = dur + 1.0.rand();
	// 		freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
	
	// 		Synth(\simpleSynth, [\freq, freq, \pan, -0.6, \dur, dur, \amp, (amp*50.0/freq)]);

			1.0.rand.wait();
		});
		(6.0.rand() + 4.0).wait();
	)});
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
	var scaleMidi, scalePitches, majorSteps, minorSteps, xSteps, root;

	majorSteps = [0, 4, 7, 12];
	minorSteps = [0, 3, 7, 12];
    xSteps = [0, 3, 5, 7, 12]; 
	inf.do({
		f.value(48, xSteps);
		1.0.wait;
		f.value(45, majorSteps);
		1.wait;
		f.value(41, minorSteps);
		1.wait;
		f.value(43, majorSteps);
		1.wait;
	});
});

)

t.play;
t.pause;


(
var grain_dur, grain_freq, freq, dur;
freq = rrand(20, 3000);
dur = rrand(20, 80);//25.0.rand() + 5.0;
grain_dur = rrand(0.0005, 0.05); // rrand(0.0001, 0.002);//0.008);
grain_freq = rrand(1,1000);// rand2(5.0) + 22.0;
a = Synth(\sin_grain, [\amp, 0.8 * (500.0/freq) * (0.001/grain_dur), \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, dur, \freq, freq]);
)