package com.reactnativenavigation.activities;

import android.widget.FrameLayout;

import com.reactnativenavigation.R;
import com.reactnativenavigation.core.RctManager;
import com.reactnativenavigation.core.objects.Screen;
import com.reactnativenavigation.utils.StyleHelper;
import com.reactnativenavigation.views.RnnToolBar;
import com.reactnativenavigation.views.ScreenStack;

/**
 * Created by guyc on 05/04/16.
 */
public class SingleScreenActivity extends BaseReactActivity {

    public static final String EXTRA_SCREEN = "extraScreen";

    private ScreenStack mScreenStack;
    private String mNavigatorId;

    @Override
    protected void handleOnCreate() {
        mReactInstanceManager = RctManager.getInstance().getReactInstanceManager();

        setContentView(R.layout.single_screen_activity);
        mToolbar = (RnnToolBar) findViewById(R.id.toolbar);

        Screen screen = (Screen) getIntent().getSerializableExtra(EXTRA_SCREEN);
        mNavigatorId = screen.navigatorId;
        setupToolbar(screen);

        mScreenStack = new ScreenStack(this);
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.contentFrame);
        assert contentFrame != null;
        contentFrame.addView(mScreenStack);
        mScreenStack.push(screen);
    }

    protected void setupToolbar(Screen screen) {
        mToolbar.update(screen);
        StyleHelper.updateStyles(mToolbar, screen);
    }
    
    @Override
    public void push(Screen screen) {
        super.push(screen);
        mScreenStack.push(screen);
        StyleHelper.updateStyles(mToolbar, screen);
    }

    @Override
    public Screen pop(String navigatorId) {
        super.pop(navigatorId);
        Screen screen = mScreenStack.pop();
        StyleHelper.updateStyles(mToolbar, screen);
        return screen;
    }

    @Override
    public String getCurrentNavigatorId() {
        return mNavigatorId;
    }

    @Override
    protected Screen getCurrentScreen() {
        Screen currentScreen = super.getCurrentScreen();
        if (currentScreen != null) {
            return currentScreen;
        }

        return mScreenStack.peek();
    }

    @Override
    public int getScreenStackSize() {
        return mScreenStack.getStackSize();
    }
}
