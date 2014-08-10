// =====================================================================
// SuperCollider Workspace
// =====================================================================
s.boot;
play{{a=SinOsc;l=LFNoise2;a.ar(666*a.ar(l.ar(l.ar(0.5))*9)*RLPF.ar(Saw.ar(9),l.ar(0.5).range(9,999),l.ar(2))).cubed}!2}
play{{a=SinOsc;a.ar(1e3*a.ar(LFNoise2.ar(0.1ok)*9)).cubed.cubed}!2}
play{Splay.ar({Pluck.ar(BPF.ar(f=product({|i|product({LFPulse.ar(2**2.rand2,2.rand/2)}!(i+2))/(1+i)+1}!8)*86,43).sin,Saw.ar,1,1/f,9)}!9)}

(
?var scale = [60, 62, 64, 65, 67, 69, 71, 72].midicps; // we fill an array with a scale;
a = Synth(\detunedSimpleSynth, [\dur, 88.8, \amp, 40.59, \freq, 28.midicps]);
a = Synth(\simpleSynth, [\dur, 88.8, \amp, 362.59, \freq, 27.midicps]);
a = Synth(\risset_clarinet_pg_153, [\amp, 81.2, \freq, 55.midicps, \gate, 1]);
a.set(\gate, 1);
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
	Pbind(\instrument, \detunedSimpleSynth, \amp, 6, \dur, Pseq([0.2,0.1,0.1], inf),
		\freq, Pseq([30, 35, 37].midicps, inf);
	)
);

b.play;
(
c = Pdef.new(\example3,
	Pbind(\instrument, \detunedSimpleSynth, \amp, 2, \dur, Pseq([0.35,0.35]),
		\freq, Pseq([35, 30].midicps, inf);
	)
);


		k = Pbind(\instrument, \bassDrum, \freq, 40, \dur, 3, \amp, 0.6).play(quant: TempoClock.default.beats);
		c.play;
	)

)
a.release();
a.free;

	a = Synth(\bassDrum, [\freq, 55, \dur, 1, \amp, 2]);
	b = Synth(\snare);

b.stop;
b = Synth(\fullkickdrum);
c = Synth(\openhat);
d = Synth(\closedhat);
a = Synth(\perc_grain, [\amp, 1]);
a = Synth(\sin_grain);

