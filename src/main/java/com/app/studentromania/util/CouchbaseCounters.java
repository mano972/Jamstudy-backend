package com.app.studentromania.util;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.subdoc.DocumentFragment;

/**
 * Atomic numeric-field updates via a Couchbase sub-document COUNTER op.
 *
 * <p>Upvote/report counts used to be a read-modify-write: load the whole document,
 * {@code ++count}, save the whole document. Two of those racing lose an increment,
 * and a concurrent write to any other field of the same doc (e.g. a new answer
 * appended to a question) clobbers it. A sub-document counter increments just the
 * one field server-side, atomically, without reading or rewriting the rest of the
 * document.
 */
public final class CouchbaseCounters {

    private CouchbaseCounters() {
    }

    /**
     * Adds {@code delta} to the numeric field at {@code path} inside document
     * {@code docId} and returns the new value. Clamps at 0 so a downvote against an
     * already-zero counter can't push it negative.
     */
    public static long adjust(Bucket bucket, String docId, String path, long delta) {
        DocumentFragment<?> result = bucket.mutateIn(docId).counter(path, delta).execute();
        // COUNTER returns the new value; read it as a Number so an Integer-vs-Long
        // decode difference can't blow up.
        long newValue = ((Number) result.content(path)).longValue();
        if (newValue < 0) {
            bucket.mutateIn(docId).upsert(path, 0).execute();
            return 0;
        }
        return newValue;
    }

}
