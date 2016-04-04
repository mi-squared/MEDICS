package maqs.ehs.form;

import com.sigilent.business.util.StringUtils;
import maqs.ehs.patient.Field;
import maqs.ehs.util.AllowBlankFieldMaskFormatter;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

class TextFormField extends AbstractFormRow{

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isText();
    }

    protected MedicsFormRow assemble() {
        TextFormField formRow = new TextFormField();
        formRow.formManager = this.formManager;
        formRow.sourceField = this.sourceField;

        MedicsLabelValueFormRow labelValueFormRow = formManager.createLabelValueFormRow();
        labelValueFormRow.setRequired( sourceField.getField().isRequired() );

        //
        JLabel fieldtitleLbl = createLabel();
        labelValueFormRow.setLabelComponent( fieldtitleLbl );

        //
        JTextField textField = createTextField();
        labelValueFormRow.setValueComponent( textField, 1 );

        formRow.rowPanel = labelValueFormRow.getAsJPanel();
        return formRow;
    }

    private JLabel createLabel() {
        JLabel fieldtitleLbl = new JLabel();
        fieldtitleLbl.setText( sourceField.getField().getLabel() );
        fieldtitleLbl.setFont( sourceField.getField().isRequired() ? formManager.getRequiredLabelFont() : formManager.getDefaultLabelFont() );
        fieldtitleLbl.setFocusable( false );
        return fieldtitleLbl;
    }

    private JTextField createTextField() {
        String fieldId = sourceField.getFieldId();
        String textValue = sourceField.getValue();
        String mask = sourceField.getField().getMask();

        JTextField textField;
        if ( StringUtils.isEmpty( mask ) ) {
            textField = new JTextField();
        } else {
            try {
                MaskFormatter formatter = new AllowBlankFieldMaskFormatter( mask );
                textField = new JFormattedTextField( formatter );
            } catch ( ParseException e ) {
                throw new RuntimeException( "Error rendering textfield!" );
            }
        }

        textField.setText( textValue );
        textField.setName( fieldId );
        textField.setFont( formManager.getDefaultLabelFont() );
        if ( sourceField.getField().isNonUserEditable() ) {
            textField.setEditable( false );
        }

        return textField;
    }
}
