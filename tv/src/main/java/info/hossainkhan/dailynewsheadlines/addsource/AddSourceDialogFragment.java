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

package info.hossainkhan.dailynewsheadlines.addsource;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.text.InputType;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.util.List;

import info.hossainkhan.android.core.CoreApplication;
import info.hossainkhan.dailynewsheadlines.R;
import timber.log.Timber;

/**
 * A dialog fragment with positive and negative options.
 */
public class AddSourceDialogFragment extends GuidedStepFragment {
    /**
     * Unique screen name used for reporting and analytics.
     */
    private static final String ANALYTICS_SCREEN_NAME = "news_source_new";

    private static final int ACTION_ID_SOURCE_NAME = 1;
    private static final int ACTION_ID_SOURCE_FEED_URL = ACTION_ID_SOURCE_NAME + 1;

    /**
     * Creates new add news source dialog fragment.
     *
     * @return Creates and instance of this fragment.
     */
    public static AddSourceDialogFragment newInstance() {
        return new AddSourceDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        CoreApplication.getAnalyticsReporter().reportScreenLoadedEvent(ANALYTICS_SCREEN_NAME);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(R.string.add_news_source_feed_guidance_title);
        String description = getString(R.string.add_news_source_feed_guidance_description);

        GuidanceStylist.Guidance guidance = new GuidanceStylist.Guidance(title, description,
                null, null);
        return guidance;
    }

    @Override
    public void onCreateActions(List<GuidedAction> actions, Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_SOURCE_NAME)
                .title(R.string.add_news_source_feed_input_name)
                .icon(R.drawable.vector_icon_format_title)
                .editTitle("")
                .description(R.string.add_news_source_feed_input_name)
                .editDescription(R.string.add_news_source_feed_input_name)
                .editable(true)
                .editInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .build()
        );

        actions.add(new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_SOURCE_FEED_URL)
                .title(R.string.add_news_source_feed_input_url)
                .icon(R.drawable.vector_icon_rss)
                .editTitle("http://www.")
                .description(R.string.add_news_source_feed_input_url)
                .editDescription(R.string.add_news_source_feed_input_url)
                .editable(true)
                .editInputType(InputType.TYPE_TEXT_VARIATION_URI)
                .build()
        );
    }

    @Override
    public void onCreateButtonActions(@NonNull List<GuidedAction> actions,
                                      Bundle savedInstanceState) {
        actions.add(new GuidedAction.Builder(getActivity())
                .clickAction(GuidedAction.ACTION_ID_OK)
                .build()
        );
        actions.get(actions.size() - 1).setEnabled(false); // Disable OK, can't be enabled without validation
        actions.add(new GuidedAction.Builder(getActivity())
                .clickAction(GuidedAction.ACTION_ID_CANCEL)
                .build()
        );
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (GuidedAction.ACTION_ID_OK == action.getId()) {
            String name = findActionById(ACTION_ID_SOURCE_NAME).getEditTitle().toString();
            String url = findActionById(ACTION_ID_SOURCE_FEED_URL).getEditTitle().toString();
            GuidedStepFragment fragment = ValidateNewsSourceDialogFragment.newInstance(name, url);
            add(getFragmentManager(), fragment);
        } else if (GuidedAction.ACTION_ID_CANCEL == action.getId()) {
            getActivity().finish();
        } else {
            Timber.w("Action %s not supported.", action);
        }
    }

    @Override
    public long onGuidedActionEditedAndProceed(GuidedAction action) {

        boolean validSourceName;
        boolean validSourceUrl;

        if (action.getId() == ACTION_ID_SOURCE_NAME) {
            CharSequence newsSourceName = action.getEditTitle();
            validSourceName = isValidNewsSourceName(newsSourceName);
            validSourceUrl = isValidUrl(findActionById(ACTION_ID_SOURCE_FEED_URL).getEditTitle().toString());

            updateOkButton(validSourceName && validSourceUrl);

            if (!validSourceName) {
                action.setDescription(getString(R.string.add_news_source_feed_input_name_empty_error));
                return GuidedAction.ACTION_ID_CURRENT;
            } else {
                action.setDescription(newsSourceName);
                return GuidedAction.ACTION_ID_NEXT;
            }

        } else if (action.getId() == ACTION_ID_SOURCE_FEED_URL) {
            String feedUrl = action.getEditTitle().toString();
            // When entering URL, sometimes there is unintended spaces, clean those
            feedUrl = feedUrl.replaceAll("\\s+", "");
            validSourceUrl = isValidUrl(feedUrl);
            validSourceName = isValidNewsSourceName(findActionById(ACTION_ID_SOURCE_NAME)
                    .getEditTitle());
            updateOkButton(validSourceName && validSourceUrl);

            if (!validSourceUrl) {
                action.setDescription(getString(R.string.add_news_source_feed_input_invalid_url_error));
                return GuidedAction.ACTION_ID_CURRENT;
            } else {
                action.setDescription(feedUrl);
                action.setEditTitle(feedUrl);  // Updates with valid URL
                return GuidedAction.ACTION_ID_NEXT;
            }
        }
        return GuidedAction.ACTION_ID_CURRENT;
    }

    private void updateOkButton(boolean enabled) {
        findButtonActionById(GuidedAction.ACTION_ID_OK).setEnabled(enabled);
        notifyButtonActionChanged(findButtonActionPositionById(GuidedAction.ACTION_ID_OK));
    }

    private static boolean isValidUrl(String input) {
        return URLUtil.isValidUrl(input);
    }

    private static boolean isValidNewsSourceName(CharSequence input) {
        return !TextUtils.isEmpty(input) && input.length() > 1;
    }


}
