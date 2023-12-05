package com.paulprice.rssassignment;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class RssReader extends AppCompatActivity {

    private ListView myRss;
    private ArrayList<String> titles;
    private ArrayList<String> links;
    private ArrayList<String> imageUrls;
    private ArrayList<String> descriptions;

    private ArrayList<String> published;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_reader);

        // Initialize UI components
        myRss = findViewById(R.id.myRss);
        titles = new ArrayList<>();
        links = new ArrayList<>();
        imageUrls = new ArrayList<>();
        descriptions = new ArrayList<>();
        published = new ArrayList<>();

        // Set item click listener to open the link in a browser
        myRss.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openLinkInBrowser(position);
            }
        });

        // Start background task to fetch and process RSS feed
        new ProcessInBackground().execute();
    }

    // Open the selected link in a browser
    private void openLinkInBrowser(int position) {
        Uri uri = Uri.parse(links.get(position));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    // Simplify network connection to handle HTTPS
    private InputStream getInputStream(URL url) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Background task to fetch and process RSS feed
    private class ProcessInBackground extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show progress dialog while fetching data
            progressDialog = new ProgressDialog(RssReader.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Intent intent = getIntent();
                String str = intent.getStringExtra("message_key");
                String Message = (str);
                // URL for the RSS feed
                URL url = new URL(Message);

                // XML parsing setup
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(url), "UTF_8");

                // Variables for parsing
                boolean insideItem = false;
                int eventType = xpp.getEventType();

                String imageUrl = null;

                // Loop through XML elements
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                titles.add(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (insideItem) {
                                descriptions.add(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem) {
                                links.add(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            if (insideItem) {
                                published.add(xpp.nextText());
                            }
                        } else if (xpp.getName().equalsIgnoreCase("media:content")) {
                            // Check if the current tag is media:content
                            imageUrl = xpp.getAttributeValue(null, "url");
                            if (insideItem && imageUrl != null) {
                                // Add the image URL to the list
                                imageUrls.add(imageUrl);
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                    }
                    eventType = xpp.next();
                }

            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Update UI with the fetched titles
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(RssReader.this, R.layout.list_item_layout, R.id.titleTextView, titles) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    // Get references to the ImageView and TextView
                    ImageView imageView = view.findViewById(R.id.imageView);
                    TextView titleTextView = view.findViewById(R.id.titleTextView);
                    TextView descriptionTextView = view.findViewById(R.id.descriptionTextView);
                    TextView publishedTextView = view.findViewById(R.id.publishedTextView);

                    // Set title
                    titleTextView.setText(titles.get(position));

                    // Set description
                    descriptionTextView.setText(descriptions.get(position));

                    // Set published date and time
                    publishedTextView.setText(published.get(position));

                    // Log image URL
                    String imageUrl = imageUrls.get(position);
                    Log.d("ImageUrlDebug", "Image URL at position: " + imageUrl);

                    // Load image using Picasso with error handling
                    Picasso.get().load(imageUrl).placeholder(R.drawable.img).error(R.drawable.img).into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loaded successfully
                        }

                        @Override
                        public void onError(Exception e) {
                            e.printStackTrace();
                            // Retry the image loading
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Picasso.get().load(imageUrl).placeholder(R.drawable.img).error(R.drawable.img).into(imageView);
                            }, 2000);
                        }
                    });

                    return view;
                }
            };

            myRss.setAdapter(adapter);

            // Dismiss the progress dialog
            progressDialog.dismiss();
        }
    }
}