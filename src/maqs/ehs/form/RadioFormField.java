package maqs.ehs.form;

import maqs.ehs.patient.Field;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

class RadioFormField extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isRadio();
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
        JPanel textField = createRadioPanel();
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

    protected JPanel createRadioPanel() {
        String fieldId = sourceField.getFieldId();
        List<String> options = sourceField.getField().getValueList();
        String value = sourceField.getValue();

        // build column
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        radioPanel.setMaximumSize( formManager.getSingleColumnRowDimensions() );

        // add invisible textlabel
        final JLabel label1 = new JLabel();
        label1.setText( value );
        label1.setName( fieldId );
        label1.setVisible( false );

        ButtonGroup buttonGroup = new ButtonGroup();
        final ItemListener selectionListener = new ItemListener() {
            public void itemStateChanged( ItemEvent e ) {
                JRadioButton button = ( JRadioButton ) e.getItem();
                label1.setText( button.getText() );
            }
        };

        for ( String option : options ) {

            JRadioButton mRadioButton = new JRadioButton();
            mRadioButton.setText( option );
            mRadioButton.setVerticalAlignment( JRadioButton.TOP );
            mRadioButton.setVerticalTextPosition( JRadioButton.TOP );
            buttonGroup.add( mRadioButton );

            radioPanel.add( mRadioButton );

            mRadioButton.addItemListener( selectionListener );

            if ( value.equals( option ) ) {
                buttonGroup.setSelected( mRadioButton.getModel(), true );
            }
        }

        return radioPanel;
    }

}
