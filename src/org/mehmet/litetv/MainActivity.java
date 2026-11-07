package org.mehmet.litetv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

/** One Activity, no services, network, animations, image libraries or privileged writes. */
public final class MainActivity extends Activity {
    private static final String TAG = "HafifTV", VENDOR = "com.htc.luminaos";
    private static final int BG = 0xff0b111c, CARD = 0xff192536, INK = 0xffedf4ff;
    private final ArrayList<App> apps = new ArrayList<>();
    private final ArrayList<App> visible = new ArrayList<>();
    private final LruCache<String, Drawable> icons = new LruCache<>(24);
    private final LinkedHashMap<String, BundledApp> bundled = new LinkedHashMap<>();
    private SharedPreferences prefs;
    private Set<String> favorites;
    private LinearLayout root, content;
    private GridView grid;
    private String screen = "home", mode = "all";
    private View initialFocus;
    private AlertDialog activeDialog;
    private Drawable wallpaper;
    private static final class BundledApp {
        String label; int icon; long sourceBytes;
        BundledApp(String label, int icon, long sourceBytes) { this.label=label; this.icon=icon; this.sourceBytes=sourceBytes; }
    }
    private static final class App {
        ResolveInfo info; String label, key; int bundledIcon;
        App(ResolveInfo r, PackageManager pm, BundledApp cached) {
            info = r;
            boolean valid = cached != null && new java.io.File(r.activityInfo.applicationInfo.sourceDir).length() == cached.sourceBytes;
            label = valid ? cached.label : r.loadLabel(pm).toString(); bundledIcon = valid ? cached.icon : 0;
            key = new ComponentName(r.activityInfo.packageName, r.activityInfo.name).flattenToString();
        }
    }

