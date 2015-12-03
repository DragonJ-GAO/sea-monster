package me.add1.common;

import android.net.Uri;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by dragonj on 15/10/30.
 */
public class UriUtils {
    public static Map<String, String> getAllQueryParams(Uri uri) {
        Map<String, String> params = new HashMap<>();
        if (uri.isOpaque()) {
            return params;
        }
        String query = uri.getEncodedQuery();
        if (query == null) {
            return params;
        }

        Set<String> names = new LinkedHashSet<String>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(Uri.decode(name));

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        for (String item :names) {
            params.put(item, uri.getQueryParameter(item));
        }

        return params;
    }

}
