package opengl;

import java.util.Arrays;

import vectors.Vec3;

public class MeshGenerator {
	
	public static class Out{
		
		public float[] vert;
		public float[] color;
		public float[] uv;
		public float[] normal;
		public int[] ind;
		
		public void calcNormal() throws Exception {
			
			if(ind.length%3!=0)
				throw new Exception("need N*3 indexes");
			
			normal= new float[vert.length];
			
			for(int i=0;i<ind.length;i+=3)
			{
				int r0=ind[i]*3;
				Vec3 a=new Vec3(vert[r0],vert[r0+1],vert[r0+2]);
				int r1=ind[i+1]*3;
				Vec3 b=new Vec3(vert[r1],vert[r1+1],vert[r1+2]);
				int r2=ind[i+2]*3;
				Vec3 c=new Vec3(vert[r2],vert[r2+1],vert[r2+2]);
				
				
				Vec3 d=b.subtract(a).cross(c.subtract(a)).invert().normalize();
				

				normal[r0]=d.x;
				normal[r0+1]=d.y;
				normal[r0+2]=d.z;
			
				normal[r1]=d.x;
				normal[r1+1]=d.y;
				normal[r1+2]=d.z;

				normal[r2]=d.x;
				normal[r2+1]=d.y;
				normal[r2+2]=d.z;
			}
			
//			System.out.println();
//			System.out.println();
//			System.out.println(Arrays.toString(ind));
//			System.out.println(Arrays.toString(vert));
//			System.out.println();
//			System.out.println();
//			System.out.println();
//
			for(int i=0;i<normal.length;i++)
			{
				//normal[i]=1;

			}
			
		}
	}

	public static Out getTriangle(){
		Out out=new Out();

		float[] vert= {
				1,0,0,
				0,1,0,
				-1,0,0,
		};
		out.vert=vert;
		
		out.color=new float[3*3];
		for(int i=0;i<3;i++)
		{
			Vec3 p=Vec3.rainbow((float) Math.random());
			out.color[i*3]=p.x;
			out.color[i*3+1]=p.y;
			out.color[i*3+2]=p.z;
		}
		
		float[] uv= {
				0,1,
				0.5f,0,
				1,1
		};
		out.uv=uv;
		
		
		int[] ind= {
				0,2,1
		};
		out.ind=ind;
		return out;
		
		
	}
	public static Out getQuad() {
		Out out=new Out();

		float[] vert= {
				
				-1,-1,1,
				1,-1,1,
				-1,1,1,
				1,1,1,

				
				
		};
		out.vert=vert;
		
		out.color=new float[vert.length];
		for(int i=0;i<vert.length/3;i++)
		{
			Vec3 p=Vec3.rainbow((float) Math.random());
			out.color[i*3]=p.x;
			out.color[i*3+1]=p.y;
			out.color[i*3+2]=p.z;
		}
		
		float[] uv= {
				1,0,
				1,1,
				0,0,
				0,1,
		};
		out.uv=uv;
		
		
		int[] ind= {
				0,1,2,2,1,3,
		};
		out.ind=ind;
		return out;
		
		
		
	}
	public static Out getCube() {
		Out out=new Out();

		float[] vert= {
			    // front
			    -1, -1,  1,
			     1, -1,  1,
			     1,  1,  1,
			    -1,  1,  1,
			    // back
			    -1, -1, -1,
			     1, -1, -1,
			     1,  1, -1,
			    -1,  1, -1				
		};
		out.vert=vert;
		
		out.color=new float[vert.length];
		for(int i=0;i<8;i++)
		{
			Vec3 p=Vec3.rainbow((float) Math.random());
			out.color[i*3]=p.x;
			out.color[i*3+1]=p.y;
			out.color[i*3+2]=p.z;
		}
//		float[] color= {
//			// front colors
//			1, 0, 0,
//			0, 1, 0,
//			0, 0, 1,
//			1, 1, 1,
//			// back colors
//			1, 0, 0,
//			0, 1, 0,
//			0, 0, 1,
//			1, 1, 1	
//		};
		//out.color=color;
		
		float[] uv= {
				1,0,
				1,1,
				0,1,
				0,0,

				1,0,
				1,1,
				0,1,
				0,0,
		};
		out.uv=uv;
		
		
		int[] ind= {
				// front
				0, 1, 2,
				2, 3, 0,
				// right
				1, 5, 6,
				6, 2, 1,
				// back
				7, 6, 5,
				5, 4, 7,
				// left
				4, 0, 3,
				3, 7, 4,
				// bottom
				4, 5, 1,
				1, 0, 4,
				// top
				3, 2, 6,
				6, 7, 3
		};
		out.ind=ind;

		return out;
		
	}
}
