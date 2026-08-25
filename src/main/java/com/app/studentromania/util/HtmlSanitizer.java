package com.app.studentromania.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

/**
 * Strips any HTML/script markup from free-text fields before they're persisted.
 * The client already escapes review/question/answer text on the way out (so it's
 * safe today), but that only holds as long as every render path remembers to call
 * escapeHtml() - one missed call turns any stored payload into a live stored-XSS
 * hit for every visitor. Stripping tags at write time removes that single point of
 * failure: these fields are plain text by product design, so there's no markup to
 * legitimately preserve.
 *
 * Jsoup.clean() alone returns HTML-safe output (entities like "&" re-encoded to
 * "&amp;"), which would double-encode once the client's own escapeHtml() runs on
 * top of it - so the encoded entities are decoded back to plain text afterwards.
 * prettyPrint(false) keeps Jsoup from collapsing/reformatting whitespace, since
 * this is plain user text (line breaks, spacing), not markup to be reformatted.
 */
public class HtmlSanitizer {

    private static final Document.OutputSettings PLAIN_TEXT_OUTPUT = new Document.OutputSettings().prettyPrint(false);

    public static String stripHtml(String input) {
        if (input == null) {
            return null;
        }
        String withoutTags = Jsoup.clean(input, "", Safelist.none(), PLAIN_TEXT_OUTPUT);
        return Parser.unescapeEntities(withoutTags, false);
    }

}
