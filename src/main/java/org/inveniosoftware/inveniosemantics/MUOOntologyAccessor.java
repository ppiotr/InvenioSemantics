package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import invenio.common.Pair;
import invenio.common.PrefixTree;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The accessor class to the notions of the Measurable Units Ontology
 *
 * @author Piotr Praczyk
 */
public class MUOOntologyAccessor {

    private String _defFileName;
    private String _instFileName;
    public Model _model;
    public static final String MUOURI = "http://purl.oclc.org/NET/muo/muo#";
    public static final String INSTANCESURI = "http://purl.oclc.org/NET/muo/ucum/";

    /**
     * Constructor that receives the path of the file containing the HEP
     * ontology
     */
    public MUOOntologyAccessor(String defFileName, String instFileName) {
        _defFileName = defFileName;
        _instFileName = instFileName;
        uploadModel();
        precalculateSearchIndex();
    }

    private void addFileToModel(String fname) {
        InputStream in = FileManager.get().open(fname);
        if (in == null) {
            throw new IllegalArgumentException("File: " + fname + " not found");
        }
        _model.read(in, "");
    }

    private void uploadModel() {
        _model = ModelFactory.createDefaultModel();
        this.addFileToModel(_defFileName);
        this.addFileToModel(_instFileName);
    }

    public Map<Resource, List<String>> getAllPrefixes() {
        String query =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "PREFIX uomvoc: <" + this.MUOURI + "> "
                + "SELECT DISTINCT ?subject ?symbol "
                + "WHERE {  ?subject rdf:type uomvoc:Prefix. {?subject uomvoc:prefSymbol ?symbol} UNION {?subject uomvoc:altSymbol ?symbol}}";

        QueryExecution qexec = QueryExecutionFactory.create(query, this._model);
        ResultSet qResults = qexec.execSelect();

        //working with text
        HashMap<Resource, List<String>> results = new HashMap<>();
        while (qResults.hasNext()) {
            QuerySolution soln = qResults.nextSolution();
            Resource prefix = soln.getResource("subject");
            String symbol = soln.getLiteral("symbol").getString();
            if (!results.containsKey(prefix)) {
                results.put(prefix, new LinkedList<String>());
            }
            results.get(prefix).add(symbol);
        }
        return results;
    }

    public Map<Resource, List<String>> getAllUnits() {
        String query =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> "
                + "PREFIX uomvoc: <" + MUOOntologyAccessor.MUOURI + "> "
                + "SELECT DISTINCT ?subject ?symbol "
                + "WHERE {  ?subject rdf:type uomvoc:UnitOfMeasurement. {?subject uomvoc:prefSymbol ?symbol} UNION {?subject uomvoc:altSymbol ?symbol}}";

        QueryExecution qexec = QueryExecutionFactory.create(query, this._model);
        ResultSet qResults = qexec.execSelect();

        HashMap<Resource, List<String>> results = new HashMap<>();
        while (qResults.hasNext()) {
            QuerySolution soln = qResults.nextSolution();
            Resource unit = soln.getResource("subject");
            String symbol = soln.getLiteral("symbol").getString();

            if (!results.containsKey(unit)) {
                results.put(unit, new LinkedList<String>());
            }
            results.get(unit).add(symbol);

        }
        return results;
    }
    private PrefixTree<Resource> prefixesIndex;
    private PrefixTree<Resource> unitsIndex;

    private PrefixTree<Resource> indexSymbols(Map<Resource, List<String>> symbols) {
        PrefixTree<Resource> result = new PrefixTree<>();
        int numErrors = 0;
        for (Resource res : symbols.keySet()) {
            for (String symbol : symbols.get(res)) {
                List<Resource> collisions = result.getStringObjects(symbol);
                if (collisions.size() > 0) {
                    System.out.println("    " + res.getURI() + " collides with " + collisions.get(0).getURI());
                    numErrors++;
                }
                result.addString(symbol, res);

            }
        }
        if (numErrors > 0) {
            System.out.println("Encountered collisions among the symbols ! (" + numErrors + ")");
        }

        return result;
    }

    private void precalculateSearchIndex() {
        this.prefixesIndex = indexSymbols(this.getAllPrefixes());
        this.unitsIndex = indexSymbols(this.getAllUnits());
    }

