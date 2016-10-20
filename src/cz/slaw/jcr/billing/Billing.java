package cz.slaw.jcr.billing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import cz.slaw.jcr.billing.util.IabHelper;
import cz.slaw.jcr.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import cz.slaw.jcr.billing.util.IabResult;
import cz.slaw.jcr.billing.util.Inventory;
import cz.slaw.jcr.billing.util.Purchase;
import cz.slaw.jcr.helpers.SettingsHelper;

public class Billing{
	
	private static final Logger log = LoggerFactory.getLogger(Billing.class);
	protected static final String ITEM_SKU = SettingsHelper.PURCH_KEY;
	private final IabHelper mHelper;
	private final String base64EncodedPublicKey;
	private final Activity activity;
	private final BuyCallback callback;

	public interface BuyCallback{
		public void buyResult(boolean res);
		public void isPremium(boolean res);
	}
	
	public Billing(Activity activity,BuyCallback callback) {
		this.base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtSoVoVNcWxYAtvaTXVKrH7W90PwLAEFGxfWZnQsD+FAtWp6kpIixNqZqqCxNz01XFNaUgMaf9pf59k03XrMFprJl/rNpLWG5iE7kKrwMykCslpf66z5LUFhOKD67Xf3xmd1A36NTrNUzuf2Fc58xwLrVzn4h4iT+hkO1qnw3gdcARShE6JcezEVz2R9nUI9+AMhqxnc/xSbWkCt/1efPIc856tPNwt3Y8C1E0mAnqS3qe6esdYOyqa+2mL89AwL/9RD8cdAoZEHg9iOhCki762G5JoikVDip0m0BUHEfUAXZZWpjRgBBO1KNvcECftt2YKqrE/SjVHUdrMvIYnGptQIDAQAB";
		this.activity=activity;
		this.mHelper = new IabHelper(this.activity, base64EncodedPublicKey);
		this.callback= callback;
	}

	public void checkIsPro() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.activity);
		if(resultCode != ConnectionResult.SUCCESS){
			log.error("Google services not available (cause : "+resultCode+") ");
			return;
		}
		final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
			public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

				if (result.isFailure()) {
					log.debug("check premium err "+result +":"+inventory);
					callback.isPremium(false);
				} else {
					// does the user have the premium upgrade?
					boolean mIsPremium = inventory.hasPurchase(ITEM_SKU);
					callback.isPremium(mIsPremium);
					log.debug("is prem " + mIsPremium);
				}
			}
		};
		
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					log.debug("In-app Billing setup failed: " + result);
					callback.isPremium(false);
				} else {
					log.debug("In-app Billing is set up OK");
					mHelper.queryInventoryAsync(mGotInventoryListener);
				}
			}
		});
	}

	public void buyPro() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.activity);
		if(resultCode != ConnectionResult.SUCCESS){
			log.error("Google services not available (cause : "+resultCode+") ");
			return;
		}
		final OnIabPurchaseFinishedListener mPurchaseFinishedListener = new OnIabPurchaseFinishedListener() {
			@Override
			public void onIabPurchaseFinished(IabResult result, Purchase info) {
				if (!result.isSuccess()) {
					log.debug("In-app purch failed: " + result + " L " + info);
					callback.buyResult(false);
				} else {
					log.debug("In-app purch OK" + result + ": " + info);
					callback.buyResult(true);
				}
			}
		};
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					log.debug("In-app Billing setup failed: " + result);
					callback.buyResult(false);
				} else {
					log.debug("In-app Billing is set up OK");
					mHelper.launchPurchaseFlow(Billing.this.activity, ITEM_SKU, 10001, mPurchaseFinishedListener, "payloadX");
				}
			}
		});
	}

	public void stop() {
		if (mHelper != null)
			mHelper.dispose();		
	};

}
