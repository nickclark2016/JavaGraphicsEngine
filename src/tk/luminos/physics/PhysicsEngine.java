package tk.luminos.physics;

import java.util.ArrayList;
import java.util.List;

import tk.luminos.ConfigData;
import tk.luminos.EngineComponent;
import tk.luminos.physics.colliders.Collider;

public class PhysicsEngine extends EngineComponent {
	
	private List<Collider> colliders;	
	private boolean running = false;
	
	/**
	 * Creates physics engine
	 */
	public PhysicsEngine() {
		colliders = new ArrayList<Collider>();
	}
	
	/**
	 * Runs physics engine
	 */
	@Override
	public void run() {
		running = true;
		Thread.currentThread().setName("LUMINOS_ENGINE:_PHYSICS");
		while (running) {
			update(1 / ConfigData.UPS);
		}
	}

	/**
	 * Closes physics engine
	 */
	@Override
	public void close() {
		running = false;
	}
	
	/**
	 * Updates engine
	 */
	@Override
	public void update(float delta) {
		for (Collider collider : colliders) {
			if (collider.isColliding())
				collider.response(delta);
		}
	}
	
	/**
	 * Attaches collider
	 * 
	 * @param collider		Collider to attach
	 */
	public void attachCollider(Collider collider) {
		this.colliders.add(collider);
	}

	/**
	 * Attaches colliders
	 * 
	 * @param colliders		Colliders to attach
	 */
	public void attachCollider(List<Collider> colliders) {
		this.colliders.addAll(colliders);
	}
	
	/**
	 * Attaches collider
	 * 
	 * @param collider		Collider to attach
	 * @param location		Attach location
	 */
	public void attachCollider(Collider collider, int location) {
		this.colliders.add(location, collider);
	}
	
	/**
	 * Attaches colliders
	 * 
	 * @param colliders		Colliders to attach
	 * @param location		Attach location
	 */
	public void attachCollider(List<Collider> colliders, int location) {
		this.colliders.addAll(location, colliders);
	}
	
	/**
	 * Removes collider from engine
	 * 
	 * @param collider		Collider to remove
	 * @return				Removed colllider
	 */
	public boolean removeCollider(Collider collider) {
		return this.colliders.remove(collider);
	}
	
	/**
	 * Removes colliders from engine
	 * 
	 * @param colliders		Colliders to remove
	 * @return				Removed collliders
	 */
	public boolean removeCollider(List<Collider> colliders) {
		return this.colliders.removeAll(colliders);
	}
	
	/**
	 * Removes collider from engine
	 * 
	 * @param location		Collider to remove
	 * @return				If collider was removed
	 */
	public boolean removeCollider(int location) {
		Collider collider = this.colliders.remove(location);
		return colliders.contains(collider);
	}

}
