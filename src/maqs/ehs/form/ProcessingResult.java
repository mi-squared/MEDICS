package maqs.ehs.form;

public class ProcessingResult {

    private ResultType resultType;
    private String originId;
    private String detail;

    public ProcessingResult( ResultType resultType, String originId, String detail ) {
        this.resultType = resultType;
        this.originId = originId;
        this.detail = detail;
    }

    public ResultType getResultType() {
        return resultType;
    }

    public void setResultType( ResultType resultType ) {
        this.resultType = resultType;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId( String originId ) {
        this.originId = originId;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail( String detail ) {
        this.detail = detail;
    }
}
