package us.grahn.wiki;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Wikipedia {

	private static final String READ_URL =
			"https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&explaintext&exchars=999999999999&titles=";

	public static String getExtract(final String title) {

		// Make the connection
	    try {
    		final URL url = new URL(READ_URL + URLEncoder.encode(title, "UTF-8"));
    		final URLConnection connection = url.openConnection();
    		connection.setRequestProperty("Api-User-Agent", "Timelinr/1.0");
    		connection.connect();

    		// Get the extract
    		final JSONTokener tokener = new JSONTokener(connection.getInputStream());
    		final JSONObject  root    = new JSONObject(tokener);
    		final JSONObject  query   = root.getJSONObject("query");
    		final JSONObject  pages   = query.getJSONObject("pages");
    		final String      id      = JSONObject.getNames(pages)[0];
    		final JSONObject  page    = pages.getJSONObject(id);

    		String extract = page.getString("extract");

    		// Remove all the titles
    		extract = extract.replaceAll("={2,6}.*={2,6}", "");

    		// Homogenize the end-lines
    		extract = extract.replaceAll("\r", "\n");

    		// Remove all the extra end-lines
    		extract = extract.replaceAll("\n+", "\n");

    		return extract;

	    } catch (final IOException e) {
	        return null;
	    }
	}

}
