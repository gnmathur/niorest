package com.gmathur.niorest.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * https://datatracker.ietf.org/doc/html/rfc2616#section-2.1 define a construct called #rule.
 */
public final class HashRuleTokenizer {
    private HashRuleTokenizer() {}

    public static List<String> tokenize(final String httpLine) {
        List<String> tokens = Arrays.stream(httpLine.split(","))
                .map(String::stripTrailing)
                .collect(Collectors.toList());
        return tokens;
    }
}
