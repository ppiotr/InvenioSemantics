/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import static org.inveniosoftware.inveniosemantics.MUOOntologyAccessor.filterWords;

/**
 *
 * @author piotr
 */
public class MUOOntologyAccessorTest extends TestCase {

    public MUOOntologyAccessorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Tests if the detection of numbers encoded in strings is correct
     */
    public void testNumberDetection() {
        assertTrue(MUOOntologyAccessor.isNumber("1"));
        assertTrue(MUOOntologyAccessor.isNumber("234."));
        assertTrue(MUOOntologyAccessor.isNumber("0"));
        assertTrue(MUOOntologyAccessor.isNumber(".1"));
        assertTrue(MUOOntologyAccessor.isNumber(".0324"));
        assertTrue(MUOOntologyAccessor.isNumber("1034234"));
        assertTrue(MUOOntologyAccessor.isNumber("1034234.94823847328878"));
        assertFalse(MUOOntologyAccessor.isNumber("."));
        assertFalse(MUOOntologyAccessor.isNumber("fds1.43"));
    }

    public void testSelectingWords() {
        
        List<String> tokens = TextUtils.tokeniseString2("bleble B kB satan dupa [volt] bancrupcy [V] 10 fdsh 10gEb 10. fd 2 jh zyrafa blah 5. fatamorgana 5.volts <megaelectronovolt>");
        List<String> filteredWords = MUOOntologyAccessor.filterWords(tokens);
        HashSet<String> words = new HashSet<>(filteredWords);
        assertTrue(words.contains("volt"));
        assertTrue(words.contains("V"));
        assertTrue(words.contains("fdsh"));
        assertTrue(words.contains("gEb"));
        assertTrue(words.contains("fd"));
        assertTrue(words.contains("jh"));
        assertTrue(words.contains("fatamorgana"));
        assertTrue(words.contains("volts"));
        
        assertFalse(words.contains("megaelectronovolt"));
        assertFalse(words.contains("bleble"));
        assertFalse(words.contains("B"));
        assertFalse(words.contains("kB"));
        assertFalse(words.contains("satan"));
        assertFalse(words.contains("dupa"));
        assertFalse(words.contains("bancrupcy"));
        assertFalse(words.contains("zyrafa"));
        assertFalse(words.contains("blah"));
        
        tokens = TextUtils.tokeniseString2("FIG. 1: A conguration used to calculate the hybrid potentials. The quark and antiquark are set at x = R=2 and x = R=2, respectively. The defect is placed at D.");
        filteredWords = MUOOntologyAccessor.filterWords(tokens);
        words = new HashSet<>(filteredWords);
        assertFalse(words.contains("A"));
        
        
    }
}
