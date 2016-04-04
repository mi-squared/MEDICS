package maqs.ehs.form;

import com.sigilent.business.util.StringUtils;
import maqs.ehs.patient.*;
import maqs.ehs.util.AllowBlankFieldMaskFormatter;
import maqs.ehs.util.AppProperties;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.util.List;

class LinkedTextFormField extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isLinkedText();
    }

    protected MedicsFormRow assemble() {
        LinkedTextFormField formRow = new LinkedTextFormField();
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

        Dimension valueColumnDimensions = formManager.getValueColumnDimensions();

        textField.setSize( ( int ) ( valueColumnDimensions.getWidth() * 0.80 ), ( int ) valueColumnDimensions.getHeight() );
        FocusListener textFieldActionListener = new TextFieldFocusListener( textField, this.sourceField );
        textField.addFocusListener( textFieldActionListener );
        textField.addKeyListener( new TextFieldKeyTypedListener( textField, sourceField ) );

        JButton lookupButton = new JButton( new ImageIcon( AppProperties.getSeachImagePath() ) );
        lookupButton.setSize( ( int ) ( valueColumnDimensions.getWidth() * 0.20 ), ( int ) valueColumnDimensions.getHeight() );
        lookupButton.addActionListener( new LookUpButtonActionListener( textField, sourceField ) );

        panel.add( textField );
        panel.add( lookupButton );

        return panel;
    }

    class TextFieldKeyTypedListener implements KeyListener {

        JTextField textField;
        FieldSvc sourceField;

        TextFieldKeyTypedListener( JTextField textField, FieldSvc sourceField ) {
            this.textField = textField;
            this.sourceField = sourceField;
        }

        public void keyTyped( KeyEvent e ) {
            if ( e.getKeyChar() == KeyEvent.VK_ENTER ) {
                setFields( textField );
            }
            if ( e.getKeyCode() == 'L' && (e.getModifiers() & InputEvent.CTRL_MASK) != 0 ) {
                LinkedFieldLookupDialog lookupDialog = new LinkedFieldLookupDialog( this.textField, getLinkedTextField(), this.sourceField );
            }
        }

        public void keyPressed( KeyEvent e ) {
            if ( e.getKeyChar() == KeyEvent.VK_ENTER ) {
                setFields( textField );
            }
            if ( e.getKeyCode() == 'L' && (e.getModifiers() & InputEvent.CTRL_MASK) != 0 ) {
                LinkedFieldLookupDialog lookupDialog = new LinkedFieldLookupDialog( this.textField, getLinkedTextField(), this.sourceField );
            }
        }

        public void keyReleased( KeyEvent e ) {
        }

        private void setFields( JTextField parentField ) {
            if ( StringUtils.isEmpty( parentField.getText() ) ) {
                getLinkedTextField().setText( "" );
            } else {
                KeyedResource resource = FieldManager.getKeyedResource( sourceField.getField().getResourceId() );
                List<KeyedResourceEntry> hits = resource.searchResourceByKey( parentField.getText(), 1 );
                if ( !hits.isEmpty() ) {
                    KeyedResourceEntry resourceEntry = hits.get( 0 );
                    parentField.setText( resourceEntry.getKey() );
                    getLinkedTextField().setText( resourceEntry.getValue() );
                } else {
                    getLinkedTextField().setText( "" );
                }
            }
        }

        private JTextField getLinkedTextField() {
            Component parentContainer = formManager.getParentContainer();
            Component component = FormHelper.searchForFormField( ( ( JTabbedPane ) parentContainer ).getComponents(), this.sourceField.getField().getLinkedFieldId() );
            if ( component instanceof JTextField ) {
                return ( JTextField ) component;
            } else {
                throw new RuntimeException( "Field " + this.sourceField.getField().getLinkedFieldId() + " is not a text field!" );
            }
        }
    }

    class TextFieldFocusListener implements FocusListener {

        JTextField textField;
        private FieldSvc sourceField;

        TextFieldFocusListener( JTextField textField, FieldSvc sourceField ) {
            this.textField = textField;
            this.sourceField = sourceField;
        }

        public void focusGained( FocusEvent e ) {
        }

        public void focusLost( FocusEvent e ) {
            setFields( textField );
        }

        private void setFields( JTextField parentField ) {
            if ( !StringUtils.isEmpty( textField.getText() ) ) {
                KeyedResource resource = FieldManager.getKeyedResource( this.sourceField.getField().getResourceId() );
                List<KeyedResourceEntry> hits = resource.searchResourceByKey( parentField.getText(), 1 );
                if ( !hits.isEmpty() ) {
                    KeyedResourceEntry resourceEntry = hits.get( 0 );
                    parentField.setText( resourceEntry.getKey() );
                    getLinkedTextField().setText( resourceEntry.getValue() );
                } else {
                    parentField.setText( "" );
                    getLinkedTextField().setText( "" );
                }
            } else {
                getLinkedTextField().setText( "" );
            }
        }

        private JTextField getLinkedTextField() {
            Component parentContainer = formManager.getParentContainer();
            Component component = FormHelper.searchForFormField( ( ( JTabbedPane ) parentContainer ).getComponents(), this.sourceField.getField().getLinkedFieldId() );
            if ( component instanceof JTextField ) {
                return ( JTextField ) component;
            } else {
                throw new RuntimeException( "Field " + this.sourceField.getField().getLinkedFieldId() + " is not a text field!" );
            }
        }
    }

    private class LookUpButtonActionListener implements ActionListener {
        private JTextField textField;
        private FieldSvc sourceField;

        public LookUpButtonActionListener( JTextField textField, FieldSvc sourceField ) {
            this.textField = textField;
            this.sourceField = sourceField;
        }

        public void actionPerformed( ActionEvent e ) {
            LinkedFieldLookupDialog lookupDialog = new LinkedFieldLookupDialog( this.textField, getLinkedTextField(), this.sourceField );

        }

        private JTextField getLinkedTextField() {
            Component parentContainer = formManager.getParentContainer();
            Component component = FormHelper.searchForFormField( ( ( JTabbedPane ) parentContainer ).getComponents(), this.sourceField.getField().getLinkedFieldId() );
            if ( component instanceof JTextField ) {
                return ( JTextField ) component;
            } else {
                throw new RuntimeException( "Field " + this.sourceField.getField().getLinkedFieldId() + " is not a text field!" );
            }
        }

    }
}