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

public class OpenGL {
	public ArrayList<Renderable> renderables;
	public HashMap<String, Shader> shader;
	public Renderable rtt;

	public static String load(String g) throws IOException {
		File file = new File(g);

		BufferedReader br;
		br = new BufferedReader(new FileReader(file));

		String st;
		String ret = "";
		while ((st = br.readLine()) != null) {
			ret += st + "\n";
		}
		return ret;
	}

	public static int loadTexture(String img) throws Exception {
		File imgPath = new File(img);
		BufferedImage image = ImageIO.read(imgPath);

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

		for (int h = 0; h < image.getHeight(); h++) {
			for (int w = 0; w < image.getWidth(); w++) {
				int pixel = pixels[h * image.getWidth() + w];

				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip();

		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
				buffer);
		glGenerateMipmap(GL_TEXTURE_2D);

		return id;
	}

	private long hWindow;
	private int hFragmentShader;
	private int error;
	private int renderTexture=-989;
	private int fbId;

	public void setUpWindow() {

		
		// let GLFW work on the main thread (for OS X)
		// read the following if you want to create windows with awt/swing/javaFX:
		// https://stackoverflow.com/questions/47006058/lwjgl-java-awt-headlessexception-thrown-when-making-a-jframe
		System.setProperty("java.awt.headless", "true");

		// open a window
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 0);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
		hWindow = GLFW.glfwCreateWindow(720, 480, "ComGr", 0, 0);
		GLFW.glfwSetWindowSizeCallback(hWindow, (window, width, height) -> {
			int[] w = new int[1];
			int[] h = new int[1];
			GLFW.glfwGetFramebufferSize(window, w, h);
			glViewport(0, 0, w[0], h[0]);
		});
		GLFW.glfwMakeContextCurrent(hWindow);
		GLFW.glfwSwapInterval(1);
		createCapabilities();
		// before window creation
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
		// after GL.createCapabilities()
		Callback debugProc = GLUtil.setupDebugMessageCallback();
	}

	public void setUpShaders() throws Exception {

		shader = new HashMap<String, Shader>();
		shader.put("def", new Shader("def"));
		shader.put("fb", new Shader("fb"));

		// set up opengl
		glEnable(GL_FRAMEBUFFER_SRGB);
		glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
		glClearDepth(1);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LESS);
		glEnable(GL_CULL_FACE);
		// During init, enable debug output

