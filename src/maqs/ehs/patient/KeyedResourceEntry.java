package maqs.ehs.patient;

public class KeyedResourceEntry {

    private String value;
    private String key;

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public String toString(){
        return "[KeyResourceEntry] { " + key + " = " + value + " }";
    }
}
