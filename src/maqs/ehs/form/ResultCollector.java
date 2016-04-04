package maqs.ehs.form;

import java.util.List;


public interface ResultCollector {
    void addResult( ProcessingResult result );

    boolean hasResultOfType( ResultType resultType );

    void clear();

    void clear( ResultType resultType );

    boolean hasResultsWithHigherSeverityThan( ResultType resultType );

    List<ProcessingResult> getResultsWithHigherSeverityThan( ResultType resultType );

    List<ProcessingResult> getResultsOfSeverity( ResultType resultType );

    boolean hasResultFor( ResultType resultType, String originId );
}
