package maqs.ehs.form;

import maqs.ehs.patient.Field;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

class DropDownFormField extends AbstractFormRow{

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isDropDown();
    }

    protected MedicsFormRow assemble() {
        DropDownFormField formRow = new DropDownFormField();
        formRow.formManager = this.formManager;
        formRow.sourceField = this.sourceField;

        MedicsLabelValueFormRow labelValueFormRow = formManager.createLabelValueFormRow();
        labelValueFormRow.setRequired( sourceField.getField().isRequired() );

        //
        JLabel fieldtitleLbl = createLabel();
        labelValueFormRow.setLabelComponent( fieldtitleLbl );

        //
        JComboBox textField = createDropDown();
        labelValueFormRow.setValueComponent( textField, 1 );

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

    private JComboBox createDropDown() {
        String fieldId = sourceField.getFieldId();
        List<String> options = sourceField.getField().getValueList();
        String value = sourceField.getValue();

        // create column
        JComboBox comboBox = new JComboBox();
        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
        Dimension dimMax = new Dimension( 1, ( int ) comboBox.getSize().getHeight() );
        comboBox.setMaximumSize( dimMax );

        if ( options == null ) {
            options = new ArrayList<String>();
        }
        for ( String option : options ) {
            comboModel.addElement( option );
            if ( value.equals( option ) ) {
                comboModel.setSelectedItem( option );
            }
        }

        comboBox.setModel( comboModel );
        comboBox.setName( fieldId );
        comboBox.setFont( formManager.getDefaultLabelFont() );

        if ( sourceField.getField().isNonUserEditable() ) {
            comboBox.setEditable( false );
        }

        return comboBox;
    }
}