(
	SynthDef(\closedhat, {

		var hatosc, hatenv, hatnoise, hatoutput;

		hatnoise = {LPF.ar(WhiteNoise.ar(1),6000)};

		hatosc = {HPF.ar(hatnoise,2000)};
		hatenv = {Line.ar(1, 0, 0.1)};

		hatoutput = (hatosc * hatenv);

		Out.ar(0,
			Pan2.ar(hatoutput, 0)
		)
	}).send(s);

	Synth(\inout);

	SynthDef(\inout, {
		var in;
		in = In.ar(1, 1);
		Out.ar(0,in);
	}).send(s);

	SynthDef(\openhat, {

		var hatosc, hatenv, hatnoise, hatoutput;

		hatnoise = {LPF.ar(WhiteNoise.ar(1),6000)};

		hatosc = {HPF.ar(hatnoise,2000)};
		hatenv = {Line.ar(1, 0, 0.3)};

		hatoutput = (hatosc * hatenv);

		Out.ar(0,
			Pan2.ar(hatoutput, 0)
		);
	}).send(s);

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

	SynthDef(\fullkickdrum, {

		var subosc, subenv, suboutput, clickosc, clickenv, clickoutput;

		subosc = {SinOsc.ar(60)};
		subenv = {Line.ar(1, 0, 1, doneAction: 2)};

		clickosc = {LPF.ar(WhiteNoise.ar(1),1500)};
		clickenv = {Line.ar(1, 0, 0.02)};

		suboutput = (subosc * subenv);
		clickoutput = (clickosc * clickenv);

		Out.ar(0,
			Pan2.ar(suboutput + clickoutput, 0);
		)

	}).send(s);

	SynthDef(\looch, {|freq=75.0, amp=1, pan=0.0, dur=1|
		var signal, env;
		env = EnvGen.ar(Env.perc(level:amp, attackTime:0.0001, releaseTime:dur), doneAction:2);
		signal = env*SinOsc.ar(Line.kr(1, 0, 0.05, 2*freq, freq));
		signal = Pan2.ar(signal, pan);
		Out.ar(0, signal);
	}).send(s);

SynthDef(\snare, {|freq=180, amp=1, dur=0.2, cutoff=6000|
	var snarenoise, snareosc, snareenv, snareout, env, signal;
	env = EnvGen.ar(Env.perc(level:amp, attackTime:0.0001, releaseTime:dur), doneAction:2);
	signal = SinOsc.ar(freq);
	//snareenv = EnvGen.ar(Env.new([0,1,0.25,0.25,0], [0.01,0.02,0.2,0.1]), doneAction:2);
	snareout = env * (signal + LPF.ar(WhiteNoise.ar(amp),cutoff));
	Out.ar(0, Pan2.ar(snareout, 0));
}).send(s);

a = Synth(\snare, [\freq, 126, \cutoff, 4000, \dur, 0.4]);
b = Synth(\bassDrum);
SynthDef(\bassDrum, {|freq=75.0, amp=1, pan=0.0, dur=1, out=0|
			var signal, env;
			env = EnvGen.ar(Env.perc(level:amp, attackTime:0.0001, releaseTime:dur), doneAction:2);
			signal = env*SinOsc.ar(Line.kr(1, 0, 0.05, 2*freq, freq));
			signal = Pan2.ar(signal, pan);
			Out.ar(out, signal);
		}).send(s);
SynthDef(\omgcompress, {|in=2, out=0|
			Out.ar(out, Compander.ar(In.ar(in,2), In.ar(in, 2), thresh: 0.7, slopeBelow: 1, slopeAbove: 0.2, clampTime: 0.01, relaxTime:0.01));
		}).send(s);
SynthDef(\omgverb, {|in=2, out=0|
      Out.ar(out, FreeVerb.ar(In.ar(in, 2), 0.9, 0.8, 0.2));
		}).send(s);
SynthDef(\omgflange, {|in=2, out=0, freq=0.2, amp=0.0025, center=0.009325|
			Out.ar(out, 1* In.ar(in,2) +  DelayC.ar(In.ar(in,2), 0.2, SinOsc.kr(freq, 0, amp, center)));
			}).send(s);
SynthDef(\simpleSynth, {|freq=220.0, amp=0.2, dur=1.0, pan=0.0, out=0|
	var signal, env, harmonics;
	harmonics = 12;
			env = EnvGen.ar(Env.new(levels: [0, 0.8, 0.8, 0], times: [0.01, dur-0.02, 1]), doneAction:2);
	signal = Mix.fill(harmonics, {|i|
				env * SinOsc.ar(freq*(i+1), 1.0.rand, amp * harmonics.reciprocal * harmonics.reciprocal/(i+1));
			});
	signal = LPF.ar(signal, SinOsc.kr(rrand(0.1,1.0), 1.0.rand, 2000, )+2100);
	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);
}).send(s);

