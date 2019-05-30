package nightcrawler.database.neo4j.service

/**
 * Created by andrea on 22/07/16.
 */
enum class FacebookPostType (value : String){
    PHOTO("PHOTO"), STORY("STORY"), POST("POST"), VIDEO("VIDEO"), COMMENT("COMMENT"), NOTE("NOTE");
}