    public List<Pair<Resource, Resource>> annotateWordWithUnits(String word) {

        for (int prefixLen = 0; prefixLen < word.length(); ++prefixLen) {
            // trying to match a prefix of a given length and the unit at the same time
            String prefixCandidate = word.substring(0, prefixLen);
            String unitCandidate = word.substring(prefixLen);
            List<Resource> prefixes = this.prefixesIndex.getStringObjects(prefixCandidate);
            List<Resource> units = this.unitsIndex.getStringObjects(unitCandidate);
            if ((!prefixes.isEmpty() || prefixLen == 0) && !units.isEmpty()) {
                // we can have only one match with prefix and many with units
                LinkedList<Pair<Resource, Resource>> result = new LinkedList<>();
                for (Resource unit : units) {
                    result.add(new Pair<>(prefixLen == 0 ? null : prefixes.get(0), unit));
                }
                return result;
            }

        }
        return new LinkedList<>();
    }

    private static List<String> expandBrackets(List<String> strings) {
        LinkedList<String> results = new LinkedList<>();
        for (String atom : strings) {
            results.add(atom);
            if (atom.startsWith("(") && atom.endsWith(")")) {
                results.add(atom.substring(1, atom.length() - 1));
            }
            if (atom.startsWith("[") && atom.endsWith("]")) {
                results.add(atom.substring(1, atom.length() - 1));
            }
            if (atom.startsWith("|") && atom.endsWith("|")) {
                results.add(atom.substring(1, atom.length() - 1));
            }
        }
        return results;
    }

    public static boolean isNumber(String word) {
        return word.matches("[0-9]*\\.?[0-9]*") && word.length() > 0 && !word.equals(".");
    }

    /**
     * Leaves only the appearances of units, which can potentially lead to a
     * correct detection: words appearing after numbers or starting with a
     * number words in various types of brackets
     *
     * @param words
     * @return
     */
    @SuppressWarnings("empty-statement")
    public static List<String> filterWords(List<String> words) {
        LinkedList<String> result = new LinkedList<>();
        String previous = "";
        for (String word : words) {
            String toAdd = null;
            if (MUOOntologyAccessor.isNumber(previous)) { // we support only the standard notation, for simplicity
                toAdd = word;
            }
            int i;
            for (i = 0; i < word.length() && MUOOntologyAccessor.isNumber(word.substring(0, i + 1)); ++i);

            if (i > 0 && i < word.length()) {
                toAdd = word.substring(i);
                word = toAdd;
            }

            if ((word.startsWith("[") && word.endsWith("]"))) {
                toAdd = word.substring(1, word.length() - 1);
            }

            if (toAdd != null) {
                result.add(toAdd);
            }
            previous = word;
        }
        return result;
    }

    /**
     * Creating new unit which is a result of adding a given prefix before the
     * given unit
     *
     * @param prefix
     * @param unit
     * @return
     */
    public Resource createPrefixedResource(Resource prefix, Resource unit) {
        return unit; // for the moment, we only return the existing basic unit
    }

    public Set<Resource> annotateStringWithUnits(String toMatch) {
        HashSet<Resource> results = new HashSet<>();
        List<String> words = TextUtils.tokeniseString(toMatch);
        words = filterWords(words);
        for (String word : words) {
            List<Pair<Resource, Resource>> res = this.annotateWordWithUnits(word);
            for (Pair<Resource, Resource> p : res) {
                results.add(p.first == null ? p.second : createPrefixedResource(p.first, p.second));
            }
        }

        return results;
    }

    public static void main(String[] args) {
        MUOOntologyAccessor muoOntologyAccessor = new MUOOntologyAccessor("./files/muo-vocab.owl", "./files/ucum-instances.owl");
        List<Pair<Resource, Resource>> annotateWordWithUnits = muoOntologyAccessor.annotateWordWithUnits("satan");
        List<Pair<Resource, Resource>> annotateWordWithUnits1 = muoOntologyAccessor.annotateWordWithUnits("kB");
        List<Pair<Resource, Resource>> annotateWordWithUnits2 = muoOntologyAccessor.annotateWordWithUnits("B");
        Set<Resource> annotateStringWithUnits = muoOntologyAccessor.annotateStringWithUnits("bleble B kB satan dupa [volt] volt [V]");

        System.out.println("a");
    }
}
