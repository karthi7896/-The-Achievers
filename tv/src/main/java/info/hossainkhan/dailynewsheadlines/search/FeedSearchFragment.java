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

package info.hossainkhan.dailynewsheadlines.search;

import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import info.hossainkhan.android.core.model.NewsHeadlineItem;
import info.hossainkhan.android.core.search.SearchContract;
import info.hossainkhan.android.core.search.SearchPresenter;
import info.hossainkhan.android.core.util.StringUtils;
import info.hossainkhan.dailynewsheadlines.R;
import info.hossainkhan.dailynewsheadlines.addsource.ValidateNewsSourceDialogFragment;
import info.hossainkhan.dailynewsheadlines.browser.RowBuilderFactory;
import rx.Emitter;
import rx.Observable;
import timber.log.Timber;

public class FeedSearchFragment extends SearchFragment implements SearchContract.View {

    private ArrayObjectAdapter mRowsAdapter;
    private SearchPresenter mPresenter;
    private NewsHeadlineItem mSelectedNewsHeadlineItem;

    /**
     * Creates new instance of this fragment.
     *
     * @return Fragment instance.
     */
    public static FeedSearchFragment newInstance() {
        FeedSearchFragment fragment = new FeedSearchFragment();

        Bundle args = new Bundle();
        // Place where we set additional arguments.
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        final SearchQueryObserver searchQueryObserver = new SearchQueryObserver(this, mRowsAdapter);
        setOnItemViewClickedListener((itemViewHolder, item, rowViewHolder, row) -> {
            Timber.d("onItemClicked() called with: itemViewHolder = [" + itemViewHolder + "], item = [" + item +
                    "], rowViewHolder = [" + rowViewHolder + "], row = [" + row + "]");

            if(item instanceof NewsHeadlineItem) {
                mSelectedNewsHeadlineItem = (NewsHeadlineItem) item;
                if(StringUtils.isNotEmpty(mSelectedNewsHeadlineItem.getContentUrl())) {
                    GuidedStepFragment fragment = ValidateNewsSourceDialogFragment
                            .newInstance(mSelectedNewsHeadlineItem.getTitle(), mSelectedNewsHeadlineItem.getContentUrl());
                    GuidedStepFragment.add(getFragmentManager(), fragment);
                } else {
                    Toast.makeText(getActivity(), R.string.search_result_no_feed_url, Toast.LENGTH_SHORT).show();
                }
            } else {
                Timber.w("Unable to process item. Type unknown: %s", item);
            }

        });
        mPresenter = new SearchPresenter(this, searchQueryObserver.getSearchQueryObservable());
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume() - past items selected: %s", mSelectedNewsHeadlineItem);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Timber.d("setUserVisibleHint() called with %s", isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }


    @Override
    public void toggleLoadingIndicator(final boolean shouldShow) {
        Timber.d("toggleLoadingIndicator() called with: shouldShow = [%s]", shouldShow);
    }

    @Override
    public void showNoSearchResults() {
        Timber.d("showNoSearchResults() called.");
    }

    @Override
    public void showSearchResults(final List<NewsHeadlineItem> newsHeadlineItems) {
        Timber.d("Found search items. Total: %d", newsHeadlineItems.size());
        mRowsAdapter.clear();
        mRowsAdapter.add(RowBuilderFactory.buildSearchResultCardRow(getActivity().getApplicationContext(),
                newsHeadlineItems));
        mRowsAdapter.notifyArrayItemRangeChanged(0, newsHeadlineItems.size());
    }

    public static class SearchQueryObserver {
        private final WeakReference<FeedSearchFragment> mSearchFragmentWeakRef;
        private final ArrayObjectAdapter mRowsAdapter;

        public SearchQueryObserver(FeedSearchFragment searchFragment, ArrayObjectAdapter rowsAdapter) {
            mSearchFragmentWeakRef = new WeakReference<FeedSearchFragment>(searchFragment);
            mRowsAdapter = rowsAdapter;
        }

        /**
         * Converts {@link SearchFragment.SearchResultProvider} to {@link Observable<String>} of search query.
         * <p>
         * References: <br>
         * https://medium.com/yammer-engineering/converting-callback-async-calls-to-rxjava-ebc68bde5831#.mr6g4k5gz
         * http://www.pacoworks.com/2016/08/21/this-is-not-an-rxjava-tutorial/
         *
         * @return Observable that provides stream of search query from user.
         */
        public Observable<String> getSearchQueryObservable() {
            return Observable.fromEmitter(emitter -> {
                SearchResultProvider searchListener = new SearchResultProvider() {
                    @Override
                    public ObjectAdapter getResultsAdapter() {
                        return mRowsAdapter;
                    }

                    @Override
                    public boolean onQueryTextChange(final String newQuery) {
                        Timber.d("onQueryTextChange() called with: newQuery = [%s]", newQuery);
                        emitter.onNext(newQuery);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextSubmit(final String query) {
                        Timber.d("onQueryTextSubmit() called with: query = [%s]", query);
                        emitter.onNext(query);
                        return true;
                    }
                };

                FeedSearchFragment feedSearchFragment = mSearchFragmentWeakRef.get();
                if (feedSearchFragment != null) {
                    feedSearchFragment.setSearchResultProvider(searchListener);
                }

                // (Rx Contract # 1) - unregistering listener when unsubscribed
                emitter.setCancellation(() -> {
                    FeedSearchFragment feedSearchFragment1 = mSearchFragmentWeakRef.get();
                    if (feedSearchFragment1 != null) {
                        Timber.d("cancel() SearchResultProvider callback listener");
                        // NOTE: Setting litener to null crashes on SearchFragment$3.run(SearchFragment.java:165)
                        //feedSearchFragment.setSearchResultProvider(null);
                    } else {
                        Timber.w("FeedSearchFragment is already destroyed.");
                    }
                });
            }, Emitter.BackpressureMode.BUFFER);
        }
    }

}

