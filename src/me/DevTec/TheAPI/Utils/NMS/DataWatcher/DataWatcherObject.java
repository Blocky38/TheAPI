package me.DevTec.TheAPI.Utils.NMS.DataWatcher;

/**
 * @apiNote This utility is only for 1.9+
 */
public class DataWatcherObject {
	private Object a;

	public DataWatcherObject(Object c) {
		a = c;
	}

	public Object get() {
		return a;
	}

	public void set(Object newData) {
		a = newData;
	}
}
