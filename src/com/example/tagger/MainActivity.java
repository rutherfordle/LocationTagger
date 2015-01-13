package com.example.tagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tagger.PreferenceConnector;
import com.example.tagger.CustomListViewAdapter;
import com.example.tagger.R;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class MainActivity extends Activity implements OnClickListener, LocationListener{

	static private final String LOG_TAG = "MainActivity";
	static private final String SERVER_URL = "http://crowdlab.soe.ucsc.edu/tagstore/default/";
	static private final int MAX_SETUP_DOWNLOAD_TRIES = 2;
	List<ListViewItem> items;
	Button one, two, three, other;
	String s1, s2, s3;
	String entry;
	
	ListView lv;
	ArrayAdapter<String> itemAdapter;
	Context context;
	CustomListViewAdapter adapter;
	String tagger;
	String tagprint;
	GPSTracker gps;
	private LocationManager locationManager;
	private String provider;
	Handler counterHandler = new Handler ();
	String NS, WE, bearing;
	float lati, lngi, latiplus, latiminus, lngiplus, lngiminus;
	String lat, lng, latplus, latminus, lngplus, lngminus;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		items = new ArrayList<MainActivity.ListViewItem>();
		adapter = new CustomListViewAdapter(this, items);
		lv = (ListView)findViewById(R.id.listView1);
		one =(Button)findViewById(R.id.button1);
		two = (Button)findViewById(R.id.button2);
		three = (Button)findViewById(R.id.button3);
		other = (Button)findViewById(R.id.button4);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
	 	Location location = locationManager.getLastKnownLocation(provider);
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} 
		//lati = (float) location.getLatitude();
		lati = (float) 37.0;
		//hardcoded
		latiplus = lati + 3;
		latiminus = lati - 3;
		//lngi = (float) location.getLongitude();
		lngi = (float) -122.0;
		lngiplus = lngi + 3;
		lngiminus = lngi - 3;
		lat = Float.toString(lati);
		lng = Float.toString(lngi);
		latplus = Float.toString(latiplus);
		lngplus = Float.toString(lngiplus);
		latminus = Float.toString(latiminus);
		lngminus = Float.toString(lngiminus);

		one.setOnClickListener(this);
		two.setOnClickListener(this);
		three.setOnClickListener(this);
		other.setOnClickListener(this);
		getTags(lv);
		getRepeat();
	}
	private void getRepeat () {
		counterHandler.postDelayed(TextViewChanger, 10000);
	}
	private Runnable TextViewChanger = new Runnable(){
		public void run(){
			getTags(lv);
			getRepeat();
		}
	};

	public void onLocationChanged(Location location) {
		// put location onject back if running on phone for GPS functionality. Thanks.
		//lati = (float) location.getLatitude();
		lati = (float) 37.0;
		latiplus = lati + 3;
		latiminus = lati - 3;
		//lngi = (float) location.getLongitude();
		lngi = (float) -122.0;
		//hardcoded
		lngiplus = lngi + 3;
		lngiminus = lngi - 3;
		lat = Float.toString(lati);
		lng = Float.toString(lngi);
		latplus = Float.toString(latiplus);
		lngplus = Float.toString(lngiplus);
		latminus = Float.toString(latiminus);
		lngminus = Float.toString(lngiminus);
	}
	@Override
	protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
	}
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.button1:
			Toast.makeText(getApplicationContext(), "Site 1", Toast.LENGTH_LONG).show();
			entry = PreferenceConnector.readString(this,PreferenceConnector.tag1, null);
			if(entry==null||entry.isEmpty()){
				entry = "Pothole";
			}
			uploadTag(v);
			break;

		case R.id.button2:

			Toast.makeText(getApplicationContext(), "Site 2", Toast.LENGTH_LONG).show();
			
			entry = PreferenceConnector.readString(this,PreferenceConnector.tag2, null);
			if(entry==null||entry.isEmpty()){
				entry = "Girlfriend";
			}
			uploadTag(v);

			break;
		case R.id.button3:
			entry = PreferenceConnector.readString(this,PreferenceConnector.tag3, null);
			if(entry==null||entry.isEmpty()){
				entry = "Puppy";
			}
			uploadTag(v);
			break;
		case R.id.button4:
			Intent i = new Intent("com.example.tagger.Menu");
			startActivity(i);
		}

	}
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(provider, 10000, 0, this);
        whichName(one,PreferenceConnector.readString(this,PreferenceConnector.tag1, null));
        whichName(two,PreferenceConnector.readString(this,PreferenceConnector.tag2, null));
        whichName(three,PreferenceConnector.readString(this,PreferenceConnector.tag3, null));
	}

