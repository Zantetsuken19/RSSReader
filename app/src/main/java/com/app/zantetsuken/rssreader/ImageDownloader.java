package com.app.zantetsuken.rssreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by arief on 4/18/16.
 */
public class ImageDownloader extends AsyncTask<String, Void, Bitmap>
{
    private ImageView _imageView;
    public void setImageView(ImageView imageView) { _imageView = imageView; }

    private Bitmap _bitmap;
    public Bitmap getBitmap() { return _bitmap; }

    public ImageDownloader()
    {

    }

    protected Bitmap doInBackground(String... urls)
    {
        String urlDisplay = urls[0];
        Bitmap bitmap = null;
        try
        {
            InputStream in = new java.net.URL(urlDisplay).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        }
        catch(Exception exc)
        {
            Log.e("Error", exc.getMessage());
            exc.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result)
    {
        if(_imageView != null)
            _imageView.setImageBitmap(result);
        _bitmap = result;
    }
}
