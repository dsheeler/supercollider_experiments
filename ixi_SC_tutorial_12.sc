

// =====================================================================
// - SuperCollider Basics -
// =====================================================================

// Tutorial 12 - Audio Effects

// =====================================================================
// - ixi audio tutorial - www.ixi-audio.net
// =====================================================================


/*		
		---------------------------------------------------------------
 		Copyright (c) 2005-2008, ixi audio.
 		This work is licensed under a Creative Commons 
		Attribution-NonCommercial-ShareAlike 2.0 England & Wales License.
 		http://creativecommons.org/licenses/by-nc-sa/2.0/uk/
		---------------------------------------------------------------
*/



// ========== Contents of this tutorial ==========

// 	1) Delays
// 	2) Phaser (Phase Shifting)
//	3) Flanger
//	4) Chorus
//	5) Reverb
//	6) Tremolo
//	7) Distortion
//	8) Compressor
//	9) Limiter
//	10) Sustainer
//	11) Noise gate
//	12) Normalizer
//	13) Limiter (Ugen)
//	14) Amplitude
//	15) Pitch
//	16) Filters
//	17) Making Audio Unit plugins




// 1) ========= Delays ==========



/* 
Delays come with different functionalities. In SC there are 3 main types of delays,
(Delay, Comb and Allpass)
- DelayN/DelayL/DelayC are simple echos with no feedback. 
- CombN/CombL/CombC are comb delays with feedback (decaytime)
- AllpassN/AllpassL/AllpassC die out faster than the comb, and have feedback as well

Delays can have fixed delay time and generate different effects according to delay time:
	Short ( < 10 ms)
	Medium ( 10 - 50 ms)
	Long ( > 50 ms)
A short delay (1-2 samples) can create a FIR lowpass filter.
Increase the delay time (1-10 ms) and a comb filter appears.
Medium delays result in thin signal but also an ambience and width in the sound.
Long delays create discrete echos which imitates sound bouncing of hard walls.

Delays can also have variable delay time which can result in the following effects:
	Phase Shifting
	Flanging
	Chorus
These effects are explained in dedicated sections here below 
*/



// load some sound files into buffers (use your own)
(
d = Buffer.read(s,"sounds/digireedoo.aif");
e = Buffer.read(s,"sounds/holeMONO.aif");
e = Buffer.read(s, "sounds/a11wlk01.wav"); // this one is in SC sounds folder
)


// -------------------- a) Short delays

// DelayL is a simple delay line without decay time arguments

// let's play with it (impulse in left ear, delayed signal in the right)
// and mouseX controlling the delay time
(
{
var signal;
var delaytime = MouseX.kr(0.001,0.2, 1);
signal = Impulse.ar(1); // the sound source

d =  DelayL.ar(signal, 0.6, delaytime);

[d, signal]
}.play
)


// now what happens if we add two signals where one has a short delay ( < 10 ms)
// - we get a lowpass filter.
// NOTE: 0.000022675 is roughly the same as 1/44100 or 44100.reciprocal
(
{
var signal;
var delaytime = MouseX.kr(0.000022675, 0.01); // from a sample to 10 ms

signal = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
d =  DelayN.ar(signal, 0.6, delaytime); // try replacing with CombN (with 0 decayTime)

(signal + d).dup
}.play
)

// We have replaced DelayN with CombN and use mouseY for decayTime ( < 10 ms)
(
{
var signal;
var delaytime = MouseX.kr(0.00022675,0.01, 1); // NOTE: sample is too short here - it explodes!

signal = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
d =  CombC.ar(signal, 0.6, delaytime, MouseY.kr(0.001,1, 1));

(signal + d).dup
}.play
)


(
{
var signal;
var delaytime = MouseX.kr(0.1,0.4, 1);
signal = Impulse.ar(1);

// delaying the impulse with 4 delays
d =  DelayL.ar(signal, 0.6, delaytime);
e =  DelayL.ar(signal, 0.6, delaytime*1.1);
f =  DelayL.ar(signal, 0.6, delaytime*1.2);
g =  DelayL.ar(signal, 0.6, delaytime*1.3);

(d+e+f+g).dup
}.play
)


// karplus gone really wrong - no decay, just a line of 10 delays
(
{
var signal;
var delaytime = MouseX.kr(0.01,2, 1);
signal = Impulse.ar(MouseY.kr(0.5, 2));

a = Mix.fill(10, {arg i; DelayL.ar(signal, 2, delaytime*(i/10+1));});

(a).dup
}.play
)



// Comb and Allpass have decaytime arguments
// the old Karplus-Strong
(
{
var signal;
var delaytime = MouseX.kr(0.001,0.2, 1);
var decaytime = MouseY.kr(0.1,2, 1);

signal = Impulse.ar(1);

d =  CombL.ar(signal, 0.6, delaytime, decaytime);

d!2
}.play(s)
)

// compare the Comb and the Allpass

