package maqs.ehs.form;

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.sigilent.business.util.StringUtils;
import maqs.ehs.patient.*;
import maqs.ehs.util.AppProperties;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class PdfApi extends PdfPageEventHelper implements PdfCloseListener {
    private File pdfFile;
    private boolean deleteOnView;

    public PdfApi() {
    }

    private String targetRecordName;
    private PatientRecord targetRecord;

    public PatientRecord getTargetRecord() {
        return targetRecord;
    }

    public void setTargetRecord( PatientRecord targetRecord ) {
        this.targetRecord = targetRecord;
    }

    public String getTargetRecordName() {
        return targetRecordName;
    }

    public void setTargetRecordName( String targetRecordName ) {
        this.targetRecordName = targetRecordName;
    }

    public File work() throws Exception {
        if ( targetRecord == null ) {
            targetRecord = PatientManager.read( targetRecordName );

        }
        Collection<Field> list = targetRecord.getFields();

        Document document = new Document();
        document.setMargins( 5f, 5f, 40f, 40f );

        File output = new File( AppProperties.getTempDirPath() + "/" + targetRecord.getRecordName() + "_" + System.currentTimeMillis() + ".pdf" );
        output.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream( output );
        PdfWriter writer = PdfWriter.getInstance( document, fileOutputStream );
        writer.setPageEvent( this );
        document.open();


        List<Tab> tabs = TabSvc.getTabs();
        for ( Tab tab : tabs ) {
            List<Field> fieldsForTab = FieldManager.getFieldsForTab( list, tab );
            doHeader( document, tab );
            populatePage( document, fieldsForTab );
            document.newPage();
        }

        document.close();
        writer.close();
        fileOutputStream.close();

        setPdfFile( output );

        return output;
    }

    private void setPdfFile( File pdfFile ) {
        this.pdfFile = pdfFile;
    }

    private void doHeader( Document document, Tab tab ) throws Exception {
        Table table = new Table( 1, 1 );
        table.setPadding( 8f );
        table.setSpacing( 8f );
        com.lowagie.text.Font font = FontFactory.getFont( FontFactory.HELVETICA, 18, Font.BOLD );
        Chunk chunk = new Chunk( tab.getTitle(), font );
        Cell cell = new Cell( chunk );
        disableBorders( cell );
        table.addCell( cell );
        document.add( table );

        Table spacer = new Table( 1 );
        Cell cell1 = new Cell( Chunk.NEWLINE );
        disableBorders( cell1 );
        spacer.addCell( cell1 );
        disableBorders( spacer );
        document.add( spacer );

    }

    public void onEndPage( PdfWriter writer, Document document ) {
        try {
            Rectangle page = document.getPageSize();
            PdfPTable foot = new PdfPTable( 1 );
            foot.setHorizontalAlignment( PdfTable.ALIGN_MIDDLE );
            foot.getDefaultCell().disableBorderSide( PdfCell.LEFT );
            foot.getDefaultCell().disableBorderSide( PdfCell.RIGHT );
            foot.getDefaultCell().disableBorderSide( PdfCell.TOP );
            foot.getDefaultCell().disableBorderSide( PdfCell.BOTTOM );
            foot.addCell( "   Page " + document.getPageNumber() + "  Creation Date: " + new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss" ).format( new Date() ) );
            foot.setTotalWidth( page.getWidth() );
            foot.writeSelectedRows( 0, -1, document.leftMargin(), document.bottomMargin(),
                    writer.getDirectContent() );
        }
        catch ( Exception e ) {
            throw new ExceptionConverter( e );
        }
    }

    private void populatePage( Document document, List<Field> fieldsForTab ) throws Exception {
        int totalRows = fieldsForTab.size();

        Table masterTable = new Table( 1, 1 );
        disableBorders( masterTable );
        masterTable.setPadding( 0.0f );

        // assign fields to the right panel
        Table childTable = new Table( 1, totalRows );
        for ( Field field : fieldsForTab ) {
            addField( childTable, field );
        }
        masterTable.insertTable( childTable );

        document.add( masterTable );
    }

    private void addField( Table table, Field field ) throws BadElementException {

        if ( field.getFieldType().isPureText() || ( field.getFieldType().isTextArea() && StringUtils.isEmpty( field.getLabel() ) ) ) {
            String displayed = field.getFieldType().isTextArea() ? field.getValue() : field.getLabel();
            if ( StringUtils.isEmpty( displayed ) ) {
                displayed = "- -";
            }
            com.lowagie.text.Font font;
            float leading;
            if ( "header".equals( field.getTextStyle() ) ) {
                font = FontFactory.getFont( FontFactory.HELVETICA, 12, Font.BOLD );
                leading = 15f;
            } else {
                font = FontFactory.getFont(
                        field.getFieldType().isTextArea() ? FontFactory.COURIER : FontFactory.HELVETICA,
                        8,
                        field.getFieldType().isTextArea() ? Font.PLAIN : Font.ITALIC );
                leading = 8.5f;
            }

            Chunk chunk = new Chunk( displayed, font );
            Cell cell = new Cell( chunk );
            cell.setLeading( leading );
            disableBorders( cell );

            table.addCell( cell );
        } else {
            Table fieldTable = new Table( 3, 1 );
            fieldTable.setPadding( 10f );
            fieldTable.setSpacing( 10f );
            float[] floats = {20f, 1f, 79f};
            fieldTable.setWidths( floats );
            disableBorders( fieldTable );

            String labelText = field.getLabel();
            labelText = labelText.trim();
            labelText = "  " + labelText;
            Cell labelCell = new Cell( new Chunk( labelText, FontFactory.getFont( FontFactory.HELVETICA, 8, Font.BOLD ) ) );
            labelCell.setHorizontalAlignment( Cell.ALIGN_RIGHT );
            labelCell.setLeading( 10f );
            disableBorders( labelCell );
            fieldTable.addCell( labelCell );

            Cell spacerCell = new Cell( " " );
            disableBorders( spacerCell );
            fieldTable.addCell( spacerCell );

            String valueText = ( !field.getFieldType().isSpacer() && StringUtils.isEmpty( field.getValue() ) ) ? "- -" : field.getValue();
            Cell valueCell = new Cell( new Chunk( valueText, FontFactory.getFont( FontFactory.COURIER, 8, Font.PLAIN ) ) );
            valueCell.setLeading( 10f );
            fieldTable.addCell( valueCell );
            disableBorders( valueCell );

            table.insertTable( fieldTable );
        }

    }

    private void disableBorders( Cell cell ) {
        cell.disableBorderSide( Cell.LEFT );
        cell.disableBorderSide( Cell.RIGHT );
        cell.disableBorderSide( Cell.TOP );
        cell.disableBorderSide( Cell.BOTTOM );
    }

    private void disableBorders( Table table ) {
        table.disableBorderSide( Table.LEFT );
        table.disableBorderSide( Table.RIGHT );
        table.disableBorderSide( Table.TOP );
        table.disableBorderSide( Table.BOTTOM );
    }

    public static void main( String s[] ) throws Exception {
        AppProperties.setUsername( "aron" );
        AppProperties.setPassPhrase( "aron" );

        // Get patient record info
        PatientManager.init();

        PdfApi api = new PdfApi();
        api.setTargetRecordName( "James-Barnaby-5789234" );
        File file = api.work();
        MedicsPdfViewer dialog = new MedicsPdfViewer();
        dialog.setCallback( api );
        dialog.init( file );
    }

    public void notifyClosed() {
        if ( pdfFile != null && pdfFile.exists() ) {
            pdfFile.deleteOnExit();
        }
    }

    public boolean isDeleteOnView() {
        return deleteOnView;
    }

    public void setDeleteOnView( boolean deleteOnView ) {
        this.deleteOnView = deleteOnView;
    }
}