public void whichName(Button btn, String pref){
	if(pref != null && pref.length()!=0){
		btn.setText(pref);
	}
}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void uploadTag(View v) {
		check(v);
		// Let us build the parameters.
		ServerCallParams serverParams = new ServerCallParams();
		serverParams.url = "add_tagging.json";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
		params.add(new BasicNameValuePair("user", "luca"));
		params.add(new BasicNameValuePair("nick", "hello"));
		params.add(new BasicNameValuePair("lat", lat));
		params.add(new BasicNameValuePair("lng", lng));
		params.add(new BasicNameValuePair("tag", entry));
		serverParams.params = params;
		serverParams.continuation = new ContinuationAddTag();
		ContactServer contacter = new ContactServer();
		contacter.execute(serverParams);
	}
	
	  public void check(View v){
	      gps = new GPSTracker(MainActivity.this);

			// check if GPS enabled		
	      if(gps.canGetLocation()){
	      	
	      	double latitude = gps.getLatitude();
	      	double longitude = gps.getLongitude();
	      	
	      	// \n is for new line
	      	//Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();	
	      }else{
	      	// can't get location
	      	// GPS or Network is not enabled
	      	// Ask user to enable GPS/network in settings
	      	gps.showSettingsAlert();
	      }  
	  
	  }
	
	class ContinuationAddTag implements Continuation {
		ContinuationAddTag() {}
		
		public void useString(String s) {
			if (s == null) {
				Log.d(LOG_TAG, "Returned an empty string.");
			} else {
				Log.d(LOG_TAG, "Returned: " + s);
			}
		}
	}

	public void getTags(View v) {
		// Let us build the parameters.
		ServerCallParams serverParams = new ServerCallParams();
		serverParams.url = "get_tags.json";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("token", "CMPS121_yehaa"));
		params.add(new BasicNameValuePair("user", "luca"));
		params.add(new BasicNameValuePair("lat_min", latminus));
		params.add(new BasicNameValuePair("lng_min", lngminus));
		params.add(new BasicNameValuePair("lat_max", latplus));
		params.add(new BasicNameValuePair("lng_max", lngplus));
		params.add(new BasicNameValuePair("n_taggings", "20"));
		serverParams.params = params;
		serverParams.continuation = new ContinuationGetTagList();
		ContactServer contacter = new ContactServer();
		contacter.execute(serverParams);
	}
	
	
	class ContinuationGetTagList implements Continuation {
		public ContinuationGetTagList() {}
		public void useString(final String s) {
			// Dejasonize the string.
			if (s == null) {
				Log.d(LOG_TAG, "Returned an empty string.");
			} else {
				Log.d(LOG_TAG, "Returned: " + s);
			    final NearbyTags newTags = decodeNearbyTags(s);
			    if (newTags != null) {
			    	Location loc = new Location("here");
			    	Location locA = new Location("there");
			    	
		    		loc.setLatitude(lati); //has to be integer
		    		loc.setLongitude(lngi);
					// We would have to replace the list in the array adaptor.
				    lv.setAdapter(null);
	                adapter.clear();
			    	for (int index = newTags.tags.length-1; index != 0; index--){
			    	tagprint = String.valueOf(newTags.tags[index].nick) + "/ ";
			    	locA.setLatitude(newTags.tags[index].lat);
			    	locA.setLongitude(newTags.tags[index].lng);
			    	bearing = gatherBearing(newTags, index);
			    	tagprint += String.valueOf(loc.distanceTo(locA)) + " ft. " + bearing + "/ ";
					tagprint += String.valueOf(newTags.tags[index].tag) + "/ ";
			    	
			    	items.add(0, new ListViewItem()
					{{
						Title = tagprint;
					}});
			    	CustomListViewAdapter adapter = new CustomListViewAdapter(MainActivity.this, items);
					lv.setAdapter(adapter);
			    	}
			    	Log.d(LOG_TAG, "The dejsonizing succeeded");
			    	Log.d(LOG_TAG, "N. of tags:" + newTags.tags.length);
			    }
			}
		}
	}
	
	public String gatherBearing(NearbyTags newTags, int index){
		if (newTags.tags[index].lat > lati){
			NS = "N";
		}else{
			NS = "S";
		}
		if (newTags.tags[index].lng > lngi){
			WE = "E";
		}else{
			WE = "W";
		}
		bearing = NS+WE;
		return bearing;
	}
	
	interface Continuation {
		void useString(String s);
	}
	
	class ServerCallParams {
		public String url; // for example: get_tags.json
		public List<NameValuePair> params;
		public Continuation continuation;
	}
	
	class FinishInfo {
		public Continuation continuation;
		public String value;
	}

	// This class executed an http call to the server. 
	// You need to pass to it the ServerCallParams, containing the method to call,
	// a list of parameters for the call, and what to do afterwards (the continuation).
    private class ContactServer extends AsyncTask<ServerCallParams, String, FinishInfo> {

    	protected FinishInfo doInBackground(ServerCallParams... callParams) {
    		Log.d(LOG_TAG, "Starting the download.");
    		String downloadedString = null;
    		int numTries = 0;
    		ServerCallParams callInfo = callParams[0];
    		List<NameValuePair> params = callInfo.params;
			FinishInfo info = new FinishInfo();
			info.continuation = callInfo.continuation;
    		while (downloadedString == null && numTries < MAX_SETUP_DOWNLOAD_TRIES && !isCancelled()) {
    			numTries++;
    			HttpClient httpclient = new DefaultHttpClient();
    		    HttpPost httppost = new HttpPost(SERVER_URL + callInfo.url);
    	        HttpResponse response = null; 
    			try {
        	        httppost.setEntity(new UrlEncodedFormEntity(params));
        	        // Execute HTTP Post Request
    				response = httpclient.execute(httppost);
    			} catch (ClientProtocolException ex) {
    				Log.e(LOG_TAG, ex.toString());
    			} catch (IOException ex) {
    				Log.w(LOG_TAG, ex.toString());
    			}
    			if (response != null) {
    				// Checks the status code.
    				int statusCode = response.getStatusLine().getStatusCode();
    				Log.d(LOG_TAG, "Status code: " + statusCode);

    				if (statusCode == HttpURLConnection.HTTP_OK) {
    					// Correct response. Reads the real result.
    					// Extracts the string content of the response.
    					HttpEntity entity = response.getEntity();
    					InputStream iStream = null;
    					try {
    						iStream = entity.getContent();
    					} catch (IOException ex) {
    						Log.e(LOG_TAG, ex.toString());
    					}
    					if (iStream != null) {
    						downloadedString = ConvertStreamToString(iStream);
    						Log.d(LOG_TAG, "Received string: " + downloadedString);
    						// Passes the string, along with the continuation, to onPostExecute.
    						info.value = downloadedString;
    				    	return info;
    					}
    				}
    			}
    		}
    		// Returns null to indicate failure.
    		info.value = null;
    		return info;
    	}
    	
    	protected void onProgressUpdate(String... s) {}
    	
    	protected void onPostExecute(FinishInfo info) {
    		// Do something with what you get. 
    		if (info != null) {
    			info.continuation.useString(info.value);
    		} else {
    			// This is just an example: we can pass back null to the continuation
    			// to indicate that no string was in fact received.
    			info.continuation.useString(null);
    		}
    	}
    }
    
    // Here is an example of how to decode a JSON string.  We will decode the taglist.
    // First, we declare a class for the info on one tag.
    // Note that if you want to have this accessible from multiple activities, as it might be
    // a good idea to do, it might be better to define this as a public class in its own file,
    // rather than here. 
    class TagInfo {
    	public double lat;
    	public double lng;
    	public String nick;
    	public String tag;
    }
    // Now we create a class for the overall message.
    class NearbyTags {
    	public TagInfo[] tags;
    }
    
    // Decoding a received tag string is then simple.
    private NearbyTags decodeNearbyTags(String s) {
    	if (s == null) {
    		// Your choice of what to do; returning null may be a simple way to 
    		// propagate the fact that the call to the server failed.
    		return null;
    	}
    	// Gets a gson object for decoding a string.
    	Gson gson = new Gson();
    	NearbyTags tags = null;
    	try {
    		tags = gson.fromJson(s, NearbyTags.class);
    	} catch (JsonSyntaxException ex) {
    		Log.w(LOG_TAG, "Error decoding json: " + s + " exception: " + ex.toString());
    	}
    	return tags;
    }
    
    
    @Override
    public void onStop() {
    	// Cancel what you have to cancel.
    	super.onStop();
    }
    
    public static String ConvertStreamToString(InputStream is) {
    	
    	if (is == null) {
    		return null;
    	}
    	
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();

	    String line = null;
	    try {
	        while ((line = reader.readLine()) != null) {
	            sb.append((line + "\n"));
	        }
	    } catch (IOException e) {
	        Log.d(LOG_TAG, e.toString());
	    } finally {
	        try {
	            is.close();
	        } catch (IOException e) {
	            Log.d(LOG_TAG, e.toString());
	        }
	    }
	    return sb.toString();
	}
	
	class ListViewItem{
		public int ThumbnailResource;
		public String Title;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();
	}
}
