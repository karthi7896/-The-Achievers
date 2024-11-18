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

package info.hossainkhan.dailynewsheadlines.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.FullWidthDetailsOverviewSharedElementHelper;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import info.hossainkhan.android.core.CoreApplication;
import info.hossainkhan.android.core.headlines.HeadlinesDetailsContract;
import info.hossainkhan.android.core.headlines.HeadlinesDetailsViewPresenter;
import info.hossainkhan.android.core.model.NewsHeadlineItem;
import info.hossainkhan.android.core.util.ActivityUtils;
import info.hossainkhan.dailynewsheadlines.R;
import info.hossainkhan.dailynewsheadlines.cards.CardListRow;
import info.hossainkhan.dailynewsheadlines.details.listeners.DetailsViewInteractionListener;
import info.hossainkhan.dailynewsheadlines.utils.PicassoBackgroundManager;
import info.hossainkhan.dailynewsheadlines.utils.PicassoImageTargetDetailsOverview;
import timber.log.Timber;

/**
 * Displays a card with more details using a {@link DetailsFragment}.
 */
public class HeadlinesDetailsFragment extends DetailsFragment implements HeadlinesDetailsContract.View {
    /**
     * Unique screen name used for reporting and analytics.
     */
    private static final String ANALYTICS_SCREEN_NAME = "headline_details";

    private ArrayObjectAdapter mRowsAdapter;
    private Context mApplicationContext;
    private HeadlinesDetailsActivity mAttachedHeadlinesActivity;
    private PicassoBackgroundManager mPicassoBackgroundManager;
    private PicassoImageTargetDetailsOverview mDetailsRowPicassoTarget;
    private HeadlinesDetailsViewPresenter mPresenter;
    private DetailsOverviewRow mDetailsOverview;


    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        Timber.d("onAttach() called with: context = [" + context + "]");
        mAttachedHeadlinesActivity = (HeadlinesDetailsActivity) context;
        mApplicationContext = getActivity().getApplicationContext();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        Timber.d("onAttach() called with: Activity = [%s]", activity);

        // Note, this callback is used in lower API, otherwise "onAttach(final Context)" is used
        if (mAttachedHeadlinesActivity == null) {
            mAttachedHeadlinesActivity = (HeadlinesDetailsActivity) activity;
            mApplicationContext = activity.getApplicationContext();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NewsHeadlineItem newsHeadlineItem = mAttachedHeadlinesActivity.getCardItem();
        mPicassoBackgroundManager = new PicassoBackgroundManager(mAttachedHeadlinesActivity);
        mPresenter = new HeadlinesDetailsViewPresenter(mApplicationContext, this, newsHeadlineItem);
        DetailsViewInteractionListener listener = new DetailsViewInteractionListener(mPresenter);
        setupEventListeners(listener);
    }

    @Override
    public void onStart() {
        super.onStart();
        CoreApplication.getAnalyticsReporter().reportScreenLoadedEvent(ANALYTICS_SCREEN_NAME);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
        if (null != mPicassoBackgroundManager) {
            Timber.d("onDestroy: " + mPicassoBackgroundManager.toString());
            mPicassoBackgroundManager.destroy();
        }
    }

    private void setupEventListeners(final DetailsViewInteractionListener listener) {
        setOnItemViewSelectedListener(listener);
        setOnItemViewClickedListener(listener);
    }


    @Override
    public void showHeadlineDetails(final NewsHeadlineItem newsHeadlineItem) {
        FullWidthDetailsOverviewRowPresenter rowPresenter = new FullWidthDetailsOverviewRowPresenter(
                new HeadlinesDetailsPresenter(getActivity())) {

            @Override
            protected RowPresenter.ViewHolder createRowViewHolder(ViewGroup parent) {
                // Customize Actionbar and Content by using custom colors.
                RowPresenter.ViewHolder viewHolder = super.createRowViewHolder(parent);

                View actionsView = viewHolder.view.
                        findViewById(R.id.details_overview_actions_background);
                actionsView.setBackgroundColor(getActivity().getResources().
                        getColor(R.color.detail_view_actionbar_background));

                View detailsView = viewHolder.view.findViewById(R.id.details_frame);
                detailsView.setBackgroundColor(
                        getResources().getColor(R.color.detail_view_background));
                return viewHolder;
            }
        };

        FullWidthDetailsOverviewSharedElementHelper mHelper = new FullWidthDetailsOverviewSharedElementHelper();
        mHelper.setSharedElementEnterTransition(getActivity(), getString(R.string.shared_transition_key_item_thumbnail));
        rowPresenter.setListener(mHelper);
        rowPresenter.setParticipatingEntranceTransition(false);
        prepareEntranceTransition();

        ListRowPresenter shadowDisabledRowPresenter = new ListRowPresenter();
        shadowDisabledRowPresenter.setShadowEnabled(false);

        // Setup PresenterSelector to distinguish between the different rows.
        ClassPresenterSelector rowPresenterSelector = new ClassPresenterSelector();
        rowPresenterSelector.addClassPresenter(DetailsOverviewRow.class, rowPresenter);
        rowPresenterSelector.addClassPresenter(CardListRow.class, shadowDisabledRowPresenter);
        rowPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(rowPresenterSelector);

        // Setup action and detail row.
        mDetailsOverview = new DetailsOverviewRow(newsHeadlineItem);
        // DEV NOTE: Without the image drawable, the details view occupies full width for texts.
        mDetailsOverview.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_loading_image));

        mRowsAdapter.add(mDetailsOverview);
        setAdapter(mRowsAdapter);


        // NOTE: Move this when data is loaded
        startEntranceTransition();
    }

    @Override
    public void loadDetailsImage(final String imageUrl) {
        mDetailsRowPicassoTarget = new PicassoImageTargetDetailsOverview(mApplicationContext, mDetailsOverview);
        Picasso.with(mApplicationContext)
                .load(imageUrl)
                .into(mDetailsRowPicassoTarget);

        mPicassoBackgroundManager.updateBackgroundWithDelay(
                imageUrl,
                PicassoBackgroundManager.TransformType.GREYSCALE);

    }

    @Override
    public void loadDefaultImage() {
        mPicassoBackgroundManager.updateBackgroundWithDelay();
    }


    @Override
    public void updateScreenTitle(final String title) {
        setTitle(title);
    }

    @Override
    public void openArticleWebUrl(final String contentUrl) {
        Intent intent = ActivityUtils.provideOpenWebUrlIntent(contentUrl);
        if (intent.resolveActivity(mApplicationContext.getPackageManager()) != null) {
            mApplicationContext.startActivity(intent);
        } else {
            String logMsg = "App does not have browser to show URL: %s.";
            Timber.w(logMsg, contentUrl);

            // NOTE: According to Google's guideline and test case "TV-WB", tv app
            // should not assume browser availability.
        }
    }
}
