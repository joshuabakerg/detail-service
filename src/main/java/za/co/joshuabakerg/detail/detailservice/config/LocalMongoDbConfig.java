package za.co.joshuabakerg.detail.detailservice.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

@Profile("!prod")
@Configuration
public class LocalMongoDbConfig extends AbstractMongoConfiguration {


    @Override
    public MongoClient mongoClient() {
        MongoClientURI uri = new MongoClientURI("mongodb://localhost:27017/auth-service?retryWrites=true");
        return new MongoClient(uri);
    }


    @Override
    protected String getDatabaseName() {
        return "auth-service";
    }

}