(
{ // use the mouse !!!
var signal;
var delaytime = MouseX.kr(0.001,0.2, 1);
var decaytime = MouseY.kr(0.1,2, 1);

signal = Impulse.ar(1);

d =  AllpassC.ar(signal, 0.6, delaytime, decaytime);

d!2
}.play
)


// and we add the good old Decay with WhiteNoise as the source
(
{ // use the mouse !!!
var signal;
var delaytime = MouseX.kr(0.001,0.2, 1);
var decaytime = MouseY.kr(0.1,2, 1);

signal = Decay.ar(Impulse.ar(1), 0.3, WhiteNoise.ar * 0.3, 0);

d =  CombL.ar(signal, 0.6, delaytime, decaytime);

d!2
}.play(s)
)



// -------------------- b) Medium delays ( 10 - 50 ms)


f = Buffer.read(s, "sounds/a11wlk01.wav");

// try out the following different delays (uncomment)
// the signals are not added (the dry and wet)
(
{
var signal;
var delaytime = MouseX.kr(0.01,0.05); // between 10 and 50 ms.
signal = PlayBuf.ar(1, f.bufnum, BufRateScale.kr(f.bufnum), loop:1);

// compare DelayL, CombL and AllpassL

//d =  DelayL.ar(signal, 0.6, delaytime);
//d = CombL.ar(signal, 0.6, delaytime, MouseY.kr(1,4));
d =  AllpassL.ar(signal, 0.6, delaytime, MouseY.kr(1,4));

[signal, d] // dry signal in left channel, delay in the right
}.play(s)
)


// same as above, but here we add the signals
(
{
var signal;
var delaytime = MouseX.kr(0.01,0.05); // between 10 and 50 ms.
signal = PlayBuf.ar(1, f.bufnum, BufRateScale.kr(f.bufnum), loop:1);

// compare DelayL, CombL and AllpassL

//d =  DelayL.ar(signal, 0.6, delaytime);
//d = CombL.ar(signal, 0.6, delaytime, MouseY.kr(0.001,4));
d =  AllpassL.ar(signal, 0.6, delaytime, MouseY.kr(0.001,4));

(signal+d).dup
}.play(s)
)


// same as above, here using AudioIn for the signal 
(
{
var signal;
var delaytime = MouseX.kr(0.01,0.05); 
signal = AudioIn.ar(1);

// compare DelayL, CombL and AllpassL

//d =  DelayL.ar(signal, 0.6, delaytime);
d = CombL.ar(signal, 0.6, delaytime, MouseY.kr(0.001,4));
//d =  AllpassL.ar(signal, 0.6, delaytime, MouseY.kr(0.001,4));

(signal+d).dup
}.play(s)
)



// -------------------- c) Longer delays ( > 50 ms)


(
{
var signal;
var delaytime = MouseX.kr(0.05, 2, 1); // between 50 ms and 2 seconds - exponential.
signal = PlayBuf.ar(1, f.bufnum, BufRateScale.kr(f.bufnum), loop:1);

// compare DelayL, CombL and AllpassL

//d =  DelayL.ar(signal, 0.6, delaytime);
//d = CombL.ar(signal, 0.6, delaytime, MouseY.kr(0.1, 10, 1)); // decay using mouseY
d =  AllpassL.ar(signal, 0.6, delaytime, MouseY.kr(0.1,10, 1));

(signal+d).dup
}.play(s)
)


// same as above, here using AudioIn for the signal instead of the NASA irritation
(
{
var signal;
var delaytime = MouseX.kr(0.05, 2, 1); // between 50 ms and 2 seconds - exponential.
signal = AudioIn.ar(1);

// compare DelayL, CombL and AllpassL

//d =  DelayL.ar(signal, 0.6, delaytime);
//d = CombL.ar(signal, 0.6, delaytime, MouseY.kr(0.1, 10, 1)); // decay using mouseY
d =  AllpassL.ar(signal, 0.6, delaytime, MouseY.kr(0.1,10, 1));

(signal+d).dup
}.play(s)
)



// -------------------- d) Random experiments


//
Server.default = s = Server.internal
FreqScope.new;
{CombL.ar(Impulse.ar(10), 6, 1, 1)}.play(s)


(
{
var signal;
var delaytime = MouseX.kr(0.01,6, 1);
var decaytime = MouseY.kr(1,2, 1);

signal = Impulse.ar(1);

d =  CombL.ar(signal, 6, delaytime, decaytime);

d!2
}.play(s)
)


// we can see the Comb effect by plotting the signal.

(
{
a = Impulse.ar(1);
d =  CombL.ar(a, 1, 0.001, 0.9);
d
}.plot(0.1)
)



// a little play with AudioIn
(
{
var signal;
var delaytime = MouseX.kr(0.001,2, 1);
signal = AudioIn.ar(1);

a = Mix.fill(10, {arg i; var dt;
		dt = delaytime*(i/10+0.1).postln;
		DelayL.ar(signal, 3.2, dt);});

(signal+a).dup
}.play(s)
)

