/*
 * ProgressListener.java
 *
 * Created on September 30, 2002, 12:55 PM
 */

package myipcserver;


/**
 *
 * @author  jsnell
 */
public interface ProgressListener {
   void percentDone(String msg, int percent);
}

