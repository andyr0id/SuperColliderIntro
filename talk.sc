/**************************************
*    Introduction to SuperCollider    *
*    Andy Lambert                     *
*    andyroid.co.uk                   *
*    @andyr0id                        *
***************************************/

/*
    1) What is SuperCollider?

    - SuperCollider is an open source environment and programming language for real time audio synthesis and algorithmic composition.
*/

(
w = Window("SC Triad", Rect(100, Window.screenBounds.height-180, 800, 600));
w.drawFunc = {
	Pen.use {
        Pen.width = 2;
		Pen.beginPath;
		Pen.moveTo(400@100);
		Pen.lineTo(700@500);
		Pen.lineTo(100@500);
		Pen.lineTo(400@100);
		Pen.stroke;
    };
};
a = StaticText(w, Rect(325, 50, 150, 35));
a.align = \center;
a.font = Font("Monaco", 35);
a.string = "Server";
b = StaticText(w, Rect(50, 510, 100, 35));
b.font = Font("Monaco", 35);
b.string = "IDE";
c = StaticText(w, Rect(700, 508, 100, 40));
c.font = Font("Monaco", 35);
c.string = "Lang";
w.front;
)

/*
    2) "Hello, SuperCollider!"
*/

//sound!
{ SinOsc.ar }.play;

//post messages
"Hello, OSC!".postln;

//maths
1 + 2
2**4

//arrays
a = [2,4,6,8]
a.sum
a.choose

//everything is an object
0.5.asFraction
5.reciprocal
440.cpsmidi
60.midicps
[69,50,38,60].choose.midicps

//loops
(
4.do {|i|
	(i + "potato").postln;
};
)

//loops over arrays
a
(
a.do {|i|
	(i + "potato").postln;
};
)

//functions
f = {}
f = {|x| x**2;}
f.(4)

//control statements
(
if (a.choose > 4) {
	"big";
} {
	"small";
};
)

//and much, much more...!

/*
    3) Let's make some noise!
*/

//back to our Hello, world:
{SinOsc.ar}.scope
//.scope shows us the waveform as it's sonified

{SinOsc.ar}.plot
//.plot draws a graph without maing a sound

//no more monophony!
{SinOsc.ar.dup}.scope
{SinOsc.ar.dup}.plot

(
{
	[SinOsc.ar, SinOsc.ar]
}.plot
)

//.dup puts a duplicate of a signal into an array
//multichannel expansion makes it come out or Left and Right channels

//two chanels with different oscillators:
(
{
	[SinOsc.ar, Saw.ar] * 0.8 //<-- make this one quieter for your ears!
}.scope
)

//mix the two signals together - back to one channel (we can make it two channels again)
(
{
	Mix([SinOsc.ar, Saw.ar]) * 0.8
}.plot
)

//bored of 440Hz?
{SinOsc.ar(320).dup}.scope
{SinOsc.ar(62.midicps).dup}.scope

(
{
	Mix(SinOsc.ar([60.midicps, 64.midicps], 0, 0.5)).dup
}.scope
)

//interaction!
{SinOsc.ar(MouseX.kr(220,880,\exponential))}.play
(
{
	Mix([
		SinOsc.ar(MouseX.kr(220,880,\exponential)),
		SinOsc.ar(MouseY.kr(220,880,\exponential))
	]).dup * 0.5
}.play
)

/*
    4) Synths
*/

//- a sound is a "synth" or:
Synth
{}.play
//- the recipe for a sound is a "synth definition" or:
SynthDef

//so far we've been using .play as a quick way to play sounds
//we can make things more flexible by making a synthdef

//old way:
(
{
	var sig, env; //these are "variables" - a way to store data
    sig = SinOsc.ar(440, 0, 0.5);
	//this line defines an "Envelope" - a way to make the sound change over time
	env = EnvGen.kr(Env.linen(0.05, 1, 0.1), doneAction: 2);
	sig = sig * env;
	sig ! 2 //<- a quick way to get two channel sound
}.play
)

//new way:
(
SynthDef(\smooth, {|freq = 440, sustain = 1, amp = 0.5|
	var sig, env;
	sig = SinOsc.ar(freq, 0, amp);
	env = EnvGen.kr(Env.linen(0.05, sustain, 0.1), doneAction: 2);
	sig = sig * env;
	//note the we need to explicitly set the "Out" channel in a synth def
	Out.ar(0, sig ! 2);
}).add;
)

//now we can play our new SynthDef like so:
a = Synth(\smooth)
a = Synth(\smooth, [\freq, 700])
a = Synth(\smooth, [\freq, 600, \sustain, 0.5])

/*
    5) Sequencing
*/
TempoClock.default.tempo = 2
(
p = Pbind(
    //the SynthDef to use for each note
    \instrument, \smooth,
    //MIDI note numbers, converted automatically to Hz
    \midinote, Pseq([60, 72, 71, 67, 69, 71, 72, 60, 69, 67], 1),
    //rhythmic values
    \dur, Pseq([2, 2, 1, 0.5, 0.5, 1, 1, 2, 2, 3], 1)
).play;
)

/*
    Final example:
*/

