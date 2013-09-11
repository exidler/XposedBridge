package de.robv.android.xposed.callbacks;

import android.os.Bundle;
import de.robv.android.xposed.XposedBridge;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public abstract class XCallback implements Comparable<XCallback> {
	public final int priority;
	public XCallback() {
		this.priority = PRIORITY_DEFAULT;
	}
	public XCallback(int priority) {
		this.priority = priority;
	}
	
	public static class Param {
		public final TreeSet<? extends XCallback> callbacks;
		/**
		 * This can be used to store anything for the scope of the callback.
		 * Use this instead of instance variables.
		 * @see #getObjectExtra
		 * @see #setObjectExtra
		 */
		public Bundle extra;
		
		protected Param() {
			callbacks = null;
		}
		
		@SuppressWarnings("unchecked")
		protected Param(TreeSet<? extends XCallback> callbacks) {
			synchronized (callbacks) {
				this.callbacks = (TreeSet<? extends XCallback>) callbacks.clone();
			}
		}
		
		/** @see #setObjectExtra */
		public Object getObjectExtra(String key) {
			if (extra == null) return null;
			Serializable o = extra.getSerializable(key);
			if (o instanceof SerializeWrapper)
				return ((SerializeWrapper) o).object;
			return null;
		}
		
		/** Provides a wrapper to store <code>Object</code>s in <code>extra</code>. */
		public void setObjectExtra(String key, Object o) {
			if (extra == null) extra = new Bundle();
			extra.putSerializable(key, new SerializeWrapper(o));
		}
		
		private static class SerializeWrapper implements Serializable {
			private static final long serialVersionUID = 1L;
			private Object object;
			public SerializeWrapper(Object o) {
				object = o;
			}
		}
	}
	
	public static final void callAll(Param param) {
		if (param.callbacks == null)
			throw new IllegalStateException("This object was not created for use with callAll");
		
		Iterator<? extends XCallback> it = param.callbacks.iterator();
		while (it.hasNext()) {
			try {
				it.next().call(param);
			} catch (Throwable t) { XposedBridge.log(t); }
		}
	}
	
	protected void call(Param param) throws Throwable {};
	
	@Override
	public int compareTo(XCallback other) {
		if (this == other)
			return 0;
		
		// order descending by priority
		if (other.priority != this.priority)
			return other.priority - this.priority;
		// then randomly
		else if (System.identityHashCode(this) < System.identityHashCode(other))
			return -1;
		else
			return 1;
	}
	
	public static class PriorityComparator implements Comparator<XCallback> {
		@Override
		public int compare(XCallback c1, XCallback c2) {
			return c1.compareTo(c2);
		}
	}

	public static final PriorityComparator PRIORITY_COMPARATOR = new PriorityComparator();

	public static final int PRIORITY_DEFAULT = 50;
	/** Call this handler last */
	public static final int PRIORITY_LOWEST = -10000;
	/** Call this handler first */
	public static final int PRIORITY_HIGHEST = 10000;
}
