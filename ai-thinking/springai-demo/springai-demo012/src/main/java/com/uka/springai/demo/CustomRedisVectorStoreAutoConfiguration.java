package com.uka.springai.demo;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;

import java.util.Arrays;

@Configuration
public class CustomRedisVectorStoreAutoConfiguration {
    @Bean
    public RedisVectorStore vectorStore(EmbeddingModel embeddingModel, RedisVectorStoreProperties properties,
                                        JedisConnectionFactory jedisConnectionFactory, ObjectProvider<ObservationRegistry> observationRegistry,
                                        ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
                                        BatchingStrategy batchingStrategy) {

        JedisPooled jedisPooled = this.jedisPooled(jedisConnectionFactory);
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .initializeSchema(properties.isInitializeSchema())
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
                .batchingStrategy(batchingStrategy)
                .indexName(properties.getIndexName())
                .prefix(properties.getPrefix())
                .metadataFields(
                        RedisVectorStore.MetadataField.tag("doc_category"),
                        RedisVectorStore.MetadataField.tag("year"),
                        RedisVectorStore.MetadataField.text("filename")
                ).build();
    }



    private JedisPooled jedisPooled(JedisConnectionFactory jedisConnectionFactory) {

        String host = jedisConnectionFactory.getHostName();
        int port = jedisConnectionFactory.getPort();

        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .ssl(jedisConnectionFactory.isUseSsl())
                .clientName(jedisConnectionFactory.getClientName())
                .timeoutMillis(jedisConnectionFactory.getTimeout())
                .password(jedisConnectionFactory.getPassword())
                .build();

        return new JedisPooled(new HostAndPort(host, port), clientConfig);
    }
}