SynthDef(\detunedSimpleSynth, {|freq=220.0, amp=0.01, dur=0.5, pan=0.0|
	var signal, env, harmonics, freq1, freq2, freq3;
	harmonics = 6;
	freq1 = freq + (rrand(-1.0, 1.0) * freq * 0.0069);
	freq2 = freq1 + (rrand(-1.0, 1.0) * freq1 * 0.0069);
	freq3 = freq2 + (rrand(-1.0, 1.0) * freq2 * 0.0069);
	env = EnvGen.ar(Env.new(levels: [0, 0.8, 0.8, 0.8, 0], times:[0.01, 0.01, dur-0.03, 0.01]), doneAction:2);
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

SynthDef(\up_trill, {arg freqlo, freqhi, freqtrill=25, dur=0.1, amp=0.5, pan=0.0, out=0;
	var signal, env, envEnv;
	env = EnvGen.ar(Env.perc(0.1, dur), doneAction:2);

	envEnv = SinOsc.ar(freqtrill, 1.0.rand, 0.5, 0.5);

	signal = SinOsc.ar(XLine.ar(freqlo,freqhi, dur)) * amp* env *envEnv;

	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);

}).load();

SynthDef(\cricket, {arg freq, freqtrill=30, dur=10.0, amp=0.1, pan=0.0, out=0;
	var signal, env, envEnv;
	env = EnvGen.ar(Env.linen(0.001, dur-0.002, 0.001), doneAction:2);

	envEnv = SinOsc.ar(freqtrill, 1.0.rand, 0.5, 0.5);

	signal = SinOsc.ar(freq, 0, amp) * env *envEnv;

	signal = Pan2.ar(signal, pan);
	Out.ar(out, signal);

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

SynthDef(\ks_guitar, { arg note, pan=0.0, rand=160, delayTime=20, noiseType=1, out=0;
	var signal, x, y, env, specs, freqs, res, dec;
	//env = Env.new(#[1.9, 1.9, 0],#[10, 0.001]);
	// A simple exciter x, with some randomness.
	signal = Decay.ar(Impulse.ar(0, 0, rand), 1+rand, WhiteNoise.ar);
 	signal = CombL.ar(signal, 0.05, note.reciprocal, delayTime);
	freqs = [102,  204, 376, 436];
	res = 0.1*[0.0005, 0.0005, 0.0002, 0.0005];
	dec = delayTime * [1, 1, 1, 1];
	specs = [freqs, res, dec];
	signal = LPF.ar(signal, 10000);
	x = Klank.ar(`specs, signal) * EnvGen.ar(Env.perc, doneAction:2);
	//	x = CombC.ar(signal, 0.6, 0.25, 10.0, EnvGen.ar(env, doneAction:2));
	x = Pan2.ar(x, pan);

	Out.ar(out, LeakDC.ar(x));
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
SynthDef(\sin_grain, {arg freq, amp, grain_dur, dur=1, grain_freq, gate=1, out=0;
var pan, env, freqdev;
pan = SinOsc.kr(rrand(0.8,1.8), rand(2*3.14));
env = EnvGen.kr(Env.sine(dur, amp), gate, doneAction: 2);

Out.ar(out, GrainSin.ar(2, Impulse.ar(grain_freq), grain_dur, freq, pan, -1, 2048) * env)
}).load();

SynthDef(\perc_grain, {arg freq=80, amp=0.1, grain_dur=1/30.0, dur=1, grain_freq=15, gate=1, out=0;
var pan, env, freqdev;
pan = 0.0;
env = EnvGen.kr(Env.perc(releaseTime:dur, level:amp), gate, doneAction: 2);

Out.ar(out, GrainSin.ar(2, Impulse.ar(grain_freq), grain_dur, freq, pan, -1, 2048) * env)
}).load();


SynthDef(\chimes, { arg dur=6.0, amp=1.0, dens=1.0;
	var signal, env;
	var chime, freqSpecs, freqSpecs2, burst, burst2, harmonics = 10;
	var burstEnv, burstLength = 0.002;

	env = EnvGen.ar(Env.new([0.00001, amp, amp, 0.00001], [0.15, dur-0.3, 0.15], \exponential), gate: 1,  doneAction:2);
	freqSpecs = `[
		{exprand(70.0, 2666.0)}.dup(harmonics), //freq array
		{rrand(0.3, 5.0)}.dup(harmonics).normalizeSum, //amp array
		{rrand(2.0, 8.0)}.dup(harmonics)]; //decay rate array

	freqSpecs2 = `[
		{exprand(300.0, 2666.0)}.dup(harmonics), //freq array
		{rrand(0.3, 5.0)}.dup(harmonics).normalizeSum, //amp array
		{rrand(2.0, 8.0)}.dup(harmonics)]; //decay rate array

	burstEnv = Env.sine(0.01, burstLength); //envelope times
	burst = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Dust.kr(dens))*0.15); //Noise burst
	burst2 = PinkNoise.ar(EnvGen.kr(burstEnv, gate: Dust.kr(dens))*0.15); //Noise burst

	signal = env * Klank.ar(freqSpecs, burst) - burst + env * Klank.ar(freqSpecs2, burst2) - burst2;
	signal = LPF.ar(signal, 666);
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

a =	Synth(\ks_guitar, [\note, 164.0, \pan, 0, \rand, 600, \delayTime, 102.5]);
a = Synth(\resonators, [\f, 40, \n, 20]);
(
//f = 80.00;
f = 119.8645;
//f = 106.787;
//f = 134.543;
n = 8;
d = 1;
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
>

//more realistic crickets
(
	~dryout = Bus.audio(s,2);
	~verbal = Synth(\omgverb, [\in, ~dryout], addAction: \addToTail);

{
	inf.do({
		a = Synth(\cricket,  [\out, ~dryout, \freq, exprand(2300, 2700), \freqtrill, exprand(30.0,30.0), \dur, rrand(15, 25), \amp, rrand(0.015, 0.017), \pan, rrand(-1,1)]);
		rrand(1, 2).wait;
	});
}.fork;
)
(
	~dryout = Bus.audio(s,2);
	~verbal = Synth(\omgverb, [\in, ~dryout], addAction: \addToTail);{
	inf.do({
		a = Synth(\up_trill, [\out, ~dryout, \freqlo, exprand(2800.0, 2900.0), \freqhi, exprand(3100.0, 3300.0), \dur, rrand(0.15, 0.22), \amp, rrand(0.005, 0.010), \pan, rrand(-1,1)]);
		rand(0.01).wait;
	});

}.fork;
)

(
{
	inf.do({
		//	a = Synth(\up_trill, [\freqlo, 80, \freqhi,  60.0, \freqtrill, 2.0, \dur, 0.22, \amp, rrand(0.2, 0.255), \pan, 0]);

w = Synth(\wind, [\dur, 200.0.rand, \freqlo, exprand(100, 4000.0), \freqhi, exprand(100, 2000.0), \amp, 20]);
	rrand(1.15,1.166).wait;
		//	a = Synth(\up_trill, [\freqlo, 400, \freqhi,  400.0, \freqtrill, 10.0, \dur, 0.22, \amp, rrand(0.5, 0.55), \pan, 0]);

		//	a = Synth(\up_trill, [\freqlo, exprand(80.0, 400.0), \freqhi, exprand(40.0, 200.0), \dur, rrand(0.11, 0.44), \amp, rrand(0.6, 0.6), \pan, rrand(-1.0,1.0)]);

	10.wait;
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
(
fork({
inf.do({

		a = Synth(\up_whistle, [\freqlo, rrand(20, 30), \freqhi, rrand(100,120), \dur, rrand(5, 15), \amp, 0.1, \pan, 0]);
rrand(0.5, 0.9).wait();
	//a.free;

});
});
)

 c = Synth(\chimes, [\amp, 1308.9125, \dens, 3.95, \dur, 2000.0]);
c.free;
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
a = Synth(\sin_grain, [\amp, 1.0, \grain_dur, 0.00155002, \grain_freq, 10, \dur, 90, \freq, 2000]);
a.free;
a = Synth(\sin_grain, [\amp, 2, \grain_dur, 0.0001591566, \grain_freq, 6000,   \dur, 11.6, \freq, 100]);
a = Synth(\sin_grain, [\amp, 0.01, \grain_dur, 0.0505002, \grain_freq, 10, \dur, 20, \freq, 500]);

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
l = Task({
	var dur, freq, freqs, freqs1, freqs2, lowfreqs, waitTime, amp, index, guitarPlayed=0;

	lowfreqs = List[36, 38, 40, 41, 43, 45, 47, 48].midicps;
	inf.do({
		4.do({
			dur = rand(20);
			index = lowfreqs.size.rand();
			freq = lowfreqs[index];
			amp = 66;
			Synth(\detunedSimpleSynth, [\freq, freq, \pan, rrand(-1.0, 1.0), \dur, dur, \amp, (amp*50.0/freq)]);
		});
	});
});
a = Synth(\omgverb, [\in, 0, \out, 0]);
a.play();
l.play();
(
t = Task({
	var dur, freq, freqs, freqs1, freqs2, lowfreqs, waitTime, amp, index, guitarPlayed=0;
	var repsOfSameTones = 8;
	var toneCount = 0;
	var count = 0;
	amp = 80;
	dur = 5;
	freqs1 = List[60, 64, 67]; // we fill an array with a scale;
	freqs2 = List[72, 76, 79, 81, 83];
	lowfreqs = List[36, 39, 43, 48, 51];
	~dryout = 0;//Bus.audio(s,2);
	~flange = Bus.audio(s,2);
	~master = Bus.audio(s,2);


	//Synth(\omgflange, [\in, ~dryout, \out, 0, \amp, 0.02, \center, 0.04, \freq, 0.1], addAction: \addToTail);
    //Synth(\omgverb, [\in, ~dryout, \out, 0], addAction: \addToTail);

	//Synth(\omgcompress, [\in, ~master], addAction: \addToTail);

	freqs = (lowfreqs).midicps;
	inf.do({
		count = (count + 7) % 48;
		freqs = (count + lowfreqs).midicps;
		repsOfSameTones.do({
			index = freqs.size.rand();
			amp = 1;
			freq = freqs.at(index);
			Synth(\simpleSynth, [\out, ~dryout, \freq, freq, \pan, -1.0, \dur, dur, \amp, amp]);
			0.1.rand.wait();
			dur = dur - dur * 0.01.rand();
			freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);

			Synth(\simpleSynth, [\out, ~dryout, \freq, freq, \pan, 1.0, \dur, dur, \amp, amp]);
			0.1.rand.wait();
			dur = dur - dur * 0.01.rand();
			freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);

			Synth(\simpleSynth, [\out, ~dryout, \freq, freq, \pan, 0.6, \dur, dur, \amp, amp]);
			0.1.rand.wait();
			dur = dur - dur * 0.01.rand();
			freq = freq + (rrand(-1.0, 1.0) * freq * 0.0069);

			Synth(\simpleSynth, [\out, ~dryout, \freq, freq, \pan, -0.6, \dur, dur, \amp, amp]);
			0.1.rand.wait();

		});
		dur.wait();
	});
});

t.play();

)

t.play();
t.pause();
t.free();


(
t = Task({
	var dur, freq, freqs, freqs1, freqs2, lowfreqs, waitTime, amp, index, guitarPlayed=0;
	var repsOfSameTones = 2;
	var toneCount = 0;
	amp = 80;
	freqs1 = List[60, 64, 67]; // we fill an array with a scale;
	freqs2 = List[72, 76, 79, 81, 83];
	lowfreqs = List[36, 40, 43, 48, 52];
	~dryout = Bus.audio(s,2);
	~flange = 0;//Bus.audio(s,2);
	~master = Bus.audio(s,2);


	//Synth(\omgflange, [\in, ~dryout, \out, ~flange, \amp, 0.002, \center, 0.004, \freq, 0.001], addAction: \addToTail);
    //Synth(\omgverb, [\in, ~dryout, \out, ~flange], addAction: \addToTail);

	Synth(\omgcompress, [\in, ~dryout], addAction: \addToTail);

	freqs = (72.rand + lowfreqs).midicps;
	inf.do({(
			if(0.5.coin, {

				var myfreqs, dur;
				myfreqs = Pseq([45, 40, 35, 30], 1).asStream;
				dur = Pseq([0.5,0.5,0.5,0.5], 1).asStream;
				r = Task({
					var delta;
					var freq;
					while {
						delta = dur.next;
						freq = myfreqs.next;
						delta.notNil;

					} {
						Synth(\bassDrum, [\freq, freq, \out, ~dryout, \amp, 0.15] );
						delta.yield;
					}
				}).play(quant: TempoClock.default.beats);




				 // 	var freq = freqs.at(freqs.size.rand);
				 // 	Synth(\sin_grain_random, [\amp, 0.15, \grain_dur, 0.025, \grain_freq, 25, \dur, 5.0.rand + 5.0, \freq, freq]);

			});

		1.0.wait();
	)});
});

t.play();

)

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

var chorus;
chorus = Routine ({
	var amp, dur, index, freq, freqs, event;
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



notes = Array.newClear(128);    // array has one slot per possible MIDI note

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

(
 var n = 18;
 { Mix.fill(n, { SinOsc.ar(300 + 100.0.rand, 0, 1 / n) }) }.play;
)

TempoClock.default.sched(1, { rrand(1, 3).postln; });


(
SynthDef(\singrain, { |freq = 440, amp = 0.2, sustain = 1|
    var sig;
    sig = SinOsc.ar(freq, 0, amp) * EnvGen.kr(Env.perc(0.01, sustain), doneAction: 2);
    Out.ar(0, sig ! 2);    // sig ! 2 is the same as [sig, sig]
}).add;

r = Routine({
    var delta;
    loop {
        delta = rrand(0.1, 0.3) * 0.5;
        Synth(\singrain, [freq: exprand(200, 1600), amp: rrand(0.1, 0.5), sustain: delta * 0.8]);
        delta.yield;
    }
});
)
r.play;

(
t = Task({
    loop {
        [60, 62, 64, 65, 67, 69, 71, 72].do({ |midi|
            Synth(\singrain, [freq: midi.midicps, amp: 0.2, sustain: 2.1]);
            0.25.wait;
        });
    }
}).play;
)

// probably stops in the middle of the scale
t.stop;

t.play;    // should pick up with the next note

t.stop;

(
t = Task({
    loop {
        s.makeBundle(s.latency, {
            Synth(\singrain, [freq: exprand(400, 1200), sustain: 0.8]);
        });
        0.1.wait;
    }
}).play;
)

t.stop;

(
var midi, dur;
midi = Routine({
    [60, 72, 71, 67, 69, 71, 72, 60, 69, 67].do({ |midi| midi.yield });
});
dur = Routine({
    [2, 2, 1, 0.5, 0.5, 1, 1, 2, 2, 3].do({ |dur| dur.yield });
});

SynthDef(\smooth, { |freq = 440, sustain = 1, amp = 0.5|
    var sig;
    sig = SinOsc.ar(freq, 0, amp) * EnvGen.kr(Env.linen(0.05, sustain, 0.1), doneAction: 2);
    Out.ar(0, sig ! 2)
}).add;

r = Task({
    var delta;
    while {
        delta = dur.next;
        delta.notNil
    } {
        Synth(\smooth, [freq: midi.next.midicps, sustain: delta]);
        delta.yield;
    }
}).play(quant: TempoClock.default.beats + 1.0);
)



(
var dur, amp;
amp = 1.0;
dur = Pseq([0.5,0.5,0.5,0.5], inf).asStream;
r = Task({
	var delta;
	while {
		delta = dur.next;
		delta.notNil;
	} {
		Synth(\bassDrum, [\freq, 30, \out, ~dryout, \amp, amp] );
		(0.5*delta).yield;
		Synth(\bassDrum, [\freq, 30, \out, ~dryout, \amp, amp] );
		(0.5*delta).yield;
		Synth(\snare, [\amp, amp]);
		delta.yield;
	}
}).play(quant: TempoClock.default.beats);
)