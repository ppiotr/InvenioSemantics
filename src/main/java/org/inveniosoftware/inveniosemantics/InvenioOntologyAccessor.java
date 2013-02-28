package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStream;

public class InvenioOntologyAccessor {

    private String _inputFileName;
    private Model _model;
    public static final String BASEDON = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#basedOn";
    public static final String EXTRACTEDFROM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#extractedFrom";
    public static final String BIBOBJECT = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#BibObject";
    public static final String CODE = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Code";
    public static final String DATA = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Data";
    public static final String DIAGRAM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Diagram";
    public static final String EXCLUSION_AREA_PLOT = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Exclusion_area_plot";
    public static final String FEYNMAN_DIAGRAM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Feynmann_diagram";
    public static final String FIGURE = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Figure";
    public static final String GENERIC_DIAGRAM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Generic_diagram";
    public static final String GENERIC_FUNCTION_MEASUREMENT_PLOT = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Generic_function_measurement_plot";
    public static final String HISTOGRAM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Histogram";
    public static final String MATHEMATICAL_DIAGRAM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Mathematical_diagram";
    public static final String PLOT = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Plot";
    public static final String PUBLICATION = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Publication";
    public static final String TABLE = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Table";
    public static final String NSPREFIX = "inveniomodel";
    public static final String NSURI = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#";

    public InvenioOntologyAccessor(String inputFileName) {
        _inputFileName = inputFileName;
        uploadModel();
    }

    private void uploadModel() {
        InputStream in = FileManager.get().open(_inputFileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + _inputFileName + " not found");
        }
        _model = ModelFactory.createDefaultModel();
        // read the RDF/XML file
        _model.read(in, "");

    }

    /**
     * This is a test method to show the the first 'limit' statements (triples)
     * of the model
     *
     * @param limit maximum number of statements to be displayed. -1 if you want
     * to display all the statements
     */
    public void showStatements(int limit) {
        StmtIterator iter = _model.listStatements();
        int i = 0;
        while (iter.hasNext() && ((i < limit) || (limit == -1))) {
            Statement st = iter.next();
            System.out.println("  " + st.toString());
            i++;
        }
    }

    public static void main(String[] args) {

        InvenioOntologyAccessor invenio = new InvenioOntologyAccessor("files/inveniomodel.owl");

        invenio.showStatements(-1);
    }
}
