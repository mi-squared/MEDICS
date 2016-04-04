package maqs.ehs.patient;

import java.util.ArrayList;
import java.util.List;

public class ImportedLine {
    private String rawLine;
    private List args = new ArrayList();
    private List errors = new ArrayList();
    private String action;
    private ImportResults results;
    private int customerId;
    private int lineRowNumber;

    public ImportedLine( ImportResults _results ) {
        results = _results;
    }

    public String getAction() {
        return action;
    }

    public void setAction( String action ) {
        this.action = action;
    }

    public List getArgs() {
        return args;
    }

    public void setArgs( List args ) {
        this.args = args;
    }

    public List getErrors() {
        return errors;
    }

    public void setErrors( List errors ) {
        this.errors = errors;
    }

    public String getRawLine() {
        return rawLine;
    }

    public void setRawLine( String rawLine ) {
        this.rawLine = rawLine;
    }

    public boolean hasErrors() {
        return getErrors().size() > 0;
    }

    public String getArgAtPos( int position ) {
        return (String) getArgs().get( position - 1 );
    }

    public ImportResults getResults() {
        return results;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId( int customerId ) {
        this.customerId = customerId;
    }

    public int getLineRowNumber() {
        return lineRowNumber;
    }

    public void setLineRowNumber( int lineRowNumber ) {
        this.lineRowNumber = lineRowNumber;
    }
}
