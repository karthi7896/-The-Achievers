/*
 * MIT License
 *
 * Copyright (c) 2016 Hossain Khan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package info.hossainkhan.dailynewsheadlines.about;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;

import info.hossainkhan.android.core.model.ScreenType;
import info.hossainkhan.android.core.util.Validate;
import info.hossainkhan.dailynewsheadlines.addsource.ManageNewsSourceDialogFragment;
import timber.log.Timber;


/**
 * Activity that is used to show different kind of information using
 * {@link android.support.v17.leanback.app.GuidedStepFragment}. Based on requirement this activity uses
 * {@link info.hossainkhan.dailynewsheadlines.R.style#Theme_Leanback_GuidedStep}.
 * <p>For list of supported info, see {@link ScreenType}</p>
 * <p>
 * See https://developer.android.com/reference/android/support/v17/leanback/app/GuidedStepFragment.html
 */
public class DisplayInfoActivity extends Activity {

    private static final String INTENT_KEY_INFO_DIALOG_TYPE = "KEY_INFO_DIALOG_TYPE";



    private ScreenType mDialogType;

    /**
     * Creates a start intent for this activity.
     *
     * @param context    App context.
     * @param type {@link ScreenType}
     * @return Intent
     */
    public static Intent createStartIntent(@NonNull Context context, @NonNull ScreenType type) {
        Validate.notNull(type);
        Intent intent = new Intent(context, DisplayInfoActivity.class);
        intent.putExtra(INTENT_KEY_INFO_DIALOG_TYPE, type);
        return intent;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set content view is not required because we are adding fragment to "android.R.id.content"

        extractBundleData(getIntent());

        if (savedInstanceState == null) {
            GuidedStepFragment.addAsRoot(
                    DisplayInfoActivity.this,
                    getFragmentForType(mDialogType),
                    android.R.id.content);
        }
    }

    /**
     * Internal method to extract bundle data and prepare required local instances.
     */
    private void extractBundleData(final Intent intent) {
        mDialogType = (ScreenType) intent.getSerializableExtra(INTENT_KEY_INFO_DIALOG_TYPE);

        // Intent data must be available to continue
        Validate.notNull(mDialogType);
        Timber.d("Got screen info type: %s", mDialogType);
    }


    private GuidedStepFragment getFragmentForType(final ScreenType type) {
        GuidedStepFragment fragment = null;
        switch (type) {
            case ABOUT_APPLICATION:
                fragment = AboutAppFragment.newInstance();
                break;
            case ABOUT_CONTRIBUTION:
                fragment = ContributionInfoFragment.newInstance();
                break;
            case MANAGE_NEWS_SOURCE:
                fragment = ManageNewsSourceDialogFragment.newInstance();
                break;
            default:
                throw new UnsupportedInformationTypeException();
        }

        return fragment;
    }

    public static class UnsupportedInformationTypeException extends RuntimeException {
        UnsupportedInformationTypeException() {
            super("Provided information type is not supported.");
        }
    }
}
