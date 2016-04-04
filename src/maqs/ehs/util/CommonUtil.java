package maqs.ehs.util;

import com.sigilent.business.util.StringUtils;

public class CommonUtil {
    public static String sanitizeRecord( String record ) {
        if ( StringUtils.isEmpty( record ) ) {
            record = "none";
        } else {
            record = record.replaceAll( "[ \\t\\n\\r\\f\\v]", "" );
        }
        return record;
    }
}
