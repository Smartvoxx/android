package net.noratek.smartvoxx.common.utils;

/**
 * Created by eloudsa on 06/09/15.
 */
public class Utils {


    public static String getLastPartUrl(String url) {

        if (url == null) {
            return "";
        }

        String href = url.trim();
        if (href == "") {
            return "";
        }


        if (href.endsWith("/")) {
            // remove last trailing slash
            href = href.substring(0, href.length()-1);
        }

        return(href.substring(href.lastIndexOf("/") + 1,href.length()));
    }


}
