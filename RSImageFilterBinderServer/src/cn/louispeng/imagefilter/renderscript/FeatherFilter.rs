#pragma version(1)
#pragma rs java_package_name(cn.louispeng.imagefilter.renderscript)

// 羽化效果

#include "Clamp.rsh"

// set from the java SDK level
rs_allocation gIn;
rs_allocation gOut;
rs_script gScript;


// magic factor
const static float _Size = 0.5f;

// static variables
static uint32_t _width;
static uint32_t _height;
static float _ratio;
static uint32_t _centerX;
static uint32_t _centerY;
static uint32_t _max;
static uint32_t _min;
static uint32_t _diff;

static void setup() {
	_width = rsAllocationGetDimX(gIn);
	_height = rsAllocationGetDimY(gIn);
	_ratio = (_width >  _height) ?  ((float)_height / _width) : ((float)_width / _height);
 	_centerX = _width >> 1;
	_centerY = _height >> 1;
	_max = _centerX * _centerX + _centerY * _centerY;
	_min = _max * (1 - _Size);
	_diff = _max - _min;
}

void filter() {
	setup();
    rsForEach(gScript, gIn, gOut, 0, 0);	// for each element of the input allocation,
    										// call root() method on gScript
}

void root(const uchar4 *v_in, uchar4 *v_out, const void *usrData, uint32_t x, uint32_t y) {
	float4 f4 = rsUnpackColor8888(*v_in);	// extract RGBA values, see rs_core.rsh
	
	// Calculate distance to center and adapt aspect ratio
	int32_t distanceX = _centerX - x;
  	int32_t distanceY = _centerY - y;
  	if (_width > _height){
		distanceX = distanceX * _ratio;
  	} else {
     	distanceY = distanceY * _ratio;
  	}
  
  	uint32_t distSq = distanceX * distanceX + distanceY * distanceY;
  	float v =  (float)distSq / _diff;

  	float3 f3 = f4.rgb + v;
  	f3 = FClamp01Float3(f3);
	
    *v_out = rsPackColorTo8888(f3);
}