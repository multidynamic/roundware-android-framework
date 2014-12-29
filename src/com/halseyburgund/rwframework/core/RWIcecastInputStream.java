/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Copyright UnicaRadio
 */
package com.halseyburgund.rwframework.core;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Buffer input stream that pulls out icy meta data.
 * Adapted from code written by Paolo Cortis for UnicaRadio
 * https://code.google.com/p/unicaradio-apps/
 */
public class RWIcecastInputStream extends BufferedInputStream {
    private static final String CLASSNAME = RWIcecastInputStream.class.getName();

    private int metaint;
    private int bytesUntilNextInfos;
    private IcyMetaDataListener listener = null;


    public RWIcecastInputStream(InputStream in, int size, int metaint) {
        super(in, size);
        this.metaint = metaint;
        bytesUntilNextInfos = metaint;
    }

    public void setIcyMetaDataListener(IcyMetaDataListener listener){
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int read() throws IOException {
        if (bytesUntilNextInfos == 0) {
            getIcyInfos();
        }

        bytesUntilNextInfos--;
        return super.read();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        int ret;

        if (bytesUntilNextInfos == 0) {
            getIcyInfos();
        }

        int min = Math.min(bytesUntilNextInfos, len);
        ret = super.read(b, off, min);
        bytesUntilNextInfos -= ret;

        return ret;
    }

    private void getIcyInfos() throws IOException {
        bytesUntilNextInfos = metaint;
        int length = super.read() * 16;
        //Log.d(CLASSNAME, "Length:" + String.valueOf(length));
        if (length > 0) {
            String rawMetadata = readMetadata(length);
            Log.v(CLASSNAME, rawMetadata);

            // notify listener
            if(listener != null){
               listener.OnMetaDataReceived(rawMetadata);
            }
        }
    }

    /**
     * Splits a metadata value.
     *
     * Often a metadata value is formated like so: "Aritst - Title"
     * @param meta
     * @return
     */
    public static String[] splitMetaValue(String meta){
        return meta.split("\\s+-\\s+");
    }

    /**
     * Parse meta data into mapped name/value pairs
     * http://uniqueculture.net/2010/11/stream-metadata-plain-java/
     * @param metaString
     * @return
     */
    public static Map<String, String> parseMetadata(String metaString) {
        Map<String, String> metadata = new HashMap<String, String>();
        String[] metaParts = metaString.split(";");
        Pattern p = Pattern.compile("^([a-zA-Z]+)=\\'([^\\']*)\\'$");
        Matcher m;
        for (int i = 0; i < metaParts.length; i++) {
            m = p.matcher(metaParts[i]);
            if (m.find()) {
                metadata.put(m.group(1), m.group(2));
            }
        }

        return metadata;
    }

    //TODO maintain listener collection, allow for registration

    /**
     * @param length
     * @return
     * @throws java.io.IOException
     * @throws java.io.UnsupportedEncodingException
     */
    private String readMetadata(int length) throws IOException,
            UnsupportedEncodingException {

        byte[] metadata = new byte[length];
        super.read(metadata, 0, length);
		return new String(metadata, "UTF-8");
    }

    public interface IcyMetaDataListener{
        public void OnMetaDataReceived(String rawMetaData);
    }

}
