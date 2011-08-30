package net.lapalta.android.chilepostal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.util.ByteArrayBuffer;

public class PostalGetter {
	String url = "http://t.72.cl/postalchile/getpostal.php";
	
	public PostalGetter() {
		
	}
	
	public int getPostal(String comuna, String calle, int numero) {
		int postalcode = 0;
		URL furl;
		
		/*try {
			comuna = URLEncoder.encode(comuna, "application/x-www-form-urlencoded");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		comuna = comuna.replaceAll(" ", "+");
		calle = calle.replaceAll(" ", "+");
		String nurl = url + "?comuna=" + comuna + "&calle=" + calle + "&numero="+ numero;
		//String nurl = url + "?params="+comuna;
		try {
			furl = new URL(nurl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return postalcode;
		}
		
		URLConnection ucon;
		try {
			ucon = furl.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return postalcode;
		}
		
		ucon.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		
		InputStream is = null;
		try {
			is = ucon.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BufferedInputStream bis = new BufferedInputStream(is);
        ByteArrayBuffer baf = new ByteArrayBuffer(50);
        int current = 0;
        try {
			while((current = bis.read()) != -1){
			        baf.append((byte)current);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return postalcode;
		}
		String tmp = new String(baf.toByteArray());
		tmp = tmp.trim();
		postalcode = Integer.parseInt(tmp);
		
		return postalcode;
	}
}
