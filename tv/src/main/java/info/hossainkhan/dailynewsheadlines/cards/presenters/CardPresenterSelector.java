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

package info.hossainkhan.dailynewsheadlines.cards.presenters;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import java.util.HashMap;

import info.hossainkhan.android.core.model.NewsHeadlineItem;
import info.hossainkhan.android.core.model.CardType;
import info.hossainkhan.android.core.util.ValidationFailedException;

/**
 * This PresenterSelector will decide what Presenter to use depending on a given card's type.
 */
public class CardPresenterSelector extends PresenterSelector {

    private final Context mContext;
    private final HashMap<CardType, Presenter> presenters = new HashMap<>();

    public CardPresenterSelector(Context context) {
        mContext = context;
    }

    @Override
    public Presenter getPresenter(Object item) {
        if (!(item instanceof NewsHeadlineItem)) throw new ValidationFailedException(
                String.format("The PresenterSelector only supports data items of type '%s'",
                        NewsHeadlineItem.class.getName()));
        NewsHeadlineItem card = (NewsHeadlineItem) item;
        CardType type = card.getType();
        Presenter presenter = presenters.get(type);
        if (presenter == null) {
            switch (type) {
                case HEADLINES:
                    presenter = new TextCardPresenter(mContext);
                    break;
                case ACTION:
                    presenter = new IconCardPresenter(mContext);
                    break;
                default:
                    presenter = new ImageCardViewPresenter(mContext);
                    break;
            }
        }
        presenters.put(type, presenter);
        return presenter;
    }

}
