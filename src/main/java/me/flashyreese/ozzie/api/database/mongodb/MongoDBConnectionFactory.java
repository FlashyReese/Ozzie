package me.flashyreese.ozzie.api.database.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoDBConnectionFactory {
    private final MongoClient mongoClient;

    public MongoDBConnectionFactory() {
        ClassModel<UserSchema> userSchemaClassModel = ClassModel.builder(UserSchema.class).enableDiscriminator(true).build();
        ClassModel<ServerConfigurationSchema> serverConfigurationSchemaClassModel = ClassModel.builder(ServerConfigurationSchema.class).enableDiscriminator(true).build();
        ClassModel<RoleSchema> roleSchemaClassModel = ClassModel.builder(RoleSchema.class).enableDiscriminator(true).build();
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().register(userSchemaClassModel, serverConfigurationSchemaClassModel, roleSchemaClassModel).automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString("mongodb://127.0.0.1:27017"))
                .build();
        this.mongoClient = MongoClients.create(settings);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }
}