/*
TIP: if you get this line printed ad infinitum:
exception in real time: alloc failed
You could go into the ServerOptions.sc (source file) and change
	var <>memSize = 8192;
to
	var <>memSize = 32768;
which allows the server to use up more memory (RAM)
*/



(
{ // watch your ears !!! Use headphones and lower the volume !!!
var signal;
var delaytime = MouseX.kr(0.001,2, 1);
signal = AudioIn.ar(1);

a = Mix.fill(13, {arg i; var dt;
		dt = delaytime*(i/10+0.1).postln;
		CombL.ar(signal, 3.2, dt);});

(signal+a).dup
}.play(s)
)


// A source code for a Comb filter might look something like this:
int  i, j, s;

for(i=0; i <= delay_size;i++)

  { if (i >= delay)
     j = i - delay;    // work out the buffer position
    else 
    j = i - delay + delay_size + 1;
    // add the delayed sample to the input sample
    s = input + delay_buffer[j]*decay;
    // store the result in the delay buffer, and output
    delay_buffer[i] = s;
    output = s;
  } 
  




// 2) ========= Phaser (phase shifting) ==========

/*
In a Phaser, a signal is sent through an allpass filter, not filtering anything out,
but just shifting the phase of the sound by delaying it. This sound is then added to
the original signal. If the phase is 180 degrees, the sound is cancelled out, but if
it is less than that, it will create variations in the spectra.
*/

// phaser with a soundfile
e = Buffer.read(s, "sounds/a11wlk01.wav");

(
{
var signal;
var phase = MouseX.kr(0.000022675,0.01, 1); // from a sample resolution to 10 ms delay line

var ph;

signal = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);

ph = AllpassL.ar(PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1), 4, phase+(0.01.rand), 0);
/* // try 4 phasers
ph = Mix.ar(Array.fill(4, 
			{ AllpassL.ar(PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1), 4, phase+(0.01.rand), 0)}
		));
*/

(signal + ph).dup 
}.play
)


// try it with a sinewave (the mouse is shifting the phase of the input signal
(
{
var signal;
var phase = MouseX.kr(0.000022675,0.01); // from a sample to 10 ms delay line
var ph;

signal = SinOsc.ar(444,0,0.5);
//signal = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
ph = AllpassL.ar(SinOsc.ar(444,0,0.5), 4, phase, 0);

 (signal + ph).dup 

}.play
)


// using an oscillator to control the phase instead of MouseX
// here using the .range trick:
{SinOsc.ar(SinOsc.ar(0.3).range(440, 660), 0, 0.5) }.play
(
{
var signal;
var ph;

// base signal
signal = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
// phased signal
ph = AllpassC.ar(
		PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1), 
		4, 
		LFPar.kr(0.1, 0, 1).range(0.000022675,0.01), // a circle every 10 seconds 
		0); // experiment with what happens if you increase the decay length

 (signal + ph).dup // we add them together and route to two speakers
}.play
)

/*
NOTE: Theoretically you could use DelayC or CombC instead of AllpassC.
In the case of DelayC, you would have to delete the last argument (0) 
(as DelayC doesn't have decay argument).
*/




// 3) ========= Flanger ==========



/*
In a Flanger, a delayed signal is added to the original signal with a continuously-variable delay (usually smaller than 10 ms) creating a phasing effect. The term comes from times where tapes were used in studios and an operator would place the finger on the flange of one of the tapes to slow it down, thus causing the flanging effect.

Flanger is like a Phaser with dynamic delay filter (allpass), but usually it has a feedback loop.
*/

(
SynthDef(\flanger, { arg out=0, in=0, delay=0.1, depth=0.08, rate=0.06, fdbk=0.0, decay=0.0; 

	var input, maxdelay, maxrate, dsig, mixed, local;
	maxdelay = 0.013;
	maxrate = 10.0;
	input = In.ar(in, 1);
	local = LocalIn.ar(1);
	dsig = AllpassL.ar( // the delay (you could use AllpassC (put 0 in decay))
		input + (local * fdbk),
		maxdelay * 2,
		LFPar.kr( // very similar to SinOsc (try to replace it) - Even use LFTri
			rate * maxrate,
			0,
			depth * maxdelay,
			delay * maxdelay),
		decay);
	mixed = input + dsig;
	LocalOut.ar(mixed);
	Out.ar([out, out+1], mixed);
}).load(s);
)

// audioIn on audio bus nr 10
{Out.ar(10, AudioIn.ar(1))}.play(s, addAction:\addToHead)

a = Synth(\flanger, [\in, 10], addAction:\addToTail)
a.set(\delay, 0.04)
a.set(\depth, 0.04)
a.set(\rate, 0.01)
a.set(\fdbk, 0.08)
a.set(\decay, 0.01)

// or if you prefer a buffer:
b = Buffer.read(s, "sounds/a11wlk01.wav"); // replace this sound with a nice sounding one !!!
{Out.ar(10, PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1))}.play(addAction:\addToHead)

