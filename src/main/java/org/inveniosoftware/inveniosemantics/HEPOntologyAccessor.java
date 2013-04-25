package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.FileManager;
import invenio.common.PrefixTree;
import java.io.*;
import java.util.*;

/**
 * Class that enables the access to the HEP ontology
 */
public class HEPOntologyAccessor {

    private String _inputFileName;
    private Model _model;
    private static final String NARROWER = "narrower";
    private static final String PREFLABEL = "prefLabel";
    public static final String NSPREFIX = "hep";
    public static final String NSURI = "http://cern.ch/thesauri/HEPontology.rdf#";

    /**
     * Constructor that receives the path of the file containing the HEP
     * ontology
     */
    public HEPOntologyAccessor(String inputFileName) {
        _inputFileName = inputFileName;
        uploadModel();
        precalculateLabelsIndex();
    }

    /**
     * It uploads the model in memory. This is not efficient but it works for a
     * test. For production, a TDB or a database storage should be used instead
     */
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

    /**
     * This is a test method, to show the the first 'limit' statements (triples)
     * of the model. It's a refined version of showStatements method
     *
     * @param limit maximum number of statements to be displayed. -1 if you want
     * to display all the statements
     */
    public void showStatementsRefined(int limit) {
        // list the statements in the graph
        StmtIterator iter = _model.listStatements();

        // print out the predicate, subject and object of each statement
        int i = 0;
        while (iter.hasNext() && ((i < limit) || (limit == -1))) {
            Statement stmt = iter.nextStatement(); // get next statement
            Resource subject = stmt.getSubject();   // get the subject
            Property predicate = stmt.getPredicate(); // get the predicate
            RDFNode object = stmt.getObject();    // get the object

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }
            System.out.println(" .");
            i++;
        }

    }

    /**
     * This is a recursive auxiliary method used to display the recursive
     * hierarchy of narrower terms of a concept
     *
     * @param r resource to be browsed in each invocation
     * @param index number of commas to be shown in the output (it allows
     * detecting the hierarchy)
     */
    private static void showTaxonomy(Resource r, int index) {
        for (int i = 0; i < index; i++) {
            System.out.print(",");
        }
        System.out.println(r.toString());

        // list the statements in the graph
        StmtIterator iter = r.listProperties();

        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement(); // get next statement
            // Resource  subject   = stmt.getSubject();   // get the subject
            Property predicate = stmt.getPredicate(); // get the predicate
            RDFNode object = stmt.getObject();    // get the object

            if (predicate.getLocalName().contains(NARROWER)) {
                if (object instanceof Resource) {
                    showTaxonomy((Resource) object, index + 1);
                }
            }
        }
    }

    /**
     * This method displays the recursive hierarchy of narrower terms of
     * 'concept'. It uses a selector to filter the statements of the model.
     *
     * @param r resource to be browsed in each invocation
     * @param index number of commas to be shown in the output (it allows
     * detecting the hierarchy)
     */
    public void extractTaxonomy(final String concept) {
        // list the statements in the graph
        StmtIterator iter = _model.listStatements(
                new SimpleSelector(null, null, (RDFNode) null) {
            @Override
            public boolean selects(Statement s) {
                Resource subject = s.getSubject();
                // return subject.getURI().endsWith("#matter");
                return subject.getURI().endsWith("#" + concept);
            }
        });


        // print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement(); // get next statement
            Resource subject = stmt.getSubject();   // get the subject
            Property predicate = stmt.getPredicate(); // get the predicate
            //RDFNode   object    = stmt.getObject();    // get the object

            if (predicate.toString().contains(PREFLABEL)) {
                // found prefLabel 
                showTaxonomy(subject, 0);
            }

        }

    }

    public static String joinString(String sep, List<String> list) {
        StringBuilder resultB = new StringBuilder();
        boolean isFirst = true;
        for (String c : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                resultB.append(sep);
            }
            resultB.append(c);
        }
        return resultB.toString();
    }

    /**
     * This method search all resources containing concept as preferred label or
     * alternate label
     *
     * @param concept literal of the concept to be searched
     */
    public List<Resource> searchResources(List<String> concepts) {
        try {
            String separator = "( |:)*";
            String regex = "^" + joinString(separator, concepts) + "$";

            // the query is upper or lower case independent
            // the prefLabel or altLabel should be the same as concept
            // the query could be modified according to user needs for more flexibility
            String query =
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                    + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                    + "SELECT DISTINCT ?subject "
                    + "WHERE { "
                    + "{ ?subject skos:prefLabel ?object . "
                    + "FILTER regex(str(?object),'" + regex + "','i') }"
                    + "UNION "
                    + "{?subject skos:altLabel ?object . "
                    + "FILTER regex(str(?object),'" + regex + "','i') }"
                    + "} ORDER BY ASC(?subject) ";


            // System.out.println(query);

            QueryExecution qexec = QueryExecutionFactory.create(query, _model);
            ResultSet results = qexec.execSelect();

            //ResultSetFormatter.out(System.out, results);
            List<Resource> resources = new LinkedList<Resource>();

            //working with text
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                resources.add(soln.getResource("subject"));
            }

            return resources;

        } catch (Exception e) {
            System.out.println("Failed: " + e);
            return null;
        }

    }

    /**
     * This method search all resources containing concept as preferred label or
     * alternate label
     *
     * @param concept literal of the concept to be searched
     */
    public List<Resource> searchResources(String concept) {
        try {

            // the query is upper or lower case independent
            // the prefLabel or altLabel should be the same as concept
            // the query could be modified according to user needs for more flexibility
            String query =
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
                    + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                    + "SELECT DISTINCT ?subject "
                    + "WHERE { "
                    + "{ ?subject skos:prefLabel ?object . "
                    + "FILTER regex(str(?object),'" + "^" + concept + "$','i') }"
                    + "UNION "
                    + "{?subject skos:altLabel ?object . "
                    + "FILTER regex(str(?object),'" + "^" + concept + "$','i') }"
                    + "} ORDER BY ASC(?subject) ";


            // System.out.println(query);

            QueryExecution qexec = QueryExecutionFactory.create(query, _model);
            ResultSet results = qexec.execSelect();

            //ResultSetFormatter.out(System.out, results);
            List<Resource> resources = new LinkedList<Resource>();

            //working with text
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                resources.add(soln.getResource("subject"));
            }

            return resources;

        } catch (Exception e) {
            System.out.println("Failed: " + e);
            return null;
        }

    }

    private static boolean getValidateModel(Model model) {
        InfModel infmodel = ModelFactory.createRDFSModel(model);
        ValidityReport validity = infmodel.validate();
        if (!validity.isValid()) {
            System.out.println("Error: Inconsistent ontology");
            for (Iterator i = validity.getReports(); i.hasNext();) {
                ValidityReport.Report report = (ValidityReport.Report) i.next();
                System.out.println(" - " + report);
            }
            return false;
        }
        return true;
    }
    /**
     * This method precalculates data structures used to match
     */
    public PrefixTree<String> terms; // the mapping term -> the complete label
    public HashMap<String, List<Resource>> labelToResource; // the mapping from labels to resources

    public Set<Resource> getNostandaloneResources() {
        String query =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "SELECT DISTINCT ?subject ?note "
                + "WHERE {  ?subject skos:note ?note}";
        QueryExecution qexec = QueryExecutionFactory.create(query, this._model);
        ResultSet qResults = qexec.execSelect();

        //working with text
        HashSet<Resource> results = new HashSet<>();
        while (qResults.hasNext()) {
            QuerySolution soln = qResults.nextSolution();
            if (soln.getLiteral("note").getString().equals("nostandalone")) {
                results.add(soln.getResource("subject"));
            }
        }
        return results;
    }

    /**
     * Retrieve all the labels present in the ontology
     *
     * @return
     */
    public HashMap<Resource, List<String>> getAllLabels() {
        HashMap<Resource, List<String>> results = new HashMap<>();
        String query =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "SELECT DISTINCT ?subject ?label "
                + "WHERE { {?subject skos:prefLabel ?label} UNION {?subject skos:altLabel ?label}} ";
//                + "{ ?subject skos:prefLabel ?object}"
//                + "UNION "
//                + "{?subject skos:altLabel ?object}"
//                + "}";

        QueryExecution qexec = QueryExecutionFactory.create(query, _model);
        ResultSet qResults = qexec.execSelect();

        //working with text
        while (qResults.hasNext()) {
            QuerySolution soln = qResults.nextSolution();
            Resource subject = soln.getResource("subject");
            String label = (String) soln.getLiteral("label").getValue();
            if (!results.containsKey(subject)) {
                results.put(subject, new LinkedList<String>());
            }
            results.get(subject).add(label.toLowerCase());
        }
        return results;
    }

    private void precalculateLabelsIndex() {
        /// we retrieve all the notions from the ontology, tokenize them by removing non-alphanum characters from the ends and beginnings of strings and add all single words to the tree;
        this.terms = new PrefixTree<>();
        this.labelToResource = new HashMap<>();
        // iterate all the terms in the HEPOntology
        HashMap<Resource, List<String>> resLabels = this.getAllLabels();
        Set<Resource> nostandalone = this.getNostandaloneResources();
        for (Resource res : resLabels.keySet()) {
            if (!nostandalone.contains(res)) {
                for (String label : resLabels.get(res)) {
                    if (!this.labelToResource.containsKey(label)) {
                        this.labelToResource.put(label, new LinkedList<Resource>());
                    }
                    this.labelToResource.get(label).add(res);
                    Set<String> words = tokeniseLabel(label);
                    for (String w : words) {
                        this.terms.addString(w, label);
                    }
                }
            }
        }
        System.out.println("finished");
    }

    /**
     * split label to become a set of words
     */
    public static Set<String> tokeniseLabel(String label) {
        HashSet<String> result = new HashSet<>();
        String[] split = label.split("(\\s|:)+");
        for (int i = 0; i < split.length; ++i) {
            if (split[i] != null && !split[i].equals("")) {
                result.add(split[i]);
            }
        }
        return result;
    }

    /**
     * Searches by a set of words
     *
     * @param words
     * @return
     */
    public Set<Resource> searchResourcesCached(Set<String> words) {
        Set<String> successfulLabels = null;
        for (String word : words) {
            List<String> stringObjects = this.terms.getStringObjects(word);
            Set<String> labels = new HashSet<>();
            for (String label : stringObjects) {
                labels.add(label);
            }
            if (successfulLabels == null) {
                successfulLabels = labels;
            } else {
                successfulLabels.retainAll(labels);
            }
        }
        
        // we have a list of matching labels ... now we have to check if the matching is complete
        HashSet<Resource> result = new HashSet<>();
        for (String label : successfulLabels) {
            Set<String> labelWords = HEPOntologyAccessor.tokeniseLabel(label);
            if (labelWords.size() == words.size()) {
                // we have an exact match
                result.addAll(this.labelToResource.get(label));
            }
        }
        
        return result;
    }

    public static void main(String[] args) {
        System.out.println("Starting");

        String inputFileName = "files/HEPont.rdf";
        HEPOntologyAccessor hep = new HEPOntologyAccessor(inputFileName);

        System.out.println("\nTEST: SHOW STATEMENTS");
        hep.showStatements(20);

        System.out.println("\nTEST: SHOW STATEMENTS IN REFINED FORM");
        hep.showStatementsRefined(20);

        System.out.println("\nTEST: SHOW HIERARCHY OF CONCEPTS USING " + NARROWER + " RELATIONS");
        hep.extractTaxonomy("matter");


        System.out.println("\nTEST: SEARCH CONCEPTS");


        List<Resource> results = hep.searchResources("particle");

        for (Resource r : results) {
            System.out.println(r);
        }

        System.out.println("Finished");

    }
}
