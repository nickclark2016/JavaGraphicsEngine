package com.luminos.graphics.render;

import static com.luminos.ConfigData.HEIGHT;
import static com.luminos.ConfigData.WIDTH;
import static com.luminos.Luminos.BACK_FACE;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL30.GL_CLIP_DISTANCE0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luminos.graphics.display.Window;
import com.luminos.graphics.gameobjects.Camera;
import com.luminos.graphics.gameobjects.DirectionalLight;
import com.luminos.graphics.gameobjects.GameObject;
import com.luminos.graphics.gameobjects.PointLight;
import com.luminos.graphics.gui.GUIObject;
import com.luminos.graphics.models.TexturedModel;
import com.luminos.graphics.particles.Particle;
import com.luminos.graphics.particles.ParticleMaster;
import com.luminos.graphics.shaders.GameObjectShader;
import com.luminos.graphics.shaders.GuiShader;
import com.luminos.graphics.shaders.NormalMapShader;
import com.luminos.graphics.shaders.ParticleShader;
import com.luminos.graphics.shaders.ShadowShader;
import com.luminos.graphics.shaders.SkyboxShader;
import com.luminos.graphics.shaders.TerrainShader;
import com.luminos.graphics.shaders.TextShader;
import com.luminos.graphics.shaders.WaterShader;
import com.luminos.graphics.shadows.ShadowBox;
import com.luminos.graphics.terrains.Terrain;
import com.luminos.graphics.text.GUIText;
import com.luminos.graphics.textures.GUITexture;
import com.luminos.graphics.water.WaterFrameBuffers;
import com.luminos.graphics.water.WaterTile;
import com.luminos.loaders.Loader;
import com.luminos.tools.Maths;
import com.luminos.tools.maths.matrix.Matrix4f;
import com.luminos.tools.maths.vector.Vector3f;
import com.luminos.tools.maths.vector.Vector4f;

/**
 * 
 * Renders Terrains and Entities
 * 
 * @author Nick Clark
 * @version 1.0
 *
 */

public class MasterRenderer {

	public static float FOV = 70;
	public static final float NEAR_PLANE = .15f;
	public static final float FAR_PLANE = 500f;
	public static final float SKYBOX_PLANE = 1500f;

	static final float RED = 0.5f;
	static final float GREEN = 0.5f;
	static final float BLUE = 0.5f;

	static final Vector3f SKY_COLOR = new Vector3f(RED, GREEN, BLUE);

	private Matrix4f projectionMatrix;
	private Matrix4f skyboxMatrix;

	private GameObjectRenderer gameObjectRenderer;
	private GameObjectShader gameObjectShader;
	private GuiRenderer guiRenderer;
	private GuiShader guiShader;
	private NormalMapRenderer normalMapRenderer;
	private NormalMapShader normalMapShader;
	private ParticleRenderer particleRenderer;
	private ParticleShader particleShader;
	private ShadowMapMasterRenderer shadowRenderer;
	private ShadowShader shadowShader;
	private SkyboxRenderer skyboxRenderer;
	private SkyboxShader skyboxShader;
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader;
	private TextRenderer textRenderer;
	private TextShader textShader;
	private WaterRenderer waterRenderer;
	private WaterShader waterShader;

	private WaterFrameBuffers buffers;
	private Map<TexturedModel, List<GameObject>> entities = new HashMap<TexturedModel,List<GameObject>>();
	private Map<TexturedModel, List<GameObject>> normalMapEntities = new HashMap<TexturedModel,List<GameObject>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();

