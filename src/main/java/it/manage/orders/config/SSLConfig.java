package it.manage.orders.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
@ConditionalOnProperty(prefix = "appconfig", name = "development", havingValue = "true")
public class SSLConfig {

  @Bean
  public Boolean disableSSLValidation() throws NoSuchAlgorithmException, KeyManagementException {
    final var sslContext = SSLContext.getInstance("TLS");

    sslContext.init(
        null,
        new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
              // No check implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
              // No check implemented
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }
          }
        },
        null);

    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

    return true;
  }
}
