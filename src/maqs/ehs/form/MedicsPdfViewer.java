package maqs.ehs.form;

import com.sun.pdfview.PagePanel;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import javax.swing.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.awt.event.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

import maqs.ehs.util.AppProperties;

public class MedicsPdfViewer extends JFrame implements Printable {
    private JPanel contentPane;
    private PagePanel pdfPanel;

    // buttons
    private JButton btnFirst;
    private JButton btnPrevious;
    private JButton btnNext;
    private JButton btnLast;

    // page selector
    private JComboBox pageDropDown;

    // action buttons
    private JButton closeButton;
    private JButton printButton;

    private PDFFile pdffile;
    private int currentPage;
    private PdfCloseListener closeListener;

    public MedicsPdfViewer() {

        initUI();

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {
        }

        // close if cross is clicked
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                shutdown();
                dispose();
            }
        } );

        btnFirst.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                currentPage = 1;
                showPage();
            }
        } );
        btnPrevious.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( currentPage - 1 >= 1 ) {
                    currentPage--;
                    showPage();
                }
            }
        } );
        btnNext.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( currentPage + 1 <= pdffile.getNumPages() ) {
                    currentPage++;
                    showPage();
                }
            }
        } );
        btnLast.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                currentPage = pdffile.getNumPages();
                showPage();
            }
        } );
        pageDropDown.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                currentPage = ( Integer ) pageDropDown.getSelectedItem();
                showPage();
            }
        } );

        addKeyListener( new KeyListener() {
            public void keyTyped( KeyEvent e ) {

                if ( e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ) {
                    if ( currentPage + 1 <= pdffile.getNumPages() ) {
                        currentPage++;
                        showPage();
                    }
                }

                if ( e.getKeyCode() == KeyEvent.VK_PAGE_UP ) {
                    if ( currentPage - 1 >= 1 ) {
                        currentPage--;
                        showPage();
                    }
                }

            }

            public void keyPressed( KeyEvent e ) {
            }

            public void keyReleased( KeyEvent e ) {
            }
        } );
        closeButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                shutdown();
                dispose();
            }
        } );
        printButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                doPrint();
            }

        } );
    }

    private void initUI() {

        contentPane = new JPanel( new BorderLayout( 0, 0 ) );

        JPanel pdfPanelHost = new JPanel( new GridLayout( 1, 1 ) );
        pdfPanel = new PagePanel();
        pdfPanelHost.add( pdfPanel );
        contentPane.add( pdfPanelHost, BorderLayout.CENTER );

        // navpanel
        JPanel navPanel = new JPanel();
        contentPane.add( navPanel, BorderLayout.NORTH  );

        btnFirst = new JButton( "<<" );
        navPanel.add( btnFirst );

        btnPrevious = new JButton( "<" );
        navPanel.add( btnPrevious );

        btnNext = new JButton( ">" );
        navPanel.add( btnNext );

        btnLast = new JButton( ">>" );
        navPanel.add( btnLast );

        // page selector
        navPanel.add( new JLabel("Jump to page: ") );

        pageDropDown = new JComboBox();
        JPanel pageDropDownPanel = new JPanel();
        pageDropDownPanel.add( pageDropDown );
        navPanel.add( pageDropDownPanel );

        // action buttons
        closeButton = new JButton("Close");
        navPanel.add( closeButton );
        printButton = new JButton("Print");
        navPanel.add( printButton );
    }

    private void shutdown() {
        try {
            raf.close();
            channel.close();
            pageCache.clear();
            if ( closeListener != null ) closeListener.notifyClosed();
         } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void doPrint() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable( this );

        try {
            HashPrintRequestAttributeSet attset;
            attset = new HashPrintRequestAttributeSet();
            attset.add( new PageRanges( 1, pdffile.getNumPages() ) );
            if ( job.printDialog( attset ) )
                job.print( attset );
        }
        catch ( PrinterException pe ) {
            JOptionPane.showMessageDialog( this, pe.getMessage() );
        }
    }

    Map<Integer, PDFPage> pageCache = new HashMap<Integer, PDFPage>();

    private void showPage() {
        PDFPage page = pageCache.get( currentPage );
        if ( page == null ) {
            page = pdffile.getPage( currentPage );
            pageCache.put( currentPage, page );
        }
        pdfPanel.showPage( page );

        btnFirst.setEnabled( true );
        btnLast.setEnabled( true );
        btnNext.setEnabled( true );
        btnPrevious.setEnabled( true );

        if ( currentPage == 1 ) {
            btnFirst.setEnabled( false );
            btnPrevious.setEnabled( false );
        }

        if ( currentPage == pdffile.getNumPages() ) {
            btnLast.setEnabled( false );
            btnNext.setEnabled( false );
        }
    }

    FileChannel channel;
    RandomAccessFile raf;

    public void init( File file ) {

        setContentPane( contentPane );
        pack();
        setVisible( true );

        try {
            raf = new RandomAccessFile( file, "r" );
            channel = raf.getChannel();
            ByteBuffer buf = channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
            pdffile = new PDFFile( buf );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        currentPage = 1;

        setExtendedState( getExtendedState() | Frame.MAXIMIZED_BOTH );

        DefaultComboBoxModel comboModel = new DefaultComboBoxModel();

        for ( int i = 1; i <= pdffile.getNumPages(); i++ ) {
            comboModel.addElement( i );
        }

        pageDropDown.setModel( comboModel );
        pageDropDown.setName( "Pages" );

        if ( pdffile.getNumPages() < 2 ) {
            pageDropDown.setEnabled( false );

            btnFirst.setEnabled( false );
            btnLast.setEnabled( false );
            btnNext.setEnabled( false );
            btnPrevious.setEnabled( false );
        }

        Dimension dimMax = new Dimension( 1, ( int ) pageDropDown.getSize().getHeight() );
        pageDropDown.setMaximumSize( dimMax );

        setTitle( AppProperties.getAppTitleFull() + " - " + file.getName() );

        printButton.setEnabled( pdffile.isPrintable() );

        showPage();
    }

    public void setCallback( PdfCloseListener closeListener ) {
        this.closeListener = closeListener;
    }

    public int print( Graphics g, PageFormat format, int index )
            throws PrinterException {
        int pagenum = index + 1;
        if ( pagenum < 1 || pagenum > pdffile.getNumPages() )
            return NO_SUCH_PAGE;

        Graphics2D g2d = ( Graphics2D ) g;
        AffineTransform at = g2d.getTransform();

        PDFPage pdfPage = pdffile.getPage( pagenum );

        Dimension dim;
        dim = pdfPage.getUnstretchedSize( ( int ) format.getImageableWidth(),
                ( int ) format.getImageableHeight(),
                pdfPage.getBBox() );

        Rectangle bounds = new Rectangle( ( int ) format.getImageableX(),
                ( int ) format.getImageableY(),
                dim.width,
                dim.height );

        PDFRenderer rend = new PDFRenderer( pdfPage, ( Graphics2D ) g, bounds,
                null, null );
        try {
            pdfPage.waitForFinish();
            rend.run();
        }
        catch ( InterruptedException ie ) {
            JOptionPane.showMessageDialog( this, ie.getMessage() );
        }

        g2d.setTransform( at );
        g2d.draw( new Rectangle2D.Double( format.getImageableX(),
                format.getImageableY(),
                format.getImageableWidth(),
                format.getImageableHeight() ) );

        return PAGE_EXISTS;
    }

    public static void main( String[] args ) {

        File file = new File( "C:/out.pdf" );

        MedicsPdfViewer dialog = new MedicsPdfViewer();
        dialog.init( file );
    }

}
