package me.flashyreese.ozzie.api.database;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.database.mongodb.MongoDBConnectionFactory;
import me.flashyreese.ozzie.api.database.mongodb.SubscribeHelpers;
import me.flashyreese.ozzie.api.database.mongodb.schema.RoleSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.ServerConfigurationSchema;
import me.flashyreese.ozzie.api.database.mongodb.schema.UserSchema;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;

public class DatabaseHandler {

    private final MongoDBConnectionFactory mongoDBConnectionFactory;

    private final String DATABASE = "ozzie";
    private final String USER_COLLECTION = "users";
    private final String SERVER_SETTINGS_COLLECTION = "servers_setting";
    private final String ROLE_COLLECTION = "roles";

    private MongoDatabase mongoDatabase;
    private MongoCollection<UserSchema> userCollection;
    private MongoCollection<ServerConfigurationSchema> serverSettingsCollection;
    private MongoCollection<RoleSchema> roleCollection;

    public DatabaseHandler() {
        this.mongoDBConnectionFactory = new MongoDBConnectionFactory();
        this.mongoDatabase = this.getMongoDBConnectionFactory().getMongoClient().getDatabase(this.DATABASE);
        this.userCollection = this.mongoDatabase.getCollection(this.USER_COLLECTION, UserSchema.class);
        this.serverSettingsCollection = this.mongoDatabase.getCollection(this.SERVER_SETTINGS_COLLECTION, ServerConfigurationSchema.class);
        this.roleCollection = this.mongoDatabase.getCollection(this.ROLE_COLLECTION, RoleSchema.class);
    }

    public void createUser(long userIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<InsertOneResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.userCollection.insertOne(new UserSchema(userIdentifier)).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("User \"{}\" inserted successfully!", userIdentifier);
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while inserting user \"{}\"", userIdentifier);
        }
    }

    public UserSchema retrieveUser(long userIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<UserSchema> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.userCollection.find(eq("userIdentifier", userIdentifier)).first().subscribe(subscriber);
        subscriber.await();
        if (subscriber.result == null) {
            this.createUser(userIdentifier);
            return this.retrieveUser(userIdentifier);
        }
        return subscriber.result;
    }

    public void updateUser(UserSchema userSchema) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<UpdateResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.userCollection.updateOne(
                eq("userIdentifier", userSchema.getUserIdentifier()),
                combine(
                        set("locale", userSchema.getLocale()),
                        set("commandPrefix", userSchema.getCommandPrefix()),
                        set("serverPermissionMap", userSchema.getServerPermissionMap()),
                        currentDate("lastModified"))).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("User \"{}\" updated successfully!", userSchema.getUserIdentifier());
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while updating user \"{}\"", userSchema.getUserIdentifier());
        }
    }

    public void deleteUser(long userIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<DeleteResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.userCollection.deleteOne(eq("userIdentifier", userIdentifier)).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("User \"{}\" updated successfully!", userIdentifier);
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while updating user \"{}\"", userIdentifier);
        }
    }

    public void createServerConfiguration(long serverIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<InsertOneResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.serverSettingsCollection.insertOne(new ServerConfigurationSchema(serverIdentifier)).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("Server configuration \"{}\" inserted successfully!", serverIdentifier);
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while inserting server configuration \"{}\"", serverIdentifier);
        }
    }

    public ServerConfigurationSchema retrieveServerConfiguration(long serverIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<ServerConfigurationSchema> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.serverSettingsCollection.find(eq("serverIdentifier", serverIdentifier)).first().subscribe(subscriber);
        subscriber.await();
        if (subscriber.result == null) {
            this.createServerConfiguration(serverIdentifier);
            return this.retrieveServerConfiguration(serverIdentifier);
        }
        return subscriber.result;
    }

    public void updateServerConfiguration(ServerConfigurationSchema serverConfigurationSchema) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<UpdateResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.serverSettingsCollection.updateOne(
                eq("serverIdentifier", serverConfigurationSchema.getServerIdentifier()),
                combine(
                        set("owners", serverConfigurationSchema.getOwners()),
                        set("commandPrefix", serverConfigurationSchema.getCommandPrefix()),
                        set("locale", serverConfigurationSchema.getLocale()),
                        set("allowUserLocale", serverConfigurationSchema.isAllowUserLocale()),
                        set("allowUserCommandPrefix", serverConfigurationSchema.isAllowUserCommandPrefix()),
                        set("allowedCommandTextChannel", serverConfigurationSchema.getAllowedCommandTextChannel()),
                        currentDate("lastModified"))).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("Server configuration \"{}\" updated successfully!", serverConfigurationSchema.getServerIdentifier());
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while updating server configuration \"{}\"", serverConfigurationSchema.getServerIdentifier());
        }
    }

    public void deleteServerConfiguration(long serverIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<DeleteResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.serverSettingsCollection.deleteOne(eq("serverIdentifier", serverIdentifier)).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("Server Configuration \"{}\" updated successfully!", serverIdentifier);
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while updating server configuration \"{}\"", serverIdentifier);
        }
    }

    public void createRole(long roleIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<InsertOneResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.roleCollection.insertOne(new RoleSchema(roleIdentifier)).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("Role \"{}\" inserted successfully!", roleIdentifier);
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while inserting role \"{}\"", roleIdentifier);
        }
    }

    public RoleSchema retrieveRole(long roleIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<RoleSchema> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.roleCollection.find(eq("roleIdentifier", roleIdentifier)).first().subscribe(subscriber);
        subscriber.await();
        if (subscriber.result == null) {
            this.createRole(roleIdentifier);
            return this.retrieveRole(roleIdentifier);
        }
        return subscriber.result;
    }

    public void updateRole(RoleSchema roleSchema) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<UpdateResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.roleCollection.updateOne(
                eq("roleIdentifier", roleSchema.getRoleIdentifier()),
                combine(
                        set("permissions", roleSchema.getPermissions()),
                        currentDate("lastModified"))).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("Role \"{}\" updated successfully!", roleSchema.getRoleIdentifier());
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while updating role \"{}\"", roleSchema.getRoleIdentifier());
        }
    }

    public void deleteRole(long roleIdentifier) throws Throwable {
        SubscribeHelpers.DocumentSubscriber<DeleteResult> subscriber = new SubscribeHelpers.DocumentSubscriber<>();
        this.roleCollection.deleteOne(eq("roleIdentifier", roleIdentifier)).subscribe(subscriber);
        subscriber.await();
        if (subscriber.result.wasAcknowledged()) {
            OzzieApi.INSTANCE.getLogger().info("Role \"{}\" updated successfully!", roleIdentifier);
        } else {
            OzzieApi.INSTANCE.getLogger().error("Exception occurred while updating role \"{}\"", roleIdentifier);
        }
    }

    public MongoDBConnectionFactory getMongoDBConnectionFactory() {
        return mongoDBConnectionFactory;
    }
}
