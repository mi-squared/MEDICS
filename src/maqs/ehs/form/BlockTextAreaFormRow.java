package maqs.ehs.form;

import com.sigilent.business.util.StringUtils;
import maqs.ehs.patient.Field;

import javax.swing.*;

class BlockTextAreaFormRow extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isTextArea()
                && StringUtils.isEmpty( sourceField.getField().getLabel() );
    }

    protected MedicsFormRow assemble() {
        BlockTextAreaFormRow row = new BlockTextAreaFormRow();
        MedicsBlockFormRow formRow = formManager.createBlockFormRow();
        formRow.setScrolling( true );
        formRow.setValueComponent( createTextAreaPanel(), sourceField.getField().getTextRows() );
        row.rowPanel = formRow.getAsJPanel();
        return row;
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
        FormHelper.invertFocusTraversalBehaviour( textArea );
        return textArea;
    }

}
