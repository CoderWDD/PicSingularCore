package com.example.picsingularcore.controller

import com.example.picsingularcore.common.utils.DTOUtil.pagesToPagesDTO
import com.example.picsingularcore.dao.CommentRepository
import com.example.picsingularcore.dao.SecondCommentRepository
import com.example.picsingularcore.dao.SingularRepository
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.CommentLevelFirst
import com.example.picsingularcore.pojo.CommentLevelSecond
import com.example.picsingularcore.pojo.dto.CommentFirstDTO
import com.example.picsingularcore.pojo.dto.CommentSecondDTO
import com.example.picsingularcore.pojo.dto.PagesDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CommentController {
    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var singularRepository: SingularRepository

    @Autowired
    lateinit var commentRepository: CommentRepository

    @Autowired
    lateinit var secondCommentRepository: SecondCommentRepository

    // add a first comment to singular
    @PostMapping("/comment/first")
    fun addFirstComment(authentication: Authentication,@RequestBody commentFirstDTO: CommentFirstDTO): String {
        if (!singularRepository.existsById(commentFirstDTO.singularId)) {
            throw Exception("singular not found")
        }
        val user = userRepository.findByUsername(authentication.name)!!
        val commentLevelFirst = CommentLevelFirst(userId = user.userId!!, username = user.username, content = commentFirstDTO.content, likeCount = 0, singularId = commentFirstDTO.singularId)
        val singular = singularRepository.findById(commentFirstDTO.singularId).get()
        singular.commentLevelFirstList?.add(commentLevelFirst)
        singularRepository.save(singular)
        return "Add comment successfully"
    }

    // get all first comment of singular
    @GetMapping("/comment/first/{singularId}/{page}/{size}")
    fun getFirstCommentList(
        @PathVariable singularId: Long,
        @PathVariable page: Int,
        @PathVariable size: Int
    ): PagesDTO<CommentLevelFirst> {
        if (page <= 0 || size <= 0) {
            throw Exception("page or size is invalid")
        }
        if (!singularRepository.existsById(singularId)) {
            throw Exception("singular not found")
        }
        val commentPages = commentRepository.findAll(
            (Specification { root, _, cb ->
                cb.equal(root.get<Long>("singularId"), singularId)
            }),
            PageRequest.of(page - 1, size, Sort.by("likeCount").descending().and(Sort.by("createDate").descending()))
        )
        return pagesToPagesDTO(commentPages)
    }

    // add a second comment to first comment
    @PostMapping("/comment/second")
    fun addSecondComment(authentication: Authentication,@RequestBody commentSecondDTO: CommentSecondDTO): List<CommentLevelSecond> {
        if (!singularRepository.existsById(commentSecondDTO.singularId)) {
            throw Exception("singular not found")
        }
        if (!commentRepository.existsById(commentSecondDTO.parentCommentId)) {
            throw Exception("first comment not found")
        }
        if (!userRepository.existsById(commentSecondDTO.parentUserId)) {
            throw Exception("Parent user not found")
        }
        if (!userRepository.existsById(commentSecondDTO.replyUserId)) {
            throw Exception("Reply user not found")
        }
        // get comment first
        val commentLevelFirst = commentRepository.findById(commentSecondDTO.parentCommentId).get()
        if (commentLevelFirst.commentSecondList?.contains(CommentLevelSecond(
                commentSecondId = commentSecondDTO.replyCommentId,
                userId = commentSecondDTO.replyUserId,
                singularId = commentSecondDTO.singularId,
                likeCount = 0,
                content = ""
            )) == false
        ) {
            throw Exception("reply comment not found")
        }
        val user = userRepository.findByUsername(authentication.name)
        val replyUsername = userRepository.findById(commentSecondDTO.replyUserId).get().username
        val parentUsername = userRepository.findById(commentSecondDTO.parentUserId).get().username
        val commentLevelSecond = CommentLevelSecond(
            userId = user!!.userId!!,
            content = commentSecondDTO.content,
            likeCount = 0,
            parentCommentId = commentSecondDTO.parentCommentId,
            repliedCommentId = commentSecondDTO.replyCommentId,
            repliedUserId = commentSecondDTO.replyUserId,
            parentUserId = commentSecondDTO.parentUserId,
            repliedUserName = replyUsername,
            parentUserName = parentUsername,
            singularId = commentSecondDTO.singularId
        )
        commentLevelFirst.commentSecondList?.add(commentLevelSecond)
        commentRepository.save(commentLevelFirst)
        // return comment level second list
        return commentRepository.findById(commentSecondDTO.parentCommentId).get().commentSecondList!!
    }

    // get all second comment of first comment
    @GetMapping("/comment/second/{singularId}/{firstCommentId}/{page}/{size}")
    fun getSecondCommentList(
        @PathVariable singularId: Long,
        @PathVariable(name = "firstCommentId") parentCommentId: Long,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<CommentLevelSecond> {
        if (page <= 0 || size <= 0) throw Exception("page or size is invalid")
        if (!singularRepository.existsById(singularId)) throw Exception("singular not found")
        if (!commentRepository.existsById(parentCommentId)) throw Exception("first comment not found")
        val secondCommentPages =  secondCommentRepository.findAll(
            (Specification { root, _, cb ->
                cb.and(
                    cb.equal(root.get<Long>("singularId"), singularId),
                    cb.equal(root.get<Long>("parentCommentId"), parentCommentId)
                )
            }),
            PageRequest.of(page - 1, size, Sort.by("likeCount").descending().and(Sort.by("createDate").descending()))
        )
        return pagesToPagesDTO(secondCommentPages)
    }

    // plus first comment like count
    @PostMapping("/comment/first/like/{firstCommentId}")
    fun plusFirstCommentLikeCount(@PathVariable firstCommentId: Long): Int {
        if (!commentRepository.existsById(firstCommentId)) {
            throw Exception("first comment not found")
        }
        val commentLevelFirst = commentRepository.findById(firstCommentId).get()
        commentLevelFirst.likeCount += 1
        commentRepository.save(commentLevelFirst)
        return commentLevelFirst.likeCount
    }

    // sub first comment like count
    @PostMapping("/comment/first/unlike/{firstCommentId}")
    fun subFirstCommentLikeCount(@PathVariable firstCommentId: Long): Int {
        if (!commentRepository.existsById(firstCommentId)) {
            throw Exception("first comment not found")
        }
        val commentLevelFirst = commentRepository.findById(firstCommentId).get()
        commentLevelFirst.likeCount -= 1
        commentRepository.save(commentLevelFirst)
        return commentLevelFirst.likeCount
    }

    // plus second comment like count
    @PostMapping("/comment/second/like/{secondCommentId}")
    fun plusSecondCommentLikeCount(@PathVariable secondCommentId: Long): CommentLevelSecond {
        if (!secondCommentRepository.existsById(secondCommentId)) {
            throw Exception("second comment not found")
        }
        val secondComment = secondCommentRepository.findById(secondCommentId).get()
        secondComment.likeCount += 1
        secondCommentRepository.save(secondComment)
        return secondComment
    }

    // sub second comment like count
    @PostMapping("/comment/second/unlike/{secondCommentId}")
    fun subSecondCommentLikeCount(@PathVariable secondCommentId: Long): CommentLevelSecond {
        if (!secondCommentRepository.existsById(secondCommentId)) {
            throw Exception("second comment not found")
        }
        val secondComment = secondCommentRepository.findById(secondCommentId).get()
        secondComment.likeCount -= 1
        secondCommentRepository.save(secondComment)
        return secondComment
    }

}