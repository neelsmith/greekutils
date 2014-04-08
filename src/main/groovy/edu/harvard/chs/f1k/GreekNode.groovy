package edu.harvard.chs.f1k

/* GreekNode:  A class representing a well-formed XML node of text in Greek.
(c) 2011 Neel Smith, nsmith@holycross.edu
License: GPL 3.0.  See accompanying text of license.
*/

import edu.unc.epidoc.transcoder.TransCoder

import groovy.xml.Namespace


/** A class for working with Greek text.  A GreekNode
* represents a well-formed XML node of Greek text
* encoded in one of the explicitly listed systems for encoding Greek characters.
* In the current implentation, those encoding systems are either:
* <ol> <li>the Greek range of Unicode that falls within the Basic Multilingual Plane</li>
* <li>the ASCII-only representation of Greek in the beta code transcription scheme</li></ol>
* The beta-code system includes only that section of the TLG's convention that 
* represents character values by a single ASCII character.
* <p>In addition, several class methods support direct manipulation of Greek Strings.</p>
*/
class GreekNode {
    /** Temporary variable to delete before release version. */
    boolean debug = false

    /** Valid values for Greek encoding systems. */
    static def encodingList = ["UTF8","beta"]

    /** The subset of String values returned by Character.getType()
    * that represent different classes of punctuation.
    * Compare http://groovy.codehaus.org/JN1515-Characters.
    */
    static def unicodePunctuationNames = [
        'DASH_PUNCTUATION',  //Pd: any kind of hyphen or dash
        'START_PUNCTUATION', //Ps: any kind of opening bracket
        'END_PUNCTUATION',   //Pe: any kind of closing bracket
        'INITIAL_QUOTE_PUNCTUATION', //Pi: any kind of opening quote
        'FINAL_QUOTE_PUNCTUATION',   //Pf: any kind of closing quote
        'CONNECTOR_PUNCTUATION', //Pc: a punctuation character that connects words (eg underscore)
        'OTHER_PUNCTUATION',  //Po: any other kind of punctuation character
    ]

    // Classes of ASCII characters in TLG beta-code:
    /** Regular expression for puncutation in beta code*/
    static def betaPunct = /[\.;,?\[\]\{\}<>]/
    /** Regular expression for accents in beta code*/
    static def betaAccents = /[\\\/=]/
    /** Regular expression for breathings in beta code*/
    static def betaBreathings = /[()]/
    /** Regular expression for diaeresis in beta code*/
    static def betaDiaeresis = /\+/


    /** System for encoding Greek in this GreekNode.  Default is UTF8. */
    String charEnc = "UTF8"
    /** XML namespace for the node.  Default is TEI. */
    groovy.xml.Namespace nodeNamespace = new groovy.xml.Namespace("http://www.tei-c.org/ns/1.0")
    /** The root of the XML content as a parsed groovy.util.Node */
    def parsedNode = null


    GreekNode () {
    }

    /**
    * Constructs a GreekNode object from a groovy Node object.
    */
    GreekNode (groovy.util.Node n) {
      if (debug) {
	System.err.println "\nConstructring GreekNode from groovy node " + n + "\n"
      }
      parsedNode = n
    }



    /**
    * Constructs a GreekNode object from the XML representation of
    * contents, using default values for character encoding
    * and node namespace.
    * @throws Exception if content could not be parsed.
    */
    GreekNode (String content) {
        try {
            parsedNode = new XmlParser().parseText(content)
        } catch (Exception e) {
            throw new Exception("GreekNode: could not parse content ${content}")
        }
        if (!parsedNode) {
            throw new Exception("GreekNode: could not parse content ${content}")
        }

    }


    /** Determines if a given element is a normal element, or specially
    * identifies a wrapper for single tokens with mixed content models.
    * This implementation uses the F1K convention that elements named 'seg'
    * with a type attribute having the value 'word' uniquely groups tokens,
    * or words, with mixed content models.
    * @param n The node to examine.
    * @returns  True if the node is a wrapper for mixed content model.
    */
    private boolean magicNode(groovy.util.Node n) {
        if (n.name() == "seg") {
            if (n.attribute("type") == "word") {
                return true
            }
        }
        return false
    }



//    Object tokenizeText(String charEnc, boolean stripPunct) {
//        return "SHOULD RETURN TOKENIZED TEXT FOR HMT-CONVENTION MARKUP."
//    }


