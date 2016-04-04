package maqs.ehs.patient;

public class PositionFieldMapping {
    private String fieldId;
    private ColumnValidator columnValidator;


    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId( String fieldId ) {
        this.fieldId = fieldId;
    }

    public ColumnValidator getColumnValidator() {
        return columnValidator;
    }

    public void setColumnValidator( ColumnValidator columnValidator ) {
        this.columnValidator = columnValidator;
    }
}
