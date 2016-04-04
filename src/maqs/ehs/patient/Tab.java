package maqs.ehs.patient;

public class Tab {

    private String id;
    private String title;
    private int maxRowsPerColumn;

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }


    public int getMaxRowsPerColumn() {
        return maxRowsPerColumn;
    }

    public void setMaxRowsPerColumn( int maxRowsPerColumn ) {
        this.maxRowsPerColumn = maxRowsPerColumn;
    }
}
