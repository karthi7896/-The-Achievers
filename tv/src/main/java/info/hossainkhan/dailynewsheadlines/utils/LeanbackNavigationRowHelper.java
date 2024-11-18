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

package info.hossainkhan.dailynewsheadlines.utils;

import android.content.res.Resources;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import info.hossainkhan.android.core.model.NewsHeadlineItem;
import info.hossainkhan.android.core.model.CardType;
import info.hossainkhan.android.core.model.NewsCategoryHeadlines;
import info.hossainkhan.android.core.util.Validate;
import info.hossainkhan.dailynewsheadlines.R;

/**
 * Util classes related to leanback navigation items and application.
 */
public final class LeanbackNavigationRowHelper {

    /**
     * Builds a navigation header item.
     *
     * @param resources   Resources.
     * @param stringResId String res for the navigation header item.
     * @return {@link NewsCategoryHeadlines} for a header.
     */
    private static NewsCategoryHeadlines buildNavigationHeader(
            @NonNull Resources resources,
            @NonNull String sourceId,
            @StringRes int stringResId) {
        return NewsCategoryHeadlines.Companion.builder(sourceId)
                .title(resources.getString(stringResId))
                .type(NewsCategoryHeadlines.TYPE_SECTION_HEADER)
                .build();
    }

    /**
     * Builds a navigation divider item.
     *
     * @return {@link NewsCategoryHeadlines} for a divider.
     */
    private static NewsCategoryHeadlines buildNavigationDivider(@NonNull String sourceId) {
        return NewsCategoryHeadlines.Companion.builder(sourceId)
                .type(NewsCategoryHeadlines.TYPE_DIVIDER).build();
    }

    /**
     * Builds a Navigation row containing multiple NewsHeadlineItem with default configuration.
     *
     * @param resources   Android resources.
     * @param stringResId Navigation title.
     * @param items       Card items in the row.
     * @return Navigation row containing multiple NewsHeadlineItem.
     */
    @NonNull
    private static NewsCategoryHeadlines buildNavigationItemsRow(
            @NonNull Resources resources,
            @NonNull String sourceId,
            @StringRes int stringResId,
            @NonNull List<NewsHeadlineItem> items) {
        return NewsCategoryHeadlines.Companion.builder(sourceId)
                .title(resources.getString(stringResId))
                .type(NewsCategoryHeadlines.TYPE_DEFAULT)
                .cards(items)
                .useShadow(false)
                .build();
    }

    /**
     * Builds an action card item.
     *
     * @param resources     Resources.
     * @param titleRes      String resource ID for item title.
     * @param actionIconRes Drawable icon for action.
     * @return NewsHeadlineItem.
     */
    private static NewsHeadlineItem buildNavigationActionItem(final Resources resources,
                                                              @StringRes int titleRes, @DrawableRes int actionIconRes) {
        return NewsHeadlineItem.Companion.create(
                titleRes /* id */, resources.getString(titleRes) /* title */,
                null /* description */, null /*extraText */, null /*category */,
                null /* dateCreated */, null /* imageUrl */, null /* contentUrl */,
                actionIconRes, // localImageResourceId,
                null /* footerColor */, null /* selectedColor */, CardType.ACTION, 0 /* width */, 0 /* height */
        );
    }

    /**
     * Adds leanback app's navigation {@link android.support.v17.leanback.widget.Row} for sidebar.
     * Adds static navigation items like Menu and settings to existing list of navigation.
     *
     * @param resources Resources for using string res.
     * @param list      Existing list where setting will be added.
     */
    public static void addSettingsNavigation(final Resources resources, final List<NewsCategoryHeadlines> list) {
        Validate.notNull(list);

        // Begin settings section
        list.add(buildNavigationDivider("source_id_divider"));
        list.add(buildNavigationHeader(resources, "source_id_settings",
                R.string.navigation_header_item_settings_title));

        // Adds row for application settings
        addSettingsNavigationRows(resources, list);

        // Adds row for application information
        addInformationNavigationRows(resources, list);

        // Add end divider
        list.add(buildNavigationDivider("source_id_divider"));
    }

    /**
     * Add application settings related navigation items to navigation row list.
     *
     * @param resources {@link Resources}
     * @param list      List of navigation row where settings navigation item will be added.
     */
    private static void addSettingsNavigationRows(final Resources resources, final List<NewsCategoryHeadlines> list) {
        // Build settings items

        List<NewsHeadlineItem> settingsItems = new ArrayList<>(5);
        settingsItems.add(
                buildNavigationActionItem(resources,
                        R.string.settings_card_item_news_source_title, R.drawable.ic_settings_settings)
        );
        settingsItems.add(
                buildNavigationActionItem(resources,
                        R.string.settings_card_item_add_news_source_feed_title, R.drawable.ic_settings_add_news_source)
        );

        settingsItems.add(
                buildNavigationActionItem(resources,
                        R.string.settings_card_item_manage_news_source_feed_title, R.drawable.ic_settings_manage_news_source)
        );

        list.add(
                buildNavigationItemsRow(resources, "source_id_settings",
                        R.string.settings_navigation_row_news_source_title, settingsItems));

    }

    /**
     * Adds application information related navigation items to navigation row list.
     *
     * @param resources {@link Resources}
     * @param list      List of navigation row where application info navigation item will be added.
     */
    private static void addInformationNavigationRows(final Resources resources, final List<NewsCategoryHeadlines> list) {
        List<NewsHeadlineItem> infoItems = new ArrayList<>(5);
        infoItems.add(
                buildNavigationActionItem(resources,
                        R.string.settings_card_item_about_app_title, R.drawable.ic_settings_about_app_information)
        );

        infoItems.add(
                buildNavigationActionItem(resources,
                        R.string.settings_card_item_contribution_title, R.drawable.ic_settings_contribute_github_circle)
        );

        list.add(
                buildNavigationItemsRow(resources, "source_id_information",
                        R.string.settings_navigation_row_information_title, infoItems));
    }
}
