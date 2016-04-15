package com.app.zantetsuken.rssreader;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by Zantetsuken on 2/24/2016.
 */
public class RSSListAdapter extends ArrayAdapter<String>
{
    private String _url = null;
    public String getURL()
    {
        return _url;
    }
    public void setURL(String url)
    {
        _url = url;
    }

    public RSSListAdapter(Context context, String url)
    {
        super(context, android.R.layout.simple_list_item_1);
        _url = url;
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
                    while(eventType != XmlPullParser.END_DOCUMENT)
                    {
                        String name = xpp.getName();
                        switch(eventType)
                        {
                            case XmlPullParser.START_TAG:
                                if(name.equalsIgnoreCase("item"))
                                    isItem = true;
                                if(isItem && name.equalsIgnoreCase("title"))
                                    addItem(xpp.nextText());
                                break;
                            case XmlPullParser.END_TAG:
                                if(xpp.getName().equalsIgnoreCase("item"))
                                    isItem = false;
                                break;
                        }

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

    private void addItem(final String item)
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
}
