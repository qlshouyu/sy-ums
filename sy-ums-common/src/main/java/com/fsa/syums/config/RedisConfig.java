/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.fsa.syums.config;

import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import com.fsa.syums.utils.JSONUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.KotlinDetector;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import org.springframework.data.util.Lazy;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import reactor.util.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * @author 全栈架构师
 * @date 2023-09-24
 */
@Slf4j
@Configuration
@EnableCaching
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 设置 redis 数据默认过期时间，默认2小时
     * 设置@cacheable 序列化方式
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        MyJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer=new MyJackson2JsonRedisSerializer();
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        configuration = configuration.serializeValuesWith(RedisSerializationContext.
                SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer)).entryTtl(Duration.ofHours(2));
        return configuration;
    }

    @SuppressWarnings("all")
    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        MyJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new MyJackson2JsonRedisSerializer();


        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * 自定义缓存key生成策略，默认将使用该策略
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            Map<String, Object> container = new HashMap<>(8);
            Class<?> targetClassClass = target.getClass();
            // 类地址
            container.put("class", targetClassClass.toGenericString());
            // 方法名称
            container.put("methodName", method.getName());
            // 包名称
            container.put("package", targetClassClass.getPackage());
            // 参数列表
            for (int i = 0; i < params.length; i++) {
                container.put(String.valueOf(i), params[i]);
            }
            // 转为JSON字符串
            String jsonString = JSONUtil.objToString(container);//JSON.toJSONString(container);
            // 做SHA256 Hash计算，得到一个SHA256摘要作为Key
            return DigestUtils.sha256Hex(jsonString);
        };
    }

    @Bean
    @Override
    @SuppressWarnings({"all"})
    public CacheErrorHandler errorHandler() {
        // 异常处理，当Redis发生异常时，打印日志，但是程序正常走
        log.info("初始化 -> [{}]", "Redis CacheErrorHandler");
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheGetError：key -> [{}]", key, e);
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.error("Redis occur handleCachePutError：key -> [{}]；value -> [{}]", key, value, e);
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.error("Redis occur handleCacheEvictError：key -> [{}]", key, e);
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.error("Redis occur handleCacheClearError：", e);
            }
        };
    }
}


class MyJackson2JsonRedisSerializer implements RedisSerializer<Object>{
    static final byte[] EMPTY_ARRAY = new byte[0];
    private final ObjectMapper mapper;

    private final JacksonObjectReader reader;

    private final JacksonObjectWriter writer;

    private final Lazy<Boolean> defaultTypingEnabled;

    private final MyJackson2JsonRedisSerializer.TypeResolver typeResolver;

    /**
     * Creates {@link GenericJackson2JsonRedisSerializer} and configures {@link ObjectMapper} for default typing.
     */
    public MyJackson2JsonRedisSerializer() {
        this((String) null);
    }

    /**
     * Creates {@link GenericJackson2JsonRedisSerializer} and configures {@link ObjectMapper} for default typing using the
     * given {@literal name}. In case of an {@literal empty} or {@literal null} String the default
     * {@link JsonTypeInfo.Id#CLASS} will be used.
     *
     * @param classPropertyTypeName name of the JSON property holding type information. Can be {@literal null}.
     * @see ObjectMapper#activateDefaultTypingAsProperty(PolymorphicTypeValidator, ObjectMapper.DefaultTyping, String)
     * @see ObjectMapper#activateDefaultTyping(PolymorphicTypeValidator, ObjectMapper.DefaultTyping, JsonTypeInfo.As)
     */
    public MyJackson2JsonRedisSerializer(@org.springframework.lang.Nullable String classPropertyTypeName) {
        this(classPropertyTypeName, JacksonObjectReader.create(), JacksonObjectWriter.create());
    }

