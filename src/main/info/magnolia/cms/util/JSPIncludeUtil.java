package info.magnolia.cms.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.taglibs.standard.resources.Resources;


/**
 * The inner class ImportResponseWrapper is copied from the JSTL include tag
 * @author philipp
 */
public class JSPIncludeUtil {

    public static final String DEFAULT_ENCODING = "UTF-8";

    /** Wraps responses to allow us to retrieve results as Strings. */
    private static class ImportResponseWrapper extends HttpServletResponseWrapper {

        // ************************************************************
        // Overview

        /*
         * We provide either a Writer or an OutputStream as requested. We actually have a true Writer and an
         * OutputStream backing both, since we don't want to use a character encoding both ways (Writer -> OutputStream ->
         * Writer). So we use no encoding at all (as none is relevant) when the target resource uses a Writer. And we
         * decode the OutputStream's bytes using OUR tag's 'charEncoding' attribute, or ISO-8859-1 as the default. We
         * thus ignore setLocale() and setContentType() in this wrapper. In other words, the target's asserted encoding
         * is used to convert from a Writer to an OutputStream, which is typically the medium through with the target
         * will communicate its ultimate response. Since we short-circuit that mechanism and read the target's
         * characters directly if they're offered as such, we simply ignore the target's encoding assertion.
         */

        // ************************************************************
        // Data
        /** The Writer we convey. */
        private StringWriter sw = new StringWriter();

        /** A buffer, alternatively, to accumulate bytes. */
        private ByteArrayOutputStream bos = new ByteArrayOutputStream();

        /** A ServletOutputStream we convey, tied to this Writer. */
        private ServletOutputStream sos = new ServletOutputStream() {

            public void write(int b) throws IOException {
                bos.write(b);
            }
        };

        /** 'True' if getWriter() was called; false otherwise. */
        private boolean isWriterUsed;

        /** 'True if getOutputStream() was called; false otherwise. */
        private boolean isStreamUsed;

        /** The HTTP status set by the target. */
        private int status = 200;

        private String charEncoding;

        // ************************************************************
        // Constructor and methods

        /** Constructs a new ImportResponseWrapper. */
        public ImportResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        /** Returns a Writer designed to buffer the output. */
        public PrintWriter getWriter() {
            if (isStreamUsed)
                throw new IllegalStateException(Resources.getMessage("IMPORT_ILLEGAL_STREAM"));
            isWriterUsed = true;
            return new PrintWriter(sw);
        }

        /** Returns a ServletOutputStream designed to buffer the output. */
        public ServletOutputStream getOutputStream() {
            if (isWriterUsed)
                throw new IllegalStateException(Resources.getMessage("IMPORT_ILLEGAL_WRITER"));
            isStreamUsed = true;
            return sos;
        }

        /** Has no effect. */
        public void setContentType(String x) {
            // ignore
        }

        /** Has no effect. */
        public void setLocale(Locale x) {
            // ignore
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        /**
         * Retrieves the buffered output, using the containing tag's 'charEncoding' attribute, or the tag's default
         * encoding, <b>if necessary</b>.
         */
        // not simply toString() because we need to throw
        // UnsupportedEncodingException
        public String getString() throws UnsupportedEncodingException {
            if (isWriterUsed)
                return sw.toString();
            else if (isStreamUsed) {
                if (charEncoding != null && !charEncoding.equals(""))
                    return bos.toString(charEncoding);
                else
                    return bos.toString(DEFAULT_ENCODING);
            }
            else
                return ""; // target didn't write anything
        }
    }

    public static String get(String jsp, HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ImportResponseWrapper wrappedResponse = new ImportResponseWrapper(response);
        request.getRequestDispatcher(jsp).include(request, wrappedResponse);
        return wrappedResponse.getString();
    }
}
