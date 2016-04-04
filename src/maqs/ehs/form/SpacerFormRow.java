package maqs.ehs.form;

import maqs.ehs.patient.Field;

class SpacerFormRow extends AbstractFormRow {

    public boolean canCreate( Field.FieldType fieldType ) {
        return fieldType.isSpacer();
    }

    protected MedicsFormRow assemble() {
        SpacerFormRow row = new SpacerFormRow();
        row.rowPanel = formManager.createSpacerFormRow();
        return row;
    }

    public MedicsFormRow create() {
        // pre
        if ( sourceField == null ) {
            throw new RuntimeException( "No sourcefield provided" );
        }

        // pre
        if ( formManager == null ) {
            throw new RuntimeException( "No form manager provided" );
        }

        return assemble();
    }

}
