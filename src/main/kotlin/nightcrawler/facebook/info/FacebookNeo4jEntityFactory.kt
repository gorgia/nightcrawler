package nightcrawler.facebook.info

import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.neo4j.models.*
import nightcrawler.database.neo4j.repositories.*
import nightcrawler.database.neo4j.splitLoadersAndSavers.FacebookAccountSplitLoader
import nightcrawler.database.neo4j.splitLoadersAndSavers.FacebookSplitSaver
import nightcrawler.utils.log
import org.jsoup.nodes.Element
import org.neo4j.ogm.exception.CypherException
import org.neo4j.ogm.session.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataRetrievalFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by andrea on 13/12/16.
 */

@Component
class FacebookNeo4jEntityFactory {

    @Autowired
    lateinit var facebookAccountRepo: FacebookAccountRepository
    @Autowired
    lateinit var facebookPageRepo: FacebookPageRepository
    @Autowired
    lateinit var facebookBusinessPageRepo: FacebookBusinessPageRepository
    @Autowired
    lateinit var facebookPostRepo: FacebookPostRepository
    @Autowired
    lateinit var facebookEntityRepo: FacebookEntityRepository
    @Autowired
    lateinit var facebookGroupRepo: FacebookGroupRepository

    @Autowired
    lateinit var facebookAccountSplitLoader: FacebookAccountSplitLoader

    @Autowired
    lateinit var facebookSplitSaver: FacebookSplitSaver

    @Autowired
    lateinit var sessionFactory: SessionFactory


    fun <T : FacebookEntity> getByFacebookId(facebookId: String, clazz: Class<T>, completeLoad: Boolean = false): T? {
        try {
            var facebookEntity: T?
            when (clazz) {
                FacebookAccount::class.java -> {
                    facebookEntity = facebookAccountRepo.findByFacebookId(facebookId) as T?
                    if (completeLoad && facebookEntity != null) facebookEntity = facebookAccountSplitLoader.load(facebookEntity as FacebookAccount) as T
                }
                FacebookGroup::class.java -> {
                    facebookEntity = facebookGroupRepo.findByFacebookId(facebookId) as T?
                    if (completeLoad && facebookEntity != null) facebookEntity = facebookGroupRepo.findById(facebookEntity.id, 1).get() as T
                    //if (completeLoad && facebookEntity != null) facebookEntity = facebookGroupRepo.findOne(facebookEntity.id, 1) as T
                }
                FacebookPost::class.java -> {
                    facebookEntity = facebookPostRepo.findByFacebookId(facebookId) as T?
                    if (completeLoad && facebookEntity != null) facebookEntity = facebookPostRepo.findById(facebookEntity.id, 1).get() as T
                    //if (completeLoad && facebookEntity != null) facebookEntity = facebookPostRepo.findOne(facebookEntity.id, 1) as T
                }
                FacebookBusinessPage::class.java -> {
                    facebookEntity = facebookBusinessPageRepo.findByFacebookId(facebookId) as T?
                    if (completeLoad && facebookEntity != null) facebookEntity = facebookBusinessPageRepo.findById(facebookEntity.id, 1).get() as T
                    //if (completeLoad && facebookEntity != null) facebookEntity = facebookBusinessPageRepo.findOne(facebookEntity.id, 1) as T
                }
                FacebookPage::class.java -> {
                    facebookEntity = facebookPageRepo.findByFacebookId(facebookId) as T?
                    if (completeLoad && facebookEntity != null) facebookEntity = facebookPageRepo.findById(facebookEntity.id, 1).get() as T
                    //if (completeLoad && facebookEntity != null) facebookEntity = facebookPageRepo.findOne(facebookEntity.id, 1) as T
                }
                else -> {
                    facebookEntity = facebookEntityRepo.findByFacebookId(facebookId) as T?
                    if (completeLoad && facebookEntity != null) facebookEntity = facebookEntityRepo.findById(facebookEntity.id, 1).get() as T
                    //if (completeLoad && facebookEntity != null) facebookEntity = facebookEntityRepo.findOne(facebookEntity.id, 1) as T
                }
            }
            return facebookEntity
        } catch (e: Exception) {
            when (e) {
                is NoSuchElementException, is DataRetrievalFailureException -> log().debug("Node of type:$clazz with facebookId=$facebookId not found")
                else -> log().error("Error during retrieving of Node of type:$clazz with with facebookId=$facebookId", e)
            }
        }
        return null
    }


    fun <T : FacebookPage> getOrCreateFacebookPageFromElement(wdatagt: Element, clazz: Class<T> = FacebookPage::class.java as Class<T>): T? {
        val facebookId = extractFacebookIdFromElement(wdatagt) ?: return null
        var facebookEntity: T? = this.getByFacebookId(facebookId, clazz)
        if (facebookEntity == null) {
            facebookEntity = createFacebookPageFromElement(wdatagt, clazz)
        }
        return facebookEntity
    }

