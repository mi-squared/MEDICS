package maqs.ehs.form;

import maqs.ehs.patient.FieldSvc;
import maqs.ehs.patient.Field;

import javax.swing.*;

public interface MedicsFormRow {

    public MedicsFormRow create( MedicsFormManager formManager, FieldSvc sourceField );

    public JPanel getAsJPanel();

    public boolean canCreate( Field.FieldType fieldType );

}
