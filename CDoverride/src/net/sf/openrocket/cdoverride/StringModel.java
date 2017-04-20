package net.sf.openrocket.cdoverride;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.Invalidatable;
import net.sf.openrocket.util.Invalidator;
import net.sf.openrocket.util.MemoryManagement;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.StateChangeListener;


/**
 * A class that adapts an isXXX/setXXX String variable.  It functions as an Action suitable
 * for usage in JCheckBox or JToggleButton.  You can create a suitable button with
 * <code>
 *   check = new JCheckBox(new StringModel(component,"Value"))
 *   check.setText("Label");
 * </code>
 * This will produce a button that uses isValue() and setValue(String) of the corresponding
 * component.
 * <p>
 * Additionally a number of component enabled states may be controlled by this class using
 * the method {@link #addEnableComponent(Component, String)}.
 * 
 */
public class StringModel extends AbstractAction implements StateChangeListener, Invalidatable {
	private static final Logger log = LoggerFactory.getLogger(StringModel.class);
	
	private final ChangeSource source;
	private final String valueName;
	
	/* Only used when referencing a ChangeSource! */
	private final Method getMethod;
	private final Method setMethod;
	private final Method getEnabled;
	
	/* Only used with internal String value! */
	private String value;
	

	private final List<Component> components = new ArrayList<Component>();
	private final List<Boolean> componentEnableState = new ArrayList<Boolean>();
	
	private String toString = null;
	
	private int firing = 0;
	
	private String oldValue;
	private boolean oldEnabled;
	
	private Invalidator invalidator = new Invalidator(this);
	
	
	/**
	 * Construct a StringModel that holds the String value within itself.
	 * 
	 * @param initialValue	the initial value of the String
	 */
	public StringModel(String initialValue) {
		this.valueName = null;
		this.source = null;
		this.getMethod = null;
		this.setMethod = null;
		this.getEnabled = null;
		
		this.value = initialValue;
		
		oldValue = getValue();
		oldEnabled = getIsEnabled();
		
		this.setEnabled(oldEnabled);
		this.putValue(SELECTED_KEY, oldValue);
		
	}
	
	/**
	 * Construct a StringModel that references the String from a ChangeSource method.
	 * 
	 * @param source		the String source.
	 * @param valueName		the name of the getter/setter method (without the get/is/set prefix)
	 */
	public StringModel(ChangeSource source, String valueName) {
		this.source = source;
		this.valueName = valueName;
		
		Method getter = null, setter = null;
		

		// Try get/is and set
		try {
			getter = source.getClass().getMethod("is" + valueName);
		} catch (NoSuchMethodException ignore) {
		}
		if (getter == null) {
			try {
				getter = source.getClass().getMethod("get" + valueName);
			} catch (NoSuchMethodException ignore) {
			}
		}
		try {
			setter = source.getClass().getMethod("set" + valueName, String.class);
		} catch (NoSuchMethodException ignore) {
		}
		
		if (getter == null || setter == null) {
			throw new IllegalArgumentException("get/is methods for String '" + valueName +
					"' not present in class " + source.getClass().getCanonicalName());
		}
		
		getMethod = getter;
		setMethod = setter;
		
		Method e = null;
		try {
			e = source.getClass().getMethod("is" + valueName + "Enabled");
		} catch (NoSuchMethodException ignore) {
		}
		getEnabled = e;
		
		oldValue = getValue();
		oldEnabled = getIsEnabled();
		
		this.setEnabled(oldEnabled);
		this.putValue(SELECTED_KEY, oldValue);
		
		source.addChangeListener(this);
	}
	
