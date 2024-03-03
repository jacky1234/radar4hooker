package gz.radar;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gz.com.alibaba.fastjson.JSONArray;
import gz.com.alibaba.fastjson.JSONObject;
import gz.radar.objects.ObjectsStore;
import gz.util.HookException;
import gz.util.X;
import gz.util.XLog;
import gz.util.XUI;
import gz.util.XView;

public class AndroidUI {

    private static Class fragmentV4Clazz;

    static {
        try {
            fragmentV4Clazz = Class.forName("android.support.v4.app.Fragment");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final Thread keepScreenOnThread = new Thread() {

        private Set<Class> activityFlags = new HashSet<>();

        @Override
        public void run() {
            while (true) {
                try {
                    final Activity activity = Android.getTopActivity();
                    if (activity != null && !activityFlags.contains(activity.getClass())) {
                        activityFlags.add(activity.getClass());
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Window window = activity.getWindow();
                                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                View view = activity.getWindow().getDecorView();
                                view.setKeepScreenOn(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public final synchronized static void keepScreenOn() {
        if (keepScreenOnThread.isAlive()) {
            return;
        }
        keepScreenOnThread.start();
    }


    /**
     * 滑动
     *
     * @param x
     * @param y
     * @param stepLength
     */
    public static void hover(final float x, final float y, final int stepLength) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Instrumentation iso = new Instrumentation();
                iso.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0));
                iso.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x, y, 0));
                iso.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 20, MotionEvent.ACTION_MOVE, x, y - 30 * stepLength, 0));
                iso.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 40, MotionEvent.ACTION_MOVE, x, y - 60 * stepLength, 0));
                iso.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 60, MotionEvent.ACTION_MOVE, x, y - 90 * stepLength, 0));
                iso.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 60, MotionEvent.ACTION_UP, x, y - 90 * stepLength, 0));
            }
        };
        if (Thread.currentThread().getId() <= 2) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    public static void showToast(final String text) throws Exception {
        Android.getTopActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(Android.getApplication(), text, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static View getRootViewGroup() throws Exception {
        return Android.getTopActivity().getWindow().getDecorView();
    }

    public static View findViewByIdName(String idName) throws Exception {
        Application application = Android.getApplication();
        Resources resources = Android.getApplication().getResources();
        int id = resources.getIdentifier(idName, "id", application.getPackageName());
        if (id == 0) {
            throw new Exception("Not Found View.");
        }
        return findViewById(id);
    }

    public static View findViewById(int id) throws Exception {
        Activity activity = Android.getTopActivity();
        View view = activity.findViewById(id);
        if (view != null) {
            return view;
        }
        List fragments = getFragments(Android.getTopActivity());
        if (fragments != null) {
            for (Object fragment : fragments) {
                try {
                    View fragmentView = (View) X.invokeObject(fragment, "getView");
                    view = fragmentView.findViewById(id);
                    if (view != null) {
                        return view;
                    }
                } catch (Exception e) {
                    XLog.appendText(e);
                }
            }
        }
        return null;
    }

    public static List getFragments(Object obj) {
        try {
            Object fm = null;
            if (obj instanceof Activity) {
                fm = X.invokeObject(obj, "getSupportFragmentManager");
            } else if (fragmentV4Clazz != null && fragmentV4Clazz.isAssignableFrom(obj.getClass())) {
                fm = X.invokeObject(obj, "getChildFragmentManager");
            }
            if (fm != null) {
                List fragments = (List) X.invokeObject(fm, "getFragments");
                return fragments;
            }
        } catch (Exception e) {
            HookException.printStackTrace(e);
        }
        return null;
    }

    public static boolean clickById(int id) throws Exception {
        View view = findViewById(id);
        if (view.isClickable()) {
            final View clickableView = view;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    clickableView.performClick();
                }
            };
            clickableView.post(runnable);
            return true;
        }
        return false;
    }

    public static void search() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SEARCH);
            }
        };
        if (Thread.currentThread().getId() <= 2) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    public static void searchText(final EditText editText, final String text) throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                editText.setText(text);
            }
        };
        Thread currentThread = Thread.currentThread();
        if (currentThread.getId() <= 2) {
            runnable.run();
        } else {
            editText.post(runnable);
        }
        Thread.sleep(500);
        Runnable sendAction = new Runnable() {
            @Override
            public void run() {
                editText.onEditorAction(EditorInfo.IME_ACTION_SEARCH);
                //editText.performAccessibilityAction(EditorInfo.IME_ACTION_SEARCH, null);
            }
        };
        if (currentThread.getId() <= 2) {
            sendAction.run();
        } else {
            editText.post(sendAction);
        }
    }

    public static void searchText(int editTextId, String text) throws Exception {
        searchText((EditText) findViewById(editTextId), text);
    }

    public static void searchText(String editTextIdName, String text) throws Exception {
        searchText((EditText) findViewByIdName(editTextIdName), text);
    }

    public static <T extends View> List<T> collectViews(View containView, Class<T> tClass) throws Exception {
        List<T> list = new ArrayList<>();
        Class<?> viewClass = containView.getClass();
        if (viewClass.getName().equals(tClass.getName()) || tClass.isAssignableFrom(viewClass)) {
            list.add((T) containView);
            return list;
        }
        if (containView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) containView;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                List<T> listResult = collectViews(childView, tClass);
                if (!listResult.isEmpty()) {
                    list.addAll(listResult);
                }
            }
        }
        return list;
    }

    public static void startActivity(String activityName) throws Exception {
        //contextStartActivity(activityName);
        topActivityStartActivity(activityName);
    }

    public static void contextStartActivity(String activityName) throws Exception {
        Activity activity = Android.getTopActivity();
        if (activity.getClass().getName().equals(activityName)) {
            return;
        }
        Application application = Android.getApplication();
        Class<? extends Activity> activityClass = (Class<? extends Activity>) Class.forName(activityName);
        Intent intent = new Intent(application, activityClass);
        application.startActivity(intent);
    }

    public static void contextStartActivityForNewTask(String activityName) throws Exception {
        Activity activity = Android.getTopActivity();
        if (activity.getClass().getName().equals(activityName)) {
            return;
        }
        Application application = Android.getApplication();
        Class<? extends Activity> activityClass = (Class<? extends Activity>) Class.forName(activityName);
        Intent intent = new Intent(application, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    public static void topActivityStartActivity(String activityName) throws Exception {
        Activity activity = Android.getTopActivity();
        if (activity.getClass().getName().equals(activityName)) {
            return;
        }
        Class<? extends Activity> activityClass = (Class<? extends Activity>) Class.forName(activityName);
        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
    }

    public static void finishCurrentActivity() throws Exception {
        final Activity activity = Android.getTopActivity();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                activity.finish();
            }
        };
        activity.runOnUiThread(runnable);
    }

    public static void back() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        if (Thread.currentThread().getId() <= 2) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    public static void home() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Instrumentation instrumentation = new Instrumentation();
                    instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        if (Thread.currentThread().getId() <= 2) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    public static String viewTree() throws Exception {
        Activity activity = Android.getTopActivity();
        return viewTreeScan(activity.getWindow().getDecorView()).toJSONString();
    }

    private static JSONObject viewTreeScan(View decorView) throws Exception {
        return dumpBasicViewInfo(decorView);
    }

    public static boolean clickByText(String text) throws Exception {
        return clickByText(text, false, false);
    }

    public static boolean clickByText(String text, boolean mustBeTextEqual, boolean mustBeVisible) throws Exception {
        Activity activity = Android.getTopActivity();
        if (activity == null) {
            return false;
        }
        View decorView = activity.getWindow().getDecorView();
        View view = findViewByText(decorView, text, mustBeTextEqual, mustBeVisible);
        if (view != null) {
            while (true) {
                if (view.isClickable()) {
                    final View clickableView = view;
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            clickableView.performClick();
                        }
                    };
                    clickableView.post(runnable);
                    return true;
                }
                view = (View) view.getParent();
                if (view == null) {
                    break;
                }
            }
        }
        return false;
    }

    public static <T extends View> T findViewByText(View decorView, String text) {
        return findViewByText(decorView, text, false, false);
    }

    public static <T extends View> T findViewByText(View decorView, String text, boolean mustBeTextEqueal, boolean mustBeVisible) {
        if (mustBeVisible && decorView.getVisibility() != View.VISIBLE) {
            return null;
        }
        if (decorView instanceof TextView && !(decorView instanceof EditText)) {
            TextView textView = ((TextView) decorView);
            String textViewText = textView.getText().toString().trim();
            if (mustBeTextEqueal && textViewText.equals(text)) {
                return (T) textView;
            } else if (textViewText.contains(text)) {
                return (T) textView;
            }
        } else if (decorView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) decorView;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                TextView textView = findViewByText(childView, text);
                if (textView != null) {
                    return (T) textView;
                }
            }
        }
        return null;
    }

    public static List<View> findViewsById(View decorView, int id) {
        List<View> views = new ArrayList<View>();
        if (decorView.getId() == id) {
            views.add(decorView);
            return views;
        }
        if (decorView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) decorView;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = viewGroup.getChildAt(i);
                views.addAll(findViewsById(childView, id));
            }
        }
        return views;
    }

    public static String viewInfoByText(String text) throws Exception {
        return viewInfoByText(text, true);
    }

    public static String viewInfoByText(String text, boolean mustBeTextEqual) throws Exception {
        Activity activity = Android.getTopActivity();
        if (activity == null) {
            return "Top Activity not found";
        }
        View decorView = activity.getWindow().getDecorView();
        final View findView = findViewByText(decorView, text, mustBeTextEqual, true);
        if (findView == null) {
            return "Can't find viewByText[" + text + "]";
        }

        final JSONObject root = dumpBasicViewInfo(findView);

        final XView xView = new XView(findView);
        final View.OnClickListener mOnClickListenerInfo;
        try {
            mOnClickListenerInfo = xView.getOnClickListener();
            root.put("mOnClickListener", X.dumpFields(mOnClickListenerInfo));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root.toJSONString();
    }

    public static String topFragments() {
        JSONObject root = new JSONObject();

        try {
            Activity current = Android.getTopActivity();
            root.put("currentActivityName", current.getClass().getName());
            root.put("objectId", ObjectsStore.storeObject(current));
            List fragments = getFragments(current);
            if (fragments != null) {
                JSONArray jsonArray = new JSONArray(fragments.size());
                for (int i = 0; i < fragments.size(); i++) {
                    jsonArray.add(dumpFragmentInfo(fragments.get(i)));
                }
                root.put("fragments", jsonArray);
            }

        } catch (Exception e) {
            root.put("exception", e.getMessage());
        }
        return root.toString();
    }

    private static JSONObject dumpBasicViewInfo(View decorView) throws Exception {
        JSONObject root = new JSONObject();
        root.put("ViewClass", decorView.getClass().getName());
        root.put("ViewId", decorView.getId());
        if (decorView.getId() != -1) {
            root.put("indentify_name", XUI.getResourceNameSafe(decorView.getId()));
        }
        root.put("ViewIdName", XUI.getResourceEntryNameSafe(decorView.getId()));
        root.put("IsClickable", decorView.isClickable());
        root.put("IsVisible", decorView.getVisibility() == View.VISIBLE);
        root.put("IsEnabled", decorView.isEnabled());
        root.put("IsFocusable", decorView.isFocusable());
        root.put("IsFocused", decorView.isFocused());
        root.put("IsHorizontalScrollBarEnabled", decorView.isHorizontalScrollBarEnabled());
        root.put("IsLongClickable", decorView.isLongClickable());
        root.put("IsSelected", decorView.isSelected());
        root.put("IsShown", decorView.isShown());
        root.put("Width", decorView.getWidth());
        root.put("Height", decorView.getHeight());
        root.put("X", decorView.getX());
        root.put("Y", decorView.getY());
        if (decorView instanceof TextView) {
            root.put("ViewText", ((TextView) decorView).getText().toString());
        } else if (decorView instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) decorView;
            JSONArray childViewTree = new JSONArray();
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                JSONObject newRoot = viewTreeScan(childView);
                childViewTree.add(newRoot);
            }
            root.put("ChildViews", childViewTree);
        }
        return root;
    }

    private static JSONObject dumpFragmentInfo(Object fragment) {
        JSONObject root = new JSONObject();
        try {
            root.put("fragmentClazz", fragment.getClass().getName());
            Object view = X.invokeObject(fragment, "getView");
            root.put("rootViewClazz", view.getClass().getName());
            root.put("rootViewId", X.invokeObject(view, "getId"));
            root.put("fragmentObjectId", ObjectsStore.storeObject(fragment));
            List fragments = getFragments(fragment);
            if (fragments != null && fragments.size() > 0) {
                JSONArray jsonArray = new JSONArray(fragments.size());
                for (int i = 0; i < fragments.size(); i++) {
                    jsonArray.add(dumpFragmentInfo(fragments.get(i)));
                }
                root.put("children", jsonArray);
            }
        } catch (Exception e) {
            root.put("exception", e.getMessage());
        }
        return root;
    }
}