	/**
	 * Constructor used to create a Master Renderer
	 * 
	 * @param loader		Passes loader used for rendering
	 * @param camera		Camera used to create projection matrix of
	 * @throws Exception	Exception for if file isn't found or cannot be handled
	 */
	public MasterRenderer(Loader loader, Camera camera) throws Exception {
		enableCulling();
		cullFace(BACK_FACE);
		gameObjectShader = new GameObjectShader();
		guiShader = new GuiShader();
		normalMapShader = new NormalMapShader();
		particleShader = new ParticleShader();
		shadowShader = new ShadowShader();
		skyboxShader = new SkyboxShader();
		terrainShader = new TerrainShader();
		textShader = new TextShader();
		waterShader = new WaterShader();
		
		projectionMatrix = createProjectionMatrix(FOV, FAR_PLANE, NEAR_PLANE);
		skyboxMatrix = createProjectionMatrix(FOV, SKYBOX_PLANE, NEAR_PLANE);
		gameObjectRenderer = new GameObjectRenderer(gameObjectShader, projectionMatrix);
		guiRenderer = new GuiRenderer(guiShader, loader);
		normalMapRenderer = new NormalMapRenderer(normalMapShader, projectionMatrix);
		particleRenderer = new ParticleRenderer(particleShader, loader, projectionMatrix);
		shadowRenderer = new ShadowMapMasterRenderer(shadowShader, camera);		
		skyboxRenderer = new SkyboxRenderer(skyboxShader, loader, skyboxMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		textRenderer = new TextRenderer(textShader, loader);
		buffers = new WaterFrameBuffers();
		waterRenderer = new WaterRenderer(loader, waterShader, projectionMatrix, buffers, "res/textures/waterdudv.png", "res/textures/waternormal.png");
	}

	/**
	 * Renders the entire 3D scene
	 * 
	 * @param entities		Entities to be rendered
	 * @param terrains		Terrains to be rendered
	 * @param lights		Lights to be passed into shader
	 * @param sun 
	 * @param focalPoint	Location of camera focus
	 * @param camera		Camera to be renderer
	 * @param clipPlane		Plane to clip all rendering beyond
	 */
	public void renderScene(List<GameObject> entities, List<Terrain> terrains, List<PointLight> lights, DirectionalLight sun, Vector3f focalPoint, Camera camera, Vector4f clipPlane) {
		if (entities != null) {
			for (GameObject entity : entities) {
				if (entity.isRenderable() && Maths.getDistance(entity.getPosition(), camera.getPosition()) < entity.getRenderDistance())
					processGameObject(entity);
			}
		}
		if (terrains != null) {
			for (Terrain terrain : terrains) {
				if (terrain.isRenderable())
					processTerrain(terrain);
			}
		}

		if (lights == null) {
			lights = new ArrayList<PointLight>();
		}
		render(lights, sun, camera, clipPlane);
	}

	/**
	 * Renders GUI Textures to screen
	 * 
	 * @param objects	GUIObjects to be rendered
	 */	
	public void renderGUI(List<GUIObject> objects) {
		for(GUIObject object : objects) {
			guiRenderer.render(object.getTextures());
		}
	}

	public void renderGUI(ArrayList<GUITexture> textures) {
		guiRenderer.render(textures);
	}

	/**
	 * Renders particles through screen
	 * 
	 * @param camera		{@link Camera} to render with
	 * @param window		{@link Window} to get frame time of
	 */
	public void renderParticles(Camera camera, Window window) {
		ParticleMaster.update(window);
		particleRenderer.render(ParticleMaster.particles, camera);
	}

	/**
	 * Adds particle to ParticleMaster
	 * 
	 * @param particle		{@link Particle} to be added
	 */
	public void addParticle(Particle particle) {
		ParticleMaster.addParticle(particle);
	}

	/**
	 * Adds particle list to ParticleMaster
	 * 
	 * @param particles		{@link Particle}s to be added
	 */
	public void addParticles(List<Particle> particles) {
		ParticleMaster.addAllParticles(particles);
	}

	/**
	 * Renders text to screen
	 */
	public void renderGuiText() {
		textRenderer.render();
	}

	/**
	 * Loads {@link GUIText} to renderer
	 * 
	 * @param text	GUIText to be loaded 
	 */
	public void addText(GUIText text) {
		textRenderer.loadText(text);
	}

	/**
	 * Removes {@link GUIText} from renderer
	 * 
	 * @param text	GUIText to be removed
	 */
	public void removeText(GUIText text) {
		textRenderer.removeText(text);
	}

	/**
	 * Updates the text's value
	 * 
	 * @param text		Text to be updated
	 * @param value		String of 
	 */
	public void updateTextValue(GUIText text, String value) {
		textRenderer.updateText(text, value);
	}

	/**
	 * Prepares water for rendering
	 * 
	 * @param gameObjects	Passed to renderScene
	 * @param terrains		Passed to renderScene
	 * @param lights		Passed to renderScene
	 * @param focalPoint	Passed to renderScene
	 * @param camera		Calculates FBOs and passed to renderScene
	 */
	public void prepareWater(List<GameObject> gameObjects, List<Terrain> terrains, List<PointLight> lights, DirectionalLight sun, Vector3f focalPoint, Camera camera) {
		glEnable(GL_CLIP_DISTANCE0);
		buffers.bindReflectionFrameBuffer();
		float distance = 2 * (camera.getPosition().y);
		camera.getPosition().y -= distance;
		camera.invertPitch();
		renderScene(gameObjects, terrains, lights, sun, focalPoint, camera, new Vector4f(0, 1, 0, 1));
		camera.getPosition().y += distance;
		camera.invertPitch();
		buffers.bindRefractionFrameBuffer();
		List<GameObject> ents = new ArrayList<GameObject>();
		for(GameObject entity : gameObjects) {
			if(entity.getPosition().y < 0) ents.add(entity);
		}
		renderScene(ents, terrains, lights, sun, focalPoint, camera, new Vector4f(0, -1, 0, 0));
		buffers.unbindCurrentFrameBuffer();
		glDisable(GL_CLIP_DISTANCE0);
	}

	/**
	 * Renders {@link WaterTile}s
	 * 
	 * @param tiles		Tiles to be rendered
	 * @param camera	Camera to use
	 * @param lights		Light to reflect
	 */
	public void renderWater(List<WaterTile> tiles, Camera camera, List<PointLight> lights) {
		waterRenderer.render(tiles, camera, lights);
	}

	/**
	 * Prepares rendering
	 */
	public void prepare() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glClearColor(RED, GREEN, BLUE, 1);
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LESS);
	}

	/**
	 * Renders {@link GameObject}
	 * 
	 * @param lights	Passes lights to shaders
	 * @param sun 
	 * @param camera	Camera to create transformation matrix of
	 * @param clipPlane	Plane to clip all rendering beyond
	 */
	public void render(List<PointLight> lights, DirectionalLight sun, Camera camera, Vector4f clipPlane){
		prepare();
		Matrix4f viewMatrix = Maths.createViewMatrix(camera);
//		normalMapShader.start();
//		normalMapShader.loadClipPlane(clipPlane);
//		normalMapShader.loadSkyColor(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
//		normalMapShader.loadMaxPointLights(1);
//		normalMapShader.loadPointLights(lights, viewMatrix);
//		normalMapShader.loadViewMatrix(viewMatrix);
//		normalMapRenderer.render(normalMapEntities);
//		normalMapShader.stop();
		gameObjectShader.start();
		gameObjectShader.setUniform(gameObjectShader.getLocation("skyColor"), new Vector3f(RED, GREEN, BLUE));
		gameObjectShader.setUniformPointLights("pointLights", lights);
		gameObjectShader.setUniformDirectionalLight("sun", sun);
		gameObjectShader.setUniform(gameObjectShader.getLocation("viewMatrix"), viewMatrix);
		gameObjectRenderer.render(entities);
		gameObjectShader.stop();
		terrainShader.start();
		terrainShader.setUniform("skyColor", SKY_COLOR);
		terrainShader.setUniformPointLights("pointLights", lights);
		terrainShader.setUniformDirectionalLight("sun", sun);
		terrainShader.setUniform("viewMatrix", viewMatrix);
		terrainRenderer.render(terrains, shadowRenderer.getToShadowMapSpaceMatrix(), getShadowMapTexture());
		terrainShader.stop();
		skyboxRenderer.render(viewMatrix, SKY_COLOR);
		terrains.clear();
		entities.clear();
		normalMapEntities.clear();
	}

	/**
	 * Adds {@link Terrain} to list of terrains
	 * 
	 * @param terrain		Terrain to be processed 
	 */
	public void processTerrain(Terrain terrain){
		terrains.add(terrain);
	}

	/**
	 * Processes {@link GameObject}
	 * 
	 * @param entity 		GameObject to be processed 
	 */
	public void processGameObject(GameObject entity){
		TexturedModel model = entity.getModel();
			if(model.getMaterial().hasNormal()) {
				TexturedModel entityModel = model;
				List<GameObject> batch = normalMapEntities.get(entityModel);
				if(batch!=null){
					batch.add(entity);
				}else{
					List<GameObject> newBatch = new ArrayList<GameObject>();
					newBatch.add(entity);
					normalMapEntities.put(entityModel, newBatch);		
				}
			} else {
				TexturedModel entityModel = model;
				List<GameObject> batch = entities.get(entityModel);
				if(batch!=null){
					batch.add(entity);
				}else{
					List<GameObject> newBatch = new ArrayList<GameObject>();
					newBatch.add(entity);
					entities.put(entityModel, newBatch);		
				}
			}
	}

	/**
	 * Processes {@link GameObject} with a normal map
	 * 
	 * @param entity		GameObject to be processed
	 */
	public void processNormalMapGameObject(GameObject entity){
		TexturedModel entityModel = entity.getModel();
		List<GameObject> batch = normalMapEntities.get(entityModel);
		if(batch!=null){
			batch.add(entity);
		}else{
			List<GameObject> newBatch = new ArrayList<GameObject>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);		
		}
	}

	/**
	 * Render a shadow map
	 * 
	 * @param ents	Entities to have shadows
	 * @param focalPoint	Central rendering point
	 * @param sun			Focal light
	 */
	public void renderShadowMap(List<GameObject> ents, List<Terrain> ters, Vector3f focalPoint, DirectionalLight sun) {
		for(GameObject entity : ents) {
			if(Maths.getDistance(entity.getPosition(), focalPoint) < 2 * ShadowBox.SHADOW_DISTANCE) {
				processGameObject(entity);
			}
		}
		for (Terrain terrain : ters) {
			if (Maths.getDistance((Vector3f) terrain.getPosition(), focalPoint) < 2 * ShadowBox.SHADOW_DISTANCE) 
				processTerrain(terrain);
		}
		shadowRenderer.render(entities, terrains, sun);
		entities.clear();
		terrains.clear();
	}

	/**
	 * Gets the ID of the shadow map
	 * 
	 * @return ID of shadow map
	 */
	public int getShadowMapTexture() {
		return shadowRenderer.getShadowMap();
	}

	/**
	 * Cleans up all shaders used
	 */
	public void cleanUp(){
		gameObjectRenderer.cleanUp();
		guiRenderer.cleanUp();
		normalMapRenderer.cleanUp();
		particleRenderer.cleanUp();
		shadowRenderer.cleanUp();
		terrainRenderer.cleanUp();
		textRenderer.cleanUp();
	}

	/**
	 * Enables back face culling
	 */
	public static void enableCulling(){
		glEnable(GL_CULL_FACE);
	}
	
	public static void cullFace(int faceID) {
		glCullFace(faceID);		
	}

	/**
	 * Disables back face culling
	 */
	public static void disableCulling(){
		glDisable(GL_CULL_FACE);
	}

	/**
	 * @return Matrix4f	Contains Projection Matrix
	 * 
	 * Gets projection matrix of rendering
	 */
	public Matrix4f getProjectionMatrix(){
		return this.projectionMatrix;
	}

	/**
	 * Creates projection matrix
	 * 
	 * @param fov			Field of view
	 * @param farPlane		Far view plane
	 * @param nearPlane		Near view plane
	 * @return				Projection matrix
	 */
	public static Matrix4f createProjectionMatrix(float fov, float farPlane, float nearPlane) {
		Matrix4f projectionMatrix = new Matrix4f();
		float aspectRatio = (float) WIDTH / (float) HEIGHT;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = farPlane - nearPlane;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((farPlane + nearPlane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * nearPlane * farPlane) / frustum_length);
		projectionMatrix.m33 = 0;
		return projectionMatrix;
	}

}