a = Synth(\flanger, [\in, 10], addAction:\addToTail)
a.set(\delay, 0.04)
a.set(\depth, 0.04)
a.set(\rate, 1)
a.set(\fdbk, 0.08)
a.set(\decay, 0.01)

// a parameter explosion results in a Chorus like effect:
a.set(\decay, 0)
a.set(\delay, 0.43)
a.set(\depth, 0.2)
a.set(\rate, 0.1)
a.set(\fdbk, 0.08)

// or just go mad:
a.set(\delay, 0.93)
a.set(\depth, 0.9)
a.set(\rate, 0.8)
a.set(\fdbk, 0.8)




// 4) ========= Chorus ==========


/*
The chorus effect happens when we add a delayed signal with the original with a time-varying delay. 
The delay has to be short in order not to be perceived as echo, but above 5 ms to be audible. If the 
delay is too short, it will destructively interfere with the un-delayed signal and create a flanging 
effect. Often, the delayed signals will be pitch shifted to create a harmony with the original signal.

There is no definite algorithm to create a chorus. There are many different ways to achieve it.
As opposed to the Flanger above, this Chorus does not have a feedback loop. But you could create a
chorus effect out of a Flanger by using longer delay time (20-30 ms instead of 1-10 ms in Flanger)
*/

// a simple chorus
SynthDef(\chorus, { arg inbus=10, outbus=0, predelay=0.08, speed=0.05, depth=0.1, ph_diff=0.5;
	var in, sig, modulators, numDelays = 12;
	in = In.ar(inbus, 1);
	modulators = Array.fill(numDelays, {arg i;
������	LFPar.kr(speed * rrand(0.94, 1.06), ph_diff * i, depth, predelay);});�
	sig = DelayC.ar(in, 0.5, modulators);��
	sig = sig.sum; //Mix(sig); 
	Out.ar(outbus, sig!2); // output in stereo

}).load(s)


// try it with audio in
{Out.ar(10, AudioIn.ar(1))}.play(addAction:\addToHead)
// or a buffer:
b = Buffer.read(s, "sounds/a11wlk01.wav"); // replace this sound with a nice sounding one !!!
{Out.ar(10, PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1))}.play(addAction:\addToHead)

a = Synth(\chorus, addAction:\addToTail)
a.set(\predelay, 0.02);
a.set(\speed, 0.22);
a.set(\depth, 0.5);
a.set(\pd_diff, 0.7);
a.set(\predelay, 0.2);




// 5) ========= Reverb ==========

/*
Achieving realistic reverb is a science on its own, to deep to delve into here.
The most common reverb technique in digital acoustics is to use parallel comb delays
that are fed into few Allpass delays.

Reverb can be analysed into 3 stages:
	Direct sound (from the soundsource)
	Early reflections (discrete 1st generation reflections from walls)
	Reverberation (Nth generation reflections that take time to build up, and fade out slowly)



*/

SynthDef(\reverb, {arg inbus=0, outbus=0, predelay=0.048, combdecay=15, allpassdecay=1, revVol=0.31;
	var sig, y, z;
	sig = In.ar(inbus, 1); 
	
	// predelay
	z = DelayN.ar(sig, 0.1, predelay); // max 100 ms predelay
	
	// 7 length modulated comb delays in parallel :
	y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay) })); 

	6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
	Out.ar(outbus, sig + (y * revVol) ! 2); // as fxlevel is 1 then I lower the vol a bit
}).load(s); 


{Out.ar(10, AudioIn.ar(1))}.play(addAction:\addToHead)

b = Buffer.read(s, "sounds/a11wlk01.wav"); // replace this sound with a nice sounding one !!!
{Out.ar(10, PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1))}.play(addAction:\addToHead)


a = Synth(\reverb, [\inbus, 10], addAction:\addToTail)

a.set(\predelay, 0.048)
a.set(\combdecay, 2.048)
a.set(\allpassdecay, 1.048)
a.set(\revVol, 0.048)




// 6) ========= Tremolo ==========

/*
Tremolo is fluctuating amplitude of a signal
*/

SynthDef(\tremolo, {arg inbus=0, outbus=0, freq=1, strength=1; 
� �var fx, sig; 
� �sig = In.ar(inbus, 1); 
� �fx = sig * SinOsc.ar(freq, 0, strength, 0.5, 2); 
� �Out.ar(outbus, (fx+ sig).dup ) 
}).load(s); 


{Out.ar(10, AudioIn.ar(1))}.play(addAction:\addToHead)

b = Buffer.read(s, "sounds/a11wlk01.wav"); // replace this sound with a nice sounding one !!!
{Out.ar(10, PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1))}.play(addAction:\addToHead)


a = Synth(\tremolo, [\inbus, 10], addAction:\addToTail)

a.set(\freq, 4.8)
a.set(\strength, 0.8)



// 7) ========= Distortion ==========

// use headphones!

(
{
var in, gain;
	in = AudioIn.ar(1);
	gain = MouseX.kr(1,100);
	in=in.abs;
	((in.squared + (gain*in))/(in.squared + ((gain-1)*in) + 1))
!2}.play
)