    /**
     * Creates {@link GenericJackson2JsonRedisSerializer} and configures {@link ObjectMapper} for default typing using the
     * given {@literal name}. In case of an {@literal empty} or {@literal null} String the default
     * {@link JsonTypeInfo.Id#CLASS} will be used.
     *
     * @param classPropertyTypeName name of the JSON property holding type information. Can be {@literal null}.
     * @param reader the {@link JacksonObjectReader} function to read objects using {@link ObjectMapper}.
     * @param writer the {@link JacksonObjectWriter} function to write objects using {@link ObjectMapper}.
     * @see ObjectMapper#activateDefaultTypingAsProperty(PolymorphicTypeValidator, ObjectMapper.DefaultTyping, String)
     * @see ObjectMapper#activateDefaultTyping(PolymorphicTypeValidator, ObjectMapper.DefaultTyping, JsonTypeInfo.As)
     * @since 3.0
     */
    public MyJackson2JsonRedisSerializer(@org.springframework.lang.Nullable String classPropertyTypeName, JacksonObjectReader reader,
                                         JacksonObjectWriter writer) {

        this(getMapper(), reader, writer, classPropertyTypeName);
        // simply setting {@code mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)} does not help here since we need
        // the type hint embedded for deserialization using the default typing feature.
        registerNullValueSerializer(mapper, classPropertyTypeName);

        StdTypeResolverBuilder typer = new MyJackson2JsonRedisSerializer.TypeResolverBuilder(ObjectMapper.DefaultTyping.EVERYTHING,
                mapper.getPolymorphicTypeValidator());
        typer = typer.init(JsonTypeInfo.Id.CLASS, null);
        typer = typer.inclusion(JsonTypeInfo.As.PROPERTY);

        if (StringUtils.hasText(classPropertyTypeName)) {
            typer = typer.typeProperty(classPropertyTypeName);
        }
        mapper.setDefaultTyping(typer);
    }

    private static ObjectMapper getMapper(){
        ObjectMapper mapper = new ObjectMapper();
        /**
         * 属性可见，默认是
         * Only public fields visible
         * Only public getters, is-getters visible
         * All setters (regardless of access) visible
         * Only public Creators visible
         */
        mapper.setDefaultVisibility(JsonAutoDetect.Value.defaultVisibility());
        /**
         * 如果值为null，不序列化
         */
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        /**
         * 所有非最终类型(NON_FINAL)序列化类型信息
         */
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTimeSerializer dateTimeSerializer = new LocalDateTimeSerializer(dateTimeFormatter);
        LocalDateSerializer localDateSerializer = new LocalDateSerializer(dateFormatter);
        LocalDateTimeDeserializer dateTimeDeserializer = new LocalDateTimeDeserializer(dateTimeFormatter);
        LocalDateDeserializer localDateDeserializer = new LocalDateDeserializer(dateFormatter);
        javaTimeModule.addSerializer(LocalDateTime.class, dateTimeSerializer);
        javaTimeModule.addSerializer(LocalDate.class, localDateSerializer);
        javaTimeModule.addDeserializer(LocalDateTime.class, dateTimeDeserializer);
        javaTimeModule.addDeserializer(LocalDate.class, localDateDeserializer);
        mapper.registerModule(javaTimeModule);
        return mapper;
    }

    /**
     * Setting a custom-configured {@link ObjectMapper} is one way to take further control of the JSON serialization
     * process. For example, an extended {@link SerializerFactory} can be configured that provides custom serializers for
     * specific types.
     *
     * @param mapper must not be {@literal null}.
     */
    public MyJackson2JsonRedisSerializer(ObjectMapper mapper) {
        this(mapper, JacksonObjectReader.create(), JacksonObjectWriter.create());
    }

    /**
     * Setting a custom-configured {@link ObjectMapper} is one way to take further control of the JSON serialization
     * process. For example, an extended {@link SerializerFactory} can be configured that provides custom serializers for
     * specific types.
     *
     * @param mapper must not be {@literal null}.
     * @param reader the {@link JacksonObjectReader} function to read objects using {@link ObjectMapper}.
     * @param writer the {@link JacksonObjectWriter} function to write objects using {@link ObjectMapper}.
     * @since 3.0
     */
    public MyJackson2JsonRedisSerializer(ObjectMapper mapper, JacksonObjectReader reader,
                                         JacksonObjectWriter writer) {
        this(mapper, reader, writer, null);
    }

    private MyJackson2JsonRedisSerializer(ObjectMapper mapper, JacksonObjectReader reader,
                                          JacksonObjectWriter writer, @org.springframework.lang.Nullable String typeHintPropertyName) {

        org.springframework.util.Assert.notNull(mapper, "ObjectMapper must not be null");
        org.springframework.util.Assert.notNull(reader, "Reader must not be null");
        org.springframework.util.Assert.notNull(writer, "Writer must not be null");

        this.mapper = mapper;
        this.reader = reader;
        this.writer = writer;

        this.defaultTypingEnabled = Lazy.of(() -> mapper.getSerializationConfig().getDefaultTyper(null) != null);

        Supplier<String> typeHintPropertyNameSupplier;

        if (typeHintPropertyName == null) {

            typeHintPropertyNameSupplier = Lazy.of(() -> {
                if (defaultTypingEnabled.get()) {
                    return null;
                }

                return mapper.getDeserializationConfig().getDefaultTyper(null)
                        .buildTypeDeserializer(mapper.getDeserializationConfig(),
                                mapper.getTypeFactory().constructType(Object.class), Collections.emptyList())
                        .getPropertyName();

            }).or("@class");
        } else {
            typeHintPropertyNameSupplier = () -> typeHintPropertyName;
        }

        this.typeResolver = new MyJackson2JsonRedisSerializer.TypeResolver(Lazy.of(mapper::getTypeFactory), typeHintPropertyNameSupplier);
    }

