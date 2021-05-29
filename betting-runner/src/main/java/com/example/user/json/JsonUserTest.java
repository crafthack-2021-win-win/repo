package com.example.user.json;

import com.jayway.jsonpath.JsonPath;
import pro.heinrichs.winwin.pipeline.Config;
import pro.heinrichs.winwin.pipeline.Step;
import pro.heinrichs.winwin.pipeline.Variables;
import pro.heinrichs.winwin.pipeline.Verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonUserTest implements Step {
    private static final String EXPRESSIONS = "expressions";
    private static final String INPUT = "input";

    @Override
    // This is based on XPAth, quick an dirty copy paste
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
        final var m = JsonPath.read(body, expr);
        if (m instanceof Collection) {
            // TODO: CHECK IF STRING
            return new ArrayList<>((Collection<String>) m);
        }
        return List.of(String.valueOf(m));
    }
}