(
var basepath = "/media/andy/Data/Samples/linndrum/";

b = ["kick","sd","chh"].collect{|val| Buffer.read(s,basepath++val++".wav") };
TempoClock.default.tempo = 2;
)

(
SynthDef(\situationsynth,{|out= 0 freq = 440 amp = 0.1 gate=1 cutoff=8000 rq=0.8 lfowidth=0.001 lforate= 3.3 pan=(-0.1)|

	var pulse, filter, env, filterenv, lfo;

	lfo = LFTri.kr(lforate,Rand(0,2.0)!2);

	pulse = Mix(Pulse.ar((((freq.cpsmidi)+[0,0.14])+(lfo*lfowidth)).midicps,[0.5,0.51]+(lfowidth*lfo)))*0.5;

	filterenv = EnvGen.ar(Env([0.0,1.0,0.3,0.0],[0.005,0.57,0.1],-3));

	filter =  RLPF.ar(pulse,100+(filterenv*cutoff),rq);

	env = EnvGen.ar(Env.adsr(0.002,0.57,1.0,0.3),gate,doneAction:2);

	Out.ar(out,Pan2.ar(filter*env*amp,pan));

}).add;


SynthDef(\yazoodelayeffect, {|out =0 gate= 1 pan= 0.1|
	var source = In.ar(out,2);
	var delay;
	var env = Linen.kr(gate, 0.0, 1, 0.1, 2);

	delay= DelayC.ar(source[0].distort,0.25,0.25);

	Out.ar(out,Pan2.ar(delay*env,pan));

}).add;

SynthDef(\sampleplay,{|out= 0 bufnum = 0 amp = 0.1 gate=1 pan = 0.0|

	var playbuf, env;

	playbuf = PlayBuf.ar(1,bufnum);

	env = EnvGen.ar(Env.adsr(0.0,0.0,1.0,0.1),gate,doneAction:2);

	Out.ar(out,Pan2.ar(playbuf*env*amp,pan));

}).add;

SynthDef(\samplecompress,{|out =0 gain=2 reduction=8 ratio=3 gate= 1 attackTime=0.016 relaxTime=0.05|

	var source = In.ar(out,2);
	var compression;
	var env = Linen.kr(gate, 0.0, 1, 0.1, 2);

	compression= Compander.ar(2*source,source,(-8).dbamp,1.0,ratio.reciprocal,attackTime,relaxTime);

	XOut.ar(out,env, compression);

}).add;

SynthDef(\sampleeq1,{|out =0 gate= 1|

	var source = In.ar(out,2);
	var env = Linen.kr(gate, 0.0, 1, 0.1, 2);
	var eq;

	eq= BLowShelf.ar(source,100,1.0,3);
	eq= BPeakEQ.ar(eq,600,1.0,-3);

	XOut.ar(out,env,eq);

}).add;

SynthDef(\sampleeq2,{|out =0 gate= 1|

	var source = In.ar(out,2);
	var env = Linen.kr(gate, 0.0, 1, 0.1, 2);
	var eq;

	eq= BHiPass(150,0.3);

	XOut.ar(out,env,eq);

}).add;

SynthDef(\samplereverb,{|out =0 gate= 1|

	var source = In.ar(out,2);
	var env = Linen.kr(gate, 0.0, 0.3, 0.1, 2);
	var reverb;

	reverb= FreeVerb.ar(source,1.0,0.6,0.6);

	XOut.ar(out,env,reverb);

}).add;

)



(
var kick, snare,hat;

s.latency= 0.1;

kick = Pbind(
	\instrument, \sampleplay,
	\bufnum,b[0],
	\dur,1.0,
	\pan,0.0,
	\amp, 0.5
);


snare = Pbind(
	\instrument, \sampleplay,
	\bufnum,b[1],
	\dur,Pseq([1.25,0.75,2.0],inf),
	\bus,16,
	\amp,0.45,
	\pan,0.0
);

hat = Pbind(
	\instrument, \sampleplay,
	\bufnum,b[2],
	\dur,Pseq(0.5!8++(0.25!16),inf),
	\amp, 0.15,
	\pan,Pseq(0.3!8++((-0.3)!16),inf)
);

//Pfxb organises private busses for each sound
Ptpar([
	0.0,
	Pfxb(Pfx(kick,\samplecompress),\sampleeq1),
	1.0,
	Pfxb(Pfx(snare,\samplecompress,\gain,1,\reduction,10,\ratio,2,\attackTime,0.02),\samplereverb),
	0.0,
	Pfxb(hat,\sampleeq2)
]).play;

TempoClock.default.sched(16, {
Pfx(
	Pbind(
		\instrument,\situationsynth,
		\midinote,Pseq([1,1,13,1,-1,-1,-1,11,8,11,13,1,1,13,1,-1,-1,11,16,15,11,13]+60,inf),	\dur,Pseq([0.5,0.5,0.5,0.25,0.5,0.5,0.25,0.25,0.25,0.25,0.25,0.5,0.5,0.5,0.25,0.5,0.5,0.25,0.25,0.25,0.25,0.25],inf),
		\lfowidth,0.2,
		\cutoff,6000,
		\rq,0.6,
		\pan,-0.1,
		\amp,0.3
	),
	\yazoodelayeffect
).play
})
)