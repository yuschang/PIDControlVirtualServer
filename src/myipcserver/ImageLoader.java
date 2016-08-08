/*
 * ImageLoader.java
 *
 * Created on January 13, 2003, 12:25 PM
 */

package myipcserver;


import java.io.File;

/**
 *
 * @author  jsnell
 */
public interface ImageLoader {
    public int probe(File file);
    public ImageVolume load(File file, ProgressListener listener);
}
