package maqs.ehs.patient;

import com.sigilent.business.util.xml.XMLWrapper;
import com.sigilent.business.util.xml.XMLWrapperException;
import com.sigilent.business.util.StringUtils;

public class FieldSvc {

    private Field field;

    private FieldSvc( Field _field ) {
        field = _field;
    }

    public static FieldSvc get( Field _field ) {
        return new FieldSvc( _field );
    }

    public void setValue( XMLWrapper wrapper ) {
        String id = field.getXpath();
        if ( StringUtils.isEmpty( id ) ) {
            setValue( "" );
            return;
        }
        
        try {
            String value = wrapper.getNodeTextValue( id );
            setValue( value );
        } catch ( XMLWrapperException e ) {
//            throw new RuntimeException( "Cannot find '" + id + "': " + e );
            setValue( "" );
        }
    }

    public void setValue( String _value ) {
        field.setValue( _value );
    }

    public String getValue( XMLWrapper wrapper ) {
        setValue( wrapper );
        return getValue();
    }

    public String getValue() {
        return field.getValue();
    }

    public Field getField() {
        return field;
    }

    public String getFieldId() {
        return field.getId();
    }

}
