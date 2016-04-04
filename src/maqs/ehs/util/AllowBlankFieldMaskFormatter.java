package maqs.ehs.util;

import javax.swing.text.MaskFormatter;
import java.text.ParseException;

/**
 * A special version of the {@link javax.swing.text.MaskFormatter} for
 * {@link javax.swing.JFormattedTextField formatted text fields} that supports
 * the field being emptied/left blank.
 *
 * @author R.J. Lorimer
 */
public class AllowBlankFieldMaskFormatter extends MaskFormatter {

    private boolean allowBlankField = true;
    private String blankRepresentation;

    public AllowBlankFieldMaskFormatter() {
        super();
        setPlaceholderCharacter( '_' );
    }

    public AllowBlankFieldMaskFormatter( String mask ) throws ParseException {
        super( mask );
        setPlaceholderCharacter( '_' );
    }

    public void setAllowBlankField( boolean allowBlankField ) {
        this.allowBlankField = allowBlankField;
    }

    public boolean isAllowBlankField() {
        return allowBlankField;
    }

    /**
     * Update our blank representation whenever the mask is updated.
     */
    @Override
    public void setMask( String mask ) throws ParseException {
        super.setMask( mask );
        updateBlankRepresentation();
    }

    /**
     * Update our blank representation whenever the mask is updated.
     */
    @Override
    public void setPlaceholderCharacter( char placeholder ) {
        super.setPlaceholderCharacter( placeholder );
        updateBlankRepresentation();
    }

    /**
     * Override the stringToValue method to check the blank representation.
     */
    @Override
    public Object stringToValue( String value ) throws ParseException {
        Object result = value;
        if ( isAllowBlankField() && blankRepresentation != null && blankRepresentation.equals( value ) ) {
            // an empty field should have a 'null' value.
            result = null;
        } else {
            result = super.stringToValue( value );
        }
        return result;
    }

    private void updateBlankRepresentation() {
        try {
            // calling valueToString on the parent class with a null attribute will get the 'blank'
            // representation.
            blankRepresentation = valueToString( null );
        }
        catch ( ParseException e ) {
            blankRepresentation = null;
        }
    }

    public String getBlankRepresentation() {
        return blankRepresentation;
    }
}
