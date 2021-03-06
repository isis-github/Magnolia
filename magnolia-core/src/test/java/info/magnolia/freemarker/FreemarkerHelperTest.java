/**
 * This file Copyright (c) 2003-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.freemarker;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.link.LinkTransformerManager;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockAggregationState;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.model.Color;
import info.magnolia.test.model.Pair;

import java.io.IOException;
import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.junit.Test;

import freemarker.core.InvalidReferenceException;
import freemarker.template.TemplateException;

/**
 * @version $Id$
 */
public class FreemarkerHelperTest extends AbstractFreemarkerTestCase {

    @Test
    public void testWeCanUseAnyObjectTypeAsOurRoot() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "${left} ${right.left.blue - left} ${right.right.green} ${right.right.name}");
        final Pair<Integer, Pair<Color, Color>> root = new Pair<Integer, Pair<Color, Color>>(Integer.valueOf(100), new Pair<Color, Color>(Color.PINK, Color.ORANGE));
        assertRendereredContent("100 75 200 orange", root, "test.ftl");
    }

    @Test
    public void testSimpleNodeReferenceOutputsItsName() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        foo.addContent(new MockContent("bar"));
        foo.addContent(new MockContent("baz"));
        foo.addContent(new MockContent("gazonk"));
        final Pair<Color, Content> pair = new Pair<Color, Content>(Color.ORANGE, foo);
        final Map<String, Object> map = createSingleValueMap("pair", pair);

        tplLoader.putTemplate("test.ftl", "${pair.right} ${pair.right.gazonk} ${pair.left?string} ${pair.right?string} ${pair.right.gazonk?string}");

        assertRendereredContent("foo gazonk color:orange foo gazonk", map, "test.ftl");
    }

    @Test
    public void testSubNodesAreReachable() throws TemplateException, IOException {
        tplLoader.putTemplate("test_sub.ftl", "The child node's bli'bla property is ${bli['bla']}");

        final MockContent c = new MockContent("plop");
        c.setUUID("123");
        c.addNodeData("foo", "bar");
        final MockContent bli = new MockContent("bli");
        c.addContent(bli);
        bli.addNodeData("bla", "bloup");

        assertRendereredContent("The child node's bli'bla property is bloup", c, "test_sub.ftl");
    }

    @Test
    public void testSubSubNode() throws TemplateException, IOException {
        final MockContent baz = new MockContent("baz");
        baz.addNodeData("prop", "wassup");
        final MockContent bar = new MockContent("bar");
        final MockContent foo = new MockContent("foo");
        final MockContent c = new MockContent("root");
        bar.addContent(baz);
        foo.addContent(bar);
        c.addContent(foo);

        tplLoader.putTemplate("test.ftl", "yo, ${foo.bar.baz['prop']} ?");
        assertRendereredContent("yo, wassup ?", c, "test.ftl");
    }

    @Test
    public void testCanReachParentWithBuiltIn() throws Exception {
        final Content c = MockUtil.createNode("/foo/bar",
                "/foo.myProp=this is foo",
        "/foo/bar.myProp=this is bar");

        tplLoader.putTemplate("test.ftl", "${content.myProp} and ${content?parent.myProp}");
        assertRendereredContent("this is bar and this is foo", createSingleValueMap("content", c), "test.ftl");
    }

    /** not supported:
    @Test
    public void testCanReachParentWithProperty() throws Exception {
        final Content c = MockUtil.createNode("/foo/bar",
                "/foo.myProp=this is foo",
                "/foo/bar.myProp=this is bar");

        tplLoader.putTemplate("test.ftl", "${content.myProp} and ${content.parent.myProp}");
        assertRendereredContent("this is bar and this is foo", createSingleValueMap("content", c), "test.ftl");
    }*/

    /** not supported:
    @Test
    public void testCanReachParentWithMethod() throws Exception {
        final Content c = MockUtil.createNode("/foo/bar",
                "/foo.myProp=this is foo",
                "/foo/bar.myProp=this is bar");

        tplLoader.putTemplate("test.ftl", "${content.myProp} and ${content.getParent().myProp}");
        assertRendereredContent("this is bar and this is foo", createSingleValueMap("content", c), "test.ftl");
    }*/

    @Test
    public void testCanLoopThroughNodes() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        final MockContent c = new MockContent("root");
        foo.addContent(new MockContent("bar"));
        foo.addContent(new MockContent("baz"));
        foo.addContent(new MockContent("gazonk"));
        c.addContent(foo);

        tplLoader.putTemplate("test.ftl", "${foo?children?size}: <#list foo?children as n>${n.@handle} </#list>");

        assertRendereredContent("3: /root/foo/bar /root/foo/baz /root/foo/gazonk ", c, "test.ftl");
    }

    @Test
    public void testCanLoopThroughNodesNestedInBean() throws TemplateException, IOException {
        final MockContent foo = new MockContent("foo");
        foo.addContent(new MockContent("bar"));
        foo.addContent(new MockContent("baz"));
        foo.addContent(new MockContent("gazonk"));
        final Object pair = new Pair<Color, Content>(Color.ORANGE, foo);
        final String key = "pair";
        Map map = createSingleValueMap(key, pair);

        tplLoader.putTemplate("test.ftl", "${pair.right?children?size}: <#list pair.right?children as n>${n.@handle} </#list>");

        assertRendereredContent("3: /foo/bar /foo/baz /foo/gazonk ", map, "test.ftl");
    }

    @Test
    public void testCanLoopThroughPropertiesUsingTheKeysBuiltIn() throws TemplateException, IOException {
        final MockContent f = new MockContent("flebele");
        f.addNodeData("foo", "bar");
        f.addNodeData("bar", "baz");
        f.addNodeData("baz", "gazonk");
        final MockContent c = new MockContent("root");
        c.addContent(f);
        tplLoader.putTemplate("test.ftl", "${flebele?keys?size}:<#list flebele?keys as n> ${n}=${flebele[n]}</#list>");

        assertRendereredContent("3: foo=bar bar=baz baz=gazonk", c, "test.ftl");
    }

    @Test
    public void testCanLoopThroughPropertiesUsingTheValuesBuiltIn() throws TemplateException, IOException {
        final MockContent f = new MockContent("flebele");
        f.addNodeData("foo", "bar");
        f.addNodeData("bar", "baz");
        f.addNodeData("baz", "gazonk");
        final MockContent c = new MockContent("root");
        c.addContent(f);
        tplLoader.putTemplate("test.ftl", "${flebele?values?size}:<#list flebele?values as v> ${v}</#list>");

        assertRendereredContent("3: bar baz gazonk", c, "test.ftl");
    }

    //    public void testCanGetPropertyHandle() throws TemplateException, IOException {
    //        final MockContent f = new MockContent("flebele");
    //        f.addNodeData(new MockNodeData("foo", "bar"));
    //        final MockContent c = new MockContent("root");
    //        c.addContent(f);
    //        tplLoader.putTemplate("test2.ftl", "${flebele['foo'].@handle} ${flebele['foo'].name}");// ${flebele.foo.@handle}");
    //        assertRendereredContent("/root/flebele/foo", c, "test2.ftl");
    //    }

    @Test
    public void testBooleanPropertiesAreHandledProperly() throws TemplateException, IOException {
        final MockContent c = new MockContent("root");
        final MockContent foo = new MockContent("foo");
        foo.addNodeData("hop", Boolean.TRUE);
        foo.addNodeData("hip", Boolean.FALSE);
        c.addContent(foo);
        tplLoader.putTemplate("test.ftl", "${foo['hop']?string(\"yes\", \"no\")}" +
                " ${foo.hop?string(\"yes\", \"no\")}" +
                " ${foo['hip']?string(\"yes\", \"no\")}" +
        " ${foo.hip?string(\"yes\", \"no\")}");

        assertRendereredContent("yes yes no no", c, "test.ftl");
    }

    @Test
    public void testDatePropertiesAreHandledProperly() throws TemplateException, IOException {
        final MockContent c = new MockContent("root");
        final MockContent foo = new MockContent("foo");
        foo.addNodeData("date", new GregorianCalendar(2007, 5, 3, 15, 39, 46));
        c.addContent(foo);

        tplLoader.putTemplate("test.ftl", "${foo['date']?string('yyyy-MM-dd HH:mm:ss')}");

        assertRendereredContent("2007-06-03 15:39:46", c, "test.ftl");
    }

    @Test
    public void testNumberProperties() throws TemplateException, IOException {
        final MockContent c = new MockContent("root");
        final MockContent foo = new MockContent("foo");
        foo.addNodeData("aLong", new Long(1234567890123456789l));
        foo.addNodeData("aDouble", new Double(12345678.901234567890d));
        c.addContent(foo);
        tplLoader.putTemplate("test.ftl", "${foo['aLong']} , ${foo.aDouble}");
        // ! TODO ! this is locale dependent
        assertRendereredContent("1,234,567,890,123,456,789 , 12,345,678.901", c, "test.ftl");
    }

    @Test
    public void testReferenceProperties() throws TemplateException, IOException, RepositoryException {
        final MockContent foo = new MockContent("foo");
        final MockContent bar = new MockContent("bar");
        foo.addNodeData("some-ref", bar);
        bar.addNodeData("baz", "gazonk");
        final MockHierarchyManager hm = new MockHierarchyManager();
        hm.getRoot().addContent(foo);
        hm.getRoot().addContent(bar);

        tplLoader.putTemplate("test.ftl", "${foo['some-ref']} ${foo['some-ref'].baz}");
        assertRendereredContent("bar gazonk", createSingleValueMap("foo", foo), "test.ftl");
    }

    @Test
    public void testRendereredWithCurrentLocale() throws TemplateException, IOException {
        tplLoader.putTemplate("test.ftl", "this is a test template.");
        tplLoader.putTemplate("test_en.ftl", "this is a test template in english.");
        tplLoader.putTemplate("test_fr_BE.ftl", "Ceci est une template belge hein une fois.");
        tplLoader.putTemplate("test_fr.ftl", "Ceci est une template de test en français.");

        final MockContent c = new MockContent("pouet");
        c.setUUID("123");
        c.addNodeData("foo", "bar");

        assertRendereredContent("Ceci est une template belge hein une fois.", new Locale("fr", "BE"), c, "test.ftl");
    }

    @Test
    public void testMissingAndDefaultValueOperatorsActsAsIExceptThemTo() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#if content.title?has_content]<h2>${content.title}</h2>[/#if]");
        final MockContent c = new MockContent("pouet");
        final Map m = createSingleValueMap("content", c);
        assertRendereredContent("", m, "test.ftl");

        c.addNodeData("title", "");
        assertRendereredContent("", m, "test.ftl");

        c.addNodeData("title", "pouet");
        assertRendereredContent("<h2>pouet</h2>", m, "test.ftl");
    }

    @Test
    public void testContextPathIsAddedWithWebContext() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${contextPath}:");
        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.getContextPath()).andReturn("/tralala");
        expect(context.getAggregationState()).andReturn(new MockAggregationState());
        expect(context.getServletContext()).andReturn(null);
        expect(context.getRequest()).andReturn(null);
        expect(context.getResponse()).andReturn(null);
        replay(context);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":/tralala:", new HashMap(), "pouet");
        verify(context);
    }

    @Test
    public void testContextPathIsNotAddedWithNotWebContext() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${contextPath}:");
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.US);

        replay(context);
        MgnlContext.setInstance(context);

        final StringWriter out = new StringWriter();
        try {
            fmHelper.render("pouet", new HashMap(), out);
            fail("should have failed");
        } catch (InvalidReferenceException e) {
            assertEquals("Expression contextPath is undefined on line 1, column 4 in pouet.", e.getMessage());
        }

        verify(context);
    }

    @Test
    public void testContextPathIsAlsoAvailableThroughMagnoliaContext() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${ctx.contextPath}:");
        final WebContext context = createStrictMock(WebContext.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.getContextPath()).andReturn("/tralala"); // called when preparing the freemarker data model
        expect(context.getAggregationState()).andReturn(new MockAggregationState());
        expect(context.getServletContext()).andReturn(null);
        expect(context.getRequest()).andReturn(null);
        expect(context.getResponse()).andReturn(null);
        expect(context.getContextPath()).andReturn("/tralala"); // actual call from the template
        replay(context);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":/tralala:", new HashMap(), "pouet");
        verify(context);
    }

    @Test
    public void testMagnoliaContextIsExposed() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${ctx.user.name}:");
        final Context context = createStrictMock(Context.class);
        final User user = createStrictMock(User.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.getUser()).andReturn(user);
        expect(user.getName()).andReturn("Hiro Nakamura");
        replay(context, user);
        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":Hiro Nakamura:", new HashMap(), "pouet");
        verify(context, user);
    }

    @Test
    public void testMagnoliaContextAttributesAreAvailableWithMapSyntax() throws IOException, TemplateException {
        tplLoader.putTemplate("pouet", ":${ctx.foo}:${ctx['baz']}:");
        final Context context = createStrictMock(Context.class);
        expect(context.getLocale()).andReturn(Locale.US);

        expect(context.get("foo")).andReturn("bar");
        expect(context.get("baz")).andReturn("buzz");

        replay(context);

        MgnlContext.setInstance(context);
        assertRendereredContentWithoutCheckingContext(":bar:buzz:", new HashMap(), "pouet");
        verify(context);
    }

    @Test
    public void testEvalCanEvaluateDynamicNodeProperties() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "evaluated result: ${'content.title'?eval}");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData("title", "This is my title");
        c.addNodeData("other", "other-value");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("content", c);

        assertRendereredContent("evaluated result: This is my title", m, "test.ftl");
    }

    @Test
    public void testInterpretCanBeUsedForDynamicNodeProperties() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#assign dynTpl='${content.title}'?interpret]\n" +
        "evaluated result: [@dynTpl/]");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData("title", "This is my ${content.other} title");
        c.addNodeData("other", "other-value");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("content", c);

        assertRendereredContent("evaluated result: This is my other-value title", m, "test.ftl");
    }

    @Test
    public void testEvalCanAlsoBeUsedForNestedExpressions() throws IOException, TemplateException {
        // except we need lots of quotes
        tplLoader.putTemplate("test.ftl", "evaluated result: ${'\"${content.title}\"'?eval}");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData("title", "This is my ${content.other}");
        c.addNodeData("other", "other-value");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("content", c);

        assertRendereredContent("evaluated result: This is my other-value", m, "test.ftl");
    }

    @Test
    public void testInterpretCanBeUsedEvenIfPropertyHasNoFreemarkerStuff() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "[#assign dynTpl='${content.title}'?interpret]evaluated title: [@dynTpl/]");

        final MockContent c = new MockContent("content");
        c.setUUID("123");
        c.addNodeData("title", "This is my plain title");
        c.addNodeData("other", "other-value");
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("content", c);

        assertRendereredContent("evaluated title: This is my plain title", m, "test.ftl");
    }

    private final static String SOME_UUID = "deb0c7d0-402f-4e04-9db3-cb308695733e";

    @Test
    public void testUuidLinksAreTransformedToRelativeLinksInWebContext() throws IOException, TemplateException, RepositoryException {
        final MockContent page = new MockContent("baz");
        final MockHierarchyManager hm = prepareHM(page);

        final AggregationState agg = new AggregationState();
        agg.setMainContent(page);
        final WebContext context = createStrictMock(WebContext.class);
        final MockSession session = new MockSession("website");
        MgnlContext.setInstance(context);
        expect(context.getContextPath()).andReturn("/");
        expect(context.getLocale()).andReturn(Locale.CANADA);
        expect(context.getAggregationState()).andReturn(agg);
        expect(MgnlContext.getJCRSession("website")).andReturn(session).times(2);

        LinkTransformerManager.getInstance().setMakeBrowserLinksRelative(true);

        replay(context);
        agg.setOriginalURI("/foo/bar/boo.html");
        doTestUuidLinksAreTransformed(context, "== Some text... blah blah... <a href=\"baz.html\">Bleh</a> ! ==");
        verify(context);
    }

    @Test
    public void testUuidLinksAreTransformedToAbsoluteLinksInWebContextWithoutAggregationState() throws IOException, TemplateException, RepositoryException {
        final MockContent page = new MockContent("baz");
        final MockHierarchyManager hm = prepareHM(page);
        final MockSession session = new MockSession("website");

        LinkTransformerManager.getInstance().setAddContextPathToBrowserLinks(true);

        final WebContext context = createStrictMock(WebContext.class);
        MgnlContext.setInstance(context);
        expect(context.getLocale()).andReturn(Locale.CANADA);
        expect(context.getAggregationState()).andReturn(null);
        expect(MgnlContext.getJCRSession("website")).andReturn(session).anyTimes();
        expect(context.getContextPath()).andReturn("/some-context");

        replay(context);
        doTestUuidLinksAreTransformed(context, "== Some text... blah blah... <a href=\"/some-context/foo/bar/baz.html\">Bleh</a> ! ==");
        verify(context);
    }

    @Test
    public void testUuidLinksAreTransformedToFullUrlLinksInNonWebContext() throws IOException, TemplateException, RepositoryException {
        doTestUuidLinksAreTransformed(null, "== Some text... blah blah... <a href=\"http://myTests:1234/yay/foo/bar/baz.html\">Bleh</a> ! ==");
    }

    private void doTestUuidLinksAreTransformed(Context webCtx, String expectedOutput) throws IOException, TemplateException, RepositoryException {
        MockHierarchyManager cfgHM = MockUtil.createHierarchyManager(RepositoryConstants.WEBSITE, "fakeemptyrepo");
        MockUtil.mockObservation(cfgHM);
        final MockSession session = new MockSession("website");

        final SystemContext sysMockCtx = createStrictMock(SystemContext.class);

        if (webCtx == null) {
            expect(sysMockCtx.getLocale()).andReturn(Locale.KOREA);
            final MockHierarchyManager hm = prepareHM(new MockContent("baz"));
            expect(sysMockCtx.getJCRSession("website")).andReturn(session).anyTimes();
        }
        ComponentsTestUtil.setInstance(SystemContext.class, sysMockCtx);
        replay(sysMockCtx);

        ComponentsTestUtil.setImplementation(URI2RepositoryManager.class, URI2RepositoryManager.class);
        final I18nContentSupport i18NSupportMock = createStrictMock(I18nContentSupport.class);
        ComponentsTestUtil.setInstance(I18nContentSupport.class, i18NSupportMock);

        expect(i18NSupportMock.toI18NURI("/foo/bar.html")).andReturn("/foo/bar/baz.html").times(1, 2);

        final String text = "Some text... blah blah... <a href=\"${link:{uuid:{" + SOME_UUID + "},repository:{website},handle:{/foo/bar},nodeData:{},extension:{html}}}\">Bleh</a> !";
        final MockContent c = new MockContent("content");
        c.addNodeData("text", text);
        tplLoader.putTemplate("test", "== ${text} ==");

        replay(i18NSupportMock);
        MgnlContext.setInstance(webCtx == null ? sysMockCtx : webCtx);
        assertRendereredContentWithoutCheckingContext(expectedOutput, c, "test");
        verify(i18NSupportMock);
        verify(sysMockCtx);
    }

    @Test
    public void testUserPropertiesAreAvailable() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "${user.name} is my name, is speak ${user.language}, I'm ${user.enabled?string('', 'not ')}enabled, and testProp has a value of ${user.testProp} !");

        final User user = createStrictMock(User.class);
        expect(user.getName()).andReturn("myName");
        expect(user.getLanguage()).andReturn("fr");
        expect(user.isEnabled()).andReturn(false);
        expect(user.getProperty("testProp")).andReturn("testValue");

        replay(user);
        assertRendereredContent("myName is my name, is speak fr, I'm not enabled, and testProp has a value of testValue !", createSingleValueMap("user", user), "test.ftl");
        verify(user);
    }

    @Test
    public void testUserUnsupportedExceptionFallback() throws Exception {
        tplLoader.putTemplate("test.ftl", "${user.name} is my name, fullName: ${user.fullName!user.name}, testProp: ${user.testProp!'default'} !");
        final User user = createStrictMock(User.class);
        expect(user.getName()).andReturn("myName");
        expect(user.getProperty("fullName")).andThrow(new UnsupportedOperationException("getProperty:fullName"));
        expect(user.getName()).andReturn("myName");
        expect(user.getProperty("testProp")).andThrow(new UnsupportedOperationException("getProperty:testValue"));

        replay(user);
        assertRendereredContent("myName is my name, fullName: myName, testProp: default !", createSingleValueMap("user", user), "test.ftl");
        verify(user);
    }

    @Test
    public void testNodeNameCanBeRenderedImplicitly() throws Exception {
        tplLoader.putTemplate("test.ftl", "This should output the node's name: ${content}");
        final Map root = createSingleValueMap("content", new MockContent("myNode"));
        assertRendereredContent("This should output the node's name: myNode", root, "test.ftl");
    }

    @Test
    public void testNodeNameCanBeRenderedExplicitly() throws Exception {
        tplLoader.putTemplate("test.ftl", "This should also output the node's name: ${content.@name}");
        final Map root = createSingleValueMap("content", new MockContent("myOtherNode"));
        assertRendereredContent("This should also output the node's name: myOtherNode", root, "test.ftl");
    }

    @Test
    public void testGivenLocaleTakesOverAnyContextLocale() throws IOException, TemplateException {
        tplLoader.putTemplate("test_en.ftl", "in english");
        tplLoader.putTemplate("test_de.ftl", "in deutscher Sprache");
        tplLoader.putTemplate("test_fr.ftl", "en francais");
        tplLoader.putTemplate("test.ftl", "fallback template - no specific language");

        assertRendereredContentWithSpecifiedLocale("en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testSimpleI18NMessageCanBeUsedInTemplates() throws Exception {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n.get('testMessage')}");
        assertRendereredContentWithSpecifiedLocale("ouais: mon message en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testSimpleI18NMessageFallsBackToEnglishIfNotSpecifiedGivenLanguage() throws Exception {
        tplLoader.putTemplate("test.ftl", "hop: ${i18n.get('testMessage')}");
        assertRendereredContentWithSpecifiedLocale("hop: my message in english", Locale.GERMAN, new HashMap(), "test.ftl");
    }

    // TODO this test can't work at the moment since we're in core and the default bundle is in the admininterface module.
    //    public void testI18NFallsBackToDefaultBundle() throws Exception {
    //        tplLoader.putTemplate("test.ftl", "ouais: ${i18n['buttons.admincentral']}");
    //        assertRendereredContentWithSpecifiedLocale("ouais: Console d'administration", Locale.FRENCH, new HashMap(), "test.ftl");
    //    }

    @Test
    public void testCanUseDotSyntaxToGetASimpleI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n.testMessage}");
        assertRendereredContentWithSpecifiedLocale("ouais: mon message en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testCanUseBracketSyntaxToGetASimpleI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n['testMessage']}");
        assertRendereredContentWithSpecifiedLocale("ouais: mon message en francais", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testMustUseMethodCallSyntaxToGetAParameterizedI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withOneParam', ['bar'])}");
        assertRendereredContentWithSpecifiedLocale("result: foo:bar", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testSupportsI18NMessagesWithMultipleParameters() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withMoreParams', ['one', 'two', 'three'])}");
        assertRendereredContentWithSpecifiedLocale("result: 1:one, 2:two, 3:three", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testOutputsInterrogationMarksAroundI18NKeyIfUnknown() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "ouais: ${i18n['bleh.blah']}");
        assertRendereredContentWithSpecifiedLocale("ouais: ???bleh.blah???", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testI18NMessageParametersCanComeFromData() throws IOException, TemplateException {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withOneParam', [value])}");
        assertRendereredContentWithSpecifiedLocale("result: foo:wesh t'as vu", Locale.FRENCH, createSingleValueMap("value", "wesh t'as vu"), "test.ftl");
    }

    @Test
    public void testCanPassBundleNameFromTemplateWithMethodCallSyntaxToGetSimple18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('testMessage', 'info.magnolia.freemarker.other')}");
        assertRendereredContentWithSpecifiedLocale("result: this is the other bundle", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testCanPassBundleNameFromTemplateWithMethodCallSyntaxToGetAParameterizedI18NMessage() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withOneParam', ['bar'], 'info.magnolia.freemarker.other')}");
        assertRendereredContentWithSpecifiedLocale("result: bling:bar", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testCanPassBundleNameFromTemplateAndSupportsI18NMessagesWithMultipleParameters() throws Exception {
        tplLoader.putTemplate("test.ftl", "result: ${i18n.get('withMoreParams', ['one', 'two', 'three'], 'info.magnolia.freemarker.other')}");
        assertRendereredContentWithSpecifiedLocale("result: bling:one, bling:two, bling:three", Locale.FRENCH, new HashMap(), "test.ftl");
    }

    @Test
    public void testCanUseSharedVariables() throws Exception {
        fmConfig.addSharedVariable("mySharedVar", "default value");
        tplLoader.putTemplate("test.ftl", "shared: ${mySharedVar} - something from the context: ${foobar}");
        final Map ctx = createSingleValueMap("foobar", "chalala");
        assertRendereredContentWithoutCheckingContext("shared: default value - something from the context: chalala", ctx, "test.ftl");
    }

    @Test
    public void testContextVariablesOverloadSharedVariables() throws Exception {
        fmConfig.addSharedVariable("mySharedVar", "default value");
        tplLoader.putTemplate("test.ftl", "shared: ${mySharedVar} - something from the context: ${foobar}");
        final Map ctx = createSingleValueMap("foobar", "chalala");
        ctx.put("mySharedVar", "overridden value");
        assertRendereredContentWithoutCheckingContext("shared: overridden value - something from the context: chalala", ctx, "test.ftl");
    }

    @Test
    public void testCanAccessStaticMethodsOfSharedVariables() throws Exception {
        // we still have to instantiate the class, but if it's registered as a sharedVariable, it has been instantiated by c2b anyhow
        fmConfig.addSharedVariable("foo", new FooBar());
        tplLoader.putTemplate("test.ftl", "Coco: ${foo.say('hello')}");
        assertRendereredContentWithoutCheckingContext("Coco: FooBar says hello", new HashMap(), "test.ftl");
    }

    @Test
    public void testEnumMembersCanBeUsedInTemplates() throws Exception {
        fmConfig.addSharedVariable("chalala", Chalala.class);
        tplLoader.putTemplate("test.ftl", "3: ${chalala.three}");
        assertRendereredContentWithoutCheckingContext("3: three", new HashMap(), "test.ftl");
    }

    @Test
    public void testEnumCanBeComparedWith() throws Exception {
        fmConfig.addSharedVariable("chalala", Chalala.class);
        fmConfig.addSharedVariable("three", Chalala.three);
        fmConfig.addSharedVariable("two", Chalala.two);
        tplLoader.putTemplate("test.ftl", "${(three == chalala.three)?string}, ${(two == chalala.three)?string}, ${(three == chalala.two)?string}, ${(three != chalala.two)?string}");
        assertRendereredContentWithoutCheckingContext("true, false, false, true", new HashMap(), "test.ftl");
    }

    @Test
    public void testEnumCanBeListed() throws Exception {
        fmConfig.addSharedVariable("chalala", Chalala.class);
        tplLoader.putTemplate("test.ftl", "" +
                "list enum by keys: [#list chalala?keys as val]${val} [/#list]\n" +
        "list enum by values: [#list chalala?values as val]${val} [/#list]");
        assertRendereredContentWithoutCheckingContext("" +
                "list enum by keys: one two three \n" +
                "list enum by values: one two three ", new HashMap(), "test.ftl");
    }

    @Test
    public void testCanAccessEnumPropertiesOfVariables() throws Exception {
        fmConfig.addSharedVariable("shared", new EnumContainer(Chalala.two));
        final Map ctx = createSingleValueMap("fromContext", new EnumContainer(Chalala.one));
        tplLoader.putTemplate("test.ftl", "shared: ${shared.chalala} - from context: ${fromContext.chalala}");
        assertRendereredContentWithoutCheckingContext("shared: two - from context: one", ctx, "test.ftl");
    }

    @Test
    public void testUseCombinationOfPadSubStringAndTrimForSafeSubstring() throws Exception {
        tplLoader.putTemplate("test.ftl", "[#assign foo='a fairly short string']\n" +
        "${foo?right_pad(50)?substring(0, 50)?trim}");
        // "${foo[0..50]}"); // this syntax is deprecated and doesn't help in this case
        // if there was a ?max built-in or function we could also do
        // "${foo?substring(0, foo?length?max(50)}"
        assertRendereredContent("a fairly short string", null, "test.ftl");
    }

    private MockHierarchyManager prepareHM(MockContent page) {
        final MockContent root = new MockContent("foo");
        final MockContent bar = new MockContent("bar");
        page.setUUID(SOME_UUID);
        root.addContent(bar);
        bar.addContent(page);
        MockHierarchyManager hm = new MockHierarchyManager();
        hm.getRoot().addContent(root);
        return hm;
    }

    public static class FooBar {
        public static String say(String s) {
            return FooBar.class.getSimpleName() + " says " + s;
        }
    }

    public enum Chalala {
        one, two, three
    }

    public static class EnumContainer {
        private final Chalala c;

        public EnumContainer(Chalala c) {
            this.c = c;
        }

        public Chalala getChalala() {
            return c;
        }
    }

}
