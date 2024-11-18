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

package info.hossainkhan.android.core.base;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getView().
 */
public class BasePresenter<T extends MvpView> implements MvpPresenter<T> {

    private T view;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void attachView(T mvpView) {
        Timber.d("attachView() called with: mvpView = [" + mvpView + "]");
        view = mvpView;
    }

    @Override
    public void detachView() {
        Timber.d("detachView() called - detaching view = [" + view + "]");
        view = null;

        // Unsubscribes any subscriptions that are currently part of this CompositeSubscription.
        compositeSubscription.clear();
    }

    public T getView() {
        Timber.d("getView() called - returning view = [" + view + "]");
        return view;
    }

    /**
     * Validation method which checks if view is attached, otherwise runtime exception is thrown.
     */
    public void checkViewAttached() {
        if (!isViewAttached()) {
            throw new MvpViewNotAttachedException();
        }
    }

    /**
     * Checks if view is attached to the current presenter.
     *
     * @return {@code true} when attached, {@code false} otherwise.
     */
    public boolean isViewAttached() {
        return view != null;
    }

    /**
     * Adds {@link Subscription} to {@link CompositeSubscription}. Unsubscribes this subscription when view is detached.
     *
     * @param subscription {@link Subscription} that may be used in the presenter.
     */
    public void addSubscription(Subscription subscription) {
        this.compositeSubscription.add(subscription);
    }

    /**
     * Runtime exception when {@link MvpView} is not attached.
     */
    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" + " requesting data to the Presenter");
        }
    }
}