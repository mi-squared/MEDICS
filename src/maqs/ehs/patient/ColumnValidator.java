package maqs.ehs.patient;

public class ColumnValidator {
    private String regex;
    private String description;

    public ColumnValidator( String _regex, String _description ) {
        setRegex( _regex );
        setDescription( _description );
    }

    public String getDescription() {
        return description;
    }

    public void setDescription( String description ) {
        this.description = description;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex( String regex ) {
        this.regex = regex;
    }

}