/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.activity.support;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.attoparser.AttoParseException;
import org.mariotaku.restfu.http.Authorization;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.api.twitter.TwitterOAuth;
import de.vanita5.twittnuker.api.twitter.auth.OAuthAuthorization;
import de.vanita5.twittnuker.api.twitter.auth.OAuthEndpoint;
import de.vanita5.twittnuker.api.twitter.auth.OAuthToken;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.AsyncTaskUtils;
import de.vanita5.twittnuker.util.OAuthPasswordAuthenticator;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.webkit.DefaultWebViewClient;

import java.io.IOException;
import java.io.StringReader;

import static android.text.TextUtils.isEmpty;
import static de.vanita5.twittnuker.util.Utils.getNonEmptyString;

@SuppressLint("SetJavaScriptEnabled")
public class BrowserSignInActivity extends BaseAppCompatActivity {

    private static final String INJECT_CONTENT = "javascript:window.injector.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');";

    private WebView mWebView;
    private View mProgressContainer;

    private WebSettings mWebSettings;

    private OAuthToken mRequestToken;

    private GetRequestTokenTask mTask;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mWebView = (WebView) findViewById(R.id.webview);
        mProgressContainer = findViewById(R.id.progress_container);
    }

    @Override
    public void onDestroy() {
        getLoaderManager().destroyLoader(0);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_sign_in);
        mWebView.setWebViewClient(new AuthorizationWebViewClient(this));
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.addJavascriptInterface(new InjectorJavaScriptInterface(this), "injector");
        mWebSettings = mWebView.getSettings();
        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setBlockNetworkImage(false);
        mWebSettings.setSaveFormData(true);
        getRequestToken();
    }

    private void getRequestToken() {
        if (mRequestToken != null || mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        mTask = new GetRequestTokenTask(this);
        AsyncTaskUtils.executeTask(mTask);
    }

    private void loadUrl(final String url) {
        if (mWebView == null) return;
        mWebView.loadUrl(url);
    }

    private String readOAuthPin(final String html) {
        try {
            OAuthPasswordAuthenticator.OAuthPinData data = new OAuthPasswordAuthenticator.OAuthPinData();
            OAuthPasswordAuthenticator.readOAuthPINFromHtml(new StringReader(html), data);
            return data.oauthPin;
        } catch (final AttoParseException | IOException e) {
            Log.w(LOGTAG, e);
        }
        return null;
    }

    private void setLoadProgressShown(final boolean shown) {
        mProgressContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    private void setRequestToken(final OAuthToken token) {
        mRequestToken = token;
    }

    static class AuthorizationWebViewClient extends DefaultWebViewClient {

        AuthorizationWebViewClient(final BrowserSignInActivity activity) {
            super(activity);
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            view.loadUrl(INJECT_CONTENT);
            ((BrowserSignInActivity) getActivity()).setLoadProgressShown(false);
        }

        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ((BrowserSignInActivity) getActivity()).setLoadProgressShown(true);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description,
                                    final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            final Activity activity = getActivity();
            Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            activity.finish();
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            final Uri uri = Uri.parse(url);
            if (url.startsWith(OAUTH_CALLBACK_URL)) {
                final String oauth_verifier = uri.getQueryParameter(EXTRA_OAUTH_VERIFIER);
                final BrowserSignInActivity activity = (BrowserSignInActivity) getActivity();
                final OAuthToken requestToken = activity.mRequestToken;
                if (oauth_verifier != null && requestToken != null) {
                    final Intent intent = new Intent();
                    intent.putExtra(EXTRA_OAUTH_VERIFIER, oauth_verifier);
                    intent.putExtra(EXTRA_REQUEST_TOKEN, requestToken.getOauthToken());
                    intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, requestToken.getOauthTokenSecret());
                    activity.setResult(RESULT_OK, intent);
                    activity.finish();
                }
                return true;
            }
            return false;
        }

    }

    static class GetRequestTokenTask extends AsyncTask<Object, Object, OAuthToken> {

        private final String mConsumerKey, mConsumerSecret;
        private final TwittnukerApplication mApplication;
        private final SharedPreferences mPreferences;
        private final BrowserSignInActivity mActivity;

        public GetRequestTokenTask(final BrowserSignInActivity activity) {
            mActivity = activity;
            mApplication = TwittnukerApplication.getInstance(activity);
            mPreferences = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
            final Intent intent = activity.getIntent();
            mConsumerKey = intent.getStringExtra(Accounts.CONSUMER_KEY);
            mConsumerSecret = intent.getStringExtra(Accounts.CONSUMER_SECRET);
        }

        @Override
        protected OAuthToken doInBackground(final Object... params) {
            final String defConsumerKey = getNonEmptyString(mPreferences, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
            final String defConsumerSecret = getNonEmptyString(mPreferences, KEY_CONSUMER_SECRET,
                    TWITTER_CONSUMER_SECRET);
            final String consumerKey, consumerSecret;
            if (!isEmpty(mConsumerKey) && !isEmpty(mConsumerSecret)) {
                consumerKey = mConsumerKey;
                consumerSecret = mConsumerSecret;
            } else {
                consumerKey = defConsumerKey;
                consumerSecret = defConsumerSecret;
            }
            try {
                final OAuthEndpoint endpoint = new OAuthEndpoint(TwitterAPIFactory.getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, "api", null));
                final Authorization auth = new OAuthAuthorization(consumerKey, consumerSecret);
                final TwitterOAuth twitter = TwitterAPIFactory.getInstance(mActivity, endpoint, auth, TwitterOAuth.class);
                return twitter.getRequestToken(OAUTH_CALLBACK_OOB);
            } catch (final Exception e) {
                Log.w(LOGTAG, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final OAuthToken data) {
            mActivity.setLoadProgressShown(false);
            mActivity.setRequestToken(data);
            if (data == null) {
                if (!mActivity.isFinishing()) {
                    Toast.makeText(mActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    mActivity.finish();
                }
                return;
            }
            final OAuthEndpoint endpoint = new OAuthEndpoint(TwitterAPIFactory.getApiUrl(DEFAULT_TWITTER_API_URL_FORMAT, "api", null));
            mActivity.loadUrl(endpoint.construct("/oauth/authorize", new String[]{"oauth_token", data.getOauthToken()}));
        }

        @Override
        protected void onPreExecute() {
            mActivity.setLoadProgressShown(true);
        }

    }

    static class InjectorJavaScriptInterface {

        private final BrowserSignInActivity mActivity;

        InjectorJavaScriptInterface(final BrowserSignInActivity activity) {
            mActivity = activity;
        }

        @JavascriptInterface
        public void processHTML(final String html) {
            final String oauthVerifier = mActivity.readOAuthPin(html);
            final OAuthToken requestToken = mActivity.mRequestToken;
            if (oauthVerifier != null && requestToken != null) {
                final Intent intent = new Intent();
                intent.putExtra(EXTRA_OAUTH_VERIFIER, oauthVerifier);
                intent.putExtra(EXTRA_REQUEST_TOKEN, requestToken.getOauthToken());
                intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, requestToken.getOauthTokenSecret());
                mActivity.setResult(RESULT_OK, intent);
                mActivity.finish();
            }
        }
    }
}