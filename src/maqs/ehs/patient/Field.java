package maqs.ehs.patient;

import java.util.List;

public class Field {

    public enum FieldType {
        TEXT,
        DROPDOWN,
        DROPDOWN_EDITABLE,
        TEXTAREA,
        RADIO,
        SPACER,
        PURETEXT,
        LINESPACER,
        COLUMNBREAK,
        LINKEDTEXT,
        DATE;

        public boolean isText() {
            return this == TEXT;
        }

        public boolean isPureText() {
            return this == PURETEXT;
        }

        public boolean isDropDown() {
            return this == DROPDOWN;
        }

        public boolean isDropDownEditable() {
            return this == DROPDOWN_EDITABLE;
        }

        public boolean isTextArea() {
            return this == TEXTAREA;
        }

        public boolean isRadio() {
            return this == RADIO;
        }

        public boolean isSpacer() {
            return this == SPACER;
        }

        public boolean isLineSpacer() {
            return this == LINESPACER;
        }

        public boolean isColumnBreak() {
            return this == COLUMNBREAK;
        }

        public boolean isLinkedText() {
            return this == LINKEDTEXT;
        }

        public boolean isDateField() {
            return this == DATE;
        }

        public static FieldType get( String str ) {
            for ( FieldType type : FieldType.values() ) {
                if ( type.toString().equals( str ) ) {
                    return type;
                }
            }
            return null;
        }
    }

    private String tabId;
    private String xpath;
    private int textRows;
    private String id;
    private String label;
    private String defaultValue;
    private List<String> valueList;
    private String value = "";
    private FieldType fieldType;
    private String textStyle;
    private String mask;
    private boolean required;
    private String linkedFieldId;
    private boolean nonUserEditable;
    private String resourceId;

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel( String label ) {
        this.label = label;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue( String defaultValue ) {
        this.defaultValue = defaultValue;
    }

    public List<String> getValueList() {
        return valueList;
    }

    public void setValueList( List<String> valueList ) {
        this.valueList = valueList;
    }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }


    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType( FieldType fieldType ) {
        this.fieldType = fieldType;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath( String xpath ) {
        this.xpath = xpath;
    }


    public String getTabId() {
        return tabId;
    }

    public void setTabId( String tabId ) {
        this.tabId = tabId;
    }


    public int getTextRows() {
        return textRows;
    }

    public void setTextRows( int textRows ) {
        this.textRows = textRows;
    }


    public String getTextStyle() {
        return textStyle;
    }

    public void setTextStyle( String textStyle ) {
        this.textStyle = textStyle;
    }

    public String getMask() {
        return mask;
    }

    public void setMask( String mask ) {
        this.mask = mask;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired( boolean required ) {
        this.required = required;
    }

    public String getLinkedFieldId() {
        return linkedFieldId;
    }

    public void setLinkedFieldId( String linkedFieldId ) {
        this.linkedFieldId = linkedFieldId;
    }

    public boolean isNonUserEditable() {
        return nonUserEditable;
    }

    public void setNonUserEditable( boolean nonUserEditable ) {
        this.nonUserEditable = nonUserEditable;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId( String resourceId ) {
        this.resourceId = resourceId;
    }
}
