// =====================================================================
// SuperCollider Workspace
// =====================================================================
s.boot;
(
var scale = [60, 62, 64, 65, 67, 69, 71, 72].midicps; // we fill an array with a scale;
a = Synth(\detunedSimpleSynth, [\dur, 0.8, \amp, 20.59, \freq, 40.midicps]);
a = Synth(\detunedSimpleSynth, [\dur, 0.8, \amp, 20.59, \freq, 44.midicps]);
a = Synth(\risset_clarinet_pg_153, [\amp, 1.2, \freq, 55.midicps, \gate, 1]);
a.set(\gate, 0);
(
var dur, level;

dur = 2;
level = 0.1;
e = Env.linen(2,2.2-(1+0.64), 1, 0.8);

a = Synth("risset_clarinet_pg_153");
a.setn(\ampenv, e.asArray);
a.play(s);
)
a = Pdef.new(\example1, 
	Pbind(\instrument, \detunedSimpleSynth, \amp, 10, \dur, Pseq([0.8, 0.8, 0.8, 0.8], inf),
		\freq, Pseq([40, 50, 45, 47].midicps, inf);
	)
);

a.play;

b = Pdef.new(\example2, 
	Pbind(\instrument, \detunedSimpleSynth, \amp, 10, \dur, 0.3,
		\freq, Pseq([30, 34, 37].midicps, inf);
	)
);

b.play;
(
c = Pdef.new(\example3, 
	Pbind(\instrument, \detunedSimpleSynth, \amp, 25, \dur, 0.35,
		\freq, Pseq([35, 30].midicps, inf);
	)
);

c.play;
)
a.release();
a.free;
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

SynthDef(\detunedSimpleSynth, {|freq=220.0, amp=0.2, dur=0.5, pan=0.0|
	var signal, env, harmonics, freq1, freq2, freq3;
	harmonics = 10;
	freq1 = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
	freq2 = freq1 + (rrand(-1.0, 1.0) * freq1 * 0.0069);
	freq3 = freq2 + (rrand(-1.0, 1.0) * freq2 * 0.0069);
	env = EnvGen.ar(Env.new(levels: [0, 1, 0.8, 0.8, 0], times:[0.01, 0.01, dur-0.03, 0.01]), doneAction:2);
	//env = EnvGen.ar(Env.new([0, 1, 0.75, 0.75, 0], [0.01*dur, 0.01*dur, 0.97*dur, 0.01*dur], \cubed), doneAction:2);
	signal = Mix.fill(harmonics, {|i| 
		env * (
			SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal * harmonics.reciprocal/(i+1)) +
			SinOsc.ar(freq1*(i+1), 1.0.rand, amp * harmonics.reciprocal * harmonics.reciprocal/(i+1)) +
			SinOsc.ar(freq2*(i+1), 1.0.rand, amp * harmonics.reciprocal * harmonics.reciprocal/(i+1)) +
			SinOsc.ar(freq3*(i+1), 1.0.rand, amp * harmonics.reciprocal * harmonics.reciprocal/(i+1)));
		
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

SynthDef(\up_trill, {arg freqlo, freqhi, freqtrill=25, dur=0.1, amp=0.5, pan=0.0;
	var signal, env, envEnv;
	env = EnvGen.ar(Env.perc(0.1, dur), doneAction:2); 

	envEnv = SinOsc.ar(freqtrill, 1.0.rand, 0.5, 0.5);
	
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
SynthDef(\ks_guitar2, { arg note, pan=0.0, rand=160, delayTime=20, noiseType=1;
	var signal, x, y, env, specs, freqs, res, dec;
	//env = Env.new(#[1.9, 1.9, 0],#[10, 0.001]);
	// A simple exciter x, with some randomness.
	signal = Decay.ar(Impulse.ar(0, 0, rand), 1+rand, WhiteNoise.ar); 
 	signal = CombL.ar(signal, 0.05, note.reciprocal, delayTime);
	//freqs = [163, 121, 257, 326, 383, 431, 369, 504];
	//res = 0.1*[0.0005, 0.0005, 0.0002, 0.0005, 0.0001, 0.0005, 0.00010, 0.000010];
	//dec = delayTime * [1, 1, 1, 1, 1, 1, 1, 1];
	//specs = [freqs, res, dec];
	signal = LPF.ar(signal, 10000);
	//x = Klank.ar(`specs, signal) * EnvGen.ar(Env.perc, doneAction:2);
	//	x = CombC.ar(signal, 0.6, 0.25, 10.0, EnvGen.ar(env, doneAction:2));
	x = Pan2.ar(signal, pan);
	
	Out.ar(0, LeakDC.ar(x));
}).store;
 
SynthDef(\ks_guitar, { arg note, pan=0.0, rand=160, delayTime=20, noiseType=1;
	var signal, x, y, env, specs, freqs, res, dec;
	//env = Env.new(#[1.9, 1.9, 0],#[10, 0.001]);
	// A simple exciter x, with some randomness.
	signal = Decay.ar(Impulse.ar(0, 0, rand), 1+rand, WhiteNoise.ar); 
 	signal = CombL.ar(signal, 0.05, note.reciprocal, delayTime);
	freqs = [163,  257, 326, 383, 431, 369, 504];
	res = 0.1*[0.0005, 0.0005, 0.0002, 0.0005, 0.0001, 0.0005, 0.00010, 0.000010];
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
 
//SynthDef(
SynthDef(\sin_grain, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1;
var pan, env, freqdev;
pan = SinOsc.kr(rrand(0.8,1.8), rand(2*3.14));
env = EnvGen.kr(Env.sine(dur, amp), gate, doneAction: 2); 

Out.ar(0, GrainSin.ar(2, Impulse.ar(grain_freq), grain_dur, freq, pan, -1, 2048) * env)
}).load();

SynthDef(\chimes, { arg dur=6.0, amp=1.0, dens=1.0;
	var signal, env;
	var chime, freqSpecs, freqSpecs2, burst, burst2, harmonics = 10;
	var burstEnv, burstLength = 0.075;

	env = EnvGen.ar(Env.new([0.00001, amp, amp, 0.00001], [0.15, dur-0.3, 0.15], \exponential), gate: 1,  doneAction:2);
	freqSpecs = `[
		{exprand(70.0, 666.0)}.dup(harmonics), //freq array
		{rrand(0.3, 1.0)}.dup(harmonics).normalizeSum, //amp array
		{rrand(2.0, 4.0)}.dup(harmonics)]; //decay rate array

	freqSpecs2 = `[
		{exprand(70.0, 666.0)}.dup(harmonics), //freq array
		{rrand(0.3, 1.0)}.dup(harmonics).normalizeSum, //amp array
		{rrand(2.0, 4.0)}.dup(harmonics)]; //decay rate array

	burstEnv = Env.sine(0.01, burstLength); //envelope times
	burst = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Dust.kr(dens))*0.15); //Noise burst
	burst2 = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Dust.kr(dens))*0.15); //Noise burst

	signal = env * Klank.ar(freqSpecs, burst) + env * Klank.ar(freqSpecs2, burst);
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

//SynthDef(\resonators, { arg f=40, n=50;
//	var signal = [];

		signal = Mix.fill(n, {|i| Resonz.ar(Dust2.ar(5), f * (i + 1), 0.001, 100)}) * n.reciprocal; // scale to ensure no clipping
	 Mix.fill(n, {|i| Resonz.ar(Dust2.ar(5), f * (i + 1), 0.001, 100)}) * n.reciprocal; // scale to ensure no clipping
	
//}).store;

)
)

(
{
	var scale, cycle;
	//scale = Array.fill(12,{ arg i; 60 + i }).midicps; // we fill an array with a scale
	scale = [60, 62, 64, 65, 67, 69, 71, 72].midicps; // we fill an array with a scale
	cycle = scale.size / 2;

	SinOsc.ar(
			Select.kr( 
				LFSaw.kr(0.4, 1, cycle, cycle),
				scale
			)
	);
}.play;
)
(
a = Pdef.new(\example1, 
		Pbind(\instrument, \ks_guitar, // using our sine synthdef
					\note, Pseq([60, 62, 64, 65, 67, 69, 71, 72].midicps, inf);
		)
);
)
 
a.play;

a =	Synth(\ks_guitar, [\note, 220.0, \pan, 0, \rand, 160, \delayTime, 20.5]);
a = Synth(\resonators, [\f, 40, \n, 20]);
(
//f = 80.00;
f = 119.8645;
//f = 106.787;
//f = 134.543;
n = 4;
d = 5;
{
	[Mix.fill(n, {|i| Resonz.ar(Dust2.ar(d), f * (i + 1), 0.001, 100)}) * 108 * n.reciprocal,
	Mix.fill(n, {|i| Resonz.ar(Dust2.ar(d), f * (i + 1), 0.001, 100)}) * 108 * n.reciprocal]; // scale to ensure no clipping
}.play;
)
(n = 4;
d = 5;
{
	[Mix.fill(n, {|i| Resonz.ar(Dust2.ar(d), f * (i + 1), 0.001, 100)}) * 8 * n.reciprocal,
	Mix.fill(n, {|i| Resonz.ar(Dust2.ar(d), f * (i + 1), 0.001, 100)}) * 8 * n.reciprocal]; // scale to ensure no clipping
}.play;
)


//robot crickets:
(
{

	inf.do({
		var freqs = List[70, 64, 65, 69].midicps;
		a = Synth(\cricket,  [\freq, freqs.at(freqs.size.rand()), \freqtrill, exprand(40.0,120.0), \dur, rrand(1, 5), \amp, rrand(0.2, 0.2), \pan, rrand(-1,1)]);
		rrand(1,2).wait;
	});
}.fork;
)

//more realistic crickets
(
{
	inf.do({
		a = Synth(\cricket,  [\freq, exprand(2000, 3000), \freqtrill, exprand(20.0,50.0), \dur, rrand(10, 30), \amp, rrand(0.1, 0.2), \pan, rrand(-1,1)]);
		rrand(3, 5.0).wait;
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

(
{
	inf.do({
 		a = Synth(\up_trill, [\freqlo, 80, \freqhi,  60.0, \freqtrill, 2.0, \dur, 0.22, \amp, rrand(0.2, 0.255), \pan, 0]);

w = Synth(\wind, [\dur, 5.0.rand, \freqlo, exprand(100, 4000.0), \freqhi, exprand(100, 2000.0), \amp, 20]);
	rrand(1.15,1.166).wait;  
		//	a = Synth(\up_trill, [\freqlo, 400, \freqhi,  400.0, \freqtrill, 10.0, \dur, 0.22, \amp, rrand(0.5, 0.55), \pan, 0]);
		
		//	a = Synth(\up_trill, [\freqlo, exprand(80.0, 400.0), \freqhi, exprand(40.0, 200.0), \dur, rrand(0.11, 0.44), \amp, rrand(0.6, 0.6), \pan, rrand(-1.0,1.0)]);

	0.45.wait; 
	});

}.fork;
)

(
{
	inf.do({
 		a = Synth(\up_trill, [\freqlo, exprand(266.0, 523.0), \freqhi, exprand(266.0, 523.0), \dur, rrand(1.15, 10.22), \amp, rrand(1.6, 1.666), \pan, rrand(-1.0,1.0)]);
		rand(3.0).wait;
	});

}.fork;
)
  
 c = Synth(\chimes, [\amp, 4.9125, \dens,51.95, \dur, 10.0]);
c.free;


w.free;
a = Synth(\up_whistle, [\freqlo, 80, \freqhi, 120, \dur, rrand(0.5, 0.5), \amp, 0.1, \pan, 0]);
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
		var amp = 0.25 * rrand(0.1, 0.4);
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
				var amp = rrand (1.0, 4.0);
				var freqhi = rrand(200.0, 800.0);
				var freqlo = rrand(200.0, 800.0);

				//Synth(\wind, [\dur, dur, \amp, amp, \freqlo, freqlo, \freqhi, freqhi]);
					if (amp > 2.0, {
					Synth(\chimes, [\dur, dur, \amp, 2.0*log(freqhi), \dens, log(freqhi)]);
					});c
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
   
 b = Synth(\sin_grain_random, [\amp, 0.01, \grain_dur, 0.05, \grain_freq, 120.0, \dur, 10, \freq, 880]);
b.stop;
b.free;
)

a = Synth(\sin_grain_random, [\amp, 0.2, \grain_dur, 0.055, \grain_freq, 0.01, \dur, 10, \freq, 11]);
a.free;
a = Synth(\sin_grain, [\amp, 5, \grain_dur, 0.000591566, \grain_freq, 60,   \dur, 11.6, \freq, 50]);
a = Synth(\sin_grain, [\amp, 0.9, \grain_dur, 0.01002, \grain_freq, 300, \dur, 4, \freq, 200]);

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

	amp = 80;
	
	inf.do({(


		//freqs = List[20, 40, 60, 100, 160, 260, 420, 680, 1100, 1780, 2880, 4660, 7540, 12400];

		freqs = List[60, 62, 64, 65, 67, 69, 71, 72].midicps; // we fill an array with a scale;
		//freqs = List[160, 119.8645, 106.787, 134.543];	
		guitarPlayed = 0;
		2.do({
			//if (0.4.coin, {
			//	var dur =  rrand(10.0, 30.0);
			//	var amp = rrand (1.0, 2.0);
			//		var freqhi = rrand(200.0, 800.0);
			//	var freqlo = rrand(200.0, 800.0);

				//				Synth(\wind, [\dur, dur, \amp, amp, \freqlo, freqlo, \freqhi, freqhi]);
				//if (amp > 1.0, {
				//	Synth(\chimes, [\dur, dur, \amp, 0.8*log(freqhi), \dens, 0.1*log(freqhi)]);
				//});
				//});
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

			//	a = Synth(\sin_grain, [\amp, 13.921199265507, \freq, 74.016651942682, \grain_dur, 0.00134963384496, \grain_freq, 50, \dur, 24]);

			// (
			
		 	// (
			// 	{
			 
			// 		var grain_dur, grain_freq, sin_freq, sin_dur, env_amp;
			// 		rrand(0,5).sleep();
			// 		sin_freq = exprand(22, 66.6);
			//  		sin_dur = rrand(10,25);//25.0.rand() + 5.0;
			// 		grain_dur = exprand(0.001, 0.01); // rrand(0.0001, 0.002);//0.008);
			//  		grain_freq = rrand(2,52);// exprand(66,66);// rand2(5.0) + 22.0;
			//  		env_amp = 0.001 / (grain_dur ** (2/3) * log(sin_freq) ** 3);
			// 		"amp % freq % grain_dur % grain_freq % dur %\n".postf(env_amp, sin_freq, grain_dur, grain_freq, sin_dur);
			//  		a = Synth(\sin_grain, [\amp,  env_amp, \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, sin_dur, \freq, sin_freq]);
			// 	}.fork();
			//  	); 
			
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
		

			
			


			//			freqs = List[160, 119.8645, 106.787, 134.543];	
			dur = (1.0.rand() * 20.0 ) + 5.0;
	 		index = freqs.size.rand();
	 		freq = freqs.removeAt(index);
			
	 		amp = 66;
	 		Synth(\detunedSimpleSynth, [\freq, freq, \pan, -1.0, \dur, dur, \amp, (amp*50.0/freq)]);
	 		1.0.rand.wait();
			dur = dur + 1.0.rand();
	 		freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			
	 		Synth(\detunedSimpleSynth, [\freq, freq, \pan, 1.0, \dur, dur, \amp, (amp*50.0/freq)]);
	 		1.0.rand.wait();
	 		dur = dur + 1.0.rand();
	 		freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			
	 		Synth(\detunedSimpleSynth, [\freq, freq, \pan, 0.6, \dur, dur, \amp, (amp*50.0/freq)]);
			1.0.rand.wait();
	 		dur = dur + 1.0.rand();
	 		freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
	
	 		Synth(\detunedSimpleSynth, [\freq, freq, \pan, -0.6, \dur, dur, \amp, (amp*50.0/freq)]);
		
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
			Synth(\ks_guitar, [\note, scalePitches[i], \pan, 0, \rand, 1, \delayTime, 2]);
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
var grain_dur, grain_freq, freq, dur, amp;
freq = rrand(60, 600);
dur = rrand(2, 20);//25.0.rand() + 5.0;
grain_dur = rrand(0.0005, 0.05); // rrand(0.0001, 0.002);//0.008);
grain_freq = rrand(1,1000);// rand2(5.0) + 22.0;
a = Synth(\sin_grain, [\amp, 10 * (500.0/freq) * (0.001/grain_dur), \grain_dur, grain_dur, \grain_freq, grain_freq, \dur, dur, \freq, freq]);
)

(


chorus = Routine ({
	var dur, index, freq, event;
	dur = (1.0.rand() * 20.0 ) + 5.0;
	index = freqs.size.rand();
	freq = event.b.midicps;
			
	amp = 1.2;
	Synth(\detunedSynth, [\freq, freq, \pan, -1.0, \dur, dur, \amp, (amp*50.0/freq)]);

	1.0.rand.wait();
	dur = dur + 1.0.rand();
	freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			
	Synth(\simpleSynth, [\freq, freq, \pan, 1.0, \dur, dur, \amp, (amp*50.0/freq)]);
	1.0.rand.wait();
	dur = dur + 1.0.rand();
	freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
			
	Synth(\simpleSynth, [\freq, freq, \pan, 0.6, \dur, dur, \amp, (amp*50.0/freq)]);
	1.0.rand.wait();
	dur = dur + 1.0.rand();
	freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
	
	Synth(\simpleSynth, [\freq, freq, \pan, -0.6, \dur, dur, \amp, (amp*50.0/freq)]);
		
	1.0.rand.wait();
});
)
(
var notes, on, off, dur, index, freq,  amp;


MIDIIn.connect;

notes = Array.newClear(128);  // array has one slot per possible MIDI note

on = Routine({
	var event, newNode;
	loop {
		event = MIDIIn.waitNoteOn;	// all note-on events
	

		freq = event.b.midicps;
		
		Synth(\detunedSimpleSynth, [\freq, freq]);
		Synth(\FM, [\freq, freq]);
	}
}).play;

q = { on.stop; };
)

// when done:
q.value;


MIDIIn.connect;
MIDIIn.disconnect;
(
var notes, on, off, on2;



notes = Array.newClear(128);    // array has one slot per possible MIDI note

on = MIDIFunc.noteOn({ |veloc, num, chan, src|
	if (chan == 1, {
		notes[num] = Synth(\FM, [\freq, num.midicps, \amp, 100* veloc * 0.00315]);
	});
});



off = MIDIFunc.noteOff({ |veloc, num, chan, src|
	notes[num].set(\gate, 0);
});

q = { on.free; off.free; };
)

// when done:
q.value;

s = Server.local;
s.boot;

(
SynthDef("moto-rev", { arg ffreq=200;
    var x;
    x = RLPF.ar(LFPulse.ar(SinOsc.kr(0.1, 0, 100, 0), [0,0.1], 0.1),
        ffreq, 0.1)
        .clip2(0.4);
    Out.ar(0, x);
}).add;
)

b = Bus.control(s);

x = Synth("moto-rev");
x.set(\ffreq, 4400);

// map the synth's first input (ffreq) to read
// from the bus' output index
x.map(0, b);


MIDIIn.connect;
//set the action:
(
~noteOn = {arg src, chan, num, vel;
    b.value = num.midicps.postln;
};
MIDIIn.addFuncTo(\noteOn, ~noteOn);

~control = {arg src, chan, num, val;
    [chan,num,val].postln;
};
MIDIIn.addFuncTo(\control, ~control);

~bend = {arg src, chan, val;
    val.postln;
};
MIDIIn.addFuncTo(\bend, ~bend);
)

// cleanup
x.free;
b.free;
MIDIIn.removeFuncFrom(\noteOn, ~noteOn);
MIDIIn.removeFuncFrom(\control, ~control);
MIDIIn.removeFuncFrom(\bend, ~bend);