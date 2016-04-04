package maqs.ehs.patient;

import com.sigilent.business.util.xml.XMLWrapper;
import com.sigilent.business.util.xml.XMLWrapperException;

import java.io.File;
import java.util.List;

public class XmlWalking {

    public static void main( String s[] ) {

        XMLWrapper wrapper;
        try {
            wrapper = new XMLWrapper( new File( "C:\\projects\\ehs\\test\\maqs\\ehs\\patient\\blank.xml" ) );
        } catch ( XMLWrapperException e ) {
            throw new RuntimeException( e );
        }

    }
}
