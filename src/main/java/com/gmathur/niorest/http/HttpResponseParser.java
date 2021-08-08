/*
 * Copyright (c) 2021 Gaurav Mathur
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmathur.niorest.http;

import com.gmathur.niorest.http.HttpResponseHeader.HttpTransferEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static com.gmathur.niorest.http.HttpResponse.*;
import static com.gmathur.niorest.http.HttpResponseHeader.HttpHeaderFields.*;
import static com.gmathur.niorest.http.HttpResponseParseState.*;

/**
 * Note that this class uses Optional fields. Since this is class does not need to be serializable, there is not issue
 * using Optional field.
 */
public class HttpResponseParser {
    private static class SVars {
        public Optional<List<HttpTransferEncoding>> transferEncoding = Optional.empty();
        public Optional<Integer> contentLength = Optional.empty();
        public Integer chunkLength = 0;
        public Integer chunkReadyBytes = 0;
        public Integer contentLengthReadBytes = 0;
        public HttpResponseParseState nextState = STATUS_LINE_READY;
        public HttpResponseParseState previousState = STATUS_LINE_READY;
        public ByteArrayOutputStream messageBody = new ByteArrayOutputStream();

        public void clear() {
            transferEncoding = Optional.empty();
            contentLength = Optional.empty();
            chunkLength = 0;
            chunkReadyBytes = 0;
            contentLengthReadBytes = 0;
            contentLength = Optional.empty();
            messageBody.reset();
            nextState = STATUS_LINE_READY;
            previousState = STATUS_LINE_READY;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(HttpResponseParser.class.getCanonicalName());
    private static final Map<String, Function<String, ?>> headerMap = Map.of(
            TRANSFER_ENCODING.getFieldNameStr(), new HttpResponseHeader.TransferEncodingParser(),
            CONTENT_LENGTH.getFieldNameStr(), new HttpResponseHeader.ContentLengthParser()
    );
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final SVars sVars = new SVars();

    private HttpResponseBuilder httpResponseBuilder = newBuilder();

    /**
     * Clear state machine and make it ready to run again from the start
     */
    public void clear() {
        sVars.clear();
        buffer.reset();
        httpResponseBuilder = newBuilder();
    }

    public HttpResponse toHttpResponse() {
        return httpResponseBuilder.build();
    }

    private HttpResponseParseState stateMachine(byte input) {
        switch (sVars.nextState) {
            case STATUS_LINE_READY: {
                if (input != 0x0D) { buffer.write(input); }
                else { sVars.nextState = STATUS_LINE_DONE; }
            }
            break;

            case STATUS_LINE_DONE: {
                if (input != 0x0A) {
                    throw new IllegalStateException("Invalid HTTP status line");
                }
                final String statusLine = buffer.toString(StandardCharsets.US_ASCII);
                buffer.reset();
                HttpStatusLine sl = HttpStatusLine.parseStatusLine(statusLine);
                httpResponseBuilder.statusLine(sl);
                logger.debug("{}", sl);
                sVars.nextState = PARSE_HEADER_READY;
            }
            break;

            case PARSE_HEADER_READY: {
                if (input != 0x0D) { buffer.write(input); }
                else if (input == 0x0D && sVars.previousState == PARSE_HEADER_READY ) {
                    sVars.nextState = PARSE_HEADER_DONE;
                } else if (input == 0x0D && sVars.previousState == PARSE_HEADER_DONE ){
                    sVars.nextState = PARSE_MSG_BODY_READY;
                } else {
                    throw new IllegalStateException("Illegal state while parsing HTTP response");
                }
                sVars.previousState = PARSE_HEADER_READY;
            }
            break;

            case PARSE_HEADER_DONE: {
                if (input != 0x0A) {
                    throw new IllegalStateException("Invalid HTTP status line");
                }
                final String header = buffer.toString(StandardCharsets.US_ASCII);
                if (header.startsWith(TRANSFER_ENCODING.getFieldNameStr())) {
                    List<HttpTransferEncoding> transferEncoding = (List<HttpTransferEncoding>) headerMap.get(TRANSFER_ENCODING.getFieldNameStr()).apply(header);
                    httpResponseBuilder.transferEncoding(transferEncoding);
                    sVars.transferEncoding = Optional.of(transferEncoding);
                }
                else if (header.startsWith(DATE.getFieldNameStr())) {
                    System.out.println("DATE");
                }
                else if (header.startsWith(CONTENT_LENGTH.getFieldNameStr())) {
                    Integer cl = (Integer) headerMap.get(CONTENT_LENGTH.getFieldNameStr()).apply(header);
                    sVars.contentLength = Optional.of(cl);
                }
                else if (header.startsWith(CONTENT_TYPE.getFieldNameStr())) {
                    System.out.println("CT");
                }
                sVars.nextState = PARSE_HEADER_READY;
                sVars.previousState = PARSE_HEADER_DONE;
                buffer.reset();
            }
            break;

            case PARSE_MSG_BODY_READY: {
                boolean useTransferEncoding = false;
                if (input != 0x0A) {
                    throw new IllegalStateException("Invalid HTTP body");
                }
                if (sVars.transferEncoding.isEmpty() && sVars.contentLength.isEmpty()) {
                    throw new IllegalStateException("Error parsing HTTP response. Neither transfer encoding nor content-length present");
                }
                if (sVars.transferEncoding.isPresent()) { useTransferEncoding = true; }
                if (sVars.contentLength.isPresent()) { useTransferEncoding = false; }

                if (sVars.transferEncoding.isPresent() &&
                        sVars.contentLength.isPresent() &&
                        sVars.transferEncoding.get().contains(HttpTransferEncoding.IDENTITY) &&
                        sVars.transferEncoding.get().size() == 1) {
                    // RFC 2616 Section-4.4
                    logger.warn("Messages MUST NOT include both a Content-Length header field and a non-identity transfer-coding.");
                    useTransferEncoding = true;
                }
                if (useTransferEncoding) {
                    sVars.nextState = PARSE_CHUNK_LENGTH_READY;
                } else {
                    sVars.nextState = PARSE_CONTENT;
                }
                sVars.previousState = PARSE_MSG_BODY_READY;
            } break;

            case PARSE_CONTENT: {
                sVars.previousState = PARSE_CONTENT;
                buffer.write(input);
                sVars.contentLengthReadBytes++;
                if (sVars.contentLengthReadBytes.compareTo(sVars.contentLength.get()) == 0) {
                    sVars.nextState = BUILD_MSG_BODY_DONE;
                    try {
                        sVars.messageBody.write(buffer.toByteArray());
                    } catch (IOException e) {
                        logger.error("Error parsing message body from Content-Length");
                        sVars.messageBody.reset();
                    }
                    buffer.reset();
                    stateMachine((byte)0x0A); // Todo state machines should perhaps not take input and use an input getter?
                }
            } break;

            case PARSE_CHUNK_LENGTH_READY: {
                if (input != 0x0D) { buffer.write(input); }
                else {
                    sVars.nextState = PARSE_CHUNK_LENGTH_DONE;
                }
                sVars.previousState = PARSE_CHUNK_LENGTH_READY;
            }
            break;

            case PARSE_CHUNK_LENGTH_DONE: {
                if (input != 0x0A) { throw new IllegalStateException("Illegal chunk bytes"); }
                sVars.chunkLength = Integer.parseInt(buffer.toString(), 16);
                buffer.reset();
                if (sVars.chunkLength == 0) {
                    sVars.nextState = BUILD_MSG_BODY_READY;
                }
                else {
                    sVars.nextState = PARSE_CHUNK_READY;
                }
                sVars.previousState = PARSE_CHUNK_LENGTH_DONE;
            }
            break;

            case PARSE_CHUNK_READY: {
                if (input == 0x0D && sVars.chunkReadyBytes.equals(sVars.chunkLength)) {
                    sVars.nextState = PARSE_CHUNK_DONE;
                } else if (input == 0x0D && !sVars.chunkReadyBytes.equals(sVars.chunkLength)) {
                    throw new IllegalStateException("Illegal chunk");
                } else {
                    // continue reading chunk
                    buffer.write(input);
                    sVars.chunkReadyBytes++;
                }
                sVars.previousState = PARSE_CHUNK_READY;
            } break;

            case PARSE_CHUNK_DONE: {
                logger.trace("Enter PARSE_CHUNK_DONE");
                if (input != 0x0A) { throw new IllegalStateException("Illegal end of chunk"); }
                try {
                    sVars.messageBody.write(buffer.toByteArray());
                    buffer.reset();
                    sVars.nextState = PARSE_CHUNK_LENGTH_READY;
                    sVars.previousState = PARSE_CHUNK_DONE;
                } catch (IOException e) {
                    logger.error("I/O exception building HTTP response message body");
                    sVars.nextState = DONE;
                }
            } break;

            case BUILD_MSG_BODY_READY: {
                logger.trace("Enter BUILD_MSG_BODY_READY");
                if (input != 0x0D) { throw new IllegalThreadStateException("Illegal end of chunked body"); }
                sVars.nextState = BUILD_MSG_BODY_DONE;
                sVars.previousState = BUILD_MSG_BODY_READY;
            } break;

            case ERROR: {
                logger.trace("Enter DONE");
                clear();
                sVars.nextState = BUILD_MSG_BODY_DONE;
            } break;

            case BUILD_MSG_BODY_DONE: {
                logger.trace("Enter BUILD_MSG_BODY_DONE");
                if (input != 0x0A) { throw new IllegalStateException("Illegal parse content end"); }
                httpResponseBuilder.messageBody(sVars.messageBody.toByteArray());
                sVars.nextState = DONE;
            } break;

            case DONE: {
                logger.error("DONE called again. Ignoring input to parser");
            }
        }

        return sVars.nextState;
    }

    public HttpResponseParseState runForInput(final byte[] input)  {
        HttpResponseParseState currentState = DONE;
        for (int i = 0; i < input.length; i++) {
            currentState = stateMachine(input[i]);
        }
        return currentState;
    }

    public HttpResponseParseState runForInput(final byte input)  {
        return stateMachine(input);
    }
}
