/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.inveniosoftware.inveniosemantics;

import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author piotr
 */
public class HEPOntologyAccessorTest extends TestCase {
    
    public HEPOntologyAccessorTest(String testName) {
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
     * Test of testPrecalculateLabelsIndex method, of class HEPOntologyAccessor.
     */
    public void testPrecalculateLabelsIndex() {
        System.out.println("showStatementsRefined");
        int limit = 0;
        HEPOntologyAccessor inst = new HEPOntologyAccessor("files/HEPont.rdf");
        // TODO review the generated test code and remove the default call to fail.
        HashMap<Resource, List<String>> allLabels = inst.getAllLabels();
        List<String> stringObjects = inst.terms.getStringObjects("dupadupadupa"); /// this should certainly fail ! There is no place for dupa in the HEPOntology !
        assertEquals(stringObjects.size(), 0);
        
        for (Resource res: allLabels.keySet()){
            for (String lab: allLabels.get(res)){
                Set<String> words = HEPOntologyAccessor.tokeniseLabel(lab);
                for (String word: words){
                    List<String> matching = inst.terms.getStringObjects(word);
                    assertTrue(matching.size() > 0);
                }
            }
        }
    }

}
