package maqs.ehs.form;

import maqs.ehs.patient.Field;
import maqs.ehs.util.AllowBlankFieldMaskFormatter;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

class DateFormField extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isDateField();
    }

    protected MedicsFormRow assemble() {
        DateFormField formRow = new DateFormField();
        formRow.formManager = this.formManager;
        formRow.sourceField = this.sourceField;

        MedicsLabelValueFormRow labelValueFormRow = formManager.createLabelValueFormRow();
        labelValueFormRow.setRequired( sourceField.getField().isRequired() );

        //
        JLabel fieldtitleLbl = createLabel();
        labelValueFormRow.setLabelComponent( fieldtitleLbl );

        //
        JPanel valuePanel = createTextField();
        labelValueFormRow.setValueComponent( valuePanel, 1 );

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

    private JPanel createTextField() {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout( panel, BoxLayout.LINE_AXIS );
        panel.setLayout( layout );

        String fieldId = sourceField.getFieldId();
        String textValue = sourceField.getValue();
        String mask = sourceField.getField().getMask();

        final JTextField textField;
        try {
            String fieldMask = mask.replaceAll( "[a-z]|[A-Z][0-9]", "#" );
            MaskFormatter formatter = new AllowBlankFieldMaskFormatter( fieldMask );
            textField = new JFormattedTextField( formatter );
        } catch ( ParseException e ) {
            throw new RuntimeException( "Error rendering textfield!" );
        }

        textField.setText( textValue );
        textField.setName( fieldId );
        textField.setFont( formManager.getDefaultLabelFont() );
        if ( sourceField.getField().isNonUserEditable() ) {
            textField.setEditable( false );
        }

        panel.add( textField );

        final JXDatePicker datePicker = new JXDatePicker();
        datePicker.setFormats( mask );
        datePicker.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                textField.setText( datePicker.getFormats()[0].format( datePicker.getDate() ) );
            }
        } );
        datePicker.getEditor().setVisible( false );
        datePicker.getEditor().setEditable( false );
        datePicker.getEditor().setEnabled( false );
        datePicker.getEditor().setSize( 0, 0 );

        panel.add( datePicker );

        return panel;
    }
}