package com.reactnativenavigation.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;

import com.reactnativenavigation.R;
import com.reactnativenavigation.activities.BaseReactActivity;
import com.reactnativenavigation.core.objects.Button;
import com.reactnativenavigation.core.objects.Screen;
import com.reactnativenavigation.utils.ContextProvider;
import com.reactnativenavigation.utils.ImageUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guyc on 09/04/16.
 */
public class RnnToolBar extends Toolbar {

    private List<Screen> mScreens;
    private AsyncTask mSetupToolbarTask;
    private Drawable mBackground;

    public RnnToolBar(Context context) {
        super(context);
        init();
    }

    public RnnToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RnnToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBackground = getBackground();
    }

    public void setScreens(List<Screen> screens) {
        mScreens = screens;
    }

    public void setStyle(Screen screen) {
        if (screen.toolBarColor != null) {
            setBackgroundColor(screen.toolBarColor);
        } else {
            resetBackground();
        }

        if (screen.titleColor != null) {
            setTitleTextColor(screen.titleColor);
        } else {
            resetTitleTextColor();
        }
        
        if (screen.toolBarHidden != null && screen.toolBarHidden) {
            hideToolbar();
        } else {
            showToolbar();
        }
    }

    private void resetBackground() {
        setBackground(mBackground);
    }

    private void resetTitleTextColor() {
        setTitleTextColor(ContextCompat.getColor(getContext(), android.R.color.primary_text_light));
    }

    public void handleOnCreateOptionsMenuAsync() {
        setupToolbarButtonsAsync(null, mScreens.get(0));
    }

    public void setupToolbarButtonsAsync(Screen newScreen) {
        this.setupToolbarButtonsAsync(null, newScreen);
    }


    public void setupToolbarButtonsAsync(Screen oldScreen, Screen newScreen) {
        if (mSetupToolbarTask == null) {
            mSetupToolbarTask = new SetupToolbarButtonsTask(this, oldScreen, newScreen).execute();
        }
    }

    private void showToolbar() {
        ActionBar actionBar = ContextProvider.getActivityContext().getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }

    private void hideToolbar() {
        ActionBar actionBar = ContextProvider.getActivityContext().getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    public void showBackButton(Screen screen) {
        ActionBar actionBar = ContextProvider.getActivityContext().getSupportActionBar();
        Drawable backButton = setupBackButton(screen);
        actionBar.setHomeAsUpIndicator(backButton);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("PrivateResource")
    @SuppressWarnings({"ConstantConditions"})
    private Drawable setupBackButton(Screen screen) {
        Resources resources = getResources();
        final Drawable backButton;
        if (screen.buttonsTintColor != null) {
            backButton = ResourcesCompat.getDrawable(resources,
                    R.drawable.abc_ic_ab_back_mtrl_am_alpha,
                    null);
            ImageUtils.tint(backButton, screen.buttonsTintColor);
        } else {
            backButton = ResourcesCompat.getDrawable(resources,
                    R.drawable.abc_ic_ab_back_mtrl_am_alpha,
                    ContextProvider.getActivityContext().getTheme());
        }
        return backButton;
    }

    @SuppressWarnings({"ConstantConditions"})
    public void hideBackButton() {
        ContextProvider.getActivityContext().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    /**
     * Update the ToolBar from screen. This function sets any properties that are defined
     * in the screen.
     * @param screen The currently displayed screen
     */
    @UiThread
    public void update(Screen screen) {
        ((AppCompatActivity) getContext()).setSupportActionBar(this);
        setTitle(screen.title);
        setStyle(screen);
    }

    public void updateAndSetButtons(Screen screen) {
        update(screen);
        setupToolbarButtonsAsync(screen);
    }

    private static class SetupToolbarButtonsTask extends AsyncTask<Void, Void, Map<String, Drawable>> {
        private final List<Button> mOldButtons;
        private final List<Button> mNewButtons;
        private final WeakReference<RnnToolBar> mToolbarWR;
        private final Integer mTintColor;
        private final int mIconDimensions;

        public SetupToolbarButtonsTask(RnnToolBar toolBar, Screen oldScreen, Screen newScreen) {
            mToolbarWR = new WeakReference<>(toolBar);
            mOldButtons = oldScreen == null ? null : oldScreen.getButtons();
            mNewButtons = newScreen.getButtons();
            mTintColor = newScreen.buttonsTintColor;
            mIconDimensions = (int) (toolBar.getHeight() * 0.4f);
        }

        @Override
        protected Map<String, Drawable> doInBackground(Void... params) {
            Context context = ContextProvider.getActivityContext();
            if (context == null) {
                return null;
            }

            Map<String, Drawable> icons = new HashMap<>();
            for (Button button : mNewButtons) {
                if (button.hasIcon()) {
                    icons.put(button.id, button.getIcon(context, mIconDimensions));
                }
            }
            return icons;
        }

        @Override
        protected void onPostExecute(Map<String, Drawable> icons) {
            Context context = ContextProvider.getActivityContext();
            if (context == null) {
                return;
            }

            Menu menu = ((BaseReactActivity) context).getMenu();

            if (menu == null) {
                RnnToolBar toolBar = mToolbarWR.get();
                if (toolBar != null) {
                    toolBar.mSetupToolbarTask = null;
                }
                mToolbarWR.clear();
                return;
            }

            // Remove prev screen buttons
            if(mOldButtons == null) {
                menu.clear();
            } else {
                for (Button btn : mOldButtons) {
                    menu.removeItem(btn.getItemId());
                }
            }

            // Add new screen buttons
            for (int i = 0; i < mNewButtons.size(); i++) {
                Button button = mNewButtons.get(i);
                MenuItem item = menu.add(Menu.NONE, button.getItemId(), i, button.title);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                if (icons.containsKey(button.id)) {
                    Drawable icon = icons.get(button.id);
                    if (mTintColor != null) {
                        ImageUtils.tint(icon, mTintColor);
                    }
                    item.setIcon(icon);
                }

                if (button.disabled) {
                    item.setEnabled(false);
                }
            }

            RnnToolBar toolBar = mToolbarWR.get();
            if (toolBar != null) {
                if (mTintColor != null) {
                    ImageUtils.tint(toolBar.getOverflowIcon(), mTintColor);
                }

                toolBar.mSetupToolbarTask = null;
                mToolbarWR.clear();
            }
        }
    }
}
