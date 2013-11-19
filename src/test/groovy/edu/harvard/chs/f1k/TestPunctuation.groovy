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
class TestPunctuation extends GroovyTestCase {

    def leftdoublequote = "\u201C"
    def rightdoublequote = "\u201D"
    def leftsinglequote = "\u2018"
    def rightsinglequote = "\u2019"
    def leftpointybracket = "\u00AB"
    def rightpointybracket = "\u00BB"
    def punctList = ",.;?${leftdoublequote}${rightdoublequote}${leftsinglequote}${rightsinglequote}${leftpointybracket}${rightpointybracket}"


    void testStripPunctuation () {
        assert GreekNode.stripPunctuation(punctList).size() == 0
        assert "*mh=nin a)/eide qea/" == GreekNode.stripPunctuation("*mh=nin a)/eide, qea/","beta")
        
    }

    void testStripDiacritics () {
        def ilIntro = "*mh=nin a)/eide, qea/"
        def ilIntroStripped = "*mhnin aeide, qea"
        assert  ilIntroStripped  == GreekNode.stripDiacritics(ilIntro, "beta")

        TransCoder beta2utf = new TransCoder()
        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")
        def ilIntroUtf = beta2utf.getString(ilIntro)
        def ilIntroStrippedUtf = beta2utf.getString(ilIntroStripped)
        assert  ilIntroStripped  == GreekNode.stripDiacritics(ilIntro, "beta")

        /*
        OutputStreamWriter osw = new OutputStreamWriter(System.out, "UTF-8")
        osw.write "UTF DIAS STRIPPED ${ilIntroUtf} -> " +  GreekNode.stripDiacritics(ilIntroUtf, "UTF8")
        osw.close() */

    }



}
