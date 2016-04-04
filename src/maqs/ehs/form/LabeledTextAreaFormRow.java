package maqs.ehs.form;

import com.sigilent.business.util.StringUtils;
import maqs.ehs.patient.Field;

import javax.swing.*;

class LabeledTextAreaFormRow extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isTextArea()
                && !StringUtils.isEmpty( sourceField.getField().getLabel() );
    }

    protected MedicsFormRow assemble() {
        LabeledTextAreaFormRow formRow = new LabeledTextAreaFormRow();
        formRow.formManager = this.formManager;
        formRow.sourceField = this.sourceField;

        MedicsLabelValueFormRow labelValueFormRow = formManager.createLabelValueFormRow();
        labelValueFormRow.setScrolling( true );
        labelValueFormRow.setRequired( sourceField.getField().isRequired() );

        //
        JLabel fieldtitleLbl = createLabel();
        labelValueFormRow.setLabelComponent( fieldtitleLbl );

        //
        JTextArea textAreaPanel = createTextAreaPanel();
        labelValueFormRow.setValueComponent( textAreaPanel, sourceField.getField().getTextRows() );

        formRow.rowPanel = labelValueFormRow.getAsJPanel();
        return formRow;
    }

    private JLabel createLabel() {
        JLabel fieldtitleLbl = new JLabel();
        fieldtitleLbl.setText( sourceField.getField().getLabel() );
        fieldtitleLbl.setFont( sourceField.getField().isRequired() ? formManager.getRequiredLabelFont() : formManager.getDefaultLabelFont() );
        fieldtitleLbl.setFocusable( false );

        if ( sourceField.getField().isRequired() ) {
            final JLabel requiredAsterixLbl = new JLabel();
            requiredAsterixLbl.setText( "*" );
            requiredAsterixLbl.setFont( formManager.getRequiredAsterixFont() );
            requiredAsterixLbl.setFocusable( false );
        }
        return fieldtitleLbl;
    }

    private JTextArea createTextAreaPanel() {
        JTextArea textArea = new JTextArea();
        textArea.setName( sourceField.getFieldId() );
        textArea.setText( sourceField.getValue() );
        textArea.setRows( sourceField.getField().getTextRows() );
        textArea.setEditable( true );
        textArea.setFocusable( true );
        textArea.setLineWrap( true );
        textArea.setOpaque( true );
        textArea.setWrapStyleWord( true );
        textArea.setFont( formManager.getTextStyle( sourceField.getField() ) );

        if ( sourceField.getField().isNonUserEditable() ) {
            textArea.setEditable( false );
        }

        FormHelper.invertFocusTraversalBehaviour( textArea );
        return textArea;
    }
}