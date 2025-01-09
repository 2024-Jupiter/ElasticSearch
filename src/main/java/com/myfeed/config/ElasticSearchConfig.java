package com.myfeed.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;

@Configuration
@EnableElasticsearchAuditing
public class ElasticSearchConfig {
    @Value("${spring.elasticsearch.rest.url}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.rest.username}")
    private String username;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    @Bean
    public ElasticsearchClient getElasticsearchClient() {

        RestClientBuilder builder = RestClient.builder(HttpHost.create(elasticsearchUrl));

        builder.setHttpClientConfigCallback(httpAsyncClientBuilder -> {
            // 사용자 이름과 비밀번호를 설정
            final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });

        // objectMapper에 JavaTimeModule을 추가
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule());
//                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // RestClient 및 ElasticsearchClient 생성
        RestClient restClient = builder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
        return new ElasticsearchClient(transport);
    }
}

