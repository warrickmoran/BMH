//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.06.12 at 12:58:15 PM CDT 
//

/**
 * This factory provides a convenient way to instantiate all SSML tags.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 12, 2014 3259       bkowal      Initial creation
 * 
 * </pre>
 * 
 * @author bkowal
 * @version 1.0
 */

package com.raytheon.uf.common.bmh.schemas.ssml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the org.w3._2001._10.synthesis package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _S_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Sentence.NAME);

    private final static QName _Phoneme_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Phoneme.NAME);

    private final static QName _Struct_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", "struct");

    private final static QName _P_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Paragraph.NAME);

    private final static QName _Aws_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", "aws");

    private final static QName _Break_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Break.NAME);

    private final static QName _Emphasis_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Emphasis.NAME);

    private final static QName _SayAs_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", SayAs.NAME);

    private final static QName _Voice_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Voice.NAME);

    private final static QName _Audio_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Audio.NAME);

    private final static QName _Sub_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Sub.NAME);

    private final static QName _Desc_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Desc.NAME);

    private final static QName _Speak_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Speak.NAME);

    private final static QName _Prosody_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Prosody.NAME);

    private final static QName _Mark_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", Mark.NAME);

    private final static QName _OriginalSpeakLexicon_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", SsmlLexicon.NAME);

    private final static QName _OriginalSpeakMeta_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", SsmlMeta.NAME);

    private final static QName _OriginalSpeakMetadata_QNAME = new QName(
            "http://www.w3.org/2001/10/synthesis", SsmlMetadata.NAME);

    /**
     * Create a new ObjectFactory that can be used to create new instances of
     * schema derived classes for package: org.w3._2001._10.synthesis
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Sub }
     * 
     */
    public Sub createSub() {
        return new Sub();
    }

    /**
     * Create an instance of {@link Desc }
     * 
     */
    public Desc createDesc() {
        return new Desc();
    }

    /**
     * Create an instance of {@link Voice }
     * 
     */
    public Voice createVoice() {
        return new Voice();
    }

    /**
     * Create an instance of {@link Audio }
     * 
     */
    public Audio createAudio() {
        return new Audio();
    }

    /**
     * Create an instance of {@link Speak }
     * 
     */
    public Speak createSpeak() {
        return new Speak();
    }

    /**
     * Create an instance of {@link Prosody }
     * 
     */
    public Prosody createProsody() {
        return new Prosody();
    }

    /**
     * Create an instance of {@link Mark }
     * 
     */
    public Mark createMark() {
        return new Mark();
    }

    /**
     * Create an instance of {@link Break }
     * 
     */
    public Break createBreak() {
        return new Break();
    }

    /**
     * Create an instance of {@link Sentence }
     * 
     */
    public Sentence createSentence() {
        return new Sentence();
    }

    /**
     * Create an instance of {@link Phoneme }
     * 
     */
    public Phoneme createPhoneme() {
        return new Phoneme();
    }

    /**
     * Create an instance of {@link Paragraph }
     * 
     */
    public Paragraph createParagraph() {
        return new Paragraph();
    }

    /**
     * Create an instance of {@link Emphasis }
     * 
     */
    public Emphasis createEmphasis() {
        return new Emphasis();
    }

    /**
     * Create an instance of {@link SayAs }
     * 
     */
    public SayAs createSayAs() {
        return new SayAs();
    }

    /**
     * Create an instance of {@link SsmlMeta }
     * 
     */
    public SsmlMeta createSsmlMeta() {
        return new SsmlMeta();
    }

    /**
     * Create an instance of {@link SsmlLexicon }
     * 
     */
    public SsmlLexicon createSsmlLexicon() {
        return new SsmlLexicon();
    }

    /**
     * Create an instance of {@link SsmlMetadata }
     * 
     */
    public SsmlMetadata createSsmlMetadata() {
        return new SsmlMetadata();
    }

    /**
     * Create an instance of {@link OriginalMark }
     * 
     */
    public OriginalMark createOriginalMark() {
        return new OriginalMark();
    }

    /**
     * Create an instance of {@link OriginalSpeak }
     * 
     */
    public OriginalSpeak createOriginalSpeak() {
        return new OriginalSpeak();
    }

    /**
     * Create an instance of {@link OriginalAudio }
     * 
     */
    public OriginalAudio createOriginalAudio() {
        return new OriginalAudio();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Sentence }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "s", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "struct")
    public JAXBElement<Sentence> createS(Sentence value) {
        return new JAXBElement<Sentence>(_S_QNAME, Sentence.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Phoneme }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "phoneme", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Phoneme> createPhoneme(Phoneme value) {
        return new JAXBElement<Phoneme>(_Phoneme_QNAME, Phoneme.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "struct")
    public JAXBElement<Object> createStruct(Object value) {
        return new JAXBElement<Object>(_Struct_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Paragraph }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "p", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "struct")
    public JAXBElement<Paragraph> createP(Paragraph value) {
        return new JAXBElement<Paragraph>(_P_QNAME, Paragraph.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "aws")
    public JAXBElement<Object> createAws(Object value) {
        return new JAXBElement<Object>(_Aws_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Break }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "break", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Break> createBreak(Break value) {
        return new JAXBElement<Break>(_Break_QNAME, Break.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Emphasis }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "emphasis", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Emphasis> createEmphasis(Emphasis value) {
        return new JAXBElement<Emphasis>(_Emphasis_QNAME, Emphasis.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SayAs }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "say-as", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<SayAs> createSayAs(SayAs value) {
        return new JAXBElement<SayAs>(_SayAs_QNAME, SayAs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Voice }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "voice", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Voice> createVoice(Voice value) {
        return new JAXBElement<Voice>(_Voice_QNAME, Voice.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Audio }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "audio", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Audio> createAudio(Audio value) {
        return new JAXBElement<Audio>(_Audio_QNAME, Audio.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Sub }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "sub", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Sub> createSub(Sub value) {
        return new JAXBElement<Sub>(_Sub_QNAME, Sub.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Desc }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "desc")
    public JAXBElement<Desc> createDesc(Desc value) {
        return new JAXBElement<Desc>(_Desc_QNAME, Desc.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Speak }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "speak")
    public JAXBElement<Speak> createSpeak(Speak value) {
        return new JAXBElement<Speak>(_Speak_QNAME, Speak.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Prosody }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "prosody", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Prosody> createProsody(Prosody value) {
        return new JAXBElement<Prosody>(_Prosody_QNAME, Prosody.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Mark }{@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "mark", substitutionHeadNamespace = "http://www.w3.org/2001/10/synthesis", substitutionHeadName = "aws")
    public JAXBElement<Mark> createMark(Mark value) {
        return new JAXBElement<Mark>(_Mark_QNAME, Mark.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SsmlLexicon }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "lexicon", scope = OriginalSpeak.class)
    public JAXBElement<SsmlLexicon> createOriginalSpeakLexicon(SsmlLexicon value) {
        return new JAXBElement<SsmlLexicon>(_OriginalSpeakLexicon_QNAME,
                SsmlLexicon.class, OriginalSpeak.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SsmlMeta }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "meta", scope = OriginalSpeak.class)
    public JAXBElement<SsmlMeta> createOriginalSpeakMeta(SsmlMeta value) {
        return new JAXBElement<SsmlMeta>(_OriginalSpeakMeta_QNAME,
                SsmlMeta.class, OriginalSpeak.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SsmlMetadata }
     * {@code >}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/2001/10/synthesis", name = "metadata", scope = OriginalSpeak.class)
    public JAXBElement<SsmlMetadata> createOriginalSpeakMetadata(
            SsmlMetadata value) {
        return new JAXBElement<SsmlMetadata>(_OriginalSpeakMetadata_QNAME,
                SsmlMetadata.class, OriginalSpeak.class, value);
    }

}
