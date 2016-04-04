package maqs.ehs.form;

import maqs.ehs.patient.Field;

import javax.swing.*;
import java.awt.*;

class PureTextFormRow extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isPureText();
    }

    protected MedicsFormRow assemble() {
        PureTextFormRow row = new PureTextFormRow();
        MedicsBlockFormRow formRow = formManager.createBlockFormRow();
        formRow.setValueComponent( createTextAreaPanel(), sourceField.getField().getTextRows() );
        row.rowPanel = formRow.getAsJPanel();

        return row;
    }

    private Component createTextAreaPanel() {
        JTextArea textArea = new JTextArea();
        textArea.setName( "" );
        textArea.setText( sourceField.getField().getLabel() );
        textArea.setRows( sourceField.getField().getTextRows() );
        textArea.setEditable( false );
        textArea.setFocusable( false );
        textArea.setLineWrap( false );
        textArea.setOpaque( false );
        textArea.setWrapStyleWord( true );
        textArea.setFont( formManager.getTextStyle( sourceField.getField() ) );
        FormHelper.invertFocusTraversalBehaviour( textArea );
        return textArea;
    }

}
