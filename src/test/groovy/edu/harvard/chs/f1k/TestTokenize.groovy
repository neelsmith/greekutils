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
class TestTokenize extends GroovyTestCase {

    File testFileA = new File("testdata/sample-A.xml")


    def leftdoublequote = "\u201C"
    def rightdoublequote = "\u201D"
    def leftsinglequote = "\u2018"
    def rightsinglequote = "\u2019"
    def leftpointybracket = "\u00AB"
    def rightpointybracket = "\u00BB"
    def punctList = ",.;?${leftdoublequote}${rightdoublequote}${leftsinglequote}${rightsinglequote}${leftpointybracket}${rightpointybracket}"



    void testTokenize() {
        String stringOfTokes = "Tokens, including punctuation, here."
        // default:  leave punctuation in tokens
        assert  ["Tokens,","including","punctuation,","here."] == GreekNode.tokenizeString(stringOfTokes)
        // strip out punct when stripPunct param is true:
        assert  ["Tokens","including","punctuation","here"] == GreekNode.tokenizeString(stringOfTokes,"UTF8",true)
        assert  GreekNode.tokenizeString(punctList,"UTF8",true).size() == 0
        assert ["*mh=nin","a)/eide","qea/"] == GreekNode.tokenizeString("*mh=nin a)/eide, qea/", "beta", true)

        def cts = new groovy.xml.Namespace ("http://chs.harvard.edu/xmlns/cts3")
     
        def rootA = new XmlParser().parse(testFileA)
        def psgA = rootA[cts.reply][cts.passage][0]
        def nodeA = new GreekNode(psgA)
        assert nodeA.tokenizeText("UTF8",true).size() == 159

    }

}
