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
class TestCase extends GroovyTestCase {

    void testLowerCase() {

        def ucWord = "*m*h=*n*i*n"
        def lcWord = "mh=nin"
        assert  lcWord  == GreekNode.toLowerCase(ucWord, "beta")

        TransCoder beta2utf = new TransCoder()
        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")
        def utfUcWord = beta2utf.getString(ucWord)
        def utfLcWord = beta2utf.getString(lcWord)
/*

        OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF-8")
        osw.write "CONVERT " + utfUcWord + " to " + GreekNode.toLowerCase(utfUcWord)
        osw.close() 
*/

    }
}
