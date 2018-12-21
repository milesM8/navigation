package com.navigation.reactnative;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;

import java.util.HashMap;
import java.util.HashSet;

public class NavigationModule extends ReactContextBaseJavaModule {
    private HashMap<Integer, Intent> mIntents = new HashMap<>();
    private int activityOpenEnterAnimationId;
    private int activityOpenExitAnimationId;
    private int activityCloseEnterAnimationId;
    private int activityCloseExitAnimationId;

    public NavigationModule(ReactApplicationContext reactContext) {
        super(reactContext);

        TypedArray activityStyle = getReactApplicationContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowAnimationStyle});
        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
        activityStyle.recycle();

        activityStyle = getReactApplicationContext().getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[] {
            android.R.attr.activityOpenEnterAnimation, android.R.attr.activityOpenExitAnimation,
            android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation
        });
        activityOpenEnterAnimationId = activityStyle.getResourceId(0, 0);
        activityOpenExitAnimationId = activityStyle.getResourceId(1, 0);
        activityCloseEnterAnimationId = activityStyle.getResourceId(2, 0);
        activityCloseExitAnimationId = activityStyle.getResourceId(3, 0);
        activityStyle.recycle();
    }

    @Override
    public String getName() {
        return "NavigationModule";
    }

    @SuppressLint("NewApi")
    @ReactMethod
    public void render(int crumb, int tab, ReadableArray titles, String appKey, String enterAnim, String exitAnim) {
        final Activity currentActivity = getCurrentActivity();
        if (mIntents.size() == 0) {
            mIntents.put(0, currentActivity.getIntent());
        }
        int currentCrumb = mIntents.size() - 1;
        if (crumb < currentCrumb) {
            final Intent intent = mIntents.get(crumb);
            for(int i = crumb + 1; i <= currentCrumb; i++) {
                mIntents.remove(i);
            }
            final int enter = this.getAnimationResourceId(enterAnim, this.activityCloseEnterAnimationId);
            final int exit = this.getAnimationResourceId(exitAnim, this.activityCloseExitAnimationId);
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentCrumb - crumb == 1)
                        currentActivity.finishAfterTransition();
                    else
                        currentActivity.navigateUpTo(intent);
                    currentActivity.overridePendingTransition(enter, exit);
                }
            });
        }
        if (crumb > currentCrumb) {
            final Intent[] intents = new Intent[crumb - currentCrumb];
            for(int i = 0; i < crumb - currentCrumb; i++) {
                int nextCrumb = currentCrumb + i + 1;
                Class scene = nextCrumb % 2 == 0 ? SceneActivity.class : AlternateSceneActivity.class;
                Intent intent = new Intent(getReactApplicationContext(), scene);
                intent.putExtra(SceneActivity.CRUMB, nextCrumb);
                intent.putExtra(SceneActivity.APP_KEY, appKey);
                mIntents.put(nextCrumb, intent);
                intents[i] = intent;
            }
            final int enter = this.getAnimationResourceId(enterAnim, this.activityOpenEnterAnimationId);
            final int exit = this.getAnimationResourceId(exitAnim, this.activityOpenExitAnimationId);
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Pair[] sharedElements = crumb - currentCrumb == 1 ? getSharedElements() : null;
                    if (sharedElements != null) {
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(currentActivity, sharedElements).toBundle();
                        currentActivity.startActivity(intents[0], bundle);
                    } else {
                        currentActivity.startActivities(intents);
                    }
                    currentActivity.overridePendingTransition(enter, exit);
                }
            });
        }
    }

    private int getAnimationResourceId(String animationName, int defaultId) {
        if (animationName == null)
            return defaultId;
        String packageName = getReactApplicationContext().getPackageName();
        return getReactApplicationContext().getResources().getIdentifier(animationName, "anim", packageName);
    }

    @SuppressLint("NewApi")
    private Pair[] getSharedElements() {
        View rootView = getCurrentActivity().findViewById(android.R.id.content);
        HashSet<View> sharedElements = (HashSet<View>) rootView.getTag(R.id.sharedElements);
        if (sharedElements == null)
            return null;
        Pair[] sharedElementPairs = new Pair[sharedElements.size()];
        int size = 0;
        for(View sharedElement : sharedElements) {
            sharedElementPairs[size] = Pair.create(sharedElement, sharedElement.getTransitionName());
            size++;
        }
        return sharedElementPairs;
    }
}

