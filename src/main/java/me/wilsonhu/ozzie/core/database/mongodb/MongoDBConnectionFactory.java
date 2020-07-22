/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.wilsonhu.ozzie.core.database.mongodb;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.schemas.ServerSchema;
import me.wilsonhu.ozzie.schemas.ServerUserPermissionSchema;
import me.wilsonhu.ozzie.schemas.UserSchema;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBConnectionFactory {
    private static Logger log = LogManager.getLogger(MongoDBConnectionFactory.class);

    private final String DATABASE = "ozzie";
    private final String USER_COLLECTION = "users";
    private final String SERVER_COLLECTION = "servers";
    private final String SERVER_USER_PERMISSIONS_COLLECTION = "server_user_permissions";

    private MongoClient mongoClient;
    private Ozzie ozzie;

    public MongoDBConnectionFactory(Ozzie ozzie){//Todo: Build this better with graphql and proper "non-query"
        log.info("Building MongoDB Handler...");
        setOzzie(ozzie);
        ClassModel<UserSchema> userSchemaClassModelClassModel = ClassModel.builder(UserSchema.class).enableDiscriminator(true).build();
        ClassModel<ServerSchema> serverSchemaClassModelClassModel = ClassModel.builder(ServerSchema.class).enableDiscriminator(true).build();
        ClassModel<ServerUserPermissionSchema> serverUserPermissionSchemaClassModel = ClassModel.builder(ServerUserPermissionSchema.class).enableDiscriminator(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().register(userSchemaClassModelClassModel, serverSchemaClassModelClassModel, serverUserPermissionSchemaClassModel).automatic(true).build()));
        MongoClientSettings settings = MongoClientSettings.builder()
                .codecRegistry(pojoCodecRegistry)
                .applyConnectionString(new ConnectionString(getOzzie().getTokenManager().getToken("mongodb")))
                .build();
        setMongoClient(MongoClients.create(settings));
        log.info("MongoDB Handler built!");
    }

    public void createUser(long userId){
        MongoDatabase db = null;
        MongoCollection<UserSchema> collection = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(USER_COLLECTION, UserSchema.class);
            UserSchema userSchema = new UserSchema(userId, getOzzie());
            collection.insertOne(userSchema);
            log.info("User Insert Successfully...");
        } catch (MongoException | ClassCastException e) {
            log.error("Exception occurred while insert **User** : " + e, e);
        }
    }

    public UserSchema retrieveUser(long userId){
        MongoDatabase db = null;
        MongoCollection<Document> collection = null;
        Document result = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(USER_COLLECTION);
            query = eq("userId", userId);
            result = collection.find(query).first();
            if(result == null){
                createUser(userId);
                return retrieveUser(userId);
            }
        } catch (MongoException | ClassCastException e) {
            log.error("Exception occurred while query **User** : " + e, e);
        }
        return new Gson().fromJson(result.toJson(), UserSchema.class);
    }

    public void updateUser(UserSchema userSchema){
        MongoDatabase db = null;
        MongoCollection<UserSchema> collection = null;
        Bson filter = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(USER_COLLECTION, UserSchema.class);
            filter = eq("userId", userSchema.getUserId());
            query = combine(set("userLocale", userSchema.getUserLocale()), set("customCommandPrefix", userSchema.getCustomCommandPrefix()), set("password", userSchema.getPassword()), currentDate("lastModified"));
            UpdateResult result = collection.updateOne(filter, query);
            log.info("User Update One Status : " + result.wasAcknowledged());
        } catch (MongoException e) {
            log.error("Exception occurred while update single user : " + e, e);
        }
    }

    public void deleteUser(long userId){
        MongoDatabase db = null;
        MongoCollection<UserSchema> collection = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(USER_COLLECTION, UserSchema.class);
            query = eq("userId", userId);
            DeleteResult result = collection.deleteOne(query);
            if (result.wasAcknowledged()) {
                log.info("User deleted successfully \nNo of User Deleted : " + result.getDeletedCount());
            }
        } catch (MongoException e) {
            log.error("Exception occurred while deleteUser : " + e, e);
        }
    }

    public void createServer(long serverId){
        MongoDatabase db = null;
        MongoCollection<ServerSchema> collection = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_COLLECTION, ServerSchema.class);
            ServerSchema serverSchema = new ServerSchema(serverId, getOzzie());
            collection.insertOne(serverSchema).wasAcknowledged();
            log.info("Server Insert Successfully...");
        } catch (MongoException | ClassCastException e) {
            log.error("Exception occurred while insert **Server** : " + e, e);
        }
    }

    public ServerSchema retrieveServer(long serverId){
        MongoDatabase db = null;
        MongoCollection<Document> collection = null;
        Document result = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_COLLECTION);
            query = eq("serverId", serverId);
            result = collection.find(query).first();
            if(result == null){
                createServer(serverId);
                return retrieveServer(serverId);
            }
        } catch (MongoException | ClassCastException e) {
            log.error("Exception occurred while query **Server** : " + e, e);
        }
        return new Gson().fromJson(result.toJson(), ServerSchema.class);
    }

    public void updateServer(ServerSchema serverSchema){
        MongoDatabase db = null;
        MongoCollection<ServerSchema> collection = null;
        Bson filter = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_COLLECTION, ServerSchema.class);
            filter = eq("serverId", serverSchema.getServerId());
            query = combine(set("ownerID", serverSchema.getOwnerID()), set("allowedCommandTextChannel", serverSchema.getAllowedCommandTextChannel()),
                    set("customCommandPrefix", serverSchema.getCustomCommandPrefix()), set("allowUserCustomCommandPrefix", serverSchema.isAllowUserCustomCommandPrefix()),
                    set("serverLocale", serverSchema.getServerLocale()), set("allowUserLocale", serverSchema.isAllowUserLocale()), currentDate("lastModified"));
            UpdateResult result = collection.updateOne(filter, query);
            log.info("Server Update One Status : " + result.wasAcknowledged());
        } catch (MongoException e) {
            log.error("Exception occurred while update server : " + e, e);
        }
    }

    public void deleteServer(long serverId){
        MongoDatabase db = null;
        MongoCollection<ServerSchema> collection = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_COLLECTION, ServerSchema.class);
            query = eq("serverId", serverId);
            DeleteResult result = collection.deleteOne(query);
            if (result.wasAcknowledged()) {
                log.info("Server deleted successfully \nNo of Server Deleted : " + result.getDeletedCount());
            }
        } catch (MongoException e) {
            log.error("Exception occurred while delete server : " + e, e);
        }
    }

    public void createServerUserPermission(long serverId, long userId){
        MongoDatabase db = null;
        MongoCollection<ServerUserPermissionSchema> collection = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_USER_PERMISSIONS_COLLECTION, ServerUserPermissionSchema.class);
            ServerUserPermissionSchema serverUserPermissionSchema = new ServerUserPermissionSchema(serverId, userId);
            collection.insertOne(serverUserPermissionSchema).wasAcknowledged();
            log.info("ServerUserPermission Insert Successfully...");
        } catch (MongoException | ClassCastException e) {
            log.error("Exception occurred while insert **ServerUserPermission** : " + e, e);
        }
    }

    public ServerUserPermissionSchema retrieveServerUserPermission(long serverId, long userId){
        MongoDatabase db = null;
        MongoCollection<Document> collection = null;
        Document result = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_USER_PERMISSIONS_COLLECTION);
            query = and(eq("serverId", serverId), eq("userId", userId));
            result = collection.find(query).first();
            if(result == null){
                createServerUserPermission(serverId, userId);
                return retrieveServerUserPermission(serverId, userId);
            }
        } catch (MongoException | ClassCastException e) {
            log.error("Exception occurred while query **ServerUserPermission** : " + e, e);
        }
        return new Gson().fromJson(result.toJson(), ServerUserPermissionSchema.class);
    }

    public void updateServerUserPermission(ServerUserPermissionSchema serverUserPermissionSchema){
        MongoDatabase db = null;
        MongoCollection<ServerSchema> collection = null;
        Bson filter = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_USER_PERMISSIONS_COLLECTION, ServerSchema.class);
            filter = and(eq("serverId", serverUserPermissionSchema.getServerId()), eq("userId", serverUserPermissionSchema.getUserId()));
            query = combine(set("permissions", serverUserPermissionSchema.getPermissions()), currentDate("lastModified"));
            UpdateResult result = collection.updateOne(filter, query);
            log.info("ServerUserPermission Update One Status : " + result.wasAcknowledged());
        } catch (MongoException e) {
            log.error("Exception occurred while update ServerUserPermission : " + e, e);
        }
    }

    public void deleteServerUserPermission(long serverId, long userId){
        MongoDatabase db = null;
        MongoCollection<ServerUserPermissionSchema> collection = null;
        Bson query = null;
        try {
            db = getMongoClient().getDatabase(DATABASE);
            collection = db.getCollection(SERVER_USER_PERMISSIONS_COLLECTION, ServerUserPermissionSchema.class);
            query = and(eq("serverId", serverId), eq("userId", userId));
            DeleteResult result = collection.deleteOne(query);
            if (result.wasAcknowledged()) {
                log.info("ServerUserPermission deleted successfully \nNo of ServerUserPermission Deleted : " + result.getDeletedCount());
            }
        } catch (MongoException e) {
            log.error("Exception occurred while delete ServerUserPermission : " + e, e);
        }
    }

    public void setMongoClient(MongoClient mongoClient){
        this.mongoClient = mongoClient;
    }

    public MongoClient getMongoClient(){
        return mongoClient;
    }

    private void setOzzie(Ozzie ozzie){
        this.ozzie = ozzie;
    }

    private Ozzie getOzzie(){
        return ozzie;
    }
}
