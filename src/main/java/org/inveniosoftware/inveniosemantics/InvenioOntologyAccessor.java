package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.FileManager;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
    public static final String COORDINATE_SYSTEM = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#CoordinateSystem";
    public static final String CONTAINS = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#contains";
    public static final String AXIS = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Axis";
    public static final String AXIS_TICK = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#AxisTick";
    public static final String AXIS_LABEL = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#AxisLabel";
    public static final String AXIS_TICK_LABEL = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#AxisTickLabel";
    public static final String COORDINATE_SYSTEM_ORIGIN = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#CoordinateSystemOrigin";
    public static final String IS_LOCATED_AT = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#isLocatedAt";
    public static final String HAS_X_COORDINATE = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#hasXCoordinate";
    public static final String HAS_Y_COORDINATE = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#hasYCoordinate";
    public static final String HAS_WIDTH = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#hasWidth";
    public static final String HAS_HEIGHT = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#hasHeight";
    public static final String HAS_BOUNDARY = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#hasBoundary";
    public static final String NSPREFIX = "inveniomodel";
    public static final String NSURI = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#";
    public Resource coordinateSystem;
    public Resource plot;
    public Resource axis;
    public Resource coordinateSystemOrigin;
    public Resource axisTick;
    public Resource axisTickLabel;
    public Resource axisLabel;
    public Property isLocatedAt;
    public Property hasBoundary;
    public Property contains;
    public Property hasXCoordinate;
    public Property hasYCoordinate;
    public Property hasWidth;
    public Property hasHeight;

    public InvenioOntologyAccessor(String inputFileName) {
        _inputFileName = inputFileName;
        uploadModel();
        createResources();
    }

    /**
     * loads resources of the ontology and assigns them to object fields
     */
    private void createResources() {
        this.coordinateSystem = this._model.createResource(InvenioOntologyAccessor.COORDINATE_SYSTEM);
        this.coordinateSystemOrigin = this._model.createResource(InvenioOntologyAccessor.COORDINATE_SYSTEM_ORIGIN);
        this.axisTick = this._model.createResource(InvenioOntologyAccessor.AXIS_TICK);
        this.axisTickLabel = this._model.createResource(InvenioOntologyAccessor.AXIS_TICK_LABEL);
        this.axisLabel = this._model.createResource(InvenioOntologyAccessor.AXIS_LABEL);


        this.plot = this._model.createResource(InvenioOntologyAccessor.PLOT);
        this.axis = this._model.createResource(InvenioOntologyAccessor.AXIS);

        this.contains = this._model.createProperty(InvenioOntologyAccessor.CONTAINS);
        this.isLocatedAt = this._model.createProperty(InvenioOntologyAccessor.IS_LOCATED_AT);
        this.hasBoundary = this._model.createProperty(InvenioOntologyAccessor.HAS_BOUNDARY);

        this.hasXCoordinate = this._model.createProperty(InvenioOntologyAccessor.HAS_X_COORDINATE);
        this.hasYCoordinate = this._model.createProperty(InvenioOntologyAccessor.HAS_Y_COORDINATE);
        this.hasWidth = this._model.createProperty(InvenioOntologyAccessor.HAS_WIDTH);
        this.hasHeight = this._model.createProperty(InvenioOntologyAccessor.HAS_HEIGHT);

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

    public void saveModel() throws FileNotFoundException {
//        OutputStream os = new FileOutputStream("../InvenioSemantics/files/invenio.rdf");
        _model.write(System.out);
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

    public Model getModel() {
        return this._model;
    }
}
