package gz.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public final class KeyedWeakReference<T> extends WeakReference<T> {
    public final String key;

    public KeyedWeakReference(String key, T referent, ReferenceQueue q) {
        super(referent, q);
        this.key = key;
    }
}