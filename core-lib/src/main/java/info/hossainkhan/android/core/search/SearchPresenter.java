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

package info.hossainkhan.android.core.search;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import com.feedly.FeedlyApiClient;
import com.feedly.cloud.FeedItem;
import com.feedly.cloud.SearchApi;
import com.feedly.cloud.SearchResponse;

import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import info.hossainkhan.android.core.CoreConfig;
import info.hossainkhan.android.core.base.BasePresenter;
import info.hossainkhan.android.core.model.NewsHeadlineItem;
import info.hossainkhan.android.core.model.CardType;
import info.hossainkhan.android.core.util.ObjectUtils;
import info.hossainkhan.android.core.util.StringUtils;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Search presenter to search RSS/Atom feeds.
 */
public class SearchPresenter extends BasePresenter<SearchContract.View> implements SearchContract.Presenter {

    /**
     * Feed ID prefix from feedly.
     * @see FeedItem
     */
    private static final String FEED_RESOURCE_ID_PREFIX = "feed/";
    private final SearchApi mSearchApi;

    /**
     * Creates the presenter for search.
     *
     * @param view             The view for search.
     * @param searchObservable Observable which provides search query.
     */
    public SearchPresenter(SearchContract.View view, final Observable<String> searchObservable) {
        attachView(view);

        FeedlyApiClient feedlyApiClient = new FeedlyApiClient();
        mSearchApi = feedlyApiClient.createService(SearchApi.class);


        listenForSearchTesting(searchObservable);

    }

    private void listenForSearchTesting(final Observable<String> searchObservable) {
        Subscription subscription = searchObservable
                .debounce(CoreConfig.SEARCH_DELAY_MS, TimeUnit.MILLISECONDS)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(final String searchQueryText) {
                        return (searchQueryText != null) &&
                                (searchQueryText.length() >= CoreConfig.SEARCH_TEXT_MIN_LENGTH);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(final String searchQuery) {
                        Timber.d("Observable search query: %s", searchQuery);
                        onSearchTermEntered(searchQuery);
                    }
                });

        // Adds the subscription to presenter so that it can be un-subscribed on view is destroyed.
        addSubscription(subscription);
    }


    private void onSearchTermEntered(final String searchQuery) {
        Timber.d("onSearchTermEntered() called with: searchQuery = [%s]", searchQuery);

        final List<NewsHeadlineItem> newsHeadlineItems = new ArrayList<>();

        getView().toggleLoadingIndicator(true); // Show the loading indicator before making the request
        Observable<SearchResponse> searchResponseObservable = mSearchApi
                .searchFeedsGet(searchQuery, CoreConfig.SEARCH_RESULT_LIMIT, null);
        Subscription subscription = searchResponseObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SearchResponse>() {
                    @Override
                    public void onCompleted() {
                        Timber.d("onCompleted() called");
                        if (isViewAttached()) {
                            getView().toggleLoadingIndicator(false);
                            if (newsHeadlineItems.isEmpty()) {
                                getView().showNoSearchResults();
                            } else {
                                getView().showSearchResults(newsHeadlineItems);
                            }
                        } else {
                            Timber.i("onCompleted() - Search view is already detached.");
                        }

                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.d("onError() called with: e = [" + e + "]");
                        if (isViewAttached()) {
                            getView().toggleLoadingIndicator(false);
                        } else {
                            Timber.i("onError() - Search view is already detached.");
                        }
                    }

                    @Override
                    public void onNext(final SearchResponse searchResponse) {
                        Timber.d("onNext() called with: searchResponse size = [%d]", searchResponse.getResults().size());
                        newsHeadlineItems.addAll(convertArticleToCardItems(searchResponse.getResults()));
                    }
                });

        // Adds the subscription to presenter so that it can be un-subscribed on view is destroyed.
        addSubscription(subscription);
    }


    /**
     * Converts feed items into application's {@link NewsHeadlineItem}.
     *
     * @param feedItems Feed items.
     * @return List of card items.
     */
    private List<NewsHeadlineItem> convertArticleToCardItems(final List<FeedItem> feedItems) {
        List<NewsHeadlineItem> newsHeadlineItems = new ArrayList<>(feedItems.size());

        for (final FeedItem feedItem : feedItems) {
            newsHeadlineItems.add(
                    NewsHeadlineItem.Companion.create(
                            feedItem.getFeedId().hashCode(), // id,
                            feedItem.getTitle(), // title,
                            feedItem.getDescription(), // description,
                            ObjectUtils.defaultIfNull(feedItem.getVisualUrl(), feedItem.getIconUrl()), //extraText, (Re-using for icon)
                            feedItem.getDescription(), //category,
                            ISODateTimeFormat.dateTime().print(feedItem.getLastUpdated()), // dateCreated,
                            getImageUrl(feedItem), // imageUrl,
                            getFeedUrl(feedItem.getFeedId()), // contentUrl,
                            0, // localImageResourceId,
                            null, // footerColor,
                            null, // selectedColor,
                            CardType.HEADLINES, // type,

                            /* Re-using width and height for other purpose */
                            ObjectUtils.defaultIfNull(feedItem.getSubscribers(), 0L).intValue(), // width,
                            0 // height
                    )
            );
        }
        return newsHeadlineItems;
    }


    /**
     * Provides appropriate image URL for the feed.
     *
     * @param feedItem Feed item.
     * @return Returns URL for image if found, or {@code null}
     */
    @Nullable
    private String getImageUrl(@NonNull final FeedItem feedItem) {
        String url = null;

        if (StringUtils.isNotEmpty(feedItem.getCoverUrl())) {
            url = feedItem.getCoverUrl();
        }

        return url;
    }

    /**
     * Returns actual feed URL that is used to get RSS/ATOM feed.
     *
     * @param feedId Feed ID provided by feedly.
     * @return URL for the feed, or {@code null} for unsupported URL.
     */
    @Nullable
    private String getFeedUrl(@NonNull final String feedId) {
        if (feedId.startsWith(FEED_RESOURCE_ID_PREFIX)) {
            String feedUrl = feedId.substring(FEED_RESOURCE_ID_PREFIX.length(), feedId.length());
            if (URLUtil.isValidUrl(feedUrl)) {
                return feedUrl;
            } else {
                Timber.w("Provided feed ID does not contain valid URL: %s", feedId);
            }
        }
        return null;
    }

}
