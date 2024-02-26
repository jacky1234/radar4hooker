package gz.util;

import gz.radar.Android;

public class XUI {

    public static String getResourceNameSafe(int id) {
        try {
            return Android.getApplication().getResources().getResourceName(id);
        } catch (Exception ignore) {
            return "error_id";
        }
    }

    public static String getResourceEntryNameSafe(int id) {
        try {
            return Android.getApplication().getResources().getResourceEntryName(id);
        } catch (Exception ignore) {
            return "error_id";
        }
    }
}
