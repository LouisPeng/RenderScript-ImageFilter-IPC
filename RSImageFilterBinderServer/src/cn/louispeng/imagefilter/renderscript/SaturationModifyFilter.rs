#pragma version(1)
#pragma rs java_package_name(cn.louispeng.imagefilter.renderscript)

// 色彩饱和度特效

#include "Clamp.rsh"

// set from the java SDK level
rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;
float gSaturationFactor = 0.5f;

//magic saturation modify factor
static float saturation;
static float negosaturation;
static const float SaturationMagicFactor1 = 0.2126f;
static const float SaturationMagicFactor2 = 0.7152f;
static const float SaturationMagicFactor3 = 0.0722f;

void setup() {
	saturation = gSaturationFactor + 1.0f;
	negosaturation = 1.0f - saturation;
}

void filter() {
	setup();
    rsForEach(gScript, gIn, gOut, 0, 0);	// for each element of the input allocation,
    										// call root() method on gScript
}

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
    float4 f4 = rsUnpackColor8888(*v_in);	// extract RGBA values, see rs_core.rsh

	float nego1 = negosaturation * SaturationMagicFactor1;
    float nego2 = nego1 + saturation;
    float nego3 = negosaturation * SaturationMagicFactor2;
    float nego4 = nego3 + saturation;
    float nego5 = negosaturation * SaturationMagicFactor3;
    float nego6 = nego5 + saturation;

    float3 f3;
    f3.r = ((f4.r * nego2) + (f4.g * nego3)) + (f4.b * nego5);
    f3.g = ((f4.r * nego1) + (f4.g * nego4)) + (f4.b * nego5);
   	f3.b = ((f4.r * nego1) + (f4.g * nego3)) + (f4.b * nego6);
   	
    f3 = FClamp01Float3(f3);
    
    *v_out = rsPackColorTo8888(f3);		// pack color back to uchar4
	
}