(
{		// mouseX is pregain, mouseY is postgain
			var in, distortion, fx, y, z;
			in = AudioIn.ar(1);
			distortion = ((in * MouseX.kr(1,10)).distort * MouseY.kr(1,10)).distort;
			fx = Compander.ar(distortion, distortion, 1, 0, 1 ); // sustain
			Out.ar(0, LeakDC.ar(fx + in ) !2 );

}.play
)

// Here not using AudioIN:
b = Buffer.read(s, "sounds/a11wlk01.wav"); // replace this sound with a nice sounding one !!!
{Out.ar(10, PlayBuf.ar(1, b.bufnum, BufRateScale.kr(b.bufnum), loop:1))}.play(addAction:\addToHead)

(
{		// mouseX is pregain, mouseY is postgain
			var in, distortion, fx, y, z;
			in = In.ar(10);
			distortion = ((in * MouseX.kr(1,10)).distort * MouseY.kr(1,10)).distort;
			fx = Compander.ar(distortion, distortion, 1, 0, 1 ); // sustain
			Out.ar(0, LeakDC.ar(fx + in ) !2 );

}.play(addAction:\addToTail) // for addAction, see Synth helpfile or tutorial 13
)




// 8) ========= Compressor ==========


e = Buffer.read(s, "sounds/a11wlk01.wav");

/* 
The compressor reduces the dynamic range of a signal if it exceeds certain threshold.
The compression ratio determines how much the signal that exceeds the threshold is turned 
down. 4:1 compression ratio means that for every 4 dB of signal that goes into the unit,
it turns it down so that only 1 dB comes out.
*/

