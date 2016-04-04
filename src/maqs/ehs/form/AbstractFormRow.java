package maqs.ehs.form;

import maqs.ehs.patient.FieldSvc;

import javax.swing.*;

abstract class AbstractFormRow implements MedicsFormRow {

    protected MedicsFormManager formManager;
    protected FieldSvc sourceField;
    protected JPanel rowPanel;

    public MedicsFormRow create( MedicsFormManager formManager, FieldSvc sourceField ) {
        // pre
        if ( sourceField == null ) {
            throw new RuntimeException( "No sourcefield provided" );
        }

        // pre
        if ( formManager == null ) {
            throw new RuntimeException( "No form manager provided" );
        }

        this.sourceField = sourceField;
        this.formManager = formManager;

        if ( !canCreate( sourceField.getField().getFieldType() ) ) {
            return null;    
        }

        return assemble();
    }

    protected abstract MedicsFormRow assemble();

    public JPanel getAsJPanel() {
        return rowPanel;
    }

}