	public String getValue() {
		
		if (getMethod != null) {
			
			try {
				return (String) getMethod.invoke(source);
			} catch (IllegalAccessException e) {
				throw new BugException("getMethod execution error for source " + source, e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
			
		} else {
			
			// Use internal value
			return value;
			
		}
	}
	
	public void setValue(String s) {
		checkState(true);
		log.debug("Setting value of " + this + " to " + s);
		
		if (setMethod != null) {
			try {
				setMethod.invoke(source, new Object[] { s });
			} catch (IllegalAccessException e) {
				throw new BugException("setMethod execution error for source " + source, e);
			} catch (InvocationTargetException e) {
				throw Reflection.handleWrappedException(e);
			}
		} else {
			// Manually fire state change - normally the ChangeSource fires it
			value = s;
			stateChanged(null);
		}
	}
	
	
	/**
	 * Add a component the enabled status of which will be controlled by the value
	 * of this boolean.  The <code>component</code> will be enabled exactly when
	 * the state of this model is equal to that of <code>enableState</code>.
	 * 
	 * @param component		the component to control.
	 * @param enableState	the state in which the component should be enabled.
	 */
	public void addEnableComponent(Component component, boolean enableState) {
		checkState(true);
		components.add(component);
		componentEnableState.add(enableState);
		updateEnableStatus();
	}
	
	/**
	 * Add a component which will be enabled when this boolean is <code>true</code>.
	 * This is equivalent to <code>StringModel.addEnableComponent(component, true)</code>.
	 * 
	 * @param component		the component to control.
	 * @see #addEnableComponent(Component, boolean)
	 */
	public void addEnableComponent(Component component) {
		checkState(true);
		addEnableComponent(component, true);
	}
	
	private void updateEnableStatus() {
		boolean state = getIsEnabled();
		
		for (int i = 0; i < components.size(); i++) {
			Component c = components.get(i);
			boolean b = componentEnableState.get(i);
			c.setEnabled(state == b);
		}
	}
	
	

	private boolean getIsEnabled() {
		if (getEnabled == null)
			return true;
		try {
			return (Boolean) getEnabled.invoke(source);
		} catch (IllegalAccessException e) {
			throw new BugException("getEnabled execution error for source " + source, e);
		} catch (InvocationTargetException e) {
			throw Reflection.handleWrappedException(e);
		}
	}
	
	@Override
	public void stateChanged(EventObject event) {
		checkState(true);
		
		if (firing > 0) {
			log.debug("Ignoring stateChanged of " + this + ", currently firing events");
			return;
		}
		
		String v = getValue();
		boolean e = getIsEnabled();
		if (oldValue != v) {
			log.debug("Value of " + this + " has changed to " + v + " oldValue=" + oldValue);
			oldValue = v;
			firing++;
			this.putValue(SELECTED_KEY, getValue());
			//			this.firePropertyChange(SELECTED_KEY, !v, v);
			updateEnableStatus();
			firing--;
		}
		if (oldEnabled != e) {
			log.debug("Enabled status of " + this + " has changed to " + e + " oldEnabled=" + oldEnabled);
			oldEnabled = e;
			setEnabled(e);
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (firing > 0) {
			log.debug("Ignoring actionPerformed of " + this + ", currently firing events");
			return;
		}
		
		String v = (String) this.getValue(SELECTED_KEY);
		log.info(Markers.USER_MARKER, "Value of " + this + " changed to " + v + " oldValue=" + oldValue);
		if (v != oldValue) {
			firing++;
			setValue(v);
			oldValue = getValue();
			// Update all states
			this.putValue(SELECTED_KEY, oldValue);
			this.setEnabled(getIsEnabled());
			updateEnableStatus();
			firing--;
		}
	}
	
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		checkState(true);
		super.addPropertyChangeListener(listener);
	}
	
	
	/**
	 * Invalidates this model by removing all listeners and removing this from
	 * listening to the source.  After invalidation no listeners can be added to this
	 * model and the value cannot be set.
	 */
	@Override
	public void invalidate() {
		invalidator.invalidate();
		
		PropertyChangeListener[] listeners = this.getPropertyChangeListeners();
		if (listeners.length > 0) {
			log.warn("Invalidating " + this + " while still having listeners " + listeners);
			for (PropertyChangeListener l : listeners) {
				this.removePropertyChangeListener(l);
			}
		}
		if (source != null) {
			source.removeChangeListener(this);
		}
		MemoryManagement.collectable(this);
	}
	
	
	private void checkState(boolean error) {
		invalidator.check(error);
	}
	
	

	@Override
	public String toString() {
		if (toString == null) {
			if (source != null) {
				toString = "StringModel[" + source.getClass().getSimpleName() + ":" + valueName + "]";
			} else {
				toString = "StringModel[internal value]";
			}
		}
		return toString;
	}
}