    Object tokenizeText(String charEnc, boolean stripPunct) {
        return GreekNode.tokenizeString(this.collectText(),charEnc,stripPunct)
    }


    Object tokenizeText() {
        return tokenizeString(this.collectText())
    }

    String collectText() {
        return collectText(this.parsedNode,"",false)
    }

    /** Recursively walks through all descendants of an XML node
     * and collects the content of text nodes.
     * @param n The parsed node from which text will be extracted.
     * @return A String with the text content of the object node.
    */
    String collectText(groovy.util.Node n) {
        return collectText(n,"",false)
    }


    String collectTextFromTei(Object n, String allText, boolean inWord) {
        return "SHOULD RETURN TEXT OBSERVING HMT TEI MARKUP CONVENTIONS"
    }



    /** Recursively walks through all descendants of an XML node
     * and collects the content of text nodes. In handling white space,
     * XML elements are taken to mark new, white-space delimited tokens
     * except where markup identified by the magicNode() method
     * groups together a token with mixed content model. 
     * @param n The parsed node from which text will be extracted.
     * @param allText The String of previously accumulated text content,
     * to which the content of any further text nodes will be added.
     * @param inWord Flag indicating whether or not we're within a "magic" node
     * with mixed content model.
     * @return A String with the text content of the object node.
    */
    String collectText(Object n, String allText, boolean inWord) {
        if (n.getClass().getName() == "java.lang.String") {
            allText = allText + n

        } else {
            if (magicNode(n)) {
                inWord = true
            }
            n.children().each { child ->
                if (!inWord) {
                    allText += " "     
                }
                allText = collectText(child, allText,inWord)
            }
            if (magicNode(n)) {
                inWord = false
            }
            return allText
        }
    }

    /** Gathers text content from the object's parsed node,
    * squeezes runs of white space to a single space.  
    * @return A String with the text content of the object node.
    */
    String greekTextContent() {
        def res = this.collectText(parsedNode)
        return res.replaceAll(/\s+/,' ')
    }


    /** Removes punctuation characters from a String
    * in default character encoding (UTF8).
    * @param s String to strip punctuation from.
    * @return String with puncutation characters removed.
    */
    static String stripPunctuation(String s) {
        return stripPunctuation(s,"UTF8")
    }


    /** Removes punctuation characters from a String
    * in the specified character encoding.
    * @param s String to strip punctuation from.
    * @param enc Greek character encoding to use.
    * @throws Exception if value of enc is not in encodingsList.
    * @return String with puncutation characters removed.
    */
    static String stripPunctuation(String s, String enc) {
        if (enc in encodingList) {
            switch (enc) {
                case "beta":
                    return (stripBetaPunctuation(s))
                break

                case "UTF8":
                    return (stripUtf8Punctuation(s))
                break

                default:
                    throw new Exception("stripPunctuation.  Encoding ${enc}  not yet implemented.")
            }

        } else {
                    throw new Exception("stripPunctuation.  unrecognized encoding ${enc}.")
        }
    }


    /** Removes from a String all characters with Unicode classes referring
    * to punctuation.
    * @param s String to strip punctuation from.
    * @return String with puncutation characters removed.
    */
    private static String stripUtf8Punctuation(String s) {
        StringBuffer sb = new StringBuffer()
        s.toCharArray().each { c ->
            def typeName =   Character.fields.find {it.get() == Character.getType(c) }.name
            if (typeName in unicodePunctuationNames) {
                // ignore it
            } else {
                sb.append(c)
            }
        }
        return  sb.toString()
    }
 
    /** Removes from a String ASCII characters representing
    * punctuation in the TLG's beta code transcription.
    * @param s String to strip punctuation from.
    * @return String with puncutation characters removed.
    */
    private static stripBetaPunctuation(String s) {
        return s.replaceAll(betaPunct,'')
    }




 
    /** Tokenizes a Greek string in default character
    * encoding (UTF8), including punctuation as part of tokens.
    * @param s String to tokenize.
    * @return List of Strings.
    */
    static def tokenizeString(String s) {
        return tokenizeString(s,"UTF8",false)
    }
 
