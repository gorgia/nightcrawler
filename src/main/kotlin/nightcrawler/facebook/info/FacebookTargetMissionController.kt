package nightcrawler.facebook.info

import nightcrawler.database.mongodb.models.FacebookTargetMongo
import nightcrawler.database.mongodb.models.FacebookTargetMongoMissionControl
import nightcrawler.database.neo4j.models.*
import nightcrawler.facebook.targetmanager.MongoTargetManager
import nightcrawler.utils.log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
class FacebookTargetMissionController {

    @Autowired
    lateinit var mongoTargetManager: MongoTargetManager


    fun missionControl(ft: FacebookTargetMongo, fp: FacebookPage, posts: Set<FacebookPost>) {
        val newTargets = LinkedHashSet<FacebookTargetMongo>()
        if (ft.facebookTargetMongoMissionControl == null || ft.facebookTargetMongoMissionControl?.depth ?: 0 < 1) return
        if (ft.facebookTargetMongoMissionControl!!.lastUpdate?.isBefore(LocalDateTime.now().minusDays(ft.facebookTargetMongoMissionControl!!.updateEveryDays)) != false) return

        val missionControl: FacebookTargetMongoMissionControl = ft.facebookTargetMongoMissionControl!!.copy()
        missionControl.depth = ft.facebookTargetMongoMissionControl!!.depth - 1
        missionControl.parentTargetId = ft.facebookTargetMongoMissionControl!!.parentTargetId ?: ft.facebookIdentifier
        when (fp) {
            is FacebookAccount -> {
                fp.friends.forEach {
                    newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                            ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                }
                fp.followers.forEach {
                    newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                            ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                }
                fp.following.forEach {
                    newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                            ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                }
                posts.forEach {
                    it.likes.forEach {
                        newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                                ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                    }
                    it.comments.forEach {
                        if (it.author != null) newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.author!!.facebookName
                                ?: it.author!!.facebookName!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = 1, missionControl = missionControl))
                    }
                }
            }
            is FacebookGroup -> {
                fp.members.forEach {
                    newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                            ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                }
                posts.forEach {
                    it.likes.forEach {
                        newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                                ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                    }
                    it.comments.forEach {
                        if (it.author != null)
                            newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.author!!.facebookName
                                    ?: it.author!!.facebookName!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = 1, missionControl = missionControl))
                    }
                }
            }
            is FacebookBusinessPage -> {
                fp.likes.forEach {
                    newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                            ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                }
                fp.likers.forEach {
                    newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                            ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                }
                posts.forEach {
                    it.likes.forEach {
                        newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.facebookName
                                ?: it.facebookId!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = ft.priority, missionControl = missionControl))
                    }
                    it.comments.forEach {
                        if (it.author != null)
                            newTargets.add(mongoTargetManager.getOrCreateTargetFromFacebookIdentifer(it.author!!.facebookName
                                    ?: it.author!!.facebookName!!, saveInDb = ft.facebookTargetMongoMissionControl!!.saveDerivedAsTarget, priority = 1, missionControl = missionControl))
                    }
                }
            }
        }
        log().info("New targets to be added in temp queue: $newTargets")
        mongoTargetManager.targetsToBeProcessedQueue.addAll(newTargets)
        ft.facebookTargetMongoMissionControl!!.lastUpdate = LocalDateTime.now()
        mongoTargetManager.facebookTargetRepository.save(ft)
    }
}