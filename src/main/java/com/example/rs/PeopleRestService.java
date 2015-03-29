package com.example.rs;

import com.example.model.Person;
import com.example.services.PeopleService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

@Path( "/people" )
public class PeopleRestService {
    @Inject private PeopleService peopleService;

    @Produces( { MediaType.APPLICATION_JSON } )
    @GET
    public Collection< Person > getPeople( @QueryParam( "page") @DefaultValue( "1" ) final int page ) {
        return peopleService.getPeople( page, 5 );
    }

    @Produces( { MediaType.APPLICATION_JSON } )
    @Path( "/{email}" )
    @GET
    public Person getPeople( @PathParam( "email" ) final String email ) {
        return peopleService.getByEmail( email );
    }


    @Produces( { MediaType.APPLICATION_JSON } )
    @Path("/test")
    @GET
    public String getTest( ) {
        // Since 2.10.0, uses MongoClient
        try {
            MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
            DB db = mongoClient.getDB( "test" );
            DBCollection coll = db.getCollection("users");
            DBCursor dbCursor = coll.find();
            while (dbCursor.hasNext()) {
                System.out.println(dbCursor.next());
            }
            Configuration configuration = new Configuration();

            // Set path to acoustic model.
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            // Set path to dictionary.
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            // Set language model.
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");

            StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("10001-90210-01803.wav").getFile());

            recognizer.startRecognition(new FileInputStream(file));
            SpeechResult result;
            recognizer.stopRecognition();

            while ((result = recognizer.getResult()) != null) {
                System.out.println(result.getHypothesis());
            }

        } catch (Exception e) {
            System.out.println("Exception = " + e.getMessage());
        }

        return "tested well and fine";
    }

    @Produces( { MediaType.APPLICATION_JSON  } )
    @Consumes( { MediaType.APPLICATION_JSON  } )
    @POST
    public Response addPerson( @Context final UriInfo uriInfo,
                               @RequestParam( "email" ) final String email,
                               @RequestParam( "firstName" ) final String firstName,
                               @RequestParam( "lastName" ) final String lastName ) {

        peopleService.addPerson( email, firstName, lastName );
        return Response.created( uriInfo.getRequestUriBuilder().path( email ).build() ).build();
    }

    @Produces( { MediaType.APPLICATION_JSON  } )
    @Path( "/{email}" )
    @PUT
    public Person updatePerson(
            @PathParam( "email" ) final String email,
            @FormParam( "firstName" ) final String firstName,
            @FormParam( "lastName" )  final String lastName ) {

        final Person person = peopleService.getByEmail( email );

        if( firstName != null ) {
            person.setFirstName( firstName );
        }

        if( lastName != null ) {
            person.setLastName( lastName );
        }

        return person;
    }

    @Path( "/{email}" )
    @DELETE
    public Response deletePerson( @PathParam( "email" ) final String email ) {
        peopleService.removePerson( email );
        return Response.ok().build();
    }

}
