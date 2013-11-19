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
class TestXmling extends GroovyTestCase {

    File teiInput = new File("testdata/teiHeaderExample.xml")


    void testToXml() {
        def initialStr = "<root>parse me\n<div>First tier<div>second tier containing <em>interior emph</em>\n marked with tags.  \nSignificant <seg type='word'><unclear>whit</unclear>ish</seg> space.</div>\n\n<div>And another parallel</div>\n</div>\n</root>"
        def expected = Normalizer.normalize(initialStr,Normalizer.NFC)


        /* toXml() method should serialize back to an XML-equivalent document */
        def gn = new GreekNode(initialStr)
        def actual = Normalizer.normalize(gn.toXml(),Normalizer.NFC)


        def StringReader actRdr = new StringReader(actual)
        def StringReader expRdr = new StringReader(expected)
        def actualDoc = DOMBuilder.parse(actRdr)
        def expectedDoc = DOMBuilder.parse(expRdr)
        // Explicitly set these before XML-based comparison:
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setNormalizeWhitespace(true)
        XMLUnit.setIgnoreComments(true)

        def xmlDiff = new Diff(actualDoc, expectedDoc)
        assert xmlDiff.identical()
    }


    void testXmlTranscoder() {
        def betaStr = "mh=nin a)/eide, qea/"
        // generate utf8 equivalent with transcoder:
        TransCoder beta2utf = new TransCoder()
        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")
        def uniStr = beta2utf.getString(betaStr)

        // test that GreekNode can transcode back to beta:
        def gn = new GreekNode("<root>"+uniStr+"</root>")
        assert "<root>" + betaStr + "</root>" ==  gn.transcodeXml("beta")

        def betaGN = new GreekNode("<root>"+betaStr+"</root>")
        betaGN.setCharEnc("beta")
        assert "<root>" + uniStr + "</root>" == betaGN.transcodeXml("UTF8")
    }



    /** Reads a TEI-namespaced test document, serializes to XML-equivalent output. */
    void testNamespaceHandling() {

        def root = new XmlParser().parse(teiInput)
        def gn = new GreekNode(root)

        def expected = Normalizer.normalize(teiInput.getText("UTF-8"),Normalizer.NFC)
        def actual = Normalizer.normalize(gn.toXml(true),Normalizer.NFC)

        def StringReader expRdr = new StringReader(expected)
        def StringReader actRdr = new StringReader(actual)

        def expectedDoc = DOMBuilder.parse(expRdr)
        def actualDoc = DOMBuilder.parse(actRdr)

        // Explicitly set these before XML-based comparison:
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setNormalizeWhitespace(true)
        XMLUnit.setIgnoreComments(true)

        def xmlDiff = new Diff(actualDoc, expectedDoc)
        assert xmlDiff.identical()
    }

    // test attributes in 2 different namespaces
    // test implicitly declrared xml: namespace
    void testImpliedNamespace() {
     def initialStr = "<root>parse me\n<div>Contains <foreign xml:lang='de'>fremde Wörter</foreign>!</div>\n</root>"
     GreekNode gn = new GreekNode(initialStr)

     def expected = Normalizer.normalize(initialStr,Normalizer.NFC)
     def actual = Normalizer.normalize(gn.toXml(true),Normalizer.NFC)

     def StringReader expRdr = new StringReader(expected)
     def StringReader actRdr = new StringReader(actual)

     def expectedDoc = DOMBuilder.parse(expRdr)
     def actualDoc = DOMBuilder.parse(actRdr)

     // Explicitly set these before XML-based comparison:
     XMLUnit.setIgnoreWhitespace(true)
     XMLUnit.setNormalizeWhitespace(true)
     XMLUnit.setIgnoreComments(true)

     def xmlDiff = new Diff(actualDoc, expectedDoc)
     assert xmlDiff.identical()     
    }

}
