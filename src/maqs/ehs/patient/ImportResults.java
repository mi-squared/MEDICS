package maqs.ehs.patient;

import com.sigilent.business.util.StringUtils;

import java.util.List;
import java.util.ArrayList;

public class ImportResults {

    private String errorFileName = "";
    private int failureCtr = 0;
    private int successCtr = 0;
    private List<ImportedLine> importedLines = new ArrayList<ImportedLine>();
    private StringBuffer globalImportError = new StringBuffer();

    public String getErrorFileName() {
        return errorFileName;
    }

    public void incrementFailureCtr() {
        failureCtr++;        
    }

    public int getFailureCtr() {
        return failureCtr;
    }

    public void setErrorFileName( String errorFileName ) {
        this.errorFileName = errorFileName;
    }

    public void incrementSuccessCtr() {
        successCtr++;
    }

    public int getSuccessCtr() {
        return successCtr;
    }


    public List<ImportedLine> getImportedLines() {
        return importedLines;
    }

    public void setImportedLines( List<ImportedLine> importedLines ) {
        this.importedLines = importedLines;
    }


    public StringBuffer getGlobalImportError() {
        return globalImportError;
    }

    public void setGlobalImportError( StringBuffer globalImportError ) {
        this.globalImportError = globalImportError;
    }

    public boolean hasErrors() {
        return getFailureCtr() > 0 || !StringUtils.isEmpty( globalImportError );
    }
}
