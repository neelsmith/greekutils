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
class TestNodeExtract extends GroovyTestCase {


    void testTextExtract() {
        def gn = new GreekNode("<root>parse me<div>First tier<div>second tier containing <em>interior emph</em> marked with tags.  Significant <seg type='word'><unclear>whit</unclear>ish</seg> space.</div><div>And another parallel</div></div></root>")
        assert gn.greekTextContent().size() == 123
    }

}
