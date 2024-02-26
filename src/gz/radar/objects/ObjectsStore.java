package gz.radar.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class ObjectsStore {

	private static final String ObjectCacheKey = "ObjectStoreMapKey";
	
	private static final String ObjectExistsFlagKey = "ObjectExistsFlagKey";

	private static final long ObjectCacheExpiredTime = 1000 * 60 * 60; // 1小时

	private static String getRandomKey(int length) {
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * 对象的核心存储map
	 * 
	 * @return
	 */
	private static void checks() {
		if (!System.getProperties().containsKey(ObjectCacheKey)) {
			System.getProperties().put(ObjectCacheKey, new HashMap<String, Object>());
		}
		if (!System.getProperties().containsKey(ObjectExistsFlagKey)) {
			System.getProperties().put(ObjectExistsFlagKey, new HashMap<Integer, String>());
		}
	}
	

	public synchronized static String storeObject(Object obj) {
		checks();
		if (obj != null) {
			HashMap<Integer, String> objectExistsFlagMap = (HashMap<Integer, String>) System.getProperties().get(ObjectExistsFlagKey);
			HashMap<String, Object> objectCacheMap = (HashMap<String, Object>) System.getProperties().get(ObjectCacheKey);
			String objectFlagKey = objectExistsFlagMap.get(obj.hashCode());
			if (objectFlagKey != null) {
				objectCacheMap.remove(objectFlagKey);
			}
			objectFlagKey = getRandomKey(10);
			objectExistsFlagMap.put(obj.hashCode(), objectFlagKey);		
			objectCacheMap.put(objectFlagKey, obj);
			return objectFlagKey;
		}
		return null;
	}

	public synchronized static Object getObject(String objectId) {
		checks();
		Object obj = null;
		HashMap<String, Object> objectCacheMap = (HashMap<String, Object>) System.getProperties().get(ObjectCacheKey);
		obj = objectCacheMap.get(objectId);
		return obj;
	}

	public synchronized static String dumpObjectIds() {
		checks();
		HashMap<String, Object> objectCacheMap = (HashMap<String, Object>) System.getProperties().get(ObjectCacheKey);
		Set<Map.Entry<String, Object>> entries = objectCacheMap.entrySet();

		StringBuilder stringBuilder = new StringBuilder("total all " + objectCacheMap.size() + " objectIds\n");
		Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> next = iterator.next();
			stringBuilder.append("\tk: ").append(next.getKey()).append("\tv: ")
					.append(next.getValue().getClass().getName())
					.append("\n");
		}
		return stringBuilder.toString();
	}
}
