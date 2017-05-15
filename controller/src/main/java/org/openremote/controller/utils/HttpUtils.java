/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2015, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.utils;

import org.apache.commons.codec.binary.Base64;
import org.openremote.controller.Constants;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class HttpUtils {

    /**
     * Generates the appropriate string to use as HTTP Authorization header for basic authentication.
     *
     * @param username User name for authentication
     * @param password Credentials for authentication
     * @return Header for HTTP Authorization
     */
    public static String generateHttpBasicAuthorizationHeader(String username, String password) {
        return Constants.HTTP_BASIC_AUTHORIZATION + new String(Base64.encodeBase64((username + ":" + password).getBytes()));
    }

    /**
     * Disable SSL certificate checking for ALL {@link javax.net.ssl.HttpsURLConnection}s.
     */
    public static void trustAllCertificates() throws Exception {
        final SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, new TrustManager[]{new TrustAllX509TrustManager()}, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * Disable SSL hostname verification of server certificates for ALL {@link javax.net.ssl.HttpsURLConnection}s.
     */
    public static void disableHostnameVerification() {
        HttpsURLConnection.setDefaultHostnameVerifier(new IgnorantHostNameVerifier());
    }

    private static class TrustAllX509TrustManager implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            // empty
        }

        @Override
        public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            // empty
        }
    }

    private static class IgnorantHostNameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(final String string, final SSLSession ssls) {
            return true;
        }
    }
}
