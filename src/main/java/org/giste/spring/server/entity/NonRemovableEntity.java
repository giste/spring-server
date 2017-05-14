package org.giste.spring.server.entity;

import javax.persistence.MappedSuperclass;

/**
 * Represents an entity that can't be removed because of its dependencies.
 * Instead, the entity can be enabled or disabled.
 * 
 * @author Giste
 */
@MappedSuperclass
public class NonRemovableEntity extends BaseEntity {

	private static final long serialVersionUID = 2703602042485715754L;

	protected boolean enabled;

	/**
	 * Constructs a new NonRemovableEntity.
	 */
	public NonRemovableEntity() {
		super();
	}

	/**
	 * Constructs a new NonRemovableEntity with a given identifier and an
	 * enabled state.
	 * 
	 * @param id The identifier for this entity.
	 * @param enabled teh enabled or disabled state of this entity.
	 */
	public NonRemovableEntity(Long id, boolean enabled) {
		super(id);
		this.enabled = enabled;
	}

	/**
	 * Gets the enabled/disabled state.
	 * 
	 * @return The enabled/disabled state.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets the enabled/disabled state.
	 * 
	 * @param enabled The enabled/disabled state.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
