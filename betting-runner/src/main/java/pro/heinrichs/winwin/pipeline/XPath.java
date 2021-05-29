package pro.heinrichs.winwin.pipeline;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class XPath implements Step {
    private static final DocumentBuilderFactory XML = DocumentBuilderFactory.newInstance();
    private static final XPathFactory XPATH = XPathFactory.newInstance();
    private static final String EXPRESSIONS = "expressions";
    private static final String INPUT = "input";

    @Override
    public void execute(final Config config, final Variables variables) throws IOException {
        final var input = Verify.isString("Input", config.get(INPUT));
        final var expressions = Verify.isCollection("Expressions", config.get(EXPRESSIONS));
        for (final var expression : expressions) {
            final var expr = Verify.isMap("Expression", expression);
            final var exp = Verify.isString("Expression statement", expr.get("statement"));
            final var vars = new ArrayList<>(Verify.isCollection("Deconstruction Expression", expr.get("store")));
            final var values = retrieveContents(
                exp,
                Verify.isString("Input Variable(" + input + ")", variables.get(input)),
                vars.size()
            );
            for (var i = 0; i < values.size(); i += 1) {
                final var var = Verify.isString("Variable Name", vars.get(i));
                final var value = values.get(i);
                variables.set(var, value);
            }
        }
    }

    private List<String> retrieveContents(final String expr, final String body, final long size) throws IOException {
        if (size < 1) {
            return List.of();
        }
        try {
            final Document xmldoc = XML
                .newDocumentBuilder()
                // TODO: This is not nice
                .parse(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            final var Z = XPATH.newXPath()
                .evaluateExpression(expr, xmldoc);
            switch (Z.type()) {
                default:
                case ANY:
                case BOOLEAN:
                case NODE:
                case NUMBER:
                case STRING:
                    throw new IOException("XPath not yet supported");
                case NODESET:
                    final var nodes = (XPathNodes) Z.value();
                    return StreamSupport.stream(nodes.spliterator(), false)
                        .limit(size)
                        .map(Node::getTextContent)
                        .collect(Collectors.toList());
            }
        } catch (final ParserConfigurationException | XPathExpressionException | SAXException ex) {
            throw new IOException("Error while parsing XPath", ex);
        }
    }
}
