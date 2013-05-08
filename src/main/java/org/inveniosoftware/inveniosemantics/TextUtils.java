/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.inveniosoftware.inveniosemantics;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author piotr
 */
public class TextUtils {

    static List<String> tokeniseString(String toMatch) {
        LinkedList<String> results = new LinkedList<>();
        
        String[] parts = toMatch.split("\\s+");
        for (int i=0;i<parts.length;++i){
            results.add(parts[i]);
        }
        return results;
    }

    static List<String> tokeniseString2(String toMatch) {
        LinkedList<String> results = new LinkedList<>();
        
        String[] parts = toMatch.split("(:|\\s)+");
        for (int i=0;i<parts.length;++i){
            results.add(parts[i]);
        }
        return results;
    }
    
}
