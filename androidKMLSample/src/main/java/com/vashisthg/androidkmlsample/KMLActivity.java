package com.vashisthg.androidkmlsample;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vashisthg.androidkml.KmlReader;
import com.vashisthg.androidkml.Placemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by vashisthg on 23/03/14.
 */

public class KMLActivity extends FragmentActivity {

    private static final String LOGTAG = "KMLActivity";
    private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kml);

        map = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        startReadingKml();
    }

    private void startReadingKml() {
        try {
            KmlReader reader = new KmlReader(kmlReaderCallback);
            InputStream fs = getAssets().open("test.kml");
            reader.read(fs);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.kml, menu);
		return true;
	}

    private KmlReader.Callback kmlReaderCallback = new KmlReader.Callback() {
        @Override
        public void onDocumentParsed(List<Placemark> placemarks) {
            Log.d(LOGTAG, "onDocumentParsed");
            LatLng latLng = null;

            for(Placemark placemark : placemarks) {
                latLng = new LatLng(placemark.getPoint().getLatitude(),
                        placemark.getPoint().getLongitude());

                if(null == map) {
                    throw new NullPointerException("map is null");
                }
                map.addMarker(new MarkerOptions()
                        .title(placemark.getName())
                        .snippet(placemark.getDescription())
                        .position(latLng)
                );
            }

            if(null != latLng) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            }
        }
    };
}
