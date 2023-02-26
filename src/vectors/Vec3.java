package vectors;/*

 * Copyright (c) 2013 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2013 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Contributions by: Filip Schramka, Samuel von Stachelski, Simon Felix
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.Color;
import java.util.Collection;

/**
 * 3D vector for basic vector algebra. Instances are immutable.
 *
 * @author radar
 */
public final class Vec3 {
	public static final Vec3 ZERO = new Vec3(0, 0, 0);
	public static final Vec3 ONE = new Vec3(1, 1, 1);
	public static final Vec3 X = new Vec3(1, 0, 0);
	public static final Vec3 Y = new Vec3(0, 1, 0);
	public static final Vec3 Z = new Vec3(0, 0, 1);
	public static final Vec3 X_NEG = new Vec3(-1, 0, 0);
	public static final Vec3 Y_NEG = new Vec3(0, -1, 0);
	public static final Vec3 Z_NEG = new Vec3(0, 0, -1);

	public final float x;
	public final float y;
	public final float z;

	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3(double x, double y, double z) {
		this((float) x, (float) y, (float) z);
	}

	public Vec3(Vec4 v) {
		this(v.x, v.y, v.z);
	}

	public Vec3(float maxValue) {
		this(maxValue,maxValue,maxValue);
	}

	public Vec3(Color red) {
		x=red.getRed()/255f;
		y=red.getGreen()/255f;
		z=red.getBlue()/255f;
	}

	public Vec3(float[] fs) {
		this(fs[0],fs[1],fs[2]);
	}

	public float x() {
		return x;
	}
	
	public float y() {
		return y;
	}

	public float z() {
		return z;
	}

	public boolean isZero() {
		return MathUtilities.isZero(lengthSquared());
	}

	public float length() {
		return MathUtilities.length(x, y, z);
	}

	public float lengthSquared() {
		return MathUtilities.lengthSquared(x, y, z);
	}

	public float distance(Vec3 v) {
		return (float) Math.sqrt((v.x - x) * (v.x - x) + (v.y - y) * (v.y - y) + (v.z - z) * (v.z - z));
	}

	public Vec3 add(Vec3 v) {
		return new Vec3(x + v.x, y + v.y, z + v.z);
	}

	public Vec3 subtract(Vec3 v) {
		return new Vec3(x - v.x, y - v.y, z - v.z);
	}

	public Vec3 scale(float s) {
		return new Vec3(x * s, y * s, z * s);
	}

	public Vec3 negate() {
		return scale(-1);
	}

	public Vec3 normalize() {
		float l = length();
		if (MathUtilities.isZero(l) || l == 1)
			return this;
		return new Vec3(x / l, y / l, z / l);
	}

	public float dot(Vec3 v) {
		return MathUtilities.dot(x, y, z, v.x, v.y, v.z);
	}
	
	public float angle(Vec3 v) {
		return MathUtilities.RADIANS_TO_DEGREES * (float)Math.acos(dot(v) / length() * v.length());
	}

	public Vec3 cross(Vec3 v) {
		return new Vec3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
	}

	public Vec3 toVec3() {
		return this;
	}

