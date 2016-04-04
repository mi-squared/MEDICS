package maqs.ehs.form;

import java.util.*;

public class DefaultResultCollector implements ResultCollector {

    private Map<ResultType, List<ProcessingResult>> results = new HashMap<ResultType, List<ProcessingResult>>();

    public void addResult( ProcessingResult result ) {
        List<ProcessingResult> resultList = results.get( result.getResultType() );
        if ( resultList == null ) {
            resultList = new ArrayList<ProcessingResult>();

            results.put( result.getResultType(), resultList );
        }
        resultList.add( result );
    }

    public boolean hasResultOfType( ResultType resultType ) {
        List<ProcessingResult> resultList = results.get( resultType );
        return resultList != null || !resultList.isEmpty();
    }

    public void clear() {
        results.clear();
    }

    public void clear( ResultType resultType ) {
        results.remove( resultType );
    }

    public boolean hasResultsWithHigherSeverityThan( ResultType resultType ) {
        for ( ResultType cachedResult : results.keySet() ) {
            if ( cachedResult.getSeverity() > resultType.getSeverity() ) {
                if ( hasResultOfType( cachedResult ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasResultFor( ResultType resultType, String originId ) {
        List<ProcessingResult> resultList = results.get( resultType );
        if ( resultList == null ) {
            return false;
        }
        for ( ProcessingResult result : resultList ) {
            if ( result.getOriginId().equals( originId ) ) {
                return true;
            }
        }
        return false;
    }

    public List<ProcessingResult> getResultsWithHigherSeverityThan( ResultType resultType ) {
        List<ProcessingResult> returned = new ArrayList<ProcessingResult>();
        for ( ResultType cachedResult : results.keySet() ) {
            if ( cachedResult.getSeverity() > resultType.getSeverity() ) {
                if ( hasResultOfType( cachedResult ) ) {
                    returned.addAll( results.get( cachedResult ) );
                }
            }
        }

        Collections.sort( returned, new Comparator() {
            public int compare( Object o1, Object o2 ) {
                ProcessingResult result1 = ( ProcessingResult ) o1;
                ProcessingResult result2 = ( ProcessingResult ) o2;
                if ( result1.getResultType().getSeverity() == result2.getResultType().getSeverity() ) {
                    return 0;
                } else if ( result1.getResultType().getSeverity() < result2.getResultType().getSeverity() ) {
                    return 1;
                } else {
                    return -1;
                }
            }
        } );

        return returned;
    }

    public List<ProcessingResult> getResultsOfSeverity( ResultType resultType ) {
        List<ProcessingResult> returned = new ArrayList<ProcessingResult>();
        for ( ResultType cachedResult : results.keySet() ) {
            if ( cachedResult.getSeverity()== resultType.getSeverity() ) {
                if ( hasResultOfType( cachedResult ) ) {
                    returned.addAll( results.get( cachedResult ) );
                }
            }
        }
        return returned;
    }

}