    /**
     * Register {@link MyJackson2JsonRedisSerializer.NullValueSerializer} in the given {@link ObjectMapper} with an optional
     * {@code classPropertyTypeName}. This method should be called by code that customizes
     * {@link GenericJackson2JsonRedisSerializer} by providing an external {@link ObjectMapper}.
     *
     * @param objectMapper the object mapper to customize.
     * @param classPropertyTypeName name of the type property. Defaults to {@code @class} if {@literal null}/empty.
     * @since 2.2
     */
    public static void registerNullValueSerializer(ObjectMapper objectMapper, @org.springframework.lang.Nullable String classPropertyTypeName) {

        // simply setting {@code mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)} does not help here since we need
        // the type hint embedded for deserialization using the default typing feature.
        objectMapper.registerModule(new SimpleModule().addSerializer(new MyJackson2JsonRedisSerializer.NullValueSerializer(classPropertyTypeName)));
    }

    /**
     * Gets the configured {@link ObjectMapper} used internally by this {@link MyJackson2JsonRedisSerializer}
     * to de/serialize {@link Object objects} as {@literal JSON}.
     *
     * @return the configured {@link ObjectMapper}.
     */
    protected ObjectMapper getObjectMapper() {
        return this.mapper;
    }

    @Override
    public byte[] serialize(@org.springframework.lang.Nullable Object source) throws SerializationException {

        if (source == null) {
            return EMPTY_ARRAY;
        }
        try {
            return writer.write(mapper, source);
        } catch (IOException cause) {
            String message = String.format("Could not write JSON: %s", cause.getMessage());
            throw new SerializationException(message, cause);
        }
    }

    @Override
    public Object deserialize(@org.springframework.lang.Nullable byte[] source) throws SerializationException {
        return deserialize(source, Object.class);
    }

    /**
     * @param source can be {@literal null}.
     * @param type must not be {@literal null}.
     * @return {@literal null} for empty source.
     * @throws SerializationException
     */
    @org.springframework.lang.Nullable
    @SuppressWarnings("unchecked")
    public <T> T deserialize(@org.springframework.lang.Nullable byte[] source, Class<T> type) throws SerializationException {

        org.springframework.util.Assert.notNull(type,
                "Deserialization type must not be null Please provide Object.class to make use of Jackson2 default typing.");

        if ((source == null || source.length == 0)) {
            return null;
        }

        try {
            return (T) reader.read(mapper, source, resolveType(source, type));
        } catch (Exception cause) {
            String message = String.format("Could not read JSON:%s ", cause.getMessage());
            throw new SerializationException(message, cause);
        }
    }

    /**
     * Builder method used to configure and customize the internal Jackson {@link ObjectMapper} created by
     * this {@link GenericJackson2JsonRedisSerializer} and used to de/serialize {@link Object objects}
     * as {@literal JSON}.
     *
     * @param objectMapperConfigurer {@link Consumer} used to configure and customize the internal {@link ObjectMapper};
     * must not be {@literal null}.
     * @return this {@link GenericJackson2JsonRedisSerializer}.
     * @throws IllegalArgumentException if the {@link Consumer} used to configure and customize
     * the internal {@link ObjectMapper} is {@literal null}.
     */
    public MyJackson2JsonRedisSerializer configure(Consumer<ObjectMapper> objectMapperConfigurer) {

        org.springframework.util.Assert.notNull(objectMapperConfigurer,
                "Consumer used to configure and customize ObjectMapper must not be null");

        objectMapperConfigurer.accept(getObjectMapper());
        return this;
    }

    protected JavaType resolveType(byte[] source, Class<?> type) throws IOException {

        if (!type.equals(Object.class) || !defaultTypingEnabled.get()) {
            return typeResolver.constructType(type);
        }

        return typeResolver.resolveType(source, type);
    }

