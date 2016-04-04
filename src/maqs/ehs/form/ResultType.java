package maqs.ehs.form;

public enum ResultType {

    FATAL( 3, "Error" ),
    WARNING( 2, "Warning" ),
    INFO( 1, "Info" );

    private int severity;
    private String description;

    ResultType( int severity, String description ) {
        this.severity = severity;
        this.description = description;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isFatal() {
        return this == FATAL;
    }

    public boolean isWarning() {
        return this == FATAL;
    }

    public boolean isInfo() {
        return this == INFO;
    }

    public String getDescription() {
        return description;
    }
}