    /** Tokenizes a Greek string in the specified character
    * encoding.  If the stripPunct flag is true, tokens will
    * have punctuation stripped out.
    * @param s String to tokenize.
    * @param enc Character encoding of string to tokenize
    * @param stripPunct True if punctuation should be removed.
    * @return List of Strings.
    */
    static def tokenizeString(String s, String enc, boolean stripPunct) {
        if (stripPunct) {
            def tokenList = []
            s.split(/\s/).each { t ->
                def strippedVal = stripPunctuation(t, enc)
                if (strippedVal != "") {
                    tokenList.add(strippedVal)
                }
            }
            return tokenList
        } else {
            return s.split(/\s/)
        }
    }
 



    /** Removes accents, breathings and diaeresis from a Greek string
    * in default character encoding (UTF8).
    * @param s The String from which characters should be removed.
    * @return The String with all diacritics removed.
    * @throws Exception if value of enc is not in encodingsList.
    */
    static String stripDiacritics(String s) {
        return stripDiacritics(s, "UTF8")
    }


    /** Removes accents, breathings and diaeresis from a Greek string
    * in the specified character encoding.
    * @param s The String from which characters should be removed.
    * @param enc The Greek encoding of the String.
    * @return The String with all diacritics removed.
    * @throws Exception if value of enc is not in encodingsList.
    */
    static String stripDiacritics(String s, String enc) {
        if (enc in encodingList) {
            switch (enc) {
                case "beta":
                    return (stripBetaDiacritics(s))
                break
 
                case "UTF8":
                    return (stripUtf8Diacritics(s))
                break
 
                default:
                    throw new Exception("stripDiacritics.  Encoding ${enc}  not yet implemented.")
            }
 
        } else {
                    throw new Exception("stripDiacritics.  unrecognized encoding ${enc}.")
        }
 
    }
 
    /** Removes accents, breathings and diaeresis from a Greek string
    * in beta code.
    * @param s The String from which characters should be removed.
    * @return The String with all diacritics removed.
    */
    private static String stripBetaDiacritics(String s) {
        s = s.replaceAll(betaAccents ,'')
        s = s.replaceAll(betaBreathings,'')
        s = s.replaceAll(betaDiaeresis,'')
        return s
    }


    /** Removes accents, breathings and diaeresis from a Greek string
    * in UTF8.
    * @param s The String from which characters should be removed.
    * @return The String with all diacritics removed.
    */
    private static String stripUtf8Diacritics(String s) {

        TransCoder utf2beta = new TransCoder()
        TransCoder beta2utf = new TransCoder()

        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")

        utf2beta.setParser("Unicode")
        utf2beta.setConverter("BetaCode")


        String betaStr = utf2beta.getString(s)
        betaStr = stripBetaDiacritics(betaStr)
        return  beta2utf.getString(betaStr)

    }

    /** Sorts a list of tokens in beta code
    * according to the Greek alphabet.
    * Punctuation and all diacritics are ignored for sort order.
    * @param tokenList List of tokens to sort.
    * @return An ordered list of tokens in beta code.
    */
    private static def alphabetizeBeta(List tokenList) {
        BetaComparator bc = new BetaComparator()
        return tokenList.sort(bc)
    }


    /** Sorts a list of tokens in UTF8
    * according to the Greek alphabet.
    * Punctuation and all diacritics are ignored for sort order.
    * @param tokenList List of tokens to sort.
    * @return An ordered list of tokens in UTF8.
    */
    private static def alphabetizeUtf8(List tokenList) {
        TransCoder utf2beta = new TransCoder()
        TransCoder beta2utf = new TransCoder()

        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")

        utf2beta.setParser("Unicode")
        utf2beta.setConverter("BetaCode")

        BetaComparator bc = new BetaComparator()        
        def betaTokens = []
        tokenList.each { 
            betaTokens.add(utf2beta.getString(it))
        }
        betaTokens = betaTokens.sort(bc)
        
        def sortedList = []
        betaTokens.each {
            sortedList.add(beta2utf.getString(it))
        }
        return sortedList
    }


    /** Sorts a list of tokens in the default character encoding
    * (UTF8) according to the Greek alphabet.
    * Punctuation and all diacritics are ignored for sort order.
    * @param tokenList List of tokens to sort.
    * @return An ordered list of tokens in UTF8.
    */
    static def alphabetize(List tokenList) {
        return alphabetize(tokenList,"UTF8")
    }