    fun <T : FacebookPage> getOrCreateFacebookPageFromElementsWithFacebookId(clazz: Class<T> = FacebookPage::class.java as Class<T>, hashMap: LinkedHashMap<String, Element>): Set<T> {
        val facebookEntities = HashSet<T>()
        if (!hashMap.isEmpty())
            hashMap.forEach {
                val facebookEntityToAdd: T? = facebookPageRepo.findByFacebookId(it.key) as T?
                        ?: createFacebookPageFromSearchElement(it.value)
                if (facebookEntityToAdd != null)
                    facebookEntities.add(facebookEntityToAdd)
                else log().warn("Impossible to create facebookEntity from Element:\n ${it.value}")
            }
        return facebookEntities
    }

    fun <T : FacebookPage> getOrCreateFacebookPageFromElements(clazz: Class<T> = FacebookPage::class.java as Class<T>, elements: Collection<Element>): Set<T> {

        if (elements.isEmpty()) return HashSet()
        val facebookIdsMap = HashMap<String, Element>()
        elements.forEach { element ->
            val facebookId = extractFacebookIdFromElement(element)
            if (facebookId != null) facebookIdsMap.put(facebookId, element)
            else
                log().error("Impossible to extract facebookId from element:\n ${element.html()}")
        }
        val facebookEntities = facebookPageRepo.findByFacebookIdIn(facebookIdsMap.keys.toList()).toMutableSet()
        val facebookEntitiesId: MutableSet<String> = HashSet()
        facebookEntities.mapTo(facebookEntitiesId) { facebookEntity -> facebookEntity.facebookId!! }
        val facebookIdsSet: MutableSet<String> = HashSet(facebookIdsMap.keys)
        facebookIdsSet.removeAll(facebookEntitiesId)
        log().debug("Entities found in page: ${facebookIdsMap.keys.count()} | entities found in db: ${facebookEntitiesId.count()} | new entities = ${facebookIdsSet.count()}")
        facebookIdsSet.forEach { facebookId ->
            val facebookName = extractFacebookNameFromElement(facebookIdsMap[facebookId]!!)
            var facebookEntityToAdd: T? = null
            if (facebookName != null) {
                facebookEntityToAdd = facebookPageRepo.findByFacebookName(facebookName) as T?
            }
            if (facebookEntityToAdd == null)
                facebookEntityToAdd = createFacebookPageFromElement(facebookIdsMap[facebookId]!!)
            if (facebookEntityToAdd != null) facebookEntities.add(facebookEntityToAdd)
        }
        return facebookEntities.toSet() as Set<T>
    }

    fun saveFacebookAccount(f: FacebookPage, level: Int = 1): FacebookPage {
        //if (level == 1) return facebookSplitSaver.save(f) as FacebookPage // removed: try to see if new version can works also without splitted saver
        return synchronized(facebookPageRepo) { facebookPageRepo.save(f, level) }
    }

    fun <T : FacebookPage> createFacebookPageFromSearchElement(panel: Element, clazz: Class<T> = FacebookPage::class.java as Class<T>): T? {
        val facebookId = extractFacebookIdFromSearchElement(panel) ?: return null
        val hrefElement: Element = panel.getElementsByClass("_32mo").first() ?: return null
        var facebookPage: T? = null
        try {
            facebookPage = clazz.newInstance()
            facebookPage.facebookId = facebookId
            facebookPage.url = baseUrlFromHref(hrefElement.getElementsByAttributeValueStarting("href", "https://www.facebook.com").attr("href"))
            facebookPage.facebookName = extractFacebookNameFromHref(facebookPage.url)?.toLowerCase()
            facebookPage.name = hrefElement.text()
            facebookPage.profilePic = panel.getElementsByTag("img").first()?.attr("src")
            facebookPage = facebookPageRepo.save(facebookPage, 0)
        } catch (cv: Exception) {
            log().warn("momentary remediation for facebookName = ${facebookPage?.facebookName} . delete node and recreate")
            val entityToBeDeleted = facebookPageRepo.findByFacebookName(facebookPage!!.facebookName!!)
            if(entityToBeDeleted!=null)
                facebookPageRepo.deleteById(entityToBeDeleted.id!!)
            //facebookPageRepo.delete(entityToBeDeleted.id)
            createFacebookPageFromSearchElement(panel, clazz)
            /*
            log().warn("Cypher validation exception during saveFacebookAccount of FacebookPage name:${facebookPage?.facebookName} | facebookId: ${facebookPage?.facebookId} | type $clazz", cv)
            val facebookEntity = getByFacebookId(facebookId, FacebookEntity::class.java, completeLoad = false)
            if (facebookEntity != null) {
                facebookPage = changeFacebookPageType(facebookEntity, clazz)
                log().info("facebookEntity was of wrong type: oldType = ${facebookEntity::class.java} | newType = ${facebookPage::class.java} ")
            }*/
        }
        return facebookPage
    }


