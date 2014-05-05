package edu.mit.mitmobile2.livinglabs;

import java.net.URI;

import edu.mit.media.openpds.client.PreferencesWrapper;
import edu.mit.media.openpds.client.RegistryClient;
import edu.mit.media.openpds.client.UserInfoTask;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.NewModuleFragmentActivity;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.os.Build;

public class LivingLabsLoginActivity extends NewModuleFragmentActivity {
	private static final String REDIRECT_URI = "https://linkedpersonaldata.mit.edu/redirect_uri";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.living_labs_login_activity);

		if (savedInstanceState == null) {
			final RegistryClient registry = new RegistryClient(getString(R.string.registry_url), getString(R.string.pds_client_key), getString(R.string.pds_client_secret), getString(R.string.pds_client_scope), getString(R.string.pds_client_basic_auth));
		
			WebViewFragment webViewFragment = WebViewFragment.Create(registry.getShibbolethAuthorizationUrl(REDIRECT_URI), "Login", this, null);
			webViewFragment.setWebViewClient(new WebViewFragmentClient(this, webViewFragment) {
				@Override
				public void onPageFinished(WebView view, String url) {
					if (url.startsWith(REDIRECT_URI)) {
						// NOTE: the Oauth2 app on the registry server uses fragment, rather than querystring, parameters
						// As a result, to integrate with existing querystring parsers, we're replacing the # with a ?
						Uri fragmentQueryUri = Uri.parse(url.replace("#", "?"));
						PreferencesWrapper prefs = new PreferencesWrapper(LivingLabsLoginActivity.this);
						String token = null;
						String refreshToken = null;
						String expirationTimeString = null;
						try {
							token = fragmentQueryUri.getQueryParameter("access_token");
							refreshToken = fragmentQueryUri.getQueryParameter("refresh_token");
							expirationTimeString = fragmentQueryUri.getQueryParameter("expires_in");
						} catch (Exception ex) {
							Log.e("LivingLabsLoginActivity", "Error parsing redirect_uri parameters");
							ex.printStackTrace();
						}
						if (token != null && refreshToken != null && expirationTimeString != null) {
							prefs.setAccessToken(token);
							prefs.setRefreshToken(refreshToken);
							long expirationTime = Long.parseLong(expirationTimeString);
							prefs.setTokenExpirationTime(expirationTime);
							UserInfoTask userInfoTask = new UserInfoTask(LivingLabsLoginActivity.this, prefs, registry);
							userInfoTask.execute(token);
						} else {
							// If any (likely all) of the parameters are missing, maybe pop a toast and retry the login?
						}
					}
					super.onPageFinished(view, url);					
				}
			});
			getSupportFragmentManager().beginTransaction().add(R.id.container,webViewFragment).commit();
		}
	}

	@Override
	protected NewModule getNewModule() {
		return new LivingLabsModule();
	}

	@Override
	protected boolean isScrollable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onOptionSelected(String optionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isModuleHomeActivity() {
		// TODO Auto-generated method stub
		return false;
	}

}
