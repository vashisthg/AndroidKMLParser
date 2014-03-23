package com.vashisthg.androidkml;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


/**
 * Created by vashisthg on 23/03/14.
 */

public class KmlReader {


    public static interface Callback {
        public void onDocumentParsed(List<Placemark> placemarks);
    }

    private static final String LOGTAG = "KMLReader";
    private Callback callback;

    public KmlReader(Callback callback) {
        this.callback = callback;
    }

    public void read(InputStream inputStream) throws IOException, XmlPullParserException {
        ReaderTask task = ReaderTask.getReaderTask(inputStream, callback);
        task.execute();
    }

    public static class ReaderTask extends AsyncTask<Void, Void, List<Placemark>> {

        private static final String DOCUMENT_TAG = "Document";
        private static final String PLACEMARK_TAG = "Placemark";
        private static final String PLACEMARK_NAME_TAG = "name";
        private static final String PLACEMARK_POINT_TAG = "Point";
        private static final String PLACEMARK_POINT_COORDINATES_TAG = "coordinates";
        private static final String STYLE_TAG = "Style";
        private static final String STYLE_MAP_TAG = "StyleMap";
        private static final String STYLE_ICON_SCALE_TAG = "scale";
        private static final String STYLE_ICON_HREF_TAG = "href";
        private static final String PLACEMARK_DESCRIPTION_TAG = "description";
        private static final String PLACEMARK_ADDRESS_TAG = "address";
        private static final String PLACEMARK_SNIPPET_TAG = "Snippet";
        private static final String PLACEMARK_XDATA_TAG = "ExtendedData";
        private static final String PLACEMARK_STYLE_URL_TAG = "styleUrl";
        private static final String STYLE_MAP_PAIR_TAG = "Pair";
        private static final String KEY_TAG = "key";
        private static final String POLYGON_TAG = "Polygon";
        public static final String LINE_STRING_TAG = "LineString";
        private static final String LINE_STYLE_TAG = "LineStyle";
        private static final String POLY_STYLE_TAG = "PolyStyle";
        private static final String COLOR_TAG = "color";
        private static final String WIDTH_TAG = "width";
        private static final String DATA_TAG = "Data";
        private static final String VALUE_TAG = "value";
        private static final String KML_TAG = "kml";


        private boolean isDocument;
        private boolean isPlacemark;
        private boolean isPlacemarkName;
        private boolean isPlacemarkDescription;
        private boolean isPlacemarkPoint;
        private boolean isPlacemarkCoordinates;

        private String text;

        private Placemark placemark;

        private List<Placemark> placemarkList;

        private Callback callback;

        InputStream inputStream;
        public static ReaderTask getReaderTask(InputStream inputStream, Callback callback) {
            ReaderTask task = new ReaderTask();
            task.inputStream = inputStream;
            task.callback = callback;
            return task;
        }

        @Override
        protected List<Placemark> doInBackground(Void... params) {
            try {
                read(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            return placemarkList;
        }

        @Override
        protected void onPostExecute(List<Placemark> placemarks) {
            super.onPostExecute(placemarks);
            callback.onDocumentParsed(placemarks);
        }

        public void read(InputStream inputStream) throws IOException, XmlPullParserException {
            XmlPullParserFactory factory = XmlPullParserFactory
                    .newInstance();
            factory.setValidating(false);
            XmlPullParser myxml = factory.newPullParser();

            myxml.setInput(inputStream, null);
            processDocument(myxml);
        }

        protected void processDocument(XmlPullParser xpp)
                throws XmlPullParserException, IOException {

            placemarkList = new ArrayList<Placemark>();
            int eventType = xpp.getEventType();

            String text = null;
            do {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    Log.d(LOGTAG, "Start document");
                } else if (eventType == XmlPullParser.END_DOCUMENT) {

                    Log.d(LOGTAG, "End document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    processStartElement(xpp);
                } else if (eventType == XmlPullParser.END_TAG) {
                    processEndElement(xpp, text);
                } else if (eventType == XmlPullParser.TEXT) {
                    processText(xpp);
                }
                eventType = xpp.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);
        }

        protected void processStartElement(XmlPullParser xpp) {
            String name = xpp.getName();
            processName(name, true);

        }

        private void processName(String name, boolean isStart) {
            if(DOCUMENT_TAG.equals(name)) {
                isDocument = isStart;
            }

            if(PLACEMARK_TAG.equals(name) && isDocument) {
                isPlacemark = isStart;
                if(isStart) {
                    placemark = new Placemark();
                } else {
                    placemarkList.add(placemark);
                }
            }

            if(PLACEMARK_NAME_TAG.equals(name) && isPlacemark) {
                isPlacemarkName = isStart;
            }

            if(PLACEMARK_DESCRIPTION_TAG.equals(name) && isPlacemark) {
                isPlacemarkDescription = isStart;
            }

            if(PLACEMARK_POINT_TAG.equals(name) && isPlacemark) {
                isPlacemarkPoint = isStart;
            }

            if(PLACEMARK_POINT_COORDINATES_TAG.equals(name) && isPlacemarkPoint) {
                isPlacemarkCoordinates = isStart;
            }


        }

        protected void processEndElement(XmlPullParser xpp, String text) {
            String name = xpp.getName();
            processName(name, false);

        }

        protected void processText(XmlPullParser xpp) throws XmlPullParserException {
            String text = xpp.getText();
            if(text.trim().equals("\n")) {
                return;
            }
            if(isPlacemark && isPlacemarkName) {
                placemark.setName(text);
            }

            if(isPlacemark && isPlacemarkDescription) {
                placemark.setDescription(text);
            }

            if(isPlacemark && isPlacemarkCoordinates) {
                Point point = new Point();
                String[] coordinates = text.split(",");
                point.setLongitude(Double.valueOf(coordinates[0]));
                point.setLatitude(Double.valueOf(coordinates[1]));
                point.setHeight(Double.valueOf(coordinates[2]));
                placemark.setPoint(point);
            }
        }
    }
}