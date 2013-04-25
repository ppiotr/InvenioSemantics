package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.CannotEncodeCharacterException;
import com.hp.hpl.jena.util.FileManager;
import invenio.pdf.features.FigureCandidate;
import java.io.*;
import java.util.*;

/**
 * Class with methods to annotate resources (publications, figures, data) and
 * search them
 */
public class InspireDatabase {

    private HEPOntologyAccessor _hep;
    private InvenioOntologyAccessor _invenio;
    private String _outputFileName;
    private Model _model;
    private boolean _debug;

    public static String cleanString(String s) {
        StringBuilder o = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == ' ' || c == '\t' || (c >= '0' && c <= '9')) {
                o.append(c);
            }
        }
        //        String out = s;
//        
//        
//        out = out.replaceAll("\f", "");
//        out = out.replaceAll("", "");
//        out = out.replaceAll("", "");
//        out = out.replaceAll("", "");   
//        out = out.replaceAll("", "");   
//        out = out.replaceAll("", "");   
//        out = out.replaceAll("", "");   

        return o.toString();
    }

    /**
     * Constructor
     *
     * @param hepFile file containing the HEP ontology
     * @param invenioFile file containing the Invenio ontology
     * @param outputFileName file for writing the output
     */
    public InspireDatabase(String hepFile, String invenioFile, String outputFileName) {
        _hep = new HEPOntologyAccessor(hepFile);
        _invenio = new InvenioOntologyAccessor(invenioFile);
        _outputFileName = outputFileName;

        // create an empty model
        _model = ModelFactory.createDefaultModel();

        // reading the exising statements
//        InputStream in = FileManager.get().open(outputFileName);
//        if (in != null) {
//            // read the RDF/XML file
//            _model.read(in, "");
//
//        }
        _debug = true;
    }

    /**
     * Checks if the given publiction already exists in the knowledge base. In
     * the case if it is missing, it adds a new entity
     *
     * or the moment we create empty description ... in order to have more, we
     * would have to retreive the data from Inspire
     *
     * @param uri
     */
    public Resource createPublication(String uri) {
//        String publicationURI = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568";
//        String description    = "A measurement of the jet activity in ttbar events produced in proton-proton collisions at a centre-of-mass energy of 7 TeV is presented, using 2.05 fb^-1 of integrated luminosity collected by the ATLAS detector at the Large Hadron Collider. The ttbar events are selected in the dilepton decay channel with two identified b-jets from the top quark decays. Events are vetoed if they contain an additional jet with transverse momentum above a threshold in a central rapidity interval. The fraction of events surviving the jet veto is presented as a function of this threshold for four different central rapidity interval definitions. An alternate measurement is also performed, in which events are vetoed if the scalar transverse momentum sum of the additional jets in each rapidity interval is above a threshold. In both measurements, the data are corrected for detector effects and compared to the theoretical models implemented in MC@NLO, POWHEG, ALPGEN and SHERPA. The experimental uncertainties are often smaller than the spread of theoretical predictions, allowing deviations between data and theory to be observed in some regions of phase space.";
//        String publicationDescription = "A measurement of the jet activity in ttbar events produced in proton-proton collisions at a centre-of-mass energy of 7 TeV...";
//        String title   = "Measurement of $t \bar{t}$ production with a veto on additional central jet activity in pp collisions at sqrt(s) = 7 TeV using the ATLAS detector";
//        String publicationTitle = "Measurement of ttbar production with a veto on additional central jet activity in pp collisions at sqrt(s) = 7 TeV using the ATLAS detector";
//        String publicationIdentifierURI = "https://inspirehep.net/record/1094568";

        // create the resource
        //   and add the properties cascading style
        Resource publication = _model.createResource(uri);
//        publication.addProperty(com.hp.hpl.jena.vocabulary.DC.description, publicationDescription);
//        publication.addProperty(com.hp.hpl.jena.vocabulary.DC.title, publicationTitle);
//        publication.addProperty(com.hp.hpl.jena.vocabulary.DC.identifier, publicationIdentifierURI);
        publication.addProperty(com.hp.hpl.jena.vocabulary.RDF.type, _model.createResource(InvenioOntologyAccessor.PUBLICATION));
        return publication;
    }

    /**
     * This method creates an annotation in the internal model containing three
     * related resources: publication, figure and data
     */
    public void annotateExample() {


        /**
         * <!--
         * http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568
         * -->
         *
         * <owl:NamedIndividual
         * rdf:about="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568">
         * <rdf:type
         * rdf:resource="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Publication"/>
         * <dc:identifier>https://inspirehep.net/record/1094568</dc:identifier>
         * <dc:description xml:lang="en">A measurement of the jet activity in
         * ttbar events produced in proton-proton collisions at a centre-of-mass
         * energy of 7 TeV is presented, using 2.05 fb^-1 of integrated
         * luminosity collected by the ATLAS detector at the Large Hadron
         * Collider. The ttbar events are selected in the dilepton decay channel
         * with two identified b-jets from the top quark decays. Events are
         * vetoed if they contain an additional jet with transverse momentum
         * above a threshold in a central rapidity interval. The fraction of
         * events surviving the jet veto is presented as a function of this
         * threshold for four different central rapidity interval definitions.
         * An alternate measurement is also performed, in which events are
         * vetoed if the scalar transverse momentum sum of the additional jets
         * in each rapidity interval is above a threshold. In both measurements,
         * the data are corrected for detector effects and compared to the
         * theoretical models implemented in MC@NLO, POWHEG, ALPGEN and SHERPA.
         * The experimental uncertainties are often smaller than the spread of
         * theoretical predictions, allowing deviations between data and theory
         * to be observed in some regions of phase space.</dc:description>
         * <dc:title xml:lang="en">Measurement of $t \bar{t}$ production with a
         * veto on additional central jet activity in pp collisions at sqrt(s) =
         * 7 TeV using the ATLAS detector</dc:title> </owl:NamedIndividual>
         *
         */
        // some definitions
        String publicationURI = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568";
//        String description    = "A measurement of the jet activity in ttbar events produced in proton-proton collisions at a centre-of-mass energy of 7 TeV is presented, using 2.05 fb^-1 of integrated luminosity collected by the ATLAS detector at the Large Hadron Collider. The ttbar events are selected in the dilepton decay channel with two identified b-jets from the top quark decays. Events are vetoed if they contain an additional jet with transverse momentum above a threshold in a central rapidity interval. The fraction of events surviving the jet veto is presented as a function of this threshold for four different central rapidity interval definitions. An alternate measurement is also performed, in which events are vetoed if the scalar transverse momentum sum of the additional jets in each rapidity interval is above a threshold. In both measurements, the data are corrected for detector effects and compared to the theoretical models implemented in MC@NLO, POWHEG, ALPGEN and SHERPA. The experimental uncertainties are often smaller than the spread of theoretical predictions, allowing deviations between data and theory to be observed in some regions of phase space.";
        String publicationDescription = "A measurement of the jet activity in ttbar events produced in proton-proton collisions at a centre-of-mass energy of 7 TeV...";
//        String title   = "Measurement of $t \bar{t}$ production with a veto on additional central jet activity in pp collisions at sqrt(s) = 7 TeV using the ATLAS detector";
        String publicationTitle = "Measurement of ttbar production with a veto on additional central jet activity in pp collisions at sqrt(s) = 7 TeV using the ATLAS detector";
        String publicationIdentifierURI = "https://inspirehep.net/record/1094568";

        // create the resource
        //   and add the properties cascading style
        Resource publication = _model.createResource(publicationURI);
        publication.addProperty(com.hp.hpl.jena.vocabulary.DC.description, publicationDescription);
        publication.addProperty(com.hp.hpl.jena.vocabulary.DC.title, publicationTitle);
        publication.addProperty(com.hp.hpl.jena.vocabulary.DC.identifier, publicationIdentifierURI);
        publication.addProperty(com.hp.hpl.jena.vocabulary.RDF.type, _model.createResource(InvenioOntologyAccessor.PUBLICATION));


        /**
         * <!--
         * http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568_fig_04a.png
         * -->
         *
         * <owl:NamedIndividual rdf:about="&inveniomodel;_fig_04a.png">
         * <rdf:type
         * rdf:resource="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Generic_function_measurement_plot"/>
         * <dc:identifier>https://inspirehep.net/record/1094568/files/fig_04a.png</dc:identifier>
         * <dc:title xml:lang="en">The measured gap fraction as a function of
         * \Qz~is compared with the prediction from the NLO and multi-leg LO MC
         * generators in the three rapidity regions, (a)
         * |y|&amp;amp;lt;0.8</dc:title> <extractedFrom
         * rdf:resource="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568"/>
         * <basedOn
         * rdf:resource="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1179924"/>
         * </owl:NamedIndividual>
         *
         */
        String figureURI = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1094568_fig_04a.png";
//        String figureTitle   = "The measured gap fraction as a function of \Qz~is compared with the prediction from the NLO and multi-leg LO MC generators in the three rapidity regions, (a) |y|&amp;amp;lt;0.8";
        String figureTitle = "The measured gap fraction as a function of ...";
        String figureIdentifierURI = "https://inspirehep.net/record/1094568/files/fig_04a.png";

        Resource figure = _model.createResource(figureURI);
        figure.addProperty(com.hp.hpl.jena.vocabulary.RDF.type, _model.createResource(InvenioOntologyAccessor.FIGURE));


        figure.addProperty(com.hp.hpl.jena.vocabulary.DC.title, figureTitle);
        figure.addProperty(com.hp.hpl.jena.vocabulary.DC.identifier, figureIdentifierURI);
        // add subjects from HEPOnt: ATLAS, MONTECARLO (MC), gap, HERWIG or PYTHIA could be annotated
        String[] subjects = {"ATLAS", "MONTE CARLO", "gap", "HERWIG", "PYTHIA"};
        for (String s : subjects) {
            List<Resource> resources = _hep.searchResources(s);
            if (resources != null) {
                for (Resource r : resources) {
                    figure.addProperty(com.hp.hpl.jena.vocabulary.DC.subject, r);
                }
            }
        }


        /**
         * <!--
         * http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1179924
         * -->
         *
         * <owl:NamedIndividual
         * rdf:about="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1179924">
         * <rdf:type
         * rdf:resource="http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#Data"/>
         * <dc:identifier>https://inspirehep.net/record/1179924</dc:identifier>
         * <dc:title>Data from figure 4 from: Measurement of $t \bar{t}$
         * production with a veto on additional central jet activity in pp
         * collisions at sqrt(s) = 7 TeV using the ATLAS detector</dc:title>
         * <rdfs:comment xml:lang="en">The measured fraction of events, the gap
         * fraction, surviving the veto cut of having no additional jets in the
         * |rapidity| interval &amp;lt; 0.8 having a transverse momentum greater
         * than Q, as a function of Q.</rdfs:comment> </owl:NamedIndividual>
         * </rdf:RDF>
         *
         */
        String dataURI = "http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#1179924";
//      String dataTitle   = "Data from figure 4 from: Measurement of $t \bar{t}$ production with a veto on additional central jet activity in pp collisions at sqrt(s) = 7 TeV using the ATLAS detector";
        String dataTitle = "Data from figure 4 from: Measurement of ttbar production with a veto on additional central jet activity in pp collisions at sqrt(s) = 7 TeV using the ATLAS detector";
        String dataIdentifierURI = "https://inspirehep.net/record/1179924";
        String dataDescription = "The measured fraction of events, the gap fraction, surviving the veto cut of having no additional jets in the |rapidity| interval &amp;lt; 0.8 having a transverse momentum greater than Q, as a function of Q.";


        Resource data = _model.createResource(dataURI);
        data.addProperty(com.hp.hpl.jena.vocabulary.RDF.type, _model.createResource(InvenioOntologyAccessor.DATA));
        data.addProperty(com.hp.hpl.jena.vocabulary.DC.title, dataTitle);
        data.addProperty(com.hp.hpl.jena.vocabulary.DC.identifier, dataIdentifierURI);
        data.addProperty(com.hp.hpl.jena.vocabulary.DC.description, dataDescription);

        Property extractedFrom = _model.createProperty(InvenioOntologyAccessor.EXTRACTEDFROM);
        figure.addProperty(extractedFrom, publication);

        Property basedOn = _model.createProperty(InvenioOntologyAccessor.BASEDON);
        figure.addProperty(basedOn, data);

        _model.setNsPrefix(InvenioOntologyAccessor.NSPREFIX, InvenioOntologyAccessor.NSURI);
    }

    /**
     * This method writes the model in XML form to a file
     */
    public void writeOuput() {
        try {
            // debugging
            if (_debug) {
                System.out.println("\nTEST: annotations in the model");
                _model.write(System.out);
            }

            FileOutputStream out = new FileOutputStream(_outputFileName);
            _model.write(out);
        } catch (CannotEncodeCharacterException e) {
            System.err.println("cannot encode " + e.getEncodingContext() + "   " + e.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This method searches figures in the repository of type 'figureType' and
     * about the concept 'concept'
     *
     * @param concept URI of the concept annotated as subject (the URI could be
     * searched first through the HEPOntologyAccessor class
     * @param figureType URI of the figure type (in the future some kind of
     * inference could be used to detect automatically subclasses of figure
     * type)
     */
    public List<Resource> search(String concept, String figureType) {
        try {
            List<Resource> resources = new LinkedList<Resource>();

            String query =
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                    + "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
                    + "PREFIX inveniomodel: <http://www.semanticweb.org/ontologies/invenio/inveniomodel.owl#> "
                    + "SELECT DISTINCT ?subject "
                    + "WHERE { "
                    + "?subject dc:subject <" + concept + "> . "
                    + "?subject rdf:type <" + figureType + "> "
                    + //"?subject rdf:type ?value "+
                    "} ORDER BY ASC(?subject) ";

            if (_debug) {
                System.out.println("\n" + query);
            }

            QueryExecution qexec = QueryExecutionFactory.create(query, _model);
            ResultSet results = qexec.execSelect();

            //working with text
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource r = soln.getResource("subject");
                resources.add(r);

                if (_debug) {
                    System.out.println(r);
                }
            }

            return resources;

        } catch (Exception e) {
            System.out.println("Failed: " + e);
            return null;
        }
    }

    public static void main(String[] args) {

        System.out.println("Starting");


        InspireDatabase db = new InspireDatabase("files/HEPont.rdf", "files/inveniomodel.owl", "files/output.rdf");

        db.annotateExample();

        db.writeOuput();

        List<Resource> resources = db.search("http://cern.ch/thesauri/HEPontology.rdf#ATLAS", InvenioOntologyAccessor.FIGURE);

        if (resources != null) {
            for (Resource r : resources) {
                System.out.println(r);
            }
        }



        System.out.println("Finished");

    }
    private static int figCount = 0;
    private static int totalMatch = 0;

    public static List<String> tokeniseString(String s) {
        String[] wordsA = s.split("\\s+");
        List<String> words = new ArrayList<String>();
        for (int i = 0; i < wordsA.length; ++i) {
            if (!wordsA[i].equals("")) {
                words.add(wordsA[i]);
            }
        }
        return words;
    }

    public Set<Resource> annotateStringWithHEPConcepts(String s) {
        List<String> words = InspireDatabase.tokeniseString(s);
        Integer[] minLengths = new Integer[words.size()];
        for (int i = 0; i < minLengths.length; ++i) {
            minLengths[i] = 0;
        }
        
        HashSet<Resource> matchedConcepts = new HashSet<Resource>();

        for (int start = 0; start < words.size(); ++start) {
            for (int end = words.size(); minLengths[start] < (end - start) ; --end) {
                Set<String> wordsS = new HashSet<String>();
                for (String w : words.subList(start, end)) {
                    wordsS.add(w);
                }

                Set<Resource> foundRes = _hep.searchResourcesCached(wordsS);
                if (foundRes != null && foundRes.size() > 0) {
                    matchedConcepts.addAll(foundRes);
                    // we have to update teh minLen array
                    for (int j = start; j < end; ++j){
                        minLengths[j] = end - j;
                    }
                }
            }
        }
        return matchedConcepts;
    }

    Resource createFigure(InspireDatabase db, Resource pub, FigureCandidate figure) {
        String figURI = pub.getURI() + "/" + figure.getId();
        Resource figRes = _model.createResource(figURI);

        figure.getPageNumber();
        String cleanCaption = cleanString(figure.getCaption().text);
        figRes.addProperty(com.hp.hpl.jena.vocabulary.DC.title, cleanCaption);

        figRes.addProperty(com.hp.hpl.jena.vocabulary.RDF.type, _model.createResource(InvenioOntologyAccessor.FIGURE));
        figRes.addProperty(com.hp.hpl.jena.vocabulary.RDFS.label, "figure " + figCount);
        figRes.addProperty(this._invenio.extractedFrom, pub);


        Set<Resource> matchedConcepts = this.annotateStringWithHEPConcepts(cleanCaption);

        System.out.println("Figure: " + figURI);
        System.out.println("   Caption: " + cleanCaption);
        for (Resource concept : matchedConcepts) {
            figRes.addProperty(com.hp.hpl.jena.vocabulary.DC.subject, concept);
            System.out.println("    " + concept.getURI());
        }

        totalMatch += matchedConcepts.size();
        System.out.println("Total matched : " + totalMatch);
        ++figCount;
        figure.getPageManager().getPageBoundary();

        return figRes;
    }
}