    private fun <T : FacebookPage> createFacebookPageFromElement(panel: Element, clazz: Class<T> = FacebookPage::class.java as Class<T>): T? {
        val facebookId = extractFacebookIdFromElement(panel) ?: return null
        val wdatagt: Element? = panel.getElementsByAttributeValueStarting("data-hovercard", "/ajax/hovercard").first()
        var facebookPage: T? = null
        try {
            facebookPage = clazz.newInstance()
            facebookPage.facebookId = facebookId
            facebookPage.url = baseUrlFromHref(panel.getElementsByAttributeValueStarting("href", "https://www.facebook.com").attr("href"))
            facebookPage.facebookName = extractFacebookNameFromHref(facebookPage.url)?.toLowerCase()
            facebookPage.name = wdatagt?.text()
            facebookPage.profilePic = panel.getElementsByTag("img").first()?.attr("src")
            facebookPage = facebookPageRepo.save(facebookPage, 0)
        } catch (cv: CypherException) {
            log().warn("Cypher validation exception during saveFacebookAccount of FacebookPage name:${facebookPage?.facebookName} | facebookId: ${facebookPage?.facebookId} | type $clazz", cv)
            val facebookEntity = getByFacebookId(facebookId, FacebookEntity::class.java, completeLoad = false)
            if (facebookEntity != null) {
                facebookPage = changeFacebookPageType(facebookEntity, clazz)
                log().info("facebookEntity was of wrong type: oldType = ${facebookEntity::class.java} | newType = ${facebookPage::class.java} ")
            }
        } catch (e: Exception) {
            log().error("Error during creation of FacebookEntity from subElement ${panel.toString()}", e)
        }
        return facebookPage
    }


    fun <T : FacebookPage> changeFacebookPageType(fe: FacebookEntity, clazz: Class<T>): T {
        if (fe.javaClass == clazz) return fe as T
        if (fe.id != null) {
            removeLabel(fe.id!!, fe.javaClass.simpleName)
            addLabel(fe.id!!, FacebookEntity::class.java.simpleName)
            if (clazz == FacebookAccount::class) addLabel(fe.id!!, FacebookPage::class.java.simpleName!!)
            addLabel(fe.id!!, clazz.simpleName)
            //neo4jTemplate.save(fe, 0)
            return facebookPageRepo.findById(fe.id!!, 0).get() as T
            //return facebookPageRepo.findOne(fe.id!!, 0) as T
        } else { // the facebookEntity has not already been saved
            val newfp = clazz.newInstance()
            newfp.facebookId = fe.facebookId
            newfp.url = fe.url
            newfp.doesExist = fe.doesExist
            return newfp
        }
    }

    @Transactional
    fun addLabel(nodeId: Long, labelName: String) {
        val session = sessionFactory.openSession()
        val transaction = session.beginTransaction()
        session.query("MATCH (a) WHERE id(a) = $nodeId SET a:$labelName", HashMap<String, Any?>())
        transaction.commit()
        transaction.close()
    }

    @Transactional
    fun removeLabel(nodeId: Long, labelName: String) {
        val session = sessionFactory.openSession()
        val transaction = session.beginTransaction()
        session.query("MATCH (a) WHERE id(a) = $nodeId REMOVE a:$labelName", HashMap<String, Any?>())
        transaction.commit()
        transaction.close()
    }

    fun <T : FacebookPage> getOrCreateFacebookPageFromTarget(target: FacebookTargetMongo, clazz: Class<T>): T {
        var facebookPage: T? = null
        if (target.neo4jNodeId != null) facebookPage = facebookPageRepo.findById(target.neo4jNodeId, 0).orElse(null) as T?
        if (target.neo4jNodeId != null && facebookPage == null) facebookPage = facebookBusinessPageRepo.findById(target.neo4jNodeId, 0).orElse(null) as T?

        //if (target.neo4jNodeId != null) facebookPage = facebookPageRepo.findOne(target.neo4jNodeId, 0) as T?
        //if (target.neo4jNodeId != null && facebookPage == null) facebookPage = facebookBusinessPageRepo.findOne(target.neo4jNodeId, 0) as T? //added because facebookPage index miss facebookBusinessPage (to be investigated)

        if (facebookPage == null) {
            facebookPage = facebookPageRepo.findByFacebookIdOrFacebookName(target.facebookIdentifier!!, target.facebookIdentifier!!) as T?
            facebookPage = if (facebookPage != null) {
                log().debug("FacebookPage with facebookId or facebookName = ${target.facebookIdentifier} found. Returning it.")
                facebookPageRepo.findById(facebookPage.id, 0).orElse(null) as T?
                //facebookPageRepo.findOne(facebookPage.id, 0) as T
            } else {
                log().info("FacebookPage with facebookId or facebookName = ${target.facebookIdentifier} not found. Returning a new account")
                createFacebookPageFromTarget(target, clazz)
            }
            target.neo4jNodeId = facebookPage!!.id
        }
        return facebookPage
    }

    fun <T : FacebookPage> createFacebookPageFromTarget(target: FacebookTargetMongo, clazz: Class<T>): T {
        var facebookEntity = clazz.newInstance()
        if (facebookEntity is FacebookPage) {
            facebookEntity.facebookName = target.facebookIdentifier?.toLowerCase()
            facebookEntity.url = baseUrlFromFacebookName(facebookEntity.facebookName!!.toLowerCase())
        }
        //facebookEntity = facebookEntityRepository.saveFacebookAccount(facebookEntity)
        return facebookEntity
    }

}
