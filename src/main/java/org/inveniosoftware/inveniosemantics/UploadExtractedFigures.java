/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.inveniosoftware.inveniosemantics;

import invenio.pdf.features.FigureCandidate;
import invenio.pdf.features.XMLDocumentReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author piotr
 */
public class UploadExtractedFigures {

    public static void usage() {
        System.out.println("The tool allowing to upload a number of files or directories into the semantic repository");
        System.out.println("Usage: ");
        System.out.println("   UploadExtractedFigures file1 [file2 [file3 ... ]]");
        System.out.println("");
        System.out.println("The file must be a path of an XML file or a directory");
        System.out.println("In the case of XML file, the content is transalted into the RDF-annotated data which is uploaded into the semantic storage");
        System.out.println("Directories are searched for all available XML files (only directly under the directory path, not in subdirectories)");
    }

    public static void uploadFigure(FigureCandidate figure) {
        System.out.println("uploadng figure");
    }

    public static void processFile(File input) {
        try {
            List<FigureCandidate> figures = XMLDocumentReader.readDocument(input);
            for (FigureCandidate fig : figures) {
                uploadFigure(fig);
            }
        } catch (ParserConfigurationException ex) {
            System.err.println("XML parsing exception (ParserConfiguration) when processing" + input.getAbsolutePath());

        } catch (SAXException ex) {
            System.err.println("XML parsing exception when processing" + input.getAbsolutePath());

        } catch (IOException ex) {
            System.err.println("Problem with accessing the file (I/O error) when processing " + input.getAbsolutePath() + "   Check the permissions");
        } catch (Exception ex) {
            System.err.println("Unknown error when processing" + input.getAbsolutePath());

            Logger.getLogger(UploadExtractedFigures.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            //return;
            
            args = new String[1];
            args[0] = "/home/piotr/sampleArticlesForFigureAnalysis/cleaned";
            
        }
        for (String fname : args) {
            File input = new File(fname);
            if (!input.exists()) {
                System.err.println("The file " + fname + " does not exist");
                continue;
            }
            if (input.isDirectory()) {
                File[] files = input.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".xml");
                    }
                });
                for (File f : files) {
                    processFile(f);
                }
            } else {
                processFile(input);
            }
        }
    }
}
