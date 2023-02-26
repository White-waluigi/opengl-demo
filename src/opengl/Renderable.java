package opengl;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import opengl.MeshGenerator.Out;
import vectors.Mat4;
import vectors.Vec3;

import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderable {
	public Out mesh;
	public int textureId;
	public boolean alpha;

	public Mat4 model=Mat4.ID;
	public int vao;
	public int indId;
	public String name;
	public boolean rotating;
	public Shader shader;
	
	public int queue=0;
	
	@Override
	public String toString() {
		return name;
		
	}
	public void render(long startTime, double ctime, int w, float h,Light l) {
		Renderable x=this;
		glUseProgram(x.shader.id);
		int timeUniformIndex = glGetUniformLocation(x.shader.id, "time");
		if (timeUniformIndex != -1)
			glUniform1f(timeUniformIndex, (float) (System.currentTimeMillis() - startTime) * 0.001f);

		if (x.alpha) {
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);// switch to our shader
		} else {

			glDisable(GL_BLEND);
		}
		Mat4 roti = x.rotating ? Mat4.rotate(new Vec3(ctime / 4000.0, ctime / 1000.0, ctime / 3000.0)) : Mat4.ID;

		Mat4 fin = x.model.resetPos().preMultiply(roti).preMultiply(Mat4.translate(x.model.pos()));

		Mat4 mvp = Mat4.multiply(Mat4.perspective(45, w / (h * 1f), 0.1f, 100f), // projection
				Mat4.lookAt(new Vec3(0, 0, -10), new Vec3(0, 0, 0), new Vec3(0, 1, 0)), // view
				fin);

		int UniformIndex = glGetUniformLocation(x.shader.id, "mvpMat");
		if (UniformIndex != -1)
			glUniformMatrix4fv(UniformIndex, false, mvp.toArray());

		UniformIndex = glGetUniformLocation(x.shader.id, "mMat");
		if (UniformIndex != -1)
			glUniformMatrix4fv(UniformIndex, false, fin.toArray());

		UniformIndex = glGetUniformLocation(x.shader.id, "invRes");
		float[] invRes= {1f/w,1f/h};
		if (UniformIndex != -1)
			glUniform2fv(UniformIndex,  invRes);

		
		
		UniformIndex = glGetUniformLocation(x.shader.id, "lPos");
		if (UniformIndex != -1)
			glUniform3fv(UniformIndex,  l.pos.toArray());
		
		UniformIndex = glGetUniformLocation(x.shader.id, "lDiff");
		if (UniformIndex != -1)
			glUniform3fv(UniformIndex,  l.diff.toArray());
		

		UniformIndex = glGetUniformLocation(x.shader.id, "lAten");
		if (UniformIndex != -1)
			glUniform3fv(UniformIndex,  l.aten.toArray());
		
		UniformIndex = glGetUniformLocation(x.shader.id, "viewPos");
		if (UniformIndex != -1)
			glUniform3fv(UniformIndex,new Vec3(0,0,-10).toArray());
		
		// render our model
		glBindVertexArray(x.vao);
		int baseImageLoc = glGetUniformLocation(x.shader.id, "tex");
		glUniform1i(baseImageLoc, 0);
		glBindTexture(GL_TEXTURE_2D, x.textureId);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, x.indId);
		glDrawElements(GL_TRIANGLES, x.mesh.ind.length, GL_UNSIGNED_INT, 0);
		
	}
}
