package maqs.ehs.patient;

public class XmlTest {

    public static void main( String s[] ) {

        PatientRecord patientRecord = PatientManager.read( "dummy.xml" );
        System.out.println( patientRecord );
    }

}
