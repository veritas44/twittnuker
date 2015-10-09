/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.util.dagger;

import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.ActivityTracker;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.VideoLoader;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final ActivityTracker activityTracker;
    private final AsyncTwitterWrapper asyncTwitterWrapper;
    private final ReadStateManager readStateManager;
    private final MediaLoaderWrapper mediaLoaderWrapper;
    private final ImageLoader imageLoader;
    private final VideoLoader videoLoader;

    public ApplicationModule(TwittnukerApplication application) {
        activityTracker = new ActivityTracker();
        asyncTwitterWrapper = new AsyncTwitterWrapper(application);
        readStateManager = new ReadStateManager(application);
        imageLoader = createImageLoader(application);
        videoLoader = new VideoLoader(application);
        mediaLoaderWrapper = new MediaLoaderWrapper(imageLoader, videoLoader);
    }

    public static ApplicationModule get(Context context) {
        return TwittnukerApplication.getInstance(context).getApplicationModule();
    }

    @Provides
    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    @Provides
    public VideoLoader getVideoLoader() {
        return videoLoader;
    }

    @Provides
    public ActivityTracker getActivityTracker() {
        return activityTracker;
    }

    @Provides
    public AsyncTwitterWrapper getAsyncTwitterWrapper() {
        return asyncTwitterWrapper;
    }

    @Provides
    public ReadStateManager getReadStateManager() {
        return readStateManager;
    }

    @Provides
    public MediaLoaderWrapper getMediaLoaderWrapper() {
        return mediaLoaderWrapper;
    }

    private static ImageLoader createImageLoader(TwittnukerApplication application) {
        final ImageLoader loader = ImageLoader.getInstance();
        final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(application);
        cb.threadPriority(Thread.NORM_PRIORITY - 2);
        cb.denyCacheImageMultipleSizesInMemory();
        cb.tasksProcessingOrder(QueueProcessingType.LIFO);
        // cb.memoryCache(new ImageMemoryCache(40));
        cb.diskCache(application.getDiskCache());
        cb.imageDownloader(application.getImageDownloader());
        L.writeDebugLogs(BuildConfig.DEBUG);
        loader.init(cb.build());
        return loader;
    }
}