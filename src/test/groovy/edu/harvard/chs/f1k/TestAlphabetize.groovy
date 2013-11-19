package edu.harvard.chs.f1k

import static org.junit.Assert.*
import org.junit.Test

import edu.unc.epidoc.transcoder.TransCoder

// For XML Unit comparisons:
import org.custommonkey.xmlunit.*

// For normalizing Unicode representations:
import com.ibm.icu.text.Normalizer
import groovy.xml.DOMBuilder

/**
* Class to test GreekNode class
*/
class TestAlphabetize extends GroovyTestCase {

    void testAlphabetize() {
        def ilWords = ["mh=nin", "a)/eide", "qea/"]
        def sortedWords = ["a)/eide", "qea/", "mh=nin"]
        assert sortedWords == GreekNode.alphabetize(ilWords,"beta")


        TransCoder utf2beta = new TransCoder()
        TransCoder beta2utf = new TransCoder()

        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")

        utf2beta.setParser("Unicode")
        utf2beta.setConverter("BetaCode")

        def ilWordsUtf = []
        def ilWords2 = ["mh=nin", "a)/eide", "qea/"]
        ilWords2.each {
            ilWordsUtf.add(beta2utf.getString(it))
        }
        def sortedWordsUtf = []
        sortedWords.each {
            sortedWordsUtf.add(beta2utf.getString(it))
        }
        assert sortedWordsUtf ==  GreekNode.alphabetize(ilWordsUtf)
/*
        OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF-8")
        osw.write "INITIAL SORT ON UTF LIST: " + ilWordsUtf + "\n"
        osw.write  "UTF SORT: " +        GreekNode.alphabetize(ilWordsUtf) + "\n"
        osw.write "COMPARE FOR ASSERTION: " + sortedWordsUtf
        osw.close() 
*/

    }


}