		for (Shader x : shader.values()) {
			// load, compile and link shaders
			// see https://www.khronos.org/opengl/wiki/Vertex_Shader
			String VertexShaderSource = load("shaders/" + x.name + "-vert.glsl");
			int hVertexShader = glCreateShader(GL_VERTEX_SHADER);
			glShaderSource(hVertexShader, VertexShaderSource);
			glCompileShader(hVertexShader);
			if (glGetShaderi(hVertexShader, GL_COMPILE_STATUS) != GL_TRUE)
				throw new Exception(glGetShaderInfoLog(hVertexShader));

			// see https://www.khronos.org/opengl/wiki/Fragment_Shader
			String FragmentShaderSource = load("shaders/" + x.name + "-frag.glsl");
			hFragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
			glShaderSource(hFragmentShader, FragmentShaderSource);
			glCompileShader(hFragmentShader);
			if (glGetShaderi(hFragmentShader, GL_COMPILE_STATUS) != GL_TRUE)
				throw new Exception(glGetShaderInfoLog(hFragmentShader));

			// link shaders to a program
			x.id = glCreateProgram();
			glAttachShader(x.id, hFragmentShader);
			glAttachShader(x.id, hVertexShader);
			glLinkProgram(x.id);
			if (glGetProgrami(x.id, GL_LINK_STATUS) != GL_TRUE)
				throw new Exception(glGetProgramInfoLog(x.id));
		}
	}

	public void genFrameBuffer() throws Exception {
		fbId = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fbId);

		renderTexture = glGenTextures();

		glBindTexture(GL_TEXTURE_2D, renderTexture);

		int[] w = new int[1];
		int[] h = new int[1];
		GLFW.glfwGetWindowSize(hWindow, w, h);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w[0], h[0], 0, GL_RGB, GL_UNSIGNED_BYTE, 0);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

		// The depth buffer
		int db;
		db = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, db);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, w[0], h[0]);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, db);

		
		
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, renderTexture, 0);

		int[] buffers={GL_COLOR_ATTACHMENT0};
		
		glDrawBuffers(buffers);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new Exception("FrameBuffer failure:" + glCheckFramebufferStatus(GL_FRAMEBUFFER));
	}

	public void setUpVbos() throws Exception {

		for (Renderable x : renderables) {
			System.out.println("Mesh:" + x.name);
			Out out = x.mesh;
			// upload model vertices to a vbo
			float[] triangleVertices = out.vert;
			int vboTriangleVertices = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
			glBufferData(GL_ARRAY_BUFFER, triangleVertices, GL_STATIC_DRAW);

			// upload color vertices to a vbo
			float[] colorVertices = out.color;
			int vboTriangleColor = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboTriangleColor);
			glBufferData(GL_ARRAY_BUFFER, colorVertices, GL_STATIC_DRAW);

			// upload color vertices to a vbo
			float[] uvVertices = out.uv;
			int vboTriangleUV = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboTriangleUV);
			glBufferData(GL_ARRAY_BUFFER, uvVertices, GL_STATIC_DRAW);

			// upload color vertices to a vbo
			out.calcNormal();
			float[] normalVertices = out.normal;
			int vboTriangleNormal = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboTriangleNormal);
			glBufferData(GL_ARRAY_BUFFER, normalVertices, GL_STATIC_DRAW);

			// upload model indices to a vbo
			int[] triangleIndices = out.ind;
			x.indId = glGenBuffers();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, x.indId);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, triangleIndices, GL_STATIC_DRAW);

			// set up a vao
			x.vao = glGenVertexArrays();

			for (Shader s : shader.values()) {
				glBindVertexArray(x.vao);
				int posAttribIndex = glGetAttribLocation(s.id, "pos");
				if (posAttribIndex != -1) {
					glEnableVertexAttribArray(posAttribIndex);
					glBindBuffer(GL_ARRAY_BUFFER, vboTriangleVertices);
					glVertexAttribPointer(posAttribIndex, 3, GL_FLOAT, false, 0, 0);
				}
				posAttribIndex = glGetAttribLocation(s.id, "color");
				if (posAttribIndex != -1) {
					glEnableVertexAttribArray(posAttribIndex);
					glBindBuffer(GL_ARRAY_BUFFER, vboTriangleColor);
					glVertexAttribPointer(posAttribIndex, 3, GL_FLOAT, false, 0, 0);
				}
				posAttribIndex = glGetAttribLocation(s.id, "uv");
				if (posAttribIndex != -1) {
					glEnableVertexAttribArray(posAttribIndex);
					glBindBuffer(GL_ARRAY_BUFFER, vboTriangleUV);
					glVertexAttribPointer(posAttribIndex, 2, GL_FLOAT, false, 0, 0);
				}

				posAttribIndex = glGetAttribLocation(s.id, "normal");
				if (posAttribIndex != -1) {
					glEnableVertexAttribArray(posAttribIndex);
					glBindBuffer(GL_ARRAY_BUFFER, vboTriangleNormal);
					glVertexAttribPointer(posAttribIndex, 3, GL_FLOAT, false, 0, 0);
				}
				glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, x.indId);

			}
			// check for errors during all previous calls
			error = glGetError();
			if (error != GL_NO_ERROR)
				throw new Exception(Integer.toString(error));
		}

	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 3];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 3] = HEX_ARRAY[v >>> 4];
			hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
			hexChars[j * 3 + 2] = '-';

		}
		return new String(hexChars);
	}

	long ctr = 0;
	private Light light;

	public void pr(String g) {
		System.out.println(g + "_" + (System.currentTimeMillis() - ctr));
		ctr = System.currentTimeMillis();

	}

	public void doLoop() throws Exception {

		long startTime = System.currentTimeMillis();
		// render loop
		while (!GLFW.glfwWindowShouldClose(hWindow)) {
			// clear screen and z-buffer

			int[] w = new int[1];
			int[] h = new int[1];
			GLFW.glfwGetWindowSize(hWindow, w, h);
			// model-view-projection matrix (not yet used)

			glBindFramebuffer(GL_FRAMEBUFFER, fbId);
			glViewport(0,0,w[0], h[0]);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			renderScene(w[0], h[0], startTime,0);
			
			long ctime = System.currentTimeMillis() - startTime;
			// Render to the screen
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glViewport(0,0,w[0],h[0]);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			renderScene(w[0], h[0], startTime,1);
			
			// display
			GLFW.glfwSwapBuffers(hWindow);
			GLFW.glfwPollEvents();

			error = glGetError();
			if (error != GL_NO_ERROR)
				throw new Exception(Integer.toString(error));
		}

		GLFW.glfwDestroyWindow(hWindow);
		GLFW.glfwTerminate();
	}

	private void renderScene(int w, int h, long startTime,int queue) {
		long ctime = System.currentTimeMillis() - startTime;

		for (Renderable x : renderables) {
			if(x.queue!=queue)
				continue;

			if(queue!=0)
				x.textureId=renderTexture;
			x.render(startTime, ctime, w, h,light);
		}

	}


	private void setUpScene() throws Exception {

		renderables = new ArrayList<Renderable>();
		int testId = loadTexture("t.png");
		int brickId = loadTexture("brick.jpg");
		int swirlId = loadTexture("swirl.png");
		int floorId = loadTexture("floor.jpg");
		
		light=new Light();
		
		light.pos=new Vec3(0,-2,0);
		light.diff=new Vec3(3,3,4);
		light.aten=new Vec3(1,1,0);
		
		
		

		Renderable test;

		test = new Renderable();
		test.shader=shader.get("fb");
		test.name = "rttTriangle";
		test.alpha = false;
		test.rotating = false;
		test.textureId = brickId;
		test.queue=1;
		test.mesh = MeshGenerator.getTriangle();
		test.model = Mat4.translate(1.1f, -100, -2).postMultiply(Mat4.scale(999));
		renderables.add(test);

		for (int i = 0; i < 5; i++) {
			test = new Renderable();
			test.name = "transTriangle" + i;
			test.shader=shader.get("def");
			test.alpha = true;
			test.rotating = false;
			test.textureId = swirlId;
			test.mesh = MeshGenerator.getTriangle();
			test.model = Mat4.scale(3).preMultiply(Mat4.translate(-2.4f - (.01f * i), 2f ,17- i * 4));
			renderables.add(test);
		}
//
//		for (int i = 0; i < 6; i++) {
//
//			test = new Renderable();
//			test.name = "transCube";
//			test.alpha = true;
//			test.rotating = true;
//			test.textureId = testId;
//			test.mesh = MeshGenerator.getQuad();
//			test.model = Mat4.translate(1.1f, -1, 2)
//					.postMultiply(Mat4.rotate(new Vec3(0).set(i % 3, (float) Math.PI / 2f))
//							.postMultiply(Mat4.scale(new Vec3(1).set((i % 2) * 99 + 2, -1))));
//			// test.model = Mat4.rotate(new Vec3(0,Math.PI/2,0));
//			renderables.add(test);
//		}

		for (int i = 0; i < 6; i++) {

			test = new Renderable();

			test.shader=shader.get("def");
			test.name = "solid" + i;
			test.alpha = false;
			test.rotating = true;
			test.textureId = brickId;
			test.mesh = MeshGenerator.getQuad();

			Mat4 roti = Mat4.rotate(new Vec3(0).set(2, (float) (Math.PI / 2f * (i))));

			if (i >= 4) {
				int d = i - 4;
				roti = Mat4.rotate(new Vec3(0).set(1, (float) (Math.PI / 2f * (d * 2 + 1))));
			}

			test.model = Mat4.translate(1.1f, -1, 2).postMultiply(roti);

			// test.model = Mat4.rotate(new Vec3(0,Math.PI/2,0));
			renderables.add(test);
		}
//		test = new Renderable();
		for (int i = 0; i < 6; i++) {

			test = new Renderable();
			test.name = "transCube" + i;
			test.shader=shader.get("def");
			test.alpha = true;
			test.rotating = true;
			test.textureId = testId;
			test.mesh = MeshGenerator.getQuad();

			Mat4 roti = Mat4.rotate(new Vec3(0).set(2, (float) (Math.PI / 2f * (i))));

			if (i >= 4) {
				int d = i - 4;
				roti = Mat4.rotate(new Vec3(0).set(1, (float) (Math.PI / 2f * (d * 2 + 1))));
			}

			test.model = Mat4.translate(-1.1f, -1, 2).postMultiply(roti);

			// test.model = Mat4.rotate(new Vec3(0,Math.PI/2,0));
			renderables.add(test);
		}
		for (int i = 0; i < 6; i++) {

			test = new Renderable();
			test.name = "floorCube" + i;
			test.shader=shader.get("def");
			test.alpha = false;
			test.rotating = false;
			test.textureId = brickId;
			test.mesh = MeshGenerator.getQuad();

			Mat4 roti = Mat4.rotate(new Vec3(0).set(2, (float) (Math.PI / 2f * (i))));

			if (i >= 4) {
				int d = i - 4;
				roti = Mat4.rotate(new Vec3(0).set(1, (float) (Math.PI / 2f * (d * 2 + 1))));
			}

			test.model = Mat4.translate(0,-4,10).postMultiply(roti.preMultiply(Mat4.scale(14, 1, 14)));

			// test.model = Mat4.rotate(new Vec3(0,Math.PI/2,0));
			renderables.add(test);
		}	
		
		
		test = new Renderable();
		test.shader=shader.get("def");
		test.name = "lightTriangle";
		test.alpha = false;
		test.rotating = false;
		test.textureId = brickId;
		test.queue=0;
		test.mesh = MeshGenerator.getTriangle();
		test.model = Mat4.translate(light.pos).postMultiply(Mat4.scale(.1f));
		renderables.add(test);
//		test.name = "solidCube";
//		test.alpha = false;
//		test.rotating = true;
//		test.textureId = brickId;
//		test.mesh = MeshGenerator.getQuad();
//		test.model = Mat4.translate(-1, -1, 2);
//		renderables.add(test);

		renderables.stream().forEach((a) -> System.out.println(a));
		System.out.println("sad");

		renderables.sort((a, b) -> a.alpha ? b.alpha ? a.model.comparePos(b.model) : 1 : -1);
		renderables.stream().forEach((a) -> System.out.println(a));
	}

	public static void main(String[] args) throws Exception {
		OpenGL v = new OpenGL();
		v.setUpWindow();
		v.setUpShaders();
		v.setUpScene();
		v.genFrameBuffer();
		v.setUpVbos();

		v.doLoop();

	}
}
