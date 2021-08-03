package com.gmathur.niorest.http;

public enum HttpResponseParseState {
    START,
    /**
     * Ready to start parsing the received bytes and start with parsing and interpreting the status line
     * A CR detected in STATUS_LINE_READY transitions the state machine to STATUS_LINE_DONE
     */
    STATUS_LINE_READY,
    /**
     * Detect LF and parse the read status line.
     * Move the state to PARSE_HEADER_READY on receiving LF
     */
    STATUS_LINE_DONE,
    /**
     * Read a header line till a CR is detected. Transition to PARSE_HEADER_DONE when CR is detected
     */
    PARSE_HEADER_READY,
    /**
     * Detect LF and parse the header
     */
    PARSE_HEADER_DONE,
    CR_RCVD,
    LF_RCVD,
    PARSE_MSG_BODY_READY,
    PARSE_CHUNK_LENGTH_READY,
    PARSE_CHUNK_LENGTH_DONE,
    PARSE_CHUNK_READY,
    PARSE_CHUNK_DONE,
    PARSE_CONTENT,
    BUILD_MSG_BODY_READY,
    BUILD_MSG_BODY_DONE,
    ERROR,
    DONE
}