    /**
     * @since 3.0
     */
    static class TypeResolver {

        // need a separate instance to bypass class hint checks
        private final ObjectMapper mapper = new ObjectMapper();

        private final Supplier<TypeFactory> typeFactory;
        private final Supplier<String> hintName;

        TypeResolver(Supplier<TypeFactory> typeFactory, Supplier<String> hintName) {

            this.typeFactory = typeFactory;
            this.hintName = hintName;
        }

        protected JavaType constructType(Class<?> type) {
            return typeFactory.get().constructType(type);
        }

        protected JavaType resolveType(byte[] source, Class<?> type) throws IOException {

            JsonNode root = mapper.readTree(source);
            JsonNode jsonNode = root.get(hintName.get());

            if (jsonNode instanceof TextNode && jsonNode.asText() != null) {
                return typeFactory.get().constructFromCanonical(jsonNode.asText());
            }

            return constructType(type);
        }
    }

    /**
     * {@link StdSerializer} adding class information required by default typing. This allows de-/serialization of
     * {@link NullValue}.
     *
     * @author Christoph Strobl
     * @since 1.8
     */
    private static class NullValueSerializer extends StdSerializer<NullValue> {

        private static final long serialVersionUID = 1999052150548658808L;
        private final String classIdentifier;

        /**
         * @param classIdentifier can be {@literal null} and will be defaulted to {@code @class}.
         */
        NullValueSerializer(@org.springframework.lang.Nullable String classIdentifier) {

            super(NullValue.class);
            this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
        }

        @Override
        public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

            jgen.writeStartObject();
            jgen.writeStringField(classIdentifier, NullValue.class.getName());
            jgen.writeEndObject();
        }

        @Override
        public void serializeWithType(NullValue value, JsonGenerator gen, SerializerProvider serializers,
                                      TypeSerializer typeSer) throws IOException {
            serialize(value, gen, serializers);
        }
    }

    /**
     * Custom {@link StdTypeResolverBuilder} that considers typing for non-primitive types. Primitives, their wrappers and
     * primitive arrays do not require type hints. The default {@code DefaultTyping#EVERYTHING} typing does not satisfy
     * those requirements.
     *
     * @author Mark Paluch
     * @since 2.7.2
     */
    private static class TypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {

        public TypeResolverBuilder(ObjectMapper.DefaultTyping t, PolymorphicTypeValidator ptv) {
            super(t, ptv);
        }

        @Override
        public ObjectMapper.DefaultTypeResolverBuilder withDefaultImpl(Class<?> defaultImpl) {
            return this;
        }

        /**
         * Method called to check if the default type handler should be used for given type. Note: "natural types" (String,
         * Boolean, Integer, Double) will never use typing; that is both due to them being concrete and final, and since
         * actual serializers and deserializers will also ignore any attempts to enforce typing.
         */
        public boolean useForType(JavaType t) {

            if (t.isJavaLangObject()) {
                return true;
            }

            t = resolveArrayOrWrapper(t);

            if (t.isEnumType() || ClassUtils.isPrimitiveOrWrapper(t.getRawClass())) {
                return false;
            }

            if (t.isFinal() && !KotlinDetector.isKotlinType(t.getRawClass())
                    && t.getRawClass().getPackageName().startsWith("java")) {
                return false;
            }

            // [databind#88] Should not apply to JSON tree models:
            return !TreeNode.class.isAssignableFrom(t.getRawClass());
        }

        private JavaType resolveArrayOrWrapper(JavaType type) {

            while (type.isArrayType()) {
                type = type.getContentType();
                if (type.isReferenceType()) {
                    type = resolveArrayOrWrapper(type);
                }
            }

            while (type.isReferenceType()) {
                type = type.getReferencedType();
                if (type.isArrayType()) {
                    type = resolveArrayOrWrapper(type);
                }
            }

            return type;
        }
    }
}
/**
 * 重写序列化器
 *
 * @author 全栈架构师
 */
class StringRedisSerializer implements RedisSerializer<Object> {

    private final Charset charset;

    StringRedisSerializer() {
        this(StandardCharsets.UTF_8);
    }

    private StringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) {
        return (bytes == null ? null : new String(bytes, charset));
    }

    @Override
    public @Nullable byte[] serialize(Object object) {
        String string = JSONUtil.objToString(object);//JSON.toJSONString(object);

        if (org.apache.commons.lang3.StringUtils.isBlank(string)) {
            return null;
        }
        string = string.replace("\"", "");
        return string.getBytes(charset);
    }


}
