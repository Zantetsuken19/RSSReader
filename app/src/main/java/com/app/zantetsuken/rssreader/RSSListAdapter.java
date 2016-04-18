package com.app.zantetsuken.rssreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Zantetsuken on 2/24/2016.
 */
public class RSSListAdapter extends ArrayAdapter<RSSListAdapter.RSSData>
{
    private String _url = null;
    public String getURL()
    {
        return _url;
    }
    public void setURL(String url) { _url = url; }

    private Context _context = null;
    private int _resourceID = -1;
    private ArrayList<ImageDownloader> _bitmaps;

    public RSSListAdapter(Context context, int resourceID, String url)
    {
        super(context, android.R.layout.simple_list_item_1);
        _context = context;
        _resourceID = resourceID;
        _url = url;
        _bitmaps = new ArrayList<ImageDownloader>();
    }

    public void refreshList()
    {
        clear();
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    URL url = new URL(_url);

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(false);
                    XmlPullParser xpp = factory.newPullParser();
                    InputStream is = url.openConnection().getInputStream();
                    xpp.setInput(is, "UTF_8");

                    boolean isItem = false;
                    int eventType = xpp.getEventType();
                    RSSData data = null;
                    while(eventType != XmlPullParser.END_DOCUMENT)
                    {
                        String name = xpp.getName();
//                        if(name == null) continue;
                        switch(eventType)
                        {
                            case XmlPullParser.START_TAG:
                                if(name.equalsIgnoreCase("item"))
                                {
                                    data = new RSSData();
                                    isItem = true;
                                }
                                else if(isItem && name.equalsIgnoreCase("media:content"))
                                {
//                                    data.Content = xpp.nextText();
                                    for(int i=0; i<xpp.getAttributeCount(); i++)
                                    {
                                        String attrName = xpp.getAttributeName(i);
                                        if(attrName == null) continue;
                                        if(attrName.equalsIgnoreCase("url"))
                                        {
                                            data.URL = xpp.getAttributeValue(i);
                                            ImageDownloader id = new ImageDownloader();
                                            id.execute(data.URL);
                                            _bitmaps.add(id);
                                        }
                                        else if(attrName.equalsIgnoreCase("width"))
                                            data.Width = Integer.parseInt(xpp.getAttributeValue(i));
                                        else if(attrName.equalsIgnoreCase("height"))
                                            data.Height = Integer.parseInt(xpp.getAttributeValue(i));
                                    }
                                }
                                else if(isItem && name.equalsIgnoreCase("media:copyright"))
                                    data.Copyright = xpp.nextText();
                                else if(isItem && name.equalsIgnoreCase("title"))
                                    data.Title = xpp.nextText();
                                else if(isItem && name.equalsIgnoreCase("media:description"))
                                    data.Description = xpp.nextText();
                                else if(isItem && name.equalsIgnoreCase("media:thumbnail"))
                                    data.Thumbnail = xpp.nextText();
                                break;
                            case XmlPullParser.END_TAG:
                                if(xpp.getName().equalsIgnoreCase("item"))
                                {
                                    addItem(data);
                                    data = null;
                                    isItem = false;
                                }
                                break;
                        }

                        Log.d("RSSFeeder","tag : " + name);

                        eventType = xpp.next();
                    }
                }
                catch(Exception exc)
                {
                    exc.printStackTrace();
                }
            }
        }.start();
    }

    private void addItem(final RSSData item)
    {
        ((Activity)getContext()).runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                add(item);
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)_context).getLayoutInflater();
            row = inflater.inflate(_resourceID, parent, false);
        }

        RSSData data = getItem(position);
        ImageView imageView = (ImageView)row.findViewById(R.id.img);
//        TextView textView = (TextView)row.findViewById(R.id.txt);
        Log.d("RSSFeeder", "Count : " + position + " : " + getCount() + " : " + data.URL);

        if(_bitmaps.size() > position)
        {
            ImageDownloader id = _bitmaps.get(position);
//            textView.setText(data.URL);
            Bitmap b = id.getBitmap();
            if(b != null)
                imageView.setImageBitmap(b);
            else
                id.setImageView(imageView);
        }

        return row;
    }

    public class RSSData
    {
        public String URL;
        public int Width;
        public int Height;
        public String Copyright;
        public String Title;
        public String Description;
        public String Thumbnail;
    }
}