    /** Sorts a list of tokens in the specified character encoding
    * according to the Greek alphabet.
    * Punctuation and all diacritics are ignored for sort order.
    * @param tokenList List of tokens to sort.
    * @param enc The Greek encoding of the String.
    * @throws Exception if value of enc is not in encodingsList.
    * @return An ordered list of tokens in the specified character encoding.
    */
    static def alphabetize(List tokenList, String enc) {
        if (enc in encodingList) {
            switch (enc) {
                case "beta":
                    return (alphabetizeBeta(tokenList))
                break
 
                case "UTF8":
                    return (alphabetizeUtf8(tokenList))
                break
 
                default:
                    throw new Exception("alphabetize.  Encoding ${enc}  not yet implemented.")
            }
 
        } else {
                    throw new Exception("alphabetize.  unrecognized encoding ${enc}.")
        }

    }

    /** Lower cases a Greek String beta code.
    * @param s String to lowercase.
    * @return A beta-code String in lowercase characters
    */
    private static lowercaseBeta(String s) {
        TransCoder utf2beta = new TransCoder()
        utf2beta.setParser("Unicode")
        utf2beta.setConverter("BetaCode")

        TransCoder beta2utf = new TransCoder()
        beta2utf.setParser("BetaCode")
        beta2utf.setConverter("UnicodeC")

        def u8 = beta2utf.getString(s)
        def betaLower = lowercaseUtf8(u8)
        return utf2beta.getString(betaLower).toLowerCase()
    }


    /** Lower cases a Greek String in UTF8.
    * @param s String to lowercase.
    * @return A String in Unicode Greek in lowercase characters
    */
    private static lowercaseUtf8(String s) {
        return s.toLowerCase()
    }

    /** Lower cases a Greek String in the requested character encoding.
    * @param s String to lowercase.
    * @param enc The Greek encoding of the String.
    * @return A String in lowercase characters
    * @throws Exception if value of enc is not in encodingsList.
    */
    static def toLowerCase(String s, String enc) {
        if (enc in encodingList) {
            switch (enc) {
                case "beta":
                    return (lowercaseBeta(s))
                break
 
                case "UTF8":
                    return (lowercaseUtf8(s))
                break
 
                default:
                    throw new Exception("toLowerCase.  Encoding ${enc}  not yet implemented.")
            }
 
        } else {
                    throw new Exception("toLowerCase.  unrecognized encoding ${enc}.")
        }
    }


    /** Lower cases a Greek String in the default character encoding
    * (UTF8) 
    * @param s String to lowercase.
    * @return A String in lowercase characters.
    */
    static  String toLowerCase(String s) {
        return toLowerCase(s,"UTF8")
    }





    /** Creates a String of well-formed XML with Greek text in encoding toEncoding.
    * @param n A node resulting from XmlParser's output, which could be either
    * a groovy.util.Node object or a String object. 
    * @param fromEncoding Encoding used for node n.
    * @param toEncoding Encoding for result.
    * @param allText The String of previously accumulated text content,
    * to which the content of any further text nodes will be added.
    * @return A String of well-formed XML with Greek in encoding toEncoding.
    * @throws Exception if value of enc is not in encodingsList.
    */
    String transcodeXml(Object n, String fromEncoding, String toEncoding, String allText) {
        if (n.getClass().getName() == "java.lang.String") {
            TransCoder xc = new TransCoder()
            switch (fromEncoding) {
                case "beta":
                    xc.setParser("BetaCode")
                break
                case "UTF8":
                    xc.setParser("Unicode")
                break
                default :
                    throw new Exception("Transcode XML: unrecognized encoding ${fromEncoding}")
                break
            }

            switch (toEncoding) {
                case "beta":
                    xc.setConverter("BetaCode")
                break
                case "UTF8":
                    xc.setConverter("UnicodeC")
                break
                default :
                    throw new Exception("Transcode XML: unrecognized encoding ${fromEncoding}")
                break
            }
            if (toEncoding == "beta") {
                allText = allText + xc.getString(n).toLowerCase()
            } else {
                allText = allText + xc.getString(n).toLowerCase()
            }

        } else {
            allText += "<${n.name()}" + collectAttrs(n) + ">"
            n.children().each { child ->
                allText = transcodeXml(child, fromEncoding, toEncoding, allText)
            }
            allText+= "</${n.name()}>"
        }
        return allText
    }