	public float[] toArray() {
		return new float[] { x, y, z };
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Vec3))
			return false;
		Vec3 v = (Vec3) obj;
		return x == v.x && y == v.y && z == v.z;
	}
	
	@Override
	public String toString() {
		return String.format("[% .2f,% .2f,% .2f]", x, y, z);
	}
	
	public static Vec3 lerp(Vec3 v0, Vec3 v1, float t) {
		return new Vec3(MathUtilities.lerp(v0.x, v1.x, t), MathUtilities.lerp(v0.y, v1.y, t), MathUtilities.lerp(v0.z, v1.z, t));
	}

	public static float[] toArray(Collection<Vec3> vectors) {
		if (vectors == null)
			return null;

		float[] result = new float[vectors.size() * 3];
		int i = 0;
		for (Vec3 v : vectors) {
			result[i++] = v.x;
			result[i++] = v.y;
			result[i++] = v.z;
		}
		return result;
	}

	public Color toColor() {
		return new Color(range(x), range(y), range(z),1);
	}
	public Color toColor(float gamma) {
		if(gamma==1)
			return toColor();
		
			
		float x0=(float) .1;
		
		float xx=gammaTransform(x,  x0,gamma);
		float yy=gammaTransform(y,  x0,gamma);
		float zz=gammaTransform(z,  x0,gamma);
		
		return new Vec3(xx,yy,zz).toColor();
		
		
		
		
	}
	private float gammaTransform(float x,float x0,float y) {
		
		
		
		if(x<x0) {
			float s=(float) (y/ (x0*(y-1)+Math.pow(x0, 1-y)) );
			
			return s*x;
			
		}
		float d=(float) ((1)/ (Math.pow(x0, y)*(y-1) +1)-1);
		
		return (float) ((1+d)*Math.pow(x, y)-d);
		
		
	}
	private float range(float x) {
		return Math.max(Math.min(x, 1f),0.001f);
		
		
	}
	public Vec3 reflect(Vec3 n) {
		Vec3 i=normalize();
		
		n=n.normalize();
		
		n=n.scale( 2f*n.dot(i) );
		
		return i.subtract(n);
		//I - 2.0 * dot(N, I) * N
	}

	public float sumUp() {
		return (Math.abs(x)+Math.abs(y)+Math.abs(z))/1.7f;
	}
	public float i(int i) {
		switch(i) {
		case 0:return x;
		case 1:return y;
		default:return z;
		
		}
		
	}
	public Vec3 inv(int i) {
		
		switch(i) {
		case 0:return new Vec3(-x,y,z);
		case 1:return new Vec3(x,-y,z);
		default:return new Vec3(x,y,-z);
		}
		
	}
	public static Vec3 Identity(int i) {
		switch(i) {
		case 0:  return new Vec3(1,0,0);
		case 1:  return new Vec3(0,1,0);
		default:  return new Vec3(0,0,1);
		}
		
	}
	public boolean AxisRange(int i,float max,float min) {
		if(i!=0 && (x>max||x<min))
			return false;
		if(i!=1 && (y>max||y<min))
			return false;
		if(i!=2 &&(z>max||z<min))
			return false;
		
		return true;
		
	}

	public Vec2 xy(int i) {
		switch(i) {
		case 0:  return new Vec2(y,z);
		case 1:  return new Vec2(x,z);
		default:  return new Vec2(x,y);
		}
	}

	public Vec3 lerp(Vec3 a,float weight) {
		
		float x=MathUtilities.lerp( this.x,a.x, weight);
		float y=MathUtilities.lerp(this.y, a.y, weight);
		float z=MathUtilities.lerp( this.z,a.z, weight);
		return new Vec3(x,y,z);
		
	}

	public Vec3 invert() {
		return new Vec3(-x,-y,-z);
	}

	public Vec3 pp(int i) {
		return set(i,i(i)+1);
	}
	public Vec3 mm(int i) {
		return set(i,i(i)-1);
	}
	public Vec3 set(int i, float f) {
		switch(i) {
		case 0:  return new Vec3(f,y,z);
		case 1:  return new Vec3(x,f,z);
		case 2:  return new Vec3(x,y,f);
		}
		return this;
	}

	public Vec3 rotate() {
		return new Vec3(z,x,y);
	}

	public Vec3 clip(float f) {
		return new Vec3(Math.min(x, f),Math.min(y, f),Math.min(z, f));
	}
	public Vec3 yaw(float angle) {
		Mat3 mat=new Mat3(	(float)Math.cos(angle),(float) -Math.sin(angle), 0, 
							(float)Math.sin(angle),(float) Math.cos(angle), 0,
							0, 0, 1);
		return mat.transform(this);
		
		
	}
	public Vec3 roll(float angle) {
		Mat3 mat=new Mat3(	(float)Math.cos(angle),0,(float) Math.sin(angle),  
							0,1, 0,
							-(float)Math.sin(angle), 0, (float)Math.cos(angle));
		return mat.transform(this);
		
		
	}
	public Vec3 pitch(float angle) {
		Mat3 mat=new Mat3(	1,0,0,  
							0,(float)Math.cos(angle),-(float)Math.sin(angle),
							0, (float)Math.sin(angle), (float)Math.cos(angle));
		return mat.transform(this);
		
		
	}

	public static Vec3 rand(float shadowSoftness) {
		return new Vec3((Math.random()-.5)*2*shadowSoftness,(Math.random()-.5)*2*shadowSoftness,(Math.random()-.5)*2*shadowSoftness);
	}

	public Vec3 add(float shadowSoftness) {
		return this.add(new Vec3(shadowSoftness));
	}

	public Vec3 middle(Vec3 p) {
		return subtract(p).scale(.5f).add(p);
	}
	public static Vec3 rainbow(float f) {
		
		f=Math.abs(f)%1;
		
		float g=f*6;
		f=f-((int)g);
		
		float asc=f;
		float desc=1.f-f;
		
		switch((int)g) {
		case 0:
			return new Vec3(1,asc,0);
		case 1:
			return new Vec3(desc,1,0);
		case 2:
			return new Vec3(0,1,asc);
		case 3:
			return new Vec3(0,desc,1);
		case 4:
			return new Vec3(asc,0,1);
		case 5:
			return new Vec3(1,0,desc);
		}
		
		return null;
		
		
	}

	public static Vec3 bias(float roughness) {
		return new Vec3((Math.random()-.5),(Math.random()-.5),(Math.random()-.5)).scale(roughness);
		
	}

	public Vec3 mul(Vec3 a) {
		return new Vec3(x*a.x,y*a.y,z*a.z);
	}

	public Vec4 upgrade(int i) {
		return new Vec4(x,y,z,i);
	}

	public Vec3 pp(int i, double d) {
		return set(i,(float) (i(i)+d));
	}

	public static Vec3 bary(float s, float t, Vec3 red, Vec3 blue, Vec3 green) {
		return red.scale(1-s-t).add(blue.scale(s)).add(green.scale(t)); 
	}

	public static float bary(float s, float t, float z1, float z2, float z3) {
		return (1-s-t)*z1+s*z2+z3*t;
	}

	public Vec3 mod(float i) {
		return new Vec3(x%i,y%i,z%i);
	}

	public Color toFColor() {
		if(x<0||y<0||z<0) 
		{
			return Color.PINK;
		}
		if(x>1||y>1||z>1) {
			return Color.yellow;
		}
		return toColor();
	}

	public static Vec2 bary(float s, float t, Vec2 red, Vec2 blue, Vec2 green) {
		return red.scale(1-s-t).add(blue.scale(s)).add(green.scale(t)); 
	}
	public static float baryM(float s, float t, float z1, float z2, float z3) {
		return (1-s-t)*z1+s*(z2-z1)+(z3-z2)*t;
	}
	public static Vec3 baryM(float s, float t, Vec3 red, Vec3 blue, Vec3 green) {
		return red.scale(1-s-t).add(blue.subtract(red).scale(s)).add(blue.subtract(red).scale(t)); 
	}
	public static Vec3 PCbary(float s, float t, float w, float w2, float w3, Vec3 color, Vec3 color2, Vec3 color3) {

		
    	float zz=Vec3.bary(s, t, 1/w,               1/w2,                 1/w3);


    	
    	Vec3 res =Vec3.bary(s,  t, color.scale(1/w),color2.scale(1/w2),color3.scale(1/w3)).scale(1/zz);
    	

    	return res;
	}

	public static float PCbary(float s, float t, float w, float w2, float w3, float z1, float z2, float z3) {
    	float zz=Vec3.bary(s, t, 1/w,               1/w2,                 1/w3);
    	return Vec3.bary(s,  t, z1*(1/w),z2*(1/w2),z3*(1/w3))*(1/zz);

		
	}

	public static Vec4 PCbary(float s, float t, float w, float w2, float w3, Vec4 color, Vec4 color2, Vec4 color3) {
    	float zz=Vec3.bary(s, t, 1/w,               1/w2,                 1/w3);


    	
    	return Vec3.bary(s,  t, color.scale(1/w),color2.scale(1/w2),color3.scale(1/w3)).scale(1/zz);

	}

	private static Vec4 bary(float s, float t, Vec4 red, Vec4 blue, Vec4 green) {
		return red.scale(1-s-t).add(blue.scale(s)).add(green.scale(t)); 
	}

	public Vec3 clipm(float i) {
		return new Vec3(Math.max(i, x),Math.max(i, y),Math.max(i, z));
	}


}