    @Override protected void attachBaseContext(Context base) {
        android.content.res.Configuration c = new android.content.res.Configuration(base.getResources().getConfiguration());
        c.setLocale(new Locale("tr", "TR")); super.attachBaseContext(base.createConfigurationContext(c));
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setWindowAnimations(0);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        prefs = getSharedPreferences("home", MODE_PRIVATE);
        favorites = new HashSet<>(prefs.getStringSet("favorites", Collections.emptySet()));
        if (state != null) { mode = state.getString("mode", "all"); screen = state.getString("screen", "home"); }
        if (!prefs.getBoolean("slots5", false)) {
            prefs.edit().remove("slot0").remove("slot1").remove("slot2").remove("slot3").remove("slot4").putBoolean("slots5", true).apply();
        }
        loadBundled(); loadApps(); show(screen);
        diagnosticsIfRequested();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state); state.putString("mode", mode); state.putString("screen", screen);
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); mode = "all"; show("home"); diagnosticsIfRequested();
    }
    @Override protected void onResume() {
        super.onResume();
        // Re-query lightweight activity metadata to reflect installs/removals without a background service.
        if (grid != null) { loadApps(); refreshGrid(); }
    }
    @Override public void onBackPressed() {
        if ("focus".equals(screen)) { show("projection"); return; }
        if (!"home".equals(screen)) show("home");
    }
    @Override public boolean onKeyUp(int code, KeyEvent event) {
        if (code == KeyEvent.KEYCODE_MENU) { show("settings"); return true; }
        return super.onKeyUp(code, event);
    }
    @Override public void onTrimMemory(int level) { super.onTrimMemory(level); icons.evictAll(); }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private GradientDrawable shape(int color, boolean focused) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(12));
        d.setStroke(dp(focused ? 3 : 1), focused ? 0xff65dfce : 0xff29384c); return d;
    }
    private Drawable focusBackground() {
        StateListDrawable d = new StateListDrawable();
        d.addState(new int[]{android.R.attr.state_focused}, shape(0xff244956, true));
        d.addState(new int[]{android.R.attr.state_pressed}, shape(0xff244956, true));
        d.addState(new int[]{android.R.attr.state_selected}, shape(0xff244956, true));
        d.addState(new int[]{}, shape(CARD, false)); return d;
    }
    private Button button(String title, Runnable action) {
        Button b = new Button(this); b.setText(title); b.setTextSize(18); b.setTextColor(INK);
        b.setAllCaps(false); b.setGravity(Gravity.CENTER); b.setPadding(dp(8), dp(6), dp(8), dp(6));
        b.setBackground(focusBackground()); b.setStateListAnimator(null); b.setMinimumHeight(0); b.setMinHeight(0);
        b.setOnClickListener(v -> { Log.i(TAG, "tile:" + title); action.run(); }); return b;
    }
    private void show(String target) {
        if (activeDialog != null) { activeDialog.dismiss(); activeDialog = null; }
        screen = target; grid = null; initialFocus = null;
        if ("focus".equals(target)) { focusScreen(); return; }
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(34), dp(22), dp(34), dp(16)); root.setBackgroundColor(prefs.getInt("color", BG));
        applyWallpaper();
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout brand = new LinearLayout(this); brand.setOrientation(LinearLayout.VERTICAL);
        brand.addView(text("HAFİF TV", 20, 0xff65dfce));
        brand.addView(text("home".equals(target) ? "Sadece ihtiyacın olan." : titleFor(target), 17, 0xffa4b5c9));
        header.addView(brand, new LinearLayout.LayoutParams(0, dp(66), 1));
        LinearLayout timeBox = new LinearLayout(this); timeBox.setOrientation(LinearLayout.VERTICAL);
        TextClock clock = new TextClock(this); clock.setFormat24Hour("HH:mm"); clock.setFormat12Hour("HH:mm");
        clock.setTextSize(34); clock.setTextColor(INK); clock.setGravity(Gravity.RIGHT);
        TextClock date = new TextClock(this); date.setFormat24Hour("d MMMM EEEE"); date.setFormat12Hour("d MMMM EEEE");
        date.setTextSize(16); date.setTextColor(0xffa4b5c9); date.setGravity(Gravity.RIGHT);
        timeBox.addView(clock); timeBox.addView(date); header.addView(timeBox);
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(dp(50), dp(50));
        settingsParams.leftMargin = dp(18);
        header.addView(circleView(dp(50), glyph(4, dp(30)), () -> show("settings")), settingsParams);
        root.addView(header);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        if ("home".equals(target)) home();
        else if ("settings".equals(target)) settings();
        else if ("projection".equals(target)) projection();
        else if ("power".equals(target)) power();
        else if ("slots".equals(target)) slotsScreen();
        else { appScreen(); }
        String hint = "↑ ↓ ← →  Gezin    OK  Aç    Geri  Ana ekran";
        if ("home".equals(target)) hint = "↑ ↓ ← →  Gezin    OK  Aç    Kalem simgesi: alt sırayı düzenle    Geri  Ana ekran";
        if ("slots".equals(target)) hint = "OK  Değiştir veya kaldır    Geri  Ana ekran";
        if ("apps".equals(target)) hint = "↑ ↓ ← →  Gezin    OK  Aç    Uzun OK  Uygulama seçenekleri    Geri  Ana ekran";
        TextView help = text(hint, 14, 0xffa4b5c9);
        help.setPadding(0, dp(8), 0, 0); root.addView(help);
        setContentView(root); if (initialFocus != null) initialFocus.requestFocus();
        Log.i(TAG, "screen:" + target);
    }
    private String titleFor(String value) {
        if ("settings".equals(value)) return "Ayarlar ve bağlantılar";
        if ("projection".equals(value)) return "Projeksiyon ve görüntü";
        if ("power".equals(value)) return "Güç ve zamanlayıcı";
        if ("slots".equals(value)) return "Alt sırayı düzenle";
        return "Tüm uygulamalar";
    }
    private void row(String[] names, Runnable[] actions) {
        LinearLayout r = new LinearLayout(this); r.setPadding(0, dp(7), 0, dp(3));
        for (int i = 0; i < names.length; i++) {
            Button b = button(names[i], actions[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(62), 1); lp.setMargins(0, 0, dp(7), 0);
            r.addView(b, lp); if (initialFocus == null) initialFocus = b;
        }
        content.addView(r);
    }
    private void home() {
        int gap = dp(7);
        int width = getResources().getDisplayMetrics().widthPixels - dp(68);
        int tileWidth = (width - gap * 4) / 3;
        LinearLayout row = new LinearLayout(this);
        View[] tiles = {
            tile("Giriş / HDMI", glyph(0, dp(62)), () -> hdmi()),
            tile("Kablosuz Ekran", glyph(1, dp(62)), () -> openAny("com.htc.samescreen", "com.softwinner.miracastReceiver")),
            tile("Dosyalar", glyph(2, dp(62), 0xff4c9aff), () -> openAny("com.alphainventor.filemanager"))};
        for (int i = 0; i < tiles.length; i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            lp.setMargins(i == 0 ? 0 : gap, 0, gap, 0);
            row.addView(tiles[i], lp);
            if (initialFocus == null) initialFocus = tiles[i];
        }
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, tileWidth * 5 / 8, 0f);
        rp.topMargin = dp(6);
        content.addView(row, rp);
        quickRow();
        circleRow();
    }
    /** Four small rectangles under the big tiles: the streaming apps people open every day. */
    private void quickRow() {
        String[][] items = {
            {"Netflix", "com.netflix.ninja"},
            {"Prime Video", "com.amazon.amazonvideo.livingroom"},
            {"HBO Max", "com.wbd.hbomax"},
            {"YouTube", "com.google.android.youtube.tv"}};
        int gap = dp(7);
        LinearLayout row = new LinearLayout(this);
        for (int i = 0; i < items.length; i++) {
            final String pkg = items[i][1];
            Runnable action = "com.netflix.ninja".equals(pkg) ? this::netflix : () -> openAny(pkg);
            View box = smallTile(items[i][0], pkg, action);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1f);
            lp.setMargins(i == 0 ? 0 : gap, 0, 0, 0);
            row.addView(box, lp);
        }
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(84), 0f);
        rp.topMargin = dp(8);
        content.addView(row, rp);
    }
    private View smallTile(String label, String pkg, Runnable action) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL); box.setBackground(focusBackground());
        box.setFocusable(true); box.setClickable(true); box.setPadding(dp(12), dp(8), dp(10), dp(8));
        android.widget.ImageView image = new android.widget.ImageView(this);
        image.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        image.setImageDrawable(appIcon(pkg, 42));
        box.addView(image, new LinearLayout.LayoutParams(dp(42), dp(42)));
        TextView t = text(label, 16, INK); t.setMaxLines(2); t.setPadding(dp(10), 0, 0, 0);
        box.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        box.setOnClickListener(v -> { Log.i(TAG, "quick:" + label); action.run(); });
        return box;
    }
    private void circleRow() {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER);
        int size = dp(76);
        row.setPadding(0, dp(12), 0, 0);
        addCircle(row, size, glyph(3, dp(34)), () -> { mode = "all"; show("apps"); });
        for (int i = 0; i < 5; i++) {
            final int index = i;
            final String key = prefs.getString("slot" + index, null);
            Drawable icon;
            Runnable action;
            if (key == null && index == 0) {
                icon = appIcon("com.phlox.tvwebbrowser", 50);
                action = () -> openAny("com.phlox.tvwebbrowser", "com.tcl.browser");
            } else if (key == null && index == 1) {
                icon = appIcon("cm.aptoidetv.pt", 50);
                action = () -> openAny("cm.aptoidetv.pt");
            } else if (key != null && !key.isEmpty()) {
                icon = appIcon(packageOf(key), 50);
                action = () -> launchComponent(key);
            } else {
                icon = glyph(5, dp(28));
                action = () -> pickSlot(index);
            }
            addCircle(row, size, icon, action, () -> pickSlot(index));
        }
        addCircle(row, size, glyph(6, dp(30), 0xffa4b5c9), () -> show("slots"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 0, 1f); lp.topMargin = dp(10);
        content.addView(row, lp);
    }
    private void addCircle(LinearLayout row, int size, Drawable icon, Runnable action) {
        addCircle(row, size, icon, action, null);
    }
    private void addCircle(LinearLayout row, int size, Drawable icon, Runnable action, Runnable longAction) {
        View box = circleView(size, icon, action, longAction);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.setMargins(dp(7), 0, dp(7), 0);
        row.addView(box, lp);
    }
    private View circleView(int size, Drawable icon, Runnable action) {
        return circleView(size, icon, action, null);
    }
    private View circleView(int size, Drawable icon, Runnable action, Runnable longAction) {
        android.widget.FrameLayout box = new android.widget.FrameLayout(this);
        box.setBackground(roundBackground()); box.setFocusable(true); box.setClickable(true);
        android.widget.ImageView view = new android.widget.ImageView(this);
        view.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        if (icon != null) view.setImageDrawable(icon);
        box.addView(view, new android.widget.FrameLayout.LayoutParams(-1, -1));
        box.setOnClickListener(v -> { Log.i(TAG, "circle"); action.run(); });
        if (longAction != null) {
            box.setLongClickable(true);
            box.setOnLongClickListener(v -> { Log.i(TAG, "circle-long"); longAction.run(); return true; });
        }
        return box;
    }
    private void pickSlot(int index) {
        String[] names = new String[apps.size() + 1];
        names[0] = "Boş bırak";
        for (int i = 0; i < apps.size(); i++) names[i + 1] = apps.get(i).label;
        popup(new AlertDialog.Builder(this).setTitle("Yuvarlağa uygulama koy").setItems(names, (d, n) -> {
            prefs.edit().putString("slot" + index, n == 0 ? "" : apps.get(n - 1).key).apply();
            show("home");
        }).create());
    }
    private String slotLabel(int index) {
        String key = prefs.getString("slot" + index, null);
        if (key == null) return index == 0 ? "TV Bro" : index == 1 ? "Aptoide TV" : "Boş";
        if (key.isEmpty()) return "Boş";
        for (App a : apps) if (a.key.equals(key)) return a.label;
        ComponentName cn = ComponentName.unflattenFromString(key);
        return cn == null ? "Boş" : cn.getPackageName();
    }
    private void slotsScreen() {
        content.addView(text("Beş yuvarlağı buradan düzenle. Kaldırmak için “Boş bırak”ı seç.", 16, 0xffa4b5c9));
        for (int i = 0; i < 5; i++) {
            final int index = i;
            LinearLayout r = new LinearLayout(this); r.setPadding(0, dp(4), 0, dp(2));
            Button b = button((i + 1) + ". yuvarlak:   " + slotLabel(i), () -> pickSlot(index));
            r.addView(b, new LinearLayout.LayoutParams(-1, dp(52)));
            if (initialFocus == null) initialFocus = b;
            content.addView(r);
        }
        LinearLayout r = new LinearLayout(this); r.setPadding(0, dp(8), 0, dp(2));
        Button clear = button("Tümünü temizle", () -> {
            prefs.edit().putString("slot0", "").putString("slot1", "").putString("slot2", "").putString("slot3", "").putString("slot4", "").apply();
            show("slots");
        });
        Button back = button("Ana ekran", () -> show("home"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(54), 1); lp.setMargins(0, 0, dp(7), 0);
        r.addView(clear, lp);
        r.addView(back, new LinearLayout.LayoutParams(0, dp(54), 1));
        content.addView(r);
    }
    private String packageOf(String key) {
        ComponentName cn = ComponentName.unflattenFromString(key);
        return cn == null ? "" : cn.getPackageName();
    }
    private void launchComponent(String key) {
        ComponentName cn = ComponentName.unflattenFromString(key);
        if (cn == null || !launch(new Intent(Intent.ACTION_MAIN).setComponent(cn))) toast("Bu kısayol artık açılamıyor.");
    }
    private Drawable glyph(int kind, int size) { return glyph(kind, size, 0xffe2edf9); }
    private Drawable glyph(int kind, int size, int color) {
        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b); float s = size;
        android.graphics.Paint p = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        p.setColor(color); p.setStrokeWidth(s * 0.065f);
        p.setStyle(android.graphics.Paint.Style.STROKE);
        p.setStrokeCap(android.graphics.Paint.Cap.ROUND);
        p.setStrokeJoin(android.graphics.Paint.Join.ROUND);
        float m = s * 0.16f;
        if (kind == 0) {
            c.drawRoundRect(new android.graphics.RectF(m, m * 1.15f, s - m, s - m * 1.15f), s * 0.09f, s * 0.09f, p);
            android.graphics.Path t = new android.graphics.Path();
            t.moveTo(s * 0.36f, s * 0.41f); t.lineTo(s * 0.55f, s * 0.5f); t.lineTo(s * 0.36f, s * 0.59f); t.close();
            p.setStyle(android.graphics.Paint.Style.FILL); c.drawPath(t, p);
        } else if (kind == 1) {
            c.drawRoundRect(new android.graphics.RectF(m, m * 1.1f, s - m, s - m * 1.35f), s * 0.08f, s * 0.08f, p);
            for (int i = 1; i <= 3; i++) {
                float r = i * (s - 2 * m) * 0.2f;
                c.drawArc(new android.graphics.RectF(m - r, s - m * 1.3f - r, m + r, s - m * 1.3f + r), 180, 90, false, p);
            }
            p.setStyle(android.graphics.Paint.Style.FILL);
            c.drawCircle(m, s - m * 1.3f, s * 0.045f, p);
        } else if (kind == 2) {
            android.graphics.Path f = new android.graphics.Path();
            f.moveTo(m, s * 0.74f); f.lineTo(m, s * 0.3f); f.lineTo(s * 0.44f, s * 0.3f);
            f.lineTo(s * 0.52f, s * 0.41f); f.lineTo(s - m, s * 0.41f); f.lineTo(s - m, s * 0.74f); f.close();
            c.drawPath(f, p);
        } else if (kind == 3) {
            p.setStyle(android.graphics.Paint.Style.FILL);
            float step = (s - 2 * m) / 2f;
            for (int r = 0; r < 3; r++) for (int col = 0; col < 3; col++)
                c.drawCircle(m + col * step, m + r * step, s * 0.055f, p);
        } else if (kind == 4) {
            for (int i = 0; i < 3; i++) {
                float y = s * (0.3f + i * 0.2f);
                c.drawLine(m, y, s - m, y, p);
                p.setStyle(android.graphics.Paint.Style.FILL);
                c.drawCircle(m + (s - 2 * m) * (i == 1 ? 0.68f : 0.34f), y, s * 0.062f, p);
                p.setStyle(android.graphics.Paint.Style.STROKE);
            }
        } else if (kind == 5) {
            c.drawLine(s * 0.5f, m, s * 0.5f, s - m, p);
            c.drawLine(m, s * 0.5f, s - m, s * 0.5f, p);
        } else {
            android.graphics.Path tip = new android.graphics.Path();
            tip.moveTo(s * 0.22f, s * 0.80f); tip.lineTo(s * 0.34f, s * 0.63f);
            tip.lineTo(s * 0.64f, s * 0.24f); tip.lineTo(s * 0.78f, s * 0.38f);
            tip.lineTo(s * 0.39f, s * 0.71f); tip.close();
            c.drawPath(tip, p);
            c.drawLine(s * 0.34f, s * 0.63f, s * 0.39f, s * 0.71f, p);
        }
        return new BitmapDrawable(getResources(), b);
    }
    private Drawable roundBackground() {
        StateListDrawable d = new StateListDrawable();
        d.addState(new int[]{android.R.attr.state_focused}, roundShape(0xff244956, true));
        d.addState(new int[]{android.R.attr.state_pressed}, roundShape(0xff244956, true));
        d.addState(new int[]{}, roundShape(CARD, false));
        return d;
    }
    private GradientDrawable roundShape(int color, boolean focused) {
        GradientDrawable d = new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(color);
        d.setStroke(dp(focused ? 3 : 1), focused ? 0xff65dfce : 0xff29384c); return d;
    }
    private Drawable appIcon(String pkg, int sizeDp) {
        for (App a : apps) {
            if (!a.info.activityInfo.packageName.equals(pkg)) continue;
            try {
                Drawable original = a.bundledIcon != 0 ? getDrawable(a.bundledIcon) : a.info.loadIcon(getPackageManager());
                int s = dp(sizeDp); Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
                original.setBounds(0, 0, s, s); original.draw(new Canvas(b));
                return new BitmapDrawable(getResources(), b);
            } catch (RuntimeException e) { break; }
        }
        return getDrawable(android.R.drawable.sym_def_app_icon);
    }
    private View tile(String label, Drawable icon, Runnable action) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER); box.setBackground(focusBackground());
        box.setFocusable(true); box.setClickable(true); box.setPadding(dp(14), dp(14), dp(14), dp(14));
        android.widget.ImageView view = new android.widget.ImageView(this);
        view.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        if (icon != null) view.setImageDrawable(icon); else view.setImageResource(android.R.drawable.sym_def_app_icon);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(72), dp(72)); ip.bottomMargin = dp(8);
        box.addView(view, ip);
        TextView t = text(label, 20, INK); t.setGravity(Gravity.CENTER); t.setMaxLines(1);
        box.addView(t);
        box.setOnClickListener(v -> { Log.i(TAG, "tile:" + label); action.run(); });
        return box;
    }
    private void appScreen() { addGrid(); }
    private void addGrid() {
        grid = new GridView(this); grid.setNumColumns(4); grid.setHorizontalSpacing(dp(10)); grid.setVerticalSpacing(dp(10));
        grid.setPadding(0, dp(10), dp(7), 0); grid.setClipToPadding(false); grid.setSelector(android.R.color.transparent);
        grid.setDrawSelectorOnTop(false); grid.setCacheColorHint(Color.TRANSPARENT); grid.setAnimation(null);
        grid.setOnItemClickListener((p, v, pos, id) -> {
            App a = visible.get(pos);
            if ("edit".equals(mode)) toggleFavorite(a);
            else if ("manage".equals(mode)) appOptions(a);
            else launch(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(ComponentName.unflattenFromString(a.key)));
        });
        grid.setOnItemLongClickListener((p, v, pos, id) -> { appOptions(visible.get(pos)); return true; });
        content.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1)); refreshGrid();
    }
    private void loadApps() {
        PackageManager pm = getPackageManager(); LinkedHashMap<String, App> unique = new LinkedHashMap<>();
        for (String cat : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            for (ResolveInfo r : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat), 0)) {
                ActivityInfo info = r.activityInfo;
                if (getPackageName().equals(info.packageName) || !info.exported || !info.enabled) continue;
                if (!unique.containsKey(info.packageName)) unique.put(info.packageName, new App(r, pm, bundled.get(info.packageName)));
            }
        }
        apps.clear(); apps.addAll(unique.values()); Collator c = Collator.getInstance(new Locale("tr", "TR"));
        Collections.sort(apps, (a,b) -> c.compare(a.label,b.label));
    }
    private void refreshGrid() {
        if (grid == null) return;
        visible.clear();
        for (App a : apps) if (!"favorites".equals(mode) || favorites.contains(a.key)) visible.add(a);
        grid.setAdapter(new BaseAdapter() {
            public int getCount() { return visible.size(); }
            public Object getItem(int n) { return visible.get(n); }
            public long getItemId(int n) { return n; }
            public View getView(int pos, View recycled, ViewGroup parent) {
                TextView t = recycled instanceof TextView ? (TextView)recycled : text("", 18, INK);
                App a = visible.get(pos); t.setText((favorites.contains(a.key) ? "★  " : "") + a.label);
                t.setContentDescription(a.label); t.setGravity(Gravity.CENTER); t.setMaxLines(2);
                t.setPadding(dp(9), dp(10), dp(9), dp(8)); t.setBackground(focusBackground());
                t.setCompoundDrawablePadding(dp(5)); t.setCompoundDrawables(null, icon(a), null, null);
                t.setLayoutParams(new android.widget.AbsListView.LayoutParams(-1, dp(114)));
                t.setFocusable(false); return t;
            }
        });
        if ("favorites".equals(mode) && visible.isEmpty()) toast("Henüz favori yok. Favori düzenle ile bir uygulama seçin.");
        if ("edit".equals(mode)) toast("OK ile favoriye ekle / çıkar.");
    }
    private Drawable icon(App a) {
        Drawable d = icons.get(a.key);
        if (d == null) {
            try {
                Drawable original = a.bundledIcon != 0 ? getDrawable(a.bundledIcon)
                    : a.info.loadIcon(getPackageManager()); int s = dp(40);
                Bitmap b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888);
                original.setBounds(0,0,s,s); original.draw(new Canvas(b));
                d = new BitmapDrawable(getResources(), b); icons.put(a.key, d);
            } catch (RuntimeException e) { d = getDrawable(android.R.drawable.sym_def_app_icon); }
        }
        d.setBounds(0,0,dp(40),dp(40)); return d;
    }
    private void toggleFavorite(App a) {
        if (!favorites.remove(a.key)) favorites.add(a.key);
        prefs.edit().putStringSet("favorites", new HashSet<>(favorites)).apply(); refreshGrid();
    }
    private void appOptions(App a) {
        popup(new AlertDialog.Builder(this).setTitle(a.label).setItems(new String[]{
            favorites.contains(a.key) ? "Favorilerden çıkar" : "Favorilere ekle", "Uygulama bilgisi / kaldır / önbellek", "Aç"}, (d,n) -> {
            if (n == 0) toggleFavorite(a);
            else if (n == 1) launch(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + a.info.activityInfo.packageName)));
            else launch(new Intent(Intent.ACTION_MAIN).setComponent(ComponentName.unflattenFromString(a.key)));
        }).create());
    }
    private TextView note(String value) {
        TextView t = text(value, 17, 0xffa4b5c9); t.setPadding(0, dp(10), 0, dp(10)); return t;
    }
    private void tvSettings() {
        if (launch(new Intent().setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings")))) return;
        launch(new Intent(Settings.ACTION_SETTINGS));
    }
    private void displaySound() {
        ComponentName cn = new ComponentName("com.android.tv.settings", "com.android.tv.settings.device.displaysound.DisplaySoundActivity");
        try {
            ActivityInfo a = getPackageManager().getActivityInfo(cn, 0);
            if (a.exported && a.enabled && a.applicationInfo.enabled && launch(new Intent().setComponent(cn))) return;
        } catch (PackageManager.NameNotFoundException e) { Log.w(TAG, "DisplaySoundActivity yok", e); }
        tvSettings();
    }
    private void settings() {
        content.addView(note("Buradaki her kısayol gerçekten açılır. Üreticiye özel bölümlerin tamamı tek yerde: Cihaz ayarları."));
        row(new String[]{"Cihaz ayarları", "TV Ayarları", "Ağ / Wi-Fi"}, new Runnable[]{
            () -> vendor("settings.MainSettingActivity", Settings.ACTION_SETTINGS),
            () -> tvSettings(),
            () -> vendor("settings.NetworkActivity", Settings.ACTION_WIFI_SETTINGS)});
        row(new String[]{"Bluetooth", "Uygulamalar", "Duvar kağıdı"}, new Runnable[]{
            () -> confirmAction("Bluetooth ayarlarını aç?", "Üreticinin Bluetooth ekranı açılışta Bluetooth'u etkinleştirebilir ve tarama başlatabilir.", () -> vendor("settings.BluetoothActivity", Settings.ACTION_BLUETOOTH_SETTINGS)),
            () -> { mode = "manage"; show("apps"); },
            () -> wallpaper()});
        row(new String[]{"Projeksiyon", "Güç", "Kısayollar"}, new Runnable[]{
            () -> show("projection"),
            () -> show("power"),
            () -> more()});
    }
    private void projection() {
        content.addView(note("Projeksiyon ayarları üreticide. Aşağıdaki iki giriş gerçekten açılır:\nEkran ve Ses → odak, görüntü modu, projeksiyon yönü.\nCihaz ayarları → keystone, zoom, açı kalibrasyonu."));
        row(new String[]{"Ekran ve Ses (TV)", "LuminaOS Projeksiyon"}, new Runnable[]{
            () -> displaySound(),
            () -> vendor("settings.MainSettingActivity", Settings.ACTION_DISPLAY_SETTINGS)});
        content.addView(note("Odak halkası manuel. Deseni aç ve halkayı çevirerek sırayla üç şeyi kontrol et:\n1) Merkezdeki yıldızda gökkuşağı / dönen desen kaybolsun (merkez odak).\n2) Köşelerdeki ince çizgiler ve artılar keskin kalsın (kenar odak).\n3) Punto merdivenindeki en küçük satır okunabilsin (gerçek netlik).\nÜçü aynı anda olmuyorsa halkayı orta noktaya getir; desen bunu görmek için var."));
        row(new String[]{"Odak testi (desen)", "Ayarlara dön"}, new Runnable[]{
            () -> show("focus"), () -> show("settings")});
    }
    /** Üç desenli tam ekran odak referansı; OK ile desen, Geri ile projeksiyon ayarları. */
    private void focusScreen() {
        FocusView view = new FocusView(this);
        setContentView(view);
        view.requestFocus();
        Log.i(TAG, "screen:focus");
    }
    private void power() {
        content.addView(note("Dikkat: üreticinin güç ekranı 15 saniye sonra otomatik kapatabilir.\nGeri ile çıkmak veya odağı kapanma kutusundan taşımak sayacı durdurur."));
        row(new String[]{"Güç seçenekleri", "Ana ekran"}, new Runnable[]{
            () -> confirmPower(), () -> show("home")});
    }
    private void confirmPower() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Cihaz kapanabilir")
            .setMessage("LuminaOS güç ekranı açılınca 15 sn otomatik kapanma sayacı başlatır. Geri ile çıkmak veya odağı başka kutuya taşımak sayacı durdurur. Yalnız gerçekten kapatmak / yeniden başlatmak istiyorsanız devam edin.")
            .setNegativeButton("Vazgeç", null).setPositiveButton("Güç ekranını aç", (d,n) -> vendor("settings.PowerActivity", Settings.ACTION_SETTINGS)).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus()); popup(dialog);
    }
    private void popup(AlertDialog dialog) {
        if (activeDialog != null) activeDialog.dismiss();
        activeDialog = dialog;
        dialog.setOnDismissListener(d -> { if (activeDialog == dialog) activeDialog = null; });
        dialog.show();
    }
    private void confirmAction(String title, String message, Runnable action) {
        AlertDialog d = new AlertDialog.Builder(this).setTitle(title).setMessage(message)
            .setNegativeButton("Vazgeç", null).setPositiveButton("Devam et", (v,n) -> action.run()).create();
        d.setOnShowListener(v -> d.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus()); popup(d);
    }
    private void more() {
        popup(new AlertDialog.Builder(this).setTitle("Kısayollar").setItems(new String[]{"Duvar kağıdı / Tema", "Ekran yansıtma", "Dosyalar / USB", "Uygulama mağazası"}, (d,n) -> {
            if (n == 0) wallpaper();
            else if (n == 1) openAny("com.htc.samescreen", "com.softwinner.miracastReceiver");
            else if (n == 2) openAny("com.alphainventor.filemanager");
            else openAny("com.android.vending", "com.htc.storeos");
        }).create());
    }
    private void wallpaper() {
        popup(new AlertDialog.Builder(this).setTitle("Duvar kağıdı / Tema").setItems(new String[]{"Gece", "Okyanus", "Orman", "Dosya / USB'den resim seç", "Üretici duvar kağıtları"}, (d,n) -> {
            if (n == 4) vendor("WallPaperActivity", Settings.ACTION_SETTINGS);
            else if (n == 3) {
                try { startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("image/*").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION), 10); }
                catch (RuntimeException e) { toast("Bu cihazda resim seçici yok. Üretici duvar kağıtlarını kullanın."); }
            } else { wallpaper = null; prefs.edit().remove("wallpaper").putInt("color", new int[]{BG,0xff081d2d,0xff0c211b}[n]).apply(); show(screen); }
        }).create());
    }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == 10 && result == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
            catch (SecurityException e) { Log.w(TAG, "Resim kalıcı erişim izni alınamadı", e); }
            wallpaper = null; prefs.edit().putString("wallpaper", uri.toString()).apply(); show(screen);
        }
    }
    private void applyWallpaper() {
        String value = prefs.getString("wallpaper", null); if (value == null) return;
        try {
            if (wallpaper == null) {
                Uri uri = Uri.parse(value); BitmapFactory.Options o = new BitmapFactory.Options(); o.inJustDecodeBounds = true;
                try (InputStream in = getContentResolver().openInputStream(uri)) { BitmapFactory.decodeStream(in, null, o); }
                o.inSampleSize = 1; while (o.outWidth/o.inSampleSize > 960 || o.outHeight/o.inSampleSize > 540) o.inSampleSize *= 2;
                o.inJustDecodeBounds = false; o.inPreferredConfig = Bitmap.Config.RGB_565; Bitmap b;
                try (InputStream in = getContentResolver().openInputStream(uri)) { b = BitmapFactory.decodeStream(in, null, o); }
                if (b == null) throw new IllegalArgumentException("Resim çözümlenemedi");
                BitmapDrawable photo = new BitmapDrawable(getResources(), b); photo.setGravity(Gravity.FILL);
                GradientDrawable shade = new GradientDrawable(); shade.setColor(0xa60b111c);
                wallpaper = new LayerDrawable(new Drawable[]{photo, shade});
            }
            root.setBackground(wallpaper);
        } catch (Exception | OutOfMemoryError e) {
            prefs.edit().remove("wallpaper").apply(); wallpaper = null; toast("Duvar kağıdı açılamadı; koyu temaya dönüldü.");
        }
    }
    /** Whatever input the OEM last used (this board records HDMI2), so the box opens the real source. */
    private String hdmiSource() {
        try {
            String recent = Settings.Global.getString(getContentResolver(), "input_source_recent_record");
            if (recent != null && recent.startsWith("HDMI")) return recent;
        } catch (RuntimeException e) { Log.w(TAG, "Giriş kaynağı okunamadı", e); }
        return "HDMI2";
    }
    private void hdmi() {
        ComponentName cn = new ComponentName("com.softwinner.awlivetv", "com.softwinner.awlivetv.MainActivity");
        try {
            ActivityInfo a = getPackageManager().getActivityInfo(cn, 0);
            boolean usable = a.exported && a.enabled && a.applicationInfo.enabled
                && (a.permission == null || checkSelfPermission(a.permission) == PackageManager.PERMISSION_GRANTED);
            Intent i = new Intent().setComponent(cn).putExtra("input_source", hdmiSource());
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (usable && launch(i)) return;
            toast("Giriş ekranı üreticiye özel; Cihaz ayarları açılıyor.");
            vendor("settings.MainSettingActivity", Settings.ACTION_SETTINGS);
        } catch (PackageManager.NameNotFoundException e) {
            toast("Giriş ekranı bulunamadı; Cihaz ayarları açılıyor.");
            vendor("settings.MainSettingActivity", Settings.ACTION_SETTINGS);
        }
    }
    /** Netflix'in uygulaması bu cihazda -13 veriyor (Widevine L1/keybox yok), web oynatıcı tek yol. */
    private void netflix() {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.netflix.com/tv2"));
        i.setClassName("com.phlox.tvwebbrowser", "com.phlox.tvwebbrowser.activity.main.MainActivity");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { startActivity(i); }
        catch (Exception e) { toast("Tarayıcı yok; Netflix uygulaması denenecek."); openAny("com.netflix.ninja"); }
    }
    private void openAny(String... packages) {
        PackageManager pm = getPackageManager();
        for (String pkg : packages) {
            Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (i == null) i = pm.getLaunchIntentForPackage(pkg);
            if (i != null && launch(i)) return;
        }
        toast("Bu kısayol bu cihazda açılamıyor.");
    }
    private boolean component(String cls) {
        ComponentName cn = new ComponentName(VENDOR, VENDOR + ".activity." + cls);
        try {
            ActivityInfo a = getPackageManager().getActivityInfo(cn, 0);
            if (!a.exported || !a.enabled || !a.applicationInfo.enabled || (a.permission != null && checkSelfPermission(a.permission) != PackageManager.PERMISSION_GRANTED)) return false;
            return launch(new Intent().setComponent(cn));
        } catch (PackageManager.NameNotFoundException | SecurityException e) { return false; }
    }
    private void vendor(String cls, String androidAction) {
        Log.i(TAG, "route:" + cls);
        if (component(cls)) return;
        if (("SupportActivity".equals(cls) || "WallPaperActivity".equals(cls)) && component("MainActivity")) {
            toast("Bu bölüm üretici ana ekranına özeldir. Destek / duvar kağıdı simgesini LuminaOS'tan seçin."); return;
        }
        if (!"settings.MainSettingActivity".equals(cls) && component("settings.MainSettingActivity")) {
            toast("Üretici bu ekranı dışa açmıyor. İlgili bölümü Cihaz ayarlarından seçin."); return;
        }
        if (launch(new Intent(androidAction))) return;
        if (launch(new Intent().setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings")))) return;
        toast("Bu cihazda ilgili ayar ekranı açılamadı.");
    }
    private boolean launch(Intent i) {
        try { i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); Log.i(TAG, "launch:" + i.toUri(0)); return true; }
        catch (RuntimeException e) { Log.w(TAG, "Unavailable activity", e); return false; }
    }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }
    private void loadBundled() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.bundled_apps), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] fields = line.split("\t"); if (fields.length != 4) continue;
                int icon = getResources().getIdentifier(fields[2], "drawable", getPackageName());
                if (icon != 0) bundled.put(fields[0], new BundledApp(fields[1],icon,Long.parseLong(fields[3])));
            }
        } catch (Exception e) { Log.w(TAG,"Bundled icon catalog unavailable; normal package icons will be used",e); }
    }
    // Opt-in diagnostic only; normal HOME never starts this thread or reads process maps.
    private void diagnosticsIfRequested() {
        if (!getIntent().getBooleanExtra("memory_diagnostics", false)) return;
        new Thread(() -> {
            try {
                Set<String> seen = new HashSet<>();
                try (BufferedReader in = new BufferedReader(new FileReader("/proc/self/maps"))) {
                    String line;
                    while ((line = in.readLine()) != null) if (line.contains(".apk")) {
                        String path = line.substring(line.indexOf('/'));
                        if (seen.add(path)) Log.i(TAG, "apk-map:" + line);
                    }
                }
                LinkedHashMap<String,long[]> totals = new LinkedHashMap<>(); String path = null;
                try (BufferedReader in = new BufferedReader(new FileReader("/proc/self/smaps"))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        int dash = line.indexOf('-'), space = line.indexOf(' ');
                        if (dash > 0 && space > dash) {
                            path = line.contains(".apk") && line.contains("/") ? line.substring(line.indexOf('/')) : null;
                            if (path != null && !totals.containsKey(path)) totals.put(path, new long[2]);
                        } else if (path != null && (line.startsWith("Pss:") || line.startsWith("Rss:"))) {
                            totals.get(path)[line.startsWith("Pss:") ? 0 : 1] += Long.parseLong(line.trim().split("\\s+")[1]);
                        }
                    }
                }
                for (java.util.Map.Entry<String,long[]> e : totals.entrySet())
                    Log.i(TAG, "apk-memory:pssKb=" + e.getValue()[0] + ",rssKb=" + e.getValue()[1] + "," + e.getKey());
                Log.i(TAG, "apk-diagnostics-complete");
            } catch (Exception e) { Log.w(TAG, "Own-process maps unavailable", e); }
        }, "apk-maps").start();
    }

    /**
     * Tam ekran odak referansı. Projeksiyonun odak halkası manuel olduğu için üç desen var:
     * 1) odak yıldızı ve köşe artıları, 2) punto merdiveni, 3) ızgara ve kenar çizgileri.
     * Hiç görsel varlık kullanmaz; her şey Canvas ile vektörel çizilir, bu yüzden 1080p'de tam keskin.
     */
    private static final class FocusView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path shape = new Path();
        private final float density;
        private int mode;

        FocusView(Context context) {
            super(context);
            density = context.getResources().getDisplayMetrics().density;
            setBackgroundColor(0xff000000);
            setFocusable(true);
            setFocusableInTouchMode(true);
            setContentDescription("Odak testi deseni");
        }

        private float d(float value) { return value * density; }
        private void white(float strokeWidth) {
            p.setColor(0xffffffff); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(d(strokeWidth));
        }
        private void solid() { p.setColor(0xffffffff); p.setStyle(Paint.Style.FILL); }

        @Override protected void onDraw(Canvas canvas) {
            int w = getWidth(), h = getHeight();
            if (w < 2 || h < 2) return;
            canvas.drawColor(0xff000000);
            p.setTypeface(Typeface.SANS_SERIF);
            if (mode == 1) wordPattern(canvas, w, h);
            else if (mode == 2) gridPattern(canvas, w, h);
            else starPattern(canvas, w, h);
            footer(canvas, w, h);
        }

        private void footer(Canvas c, int w, int h) {
            p.setStyle(Paint.Style.FILL); p.setColor(0xff000000);
            c.drawRect(0, h - d(46), w, h, p);
            p.setColor(0xff8fa3b8); p.setTextSize(d(11)); p.setTextAlign(Paint.Align.CENTER);
            c.drawText("OK  Deseni değiştir      Geri  Projeksiyon ayarları      Desen " + (mode + 1) + "/3",
                w / 2f, h - d(15), p);
        }

        private void cross(Canvas c, float x, float y, float arm, float width) {
            white(width);
            c.drawLine(x - arm, y, x + arm, y, p);
            c.drawLine(x, y - arm, x, y + arm, p);
        }

        private void label(Canvas c, String value, float x, float y, Paint.Align align) {
            p.setStyle(Paint.Style.FILL); p.setColor(0xffb9c8da);
            p.setTextSize(d(10)); p.setTextAlign(align);
            c.drawText(value, x, y + d(4), p);
        }

        /** Desen 1: ortada odak yıldızı; köşe/kenar artıları ve ince çizgi demetleri. */
        private void starPattern(Canvas c, int w, int h) {
            float bottom = h - d(46), cx = w / 2f, cy = bottom / 2f, r = Math.min(w, bottom) * 0.35f;
            solid();
            int spokes = 36;
            shape.reset();
            for (int i = 0; i < spokes; i += 2) {
                double a = Math.PI * 2 * i / spokes, b = Math.PI * 2 * (i + 1) / spokes;
                shape.moveTo(cx, cy);
                shape.lineTo(cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r);
                shape.lineTo(cx + (float) Math.cos(b) * r, cy + (float) Math.sin(b) * r);
                shape.close();
            }
            c.drawPath(shape, p);
            white(2); c.drawCircle(cx, cy, r * 1.06f, p);
            white(1); c.drawCircle(cx, cy, r * 1.12f, p); c.drawCircle(cx, cy, r * 0.05f, p);
            float m = d(38), arm = d(28);
            float[] xs = {m, w - m, cx, cx, m, w - m};
            float[] ys = {m, m, m, bottom - m, cy, cy};
            for (int i = 0; i < xs.length; i++) cross(c, xs[i], ys[i], arm, 1.5f);
            for (int i = 0; i < 5; i++) {
                white(1 + i * 0.5f);
                float x = d(18) + i * d(7);
                c.drawLine(x, d(84), x, bottom - d(84), p);
                c.drawLine(w - x, d(84), w - x, bottom - d(84), p);
            }
            label(c, "SOL ÜST", m + arm + d(10), m, Paint.Align.LEFT);
            label(c, "SAĞ ÜST", w - m - arm - d(10), m, Paint.Align.RIGHT);
            label(c, "SOL ALT", m + arm + d(10), bottom - m, Paint.Align.LEFT);
            label(c, "SAĞ ALT", w - m - arm - d(10), bottom - m, Paint.Align.RIGHT);
            white(2);
            float f = d(12), len = d(46);
            c.drawLine(f, f, f + len, f, p); c.drawLine(f, f, f, f + len, p);
            c.drawLine(w - f - len, f, w - f, f, p); c.drawLine(w - f, f, w - f, f + len, p);
            c.drawLine(f, bottom - f, f + len, bottom - f, p); c.drawLine(f, bottom - f, f, bottom - f - len, p);
            c.drawLine(w - f - len, bottom - f, w - f, bottom - f, p); c.drawLine(w - f, bottom - f, w - f, bottom - f - len, p);
        }

        /** Desen 2: punto merdiveni; en küçük satırın okunması gerçek netliği gösterir. */
        private void wordPattern(Canvas c, int w, int h) {
            float bottom = h - d(46), x = d(56), top = d(58);
            String[][] rows = {
                {"30", "HAFİF TV ODAK TESTİ"},
                {"24", "Türkçe: ı I i İ ş Ş ğ Ğ ü Ü ö Ö ç Ç"},
                {"19", "Kenar ve orta aynı keskinlikte olmalı."},
                {"15", "15 punto satır: harflerin içi açık mı?"},
                {"12", "12 punto: küçük ayrıntılar seçilebiliyor mu?"},
                {"9", "9 punto: ğüşiç ölçü 1 mm çizgi"},
                {"7", "7 punto: bu satır yalnız odak doğruysa okunur"}};
            float step = (bottom - d(74) - top) / (rows.length - 1);
            p.setStyle(Paint.Style.FILL); p.setColor(0xffffffff); p.setTextAlign(Paint.Align.LEFT);
            float y = top;
            for (String[] row : rows) {
                p.setTextSize(d(Integer.parseInt(row[0])));
                c.drawText(row[1], x, y, p);
                y += step;
            }
            float rx = w * 0.60f, ry = d(76);
            p.setTextSize(d(9)); p.setColor(0xff8fa3b8);
            c.drawText("SAĞ ÜST KÖŞE ÖRNEKLERİ", rx, ry, p);
            ry += d(30);
            int[] samples = {16, 13, 11, 9, 8};
            for (int sample : samples) {
                p.setColor(0xffffffff); p.setTextSize(d(sample));
                c.drawText(sample + " punto · Hafif TV · ışık", rx, ry, p);
                ry += d(sample * 1.9f + 14);
            }
            p.setColor(0xffffffff); p.setTextSize(d(9));
            c.drawText("9 punto · sağ alt köşe", rx, bottom - d(62), p);
            p.setTextSize(d(7));
            c.drawText("7 punto · sağ alt köşe", rx, bottom - d(38), p);
            white(1);
            for (float gy = bottom - d(34); gy < bottom - d(8); gy += d(4)) c.drawLine(x, gy, w * 0.46f, gy, p);
        }

        /** Desen 3: ızgara, dama kenar şeridi ve köşe çizgi çiftleri. */
        private void gridPattern(Canvas c, int w, int h) {
            float bottom = h - d(46), step = d(24);
            white(1);
            for (float gx = step; gx < w; gx += step) c.drawLine(gx, 0, gx, bottom, p);
            for (float gy = step; gy < bottom; gy += step) c.drawLine(0, gy, w, gy, p);
            solid();
            float box = d(16);
            for (float gx = 0; gx < w; gx += box * 2) {
                c.drawRect(gx, 0, gx + box, box, p);
                c.drawRect(gx, bottom - box, gx + box, bottom, p);
            }
            for (float gy = 0; gy < bottom; gy += box * 2) {
                c.drawRect(0, gy, box, gy + box, p);
                c.drawRect(w - box, gy, w, gy + box, p);
            }
            float[][] corners = {{d(32), d(104)}, {w - d(32) - d(60), d(104)},
                {d(32), bottom - d(104) - d(56)}, {w - d(32) - d(60), bottom - d(104) - d(56)}};
            float[] gaps = {d(1), d(2), d(3), d(5), d(8), d(12)};
            for (float[] corner : corners) {
                float px = corner[0];
                for (int i = 0; i < gaps.length; i++) {
                    white(i < 3 ? 1 : 1.5f);
                    c.drawLine(px, corner[1], px, corner[1] + d(56), p);
                    px += gaps[i];
                }
            }
            p.setStyle(Paint.Style.FILL); p.setColor(0xff000000); p.setTextSize(d(22));
            p.setTextAlign(Paint.Align.CENTER);
            float tw = p.measureText("IZGARA · KENAR NETLİĞİ");
            c.drawRect(w / 2f - tw / 2 - d(16), bottom / 2f - d(32), w / 2f + tw / 2 + d(16), bottom / 2f + d(14), p);
            p.setColor(0xffffffff);
            c.drawText("IZGARA · KENAR NETLİĞİ", w / 2f, bottom / 2f, p);
        }

        @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) return true;
            return super.onKeyDown(keyCode, event);
        }

        @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SPACE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                mode = (mode + 1) % 3; invalidate(); return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { mode = (mode + 2) % 3; invalidate(); return true; }
            return super.onKeyUp(keyCode, event);
        }
    }
}