    /** Serializes the context of parsedNode as well-formed XML with Greek text 
    * in encoding toEncoding.
    * @return A String of well-formed XML with Greek in encoding toEncoding.
    * @throws Exception if value of enc is not in encodingsList.
    */
    String transcodeXml(String toEncoding) {
        
        if (toEncoding in encodingList) {
            if (this.charEnc == toEncoding) {
                System.err.println "Not transcoding string:  already in target character encoding ${this.charEnc}"
                return this.toXml()
            } else {
                transcodeXml(parsedNode, this.charEnc,toEncoding, "")
            }

        } else {
            throw new Exception("transcodeXml: encoding ${toEncoding} not recognized")
        }
    }


    private String closeElement(Object n) {
        if (n.name() instanceof groovy.xml.QName)  {
            if (n.name().getPrefix().size() > 0) {
                return "</${n.name().getPrefix()}:${n.name().getLocalPart()}>"
            } else {
                return "</${n.name().getLocalPart()}>"
            }
        } else {
            return "</" + n.name() + ">"
        }
    }

    private String openElement(Object n, boolean withDefaultNs) {
        StringBuffer tag = new StringBuffer()
        if (n.name() instanceof groovy.xml.QName) {
            if (n.name().getPrefix().size() > 0) {
                if (withDefaultNs) {
                    tag.append('<?xml version="1.0" encoding="UTF-8"?>\n')
                }
                tag.append("<${n.name().getPrefix()}:${n.name().getLocalPart()}")
                if (withDefaultNs) {
                    tag.append(" xmlns='" + n.name().getNamespaceURI() + "' ")
                }
                tag.append(" xmlns:${n.name().getPrefix()}='" + n.name().getNamespaceURI() + "' ")

            } else {
                tag.append("<${n.name().getLocalPart()}")
               if (withDefaultNs) {
                    tag.append(" xmlns='" + n.name().getNamespaceURI() + "' ")
                }
 
            }
        } else {
            tag.append("<" + n.name())
        }
        tag.append( collectAttrs(n))
        tag.append(">")
        return tag.toString()
    }

    private String serializeNode(Object n, String allText, boolean inWord) {
        return serializeNode(n, allText, inWord, false)
    }

    /** Creates a String of well-formed XML by recursively walking the 
    * descendants of groovy.util.Node.
    * @param n A node resulting from XmlParser's output, which could be either
    * a groovy.util.Node object or a String object. 
    * @param allText The String of previously accumulated text content,
    * to which the content of any further text nodes will be added.
    * @param inWord Flag indicating whether or not we're within a "magic" node
    * with mixed content model.
    * @return A String of well-formed XML.
    */
    private String serializeNode(Object n, String allText, boolean inWord, boolean includeRootNsDecl) {
        if (n instanceof java.lang.String) {
            allText = allText + n

        } else {
            allText += openElement(n, includeRootNsDecl)

            if (magicNode(n)) {
                inWord = true
            }

            n.children().each { child ->
                if (!inWord) {
                    allText += " "     
                }
                allText = serializeNode(child, allText,inWord)
            }
            if (magicNode(n)) {
                inWord = false
            }
            allText+=  closeElement(n) 
            return allText
        }
    }


    /** Serializes the object's parsedNode as an XML String.
    * This serialization does not include any XML or namespace declarations.
    * @return A String of well-formed XML.
    */
    String toXml() {
        return serializeNode(parsedNode, "", false)
    }

    String toXml(boolean withDefaultNsDecl) {
        return serializeNode(parsedNode, "", false, withDefaultNsDecl)
    }

    /** Creates a String representing any attributes of a given node
    * in the form key="value"
    * @param n The Node to examine.
    * @return A String with space-separated key="value" pairs.
    */
    private String collectAttrs(groovy.util.Node n) {
        StringBuffer attrStr = new StringBuffer()
        n.attributes().keySet().each { a ->
            if (a instanceof groovy.xml.QName) {
                attrStr.append(" ${a.getPrefix()}:${a.getLocalPart()}='" + n.attribute(a) + "' ")
            } else {
                attrStr.append(" ${a}='" + n.attribute(a) + "' ")
            }
        }

        return attrStr.toString()
    }


}
