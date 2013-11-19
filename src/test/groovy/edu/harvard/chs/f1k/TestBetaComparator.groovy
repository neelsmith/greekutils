package edu.harvard.chs.f1k

import static org.junit.Assert.*
import org.junit.Test

import edu.unc.epidoc.transcoder.TransCoder

/**
* Class to test BetaComparator class
*/
class TestBetaComparator extends GroovyTestCase {

    void testBetaSort () {
        def bc = new BetaComparator()
        def ilWords = ["mh=nin", "a)/eide", "qea/"]
        def sortedWords = ["a)/eide", "qea/", "mh=nin"]
        assert sortedWords == ilWords.sort(bc)

        // When two words match for every char,
        // but one word keeps going, the short
        // word sorts first.
        def longShort = ["le/gete", "le/ge"]
        def shortLong = ["le/ge", "le/gete"]
        assert shortLong == longShort.sort(bc)
    }

}
