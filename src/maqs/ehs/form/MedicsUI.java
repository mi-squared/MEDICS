package maqs.ehs.form;

public interface MedicsUI {
    
    void init();

    void activateHomePane();

    void listPatients();

    void editPatient( String recordName );

    void printRecord();

    void maximizeView();

    boolean isWorkingWithPatient();

    void clearPatient();

    void createNewPatient();

    void quit();

    void savePatient();
}