(
// compressor - Audio In
{
	var in, compander;
	in = AudioIn.ar(1);
	compander = Compander.ar(in, in, MouseX.kr(0.001, 1, 1), 1, 0.5, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)

(
// compressor - Soundfile
{
	var in, compander;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	compander = Compander.ar(in, in, MouseX.kr(0.0001, 1, 1), 1, 0.5, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)



// 9) ========= Limiter ==========

/*
The limiter does essentially the same as the compressor, but it looks at the signal's
peaks whereas the compressor looks at the average energy level. A limiter will not let 
the signal past the threshold, while the compressor does, according to the ratio settings.

The difference is in the slopeAbove argument of the Compander.
(0.5 in the compressor, but 0.1 in the limiter)
*/

(
// limiter - Audio In
{
	var in, compander;
	in = AudioIn.ar(1);
	compander = Compander.ar(in, in, MouseX.kr(0.001, 1, 1), 1, 0.1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)

(
// limiter - Soundfile
{
	var in, compander;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	compander = Compander.ar(in, in, MouseX.kr(0.0001, 1, 1), 1, 0.1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)





// 10) ========= Sustainer ==========


/*
The sustainer works like an inverted compressor, it exaggerates the low amplitudes and
tries to raise them up to the threshold defined.
*/

(
// sustainer - Audio In
{
	var in, compander;
	in = AudioIn.ar(1);
	compander = Compander.ar(in, in, MouseX.kr(0.001, 1, 1), 0.1, 1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)

(
// sustainer - Soundfile
{
	var in, compander;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	compander = Compander.ar(in, in, MouseX.kr(0.0001, 1, 1), 0.1, 1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)

// for comparison, here is the file without sustain:
{PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1)!2}.play




// 11) ========= Noise gate ==========


/*
The noise gate allows a signal to pass through the filter only when it is above a
certain threshold. If the energy of the signal is below the threshold, no sound is
allowed to pass. It is often used in settings where there is background noise and
one only wants to record the signal and not the (in this case) uninteresting noise.
*/


(
// noisegate - Audio In
{
	var in, compander;
	in = AudioIn.ar(1);
	compander = 	Compander.ar(in, in, MouseX.kr(0.005, 1, 1), 10, 1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)



(
// noisegate - sound file
{
	var in, compander;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	compander = 	Compander.ar(in, in, MouseX.kr(0.001, 1), 10, 1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)



// The noise gate needs a bit of parameter tweeking to get what you want, so here is
// the same version as above, just with a MouseY controlling the slopeAbove parameter.

(
// noisegate - Audio In
{
	var in, compander;
	in = AudioIn.ar(1);
	compander = 	Compander.ar(in, in, MouseX.kr(0.005, 1, 1), MouseY.kr(1,20), 1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)


(
// noisegate - soundfile
{
	var in, compander;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	compander = 	Compander.ar(in, in, MouseX.kr(0.001, 1), MouseY.kr(1,20), 1, 0.01, 0.01);
	compander ! 2 // stereo
}.play
)


(
// for fun: a noisegater with a bit of reverb (controlled by mouseY)
// better use headphones - danger of feedback!
{
	var in, compander;
	var predelay=0.048, combdecay=3.7, allpassdecay=0.21, revVol=0.21;
	in = AudioIn.ar(1);
	compander = 	Compander.ar(in, in, MouseX.kr(0.005, 1, 1), 10, 1, 0.01, 0.01);
	z = DelayN.ar(compander, 0.1, predelay);
	y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), MouseY.kr(1,20, 1)) })); 
	6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
	y!2
}.play
)





// 12) ========= Normalizer ==========




/*
Normalizer uses a buffer to store the sound in a small delay and look ahead in the audio.
It will not overshoot like a Compander will, but the downside is the delay. 
The normalizer normalizes the input amplitide to a given level.

*/


(
// normalizer - Audio In
{
	var in, normalizer;
	in = AudioIn.ar(1);
	normalizer = Normalizer.ar(in, MouseX.kr(0.1, 0.9), 0.01);
	normalizer ! 2 // stereo
}.play
)



(
// normalizer - sound file
{
	var in, normalizer;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	normalizer = Normalizer.ar(in, MouseX.kr(0.1, 0.9), 0.01);
	normalizer ! 2 // stereo
}.play
)




// 13) ========= Limiter (ugen) ==========

/*
Limiter uses a buffer to store the sound in a small delay and look ahead in the audio.
It will not overshoot like a Compander will, but the downside is the delay. 
The limiter limits the input amplitide to a given level 

*/


(
// limiter - Audio In
{
	var in, normalizer;
	in = AudioIn.ar(1);
	normalizer = Limiter.ar(in, MouseX.kr(0.1, 0.9), 0.01);
	normalizer ! 2 // stereo
}.play
)



(
// limiter - sound file
{
	var in, normalizer;
	in = PlayBuf.ar(1, e.bufnum, BufRateScale.kr(e.bufnum), loop:1);
	normalizer = Limiter.ar(in, MouseX.kr(0.1, 0.9), 0.01);
	normalizer ! 2 // stereo
}.play
)




// 14) ========= Amplitude ==========


/*
Amplitude tracks the peak amplitude of a signal
*/


// mapping input amplitude to frequency of a sine

{SinOsc.ar(Amplitude.kr(AudioIn.ar(1), 0.1, 0.1, 12000, 0), 0, 0.3)}.play;


// with a noise gater as explained above
(
{
var noisegate, in;
in = AudioIn.ar(1);
noisegate = 	Compander.ar(in, in, MouseX.kr(0.005, 1, 1), MouseY.kr(1,20), 1, 0.01, 0.01);
SinOsc.ar(Amplitude.kr(noisegate, 0.1, 0.1, 12000, 0), 0, 0.3) ! 2
}.play;
)



// Compare the two following examples 

{SinOsc.ar(
	MouseX.kr(100, 6000, 1),
	0,
	Amplitude.kr(AudioIn.ar(1), 0.1, 0.1, 1, 0)
)!2}.play

// -- huh? --

{SinOsc.ar(
	MouseX.kr(100, 6000, 1),
	0,
	AudioIn.ar(1)
)!2}.play




// 15) ========= Pitch ==========


/*
Pitch tracks the pitch of a signal. If the pitch tracker has found the pitch, the 
hasFreq variable will be 1 (true), if it doesn't hold a pitch then it is 0 (false).
(Read the helpfile about how it works)
NOTE: it can be useful to pass the input signal through a Low Pass Filter as it's 
easier to detect the pitch of a signal with less harmonics.
*/


// People always ask "what's this # in front of pitch and hasPitch"?
// it means that the Pitch is outputting an array and we assign the items to two variables.
// example:
# a, b = [444, 555];
a
b

// the simplest of patches - mapping pitch to the frequency of the sine

(
{
	var env, in, freq, hasFreq;
	
	// the audio input
	in = AudioIn.ar(1); 
	
	// the pitch variable and the hasFreq (Pitch.kr returns a list like this [freq, hasFreq])
	# freq, hasFreq = Pitch.kr(in, ampThreshold: 0.2, median: 7);
	
	// when the hasFreq is true (pitch is found) we generate a ADSR envelope that is open until
	// the hasFreq is false again or the amplitude is below the ampThreshold of the Pitch.
	env = EnvGen.ar(Env.adsr(0.51, 0.52, 1, 0.51, 1, -4), gate: hasFreq);
	
	// we plug the envolope to the volume argument of the Sine
	SinOsc.ar(freq, 0, env * 0.5) ! 2

}.play;
)


// a bit more complex patch where we use Amplitude to control volume:

(
{
	var env, in, freq, hasFreq, amp;
	
	// the audio input
	in = AudioIn.ar(1); 
	amp = Amplitude.kr(in, 0.25, 0.25);
	
	// the pitch variable and the hasFreq (Pitch.kr returns a list like this [freq, hasFreq])
	# freq, hasFreq = Pitch.kr(in, ampThreshold: 0.2, median: 7);
	
	// when the hasFreq is true (pitch is found) we generate a ADSR envelope that is open until
	// the hasFreq is false again or the amplitude is below the ampThreshold of the Pitch.
	env = EnvGen.ar(Env.adsr(0.51, 0.52, 1, 0.51, 1, -4), gate: hasFreq);
	
	// we plug the envolope to the volume argument of the Sine
	SinOsc.ar(freq, 0, env * amp) ! 2
	
}.play;
)



(
SynthDef(\pitcher,{
	var in, amp, freq, hasFreq, out, gate, threshold;
	
	threshold = 0.05; // change 
	
	// using a LowPassFilter to remove high harmonics
	in = LPF.ar(Mix.new(AudioIn.ar([1,2])), 2000);
	amp = Amplitude.kr(in, 0.25, 0.25);
	
	# freq, hasFreq = Pitch.kr(in, ampThreshold: 0.1, median: 7);
	gate = Lag.kr(amp > threshold, 0.01);	

	// -- to look at the values, uncomment the following lines 
	// -- (you need a recent build with the Poll class)
	//Poll.kr(Impulse.kr(10), freq, "frequency:");
	//Poll.kr(Impulse.kr(10), amp, "amplitude:");
	//Poll.kr(Impulse.kr(10), hasFreq, "hasFreq:");
	
	out = VarSaw.ar(freq, 0, 0.2, amp*hasFreq*gate);
	
	// uncomment (3 sines (octave lower, pitch and octave higher mixed into one signal (out))
	//out = Mix.new(SinOsc.ar(freq * [0.5,1,2], 0, 0.2 * amp*hasFreq*gate));
	6.do({
		out = AllpassN.ar(out, 0.040, [0.040.rand,0.040.rand], 2)
	});
	Out.ar(0,out)
}).play(s);
)



// Here using the Tartini UGen from Nick Collins.
// In my experience it performs better than Pitch and can be downloaded from here:
// http://www.cus.cam.ac.uk/~nc272/code.html

(
SynthDef(\pitcher,{
	var in, amp, freq, hasFreq, out, threshold, gate;

	threshold = 0.05; // change 
	in = LPF.ar(Mix.new(AudioIn.ar([1,2])), 2000);
	amp = Amplitude.kr(in, 0.25, 0.25);

	# freq, hasFreq = Tartini.kr(in);
	gate = Lag.kr(amp > threshold, 0.01);	
	
	// -- to look at the values, uncomment the following lines 
	// -- (you need a recent build with the Poll class)
	//Poll.kr(Impulse.kr(10), freq, "frequency:");
	//Poll.kr(Impulse.kr(10), amp, "amplitude:");
	//Poll.kr(Impulse.kr(10), hasFreq, "hasFreq:");
		
	out = Mix.new(VarSaw.ar(freq * [0.5,1,2], 0, 0.2, gate* hasFreq *amp ));
	//out = Mix.new(SinOsc.ar(freq * [0.5,1,2], 0, 0.2 * amp*hasFreq*gate));
	6.do({
		out = AllpassN.ar(out, 0.040, [0.040.rand,0.040.rand], 2)
	});
	Out.ar(0,out)
}).play(s);
)




// 16) ========= Filters ==========


// TODO: Make examples and put Filters into a special file

// http://en.wikipedia.org/wiki/Butterworth_filter

Check LPF, BPF, HPF in the helpfiles

// Low Pass Filter
(
{
	var in;
	in = AudioIn.ar(1);
	LPF.ar(in, MouseX.kr(80, 4000));
}.play
)

(
{
	var in;
	in = Blip.ar(440);
	LPF.ar(in, MouseX.kr(80, 24000));
}.play
)



// Band Pass Filter
(
{
	var in;
	in = Blip.ar(440);
	BPF.ar(in, MouseX.kr(80, 22000), MouseY.kr(0.0001, 1));
}.play
)

(
{
	var in;
	in = WhiteNoise.ar(1);
	BPF.ar(in, MouseX.kr(80, 22000), MouseY.kr(0.0001, 1));
}.play
)




// High Pass Filter
(
{
	var in;
	in = Blip.ar(440);
	HPF.ar(in, MouseX.kr(80, 22000));
}.play
)

(
{
	var in;
	in = WhiteNoise.ar(1);
	HPF.ar(in, MouseX.kr(80, 22000));
}.play
)



// Resonant High Pass Filter
(
{
	var in;
	in = Blip.ar(440);
	RHPF.ar(in, MouseX.kr(80, 22000), MouseY.kr(0.0001, 1));
}.play
)

(
{
	var in;
	in = WhiteNoise.ar(1);
	RHPF.ar(in, MouseX.kr(80, 22000), MouseY.kr(0.0001, 1));
}.play
)



// Resonant Low Pass Filter
(
{
	var in;
	in = Blip.ar(440);
	RLPF.ar(in, MouseX.kr(80, 22000), MouseY.kr(0.0001, 1));
}.play
)

(
{
	var in;
	in = WhiteNoise.ar(1);
	RLPF.ar(in, MouseX.kr(80, 22000), MouseY.kr(0.0001, 1));
}.play
)




// SOS - A biquad filter


(
{
	var rho, theta, b1, b2;
	theta = MouseX.kr(0.2pi, pi);
	rho = MouseY.kr(0.6, 0.99);
	b1 = 2.0 * rho * cos(theta);
	b2 = rho.squared.neg;
	SOS.ar(WhiteNoise.ar(0.1 ! 2), 1.0, 0.0, 0.0, b1, b2)
}.play
)




// Resonant filter

{ Resonz.ar(WhiteNoise.ar(0.5), 2000, XLine.kr(1, 0.001, 8)) }.play

// high amp input (from Impulse) and low RQ makes a note 

{Resonz.ar(Impulse.ar(1.5, 0, 50), Rand(200,2000), 0.03) }.play

// try putting 500 in amp and 0.003 in RQ
{Resonz.ar(Impulse.ar(1.5, 0, 500), Rand(200,2000), 0.003) }.play


// for fun ( if you don't like the polyrhythm, put 1 instead of trig)
// or if you like it, then put some more tempi in there and appropriate weights

(
var trig;
var wait = 4;
Task({
	20.do({
		trig = [1, 1.5].wchoose([0.7, 0.3]);
		{Resonz.ar(Impulse.ar(trig, 0, 50*rrand(5,10)), Rand(200,2000), 0.003) ! 2}.play;
		(wait + rrand(0.1,1)).wait;
		wait = wait - rrand(0.01, 0.2);
	})
}).play

)






// 17) ========= Making an Audio Unit (AU) plugin ==========


/*
It is relatively easy to make an AU plugin out of a SuperCollider SynthDefinition.
We use the AudioUnitBuilder to wrap the SuperCollider synth into an AU plugin.

Find the AU wrapper here:
http://supercolliderau.sourceforge.net/

Follow the instructions (put classfile (sc) into SCClassLib, helpfile into help
folder and the scaudk folder next to your SuperCollider.app application. You then
run the code that you want to make into an Audio Unit and you will find your new
plugin in the Audio Units Components folder in your library (if you use .makeInstall).

The Components folder here ~/Library/Audio/Plug-Ins/Components.

Oh, and finally... The AudioUnitsBuilder script uses an application called Rez to
compile the resource for the plugin. Rez is part of the Developer Tools for OS X.
You therefore need to have the Developer Tools installed on your machine.

*/


// now lets make some effect that we want to wrap as an Audio Unit.
// For example taking the input signal and converting it to noise:
(
{|white = 0.3, pink = 0.1, brown = 0.1|
 
	a = AudioIn.ar([1, 2]);
	b = a * WhiteNoise.ar(white); 
	c = a * PinkNoise.ar(pink); 
	d = a * BrownNoise.ar(brown);
	Out.ar(0, b+c+d);//Output to AU host
}.play;
)

// ok this worked fine, so now we put that into the SA builder code:

(
var name, func, specs, componentSubtype, componentType, builder;

name = "SANoise"; // name of your plugin
func =  {|white = 0.3, pink = 0.1, brown = 0.1|
 
	a = AudioIn.ar([1, 2]);
	b = a * WhiteNoise.ar(white); 
	c = a * PinkNoise.ar(pink); 
	d = a * BrownNoise.ar(brown);
	Out.ar(0, b+c+d);//Output to AU host
};

specs =  #[ 
	[ 0, 1, \Linear, 0.01, \Generic ],
	[ 0, 1, \Linear, 0.01, \Generic ],	
	[ 0, 1, \Linear, 0.01, \Generic ]
	]; 
	// these will be fed to an instrument. curve type is ignored by the AU.
	
componentType = \aufx;	

componentSubtype = \SANZ; // this must be a four chars code. It sould be unique in your system within all SuperColliderAU plugins.

builder = AudioUnitBuilder.new(name, componentSubtype,func, specs, componentType);
builder.makeInstall; // you can also try .makePlugin (which does not install it)
)



// here is another one. A simple random panner

(
var name, func, specs, componentSubtype, componentType, builder;

name = "SARandomPanner"; // name of our plugin

func =  {arg amp=1, grainSpeed=10, panWidth=0.5;
	var pan, granulizer;
	pan = LFNoise0.kr(grainSpeed, panWidth);
	granulizer = Pan2.ar(Mix.ar(AudioIn.ar([1,2])), pan) * amp;
	Out.ar(0, granulizer);
};

specs =  #[ 
	[ 0, 1, \Linear, 0.01, \Generic ],
	[ 0.001, 2, \Linear, 0.001, \Generic ],	
	[ 0, 1, \Linear, 0.01, \Generic ]
	]; 
	// these will be fed to an instrument. curve type is ignored by the AU.
	
componentType = \aufx;	

componentSubtype = \SARP; // this must be a four chars code. It sould be unique in your system within all SuperColliderAU plugins.

builder = AudioUnitBuilder.new(name, componentSubtype,func, specs, componentType);
builder.makeInstall; // you can also try .makePlugin (which does not install it)
)


// Feel free to take all the code in this file and make your own AU plugins out of them.


