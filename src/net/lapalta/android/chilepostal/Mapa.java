package net.lapalta.android.chilepostal;

import java.io.IOException;
import java.util.List;


import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.location.Address;
import android.location.Geocoder;

public class Mapa extends MapActivity {
	
	LinearLayout linearLayout;
	MapView mapView;
	public String address = null;
	public String postal_code = null;
	
	List<Overlay> mapOverlays;
	Drawable drawable;
	MapaOverlay itemizedOverlay;
	public MapController mc = null;
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa);
        
        Bundle bundle = getIntent().getExtras();
        this.address = bundle.getString("address");
        this.postal_code = bundle.getString("postal_code");
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        mapOverlays = mapView.getOverlays();
        drawable = this.getResources().getDrawable(android.R.drawable.star_on);
        itemizedOverlay = new MapaOverlay(drawable);
        
        GeoPoint point = new GeoPoint(19240000,-99120000);
        Geocoder gc = new Geocoder(getBaseContext());
        List<Address> results = null;
        try {
			results = gc.getFromLocationName(this.address +", Chile" , 1);
			if (results!=null) {
				int lat = (int) (results.get(0).getLatitude() * 1000000);
				int lon = (int) (results.get(0).getLongitude() * 1000000);
				point = new GeoPoint(lat, lon);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        OverlayItem overlayitem = new OverlayItem(point, "", "");
        
        itemizedOverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedOverlay);
        mc= mapView.getController();
        mc.setCenter(point);
        mc.setZoom(17);
    }
}
