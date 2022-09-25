package com.example.picsingularcore.controller

import com.example.picsingularcore.common.constant.FilePathConstant
import com.example.picsingularcore.common.constant.SingularConstant
import com.example.picsingularcore.common.utils.DTOUtil.listToPageDTO
import com.example.picsingularcore.common.utils.DTOUtil.pagesToPagesDTO
import com.example.picsingularcore.dao.CategoryRepository
import com.example.picsingularcore.dao.SingularRepository
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.ImageUrl
import com.example.picsingularcore.pojo.Singular
import com.example.picsingularcore.pojo.SingularCategory
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.dto.SingularDTO
import com.example.picsingularcore.pojo.dto.PagesDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID
import javax.servlet.http.HttpServletResponse
import kotlin.math.ceil

@RestController
class SingularController {

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var singularRepository: SingularRepository

    @Autowired
    lateinit var singularCategoryRepository: CategoryRepository

    @Autowired
    lateinit var httpServletResponse: HttpServletResponse

    // create a singular, and return the singular entity with the id
    @PostMapping("/singular/create")
    fun createSingular(authentication: Authentication,@RequestBody singularDTO: SingularDTO): Singular {
        val user = userRepository.findByUsername(authentication.name)
        val singularCategoryList = mutableListOf<SingularCategory>()
        // if category is not null, add to singularCategoryList
        singularDTO.category.forEach { category ->
            val categoryFound = singularCategoryRepository.findByCategoryName(category)
            if (categoryFound == null) {
                singularCategoryList.add(SingularCategory(userId = user?.userId!!, categoryName = category))
            } else {
                singularCategoryList.add(categoryFound)
            }
        }
        if (singularDTO.imagesUrl.isEmpty()) {
            throw IllegalArgumentException("Image url is empty")
        }
        // add imageUrlList to singular
        val imageUrlList = mutableListOf<ImageUrl>()
        singularDTO.imagesUrl.forEach {
            imageUrlList.add(ImageUrl(imageUrl = it))
        }
        val singular = Singular(description = singularDTO.content, singularStatus = singularDTO.status, user = user, categoryList = singularCategoryList, imageList = imageUrlList, userId = user!!.userId!!)
        return singularRepository.save(singular)
    }

    // upload images and save in files, return the imageUrlList
    @PostMapping("/singular/upload")
    fun uploadImagesToSingular(authentication: Authentication,@RequestBody multipartFileList: List<MultipartFile>): List<String> {
        if (multipartFileList.isEmpty()) {
            throw IllegalArgumentException("Image list is empty")
        }
        val filePath = FilePathConstant.IMAGE_PATH + authentication.name + "/"
        File(filePath).mkdirs()
        val imageUrlList = mutableListOf<String>()
        multipartFileList.forEach {
            val fileName = UUID.randomUUID().toString() + "-" + it.originalFilename
            val file = File(filePath + fileName)
            // write multipartFile to file
            file.writeBytes(it.bytes)
            imageUrlList.add(fileName)
        }
        return imageUrlList
    }

    // show image of singular by url
    @GetMapping("/images/singular/{singularId}/{url}")
    fun showImage(@PathVariable(name = "singularId") singularId: Long,@PathVariable(name = "url") url: String) {
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        val path = FilePathConstant.IMAGE_PATH + singular.user!!.username + "/" + url
        val file = File(path)
        if (!file.exists()){
            throw IllegalArgumentException("Image not found")
        }
        httpServletResponse.contentType = "image/jpeg, image/jpg, image/png, image/gif, image/bmp, image/webp, image/svg+xml, image/x-icon, image/vnd.microsoft.icon"
        file.inputStream().copyTo(httpServletResponse.outputStream)
    }

    // get saved singular list
    @GetMapping("/singular/list/saved/{page}/{size}")
    fun getSavedSingularList(
        authentication: Authentication,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular> {
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val singularPages = singularRepository.findAll(
            (Specification { root, _, cb ->
                val user = userRepository.findByUsername(authentication.name)
                cb.and(
                    cb.equal(root.get<Singular>("singularStatus"), SingularConstant.SAVED.name),
                    cb.equal(root.get<User>("user"), user)
                )
            }),
            PageRequest.of(page - 1, size, Sort.by("pushDate").descending())
        )
        return pagesToPagesDTO(singularPages)
    }

    // get shared singular list of current user
    @GetMapping("/singular/list/shared/{page}/{size}")
    fun getSharedSingularList(
        authentication: Authentication,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular> {
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val singularPages = singularRepository.findAll(
            (Specification { root, _, cb ->
                val user = userRepository.findByUsername(authentication.name)
                cb.and(
                    cb.equal(root.get<Singular>("singularStatus"), SingularConstant.SHARED.name),
                    cb.equal(root.get<User>("user"), user),
                )
            }),
            PageRequest.of(page - 1, size, Sort.by("pushDate").descending())
        )
        return pagesToPagesDTO(singularPages)
    }

    // get all shared singular list
    @GetMapping("/singular/list/all/{page}/{size}")
    fun getAllSingularList(
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular> {
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val singularPages = singularRepository.findAll(
            (Specification { root, _, cb ->
                cb.equal(root.get<Singular>("singularStatus"), SingularConstant.SHARED.name)
            }),
            PageRequest.of(page - 1, size, Sort.by("pushDate").descending())
        )
        return pagesToPagesDTO(singularPages)
    }

    // change singular status to saved
    @PostMapping("/singular/change/saved/{singularId}")
    fun changeSingularStatusToSaved(authentication: Authentication,@PathVariable singularId: Long): Singular {
        val singular = singularRepository.findById(singularId).get()
        singular.singularStatus = SingularConstant.SAVED.name
        return singularRepository.save(singular)
    }

    // change singular status to saved
    @PostMapping("/singular/change/shared/{singularId}")
    fun changeSingularStatusToShared(authentication: Authentication,@PathVariable singularId: Long): Singular {
        val singular = singularRepository.findById(singularId).get()
        singular.singularStatus = SingularConstant.SHARED.name
        return singularRepository.save(singular)
    }

    // delete singular
    @PostMapping("/singular/delete/{singularId}")
    fun deleteSingular(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (!singularRepository.existsById(singularId)) {
            throw IllegalArgumentException("Singular not found")
        }
        if (singularRepository.findById(singularId).get().user?.username != authentication.name) {
            throw IllegalArgumentException("You are not the owner of this singular")
        }
        val singular = singularRepository.findById(singularId).get()
        singularRepository.deleteById(singularId)
        return singular
    }

    // get singular by id
    @GetMapping("/singular/get/{singularId}")
    fun getSingularById(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (singularRepository.existsById(singularId)) {
            return singularRepository.findById(singularId).get()
        }
        throw IllegalArgumentException("Singular not found")
    }

    // add favorite singular to current user
    @PostMapping("/singular/favorite/{singularId}")
    fun addFavoriteSingular(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        val user = userRepository.findByUsername(authentication.name)!!
        if (user.favoriteList.contains(singular)) {
            throw IllegalArgumentException("Singular already in favorite list")
        }
        user.favoriteList.add(singular)
        userRepository.save(user)
        return singularRepository.save(singular)
    }

    // remove favorite singular from current user
    @PostMapping("/singular/unfavorite/{singularId}")
    fun removeFavoriteSingular(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        val user = userRepository.findByUsername(authentication.name)!!
        if (!user.favoriteList.contains(singular)) {
            throw IllegalArgumentException("Singular not in favorite list")
        }
        user.favoriteList.remove(singular)
        userRepository.save(user)
        return singularRepository.save(singular)
    }

    // check if user has added the singular to favorite list
    @GetMapping("/singular/hasFavorite/{singularId}")
    fun checkHasAddToFavoriteList(authentication: Authentication, @PathVariable singularId: Long): Boolean{
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        val owner = userRepository.findByUsername(authentication.name)!!
        if (owner.favoriteList.contains(singular)) return true
        return false
    }


    // get favorite singular list of current user
    @GetMapping("/singular/favorite/list/{page}/{size}")
    fun getFavoriteSingularList(
        authentication: Authentication,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular> {
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val user = userRepository.findByUsername(authentication.name)
        val res = user?.favoriteList ?: listOf()
        if ((page - 1) * size > res.size) {
            throw IllegalArgumentException("Page not found")
        }
        return listToPageDTO(res,page,size)
    }

    // add singular like count
    @PostMapping("/singular/like/{singularId}")
    fun addSingularLikeCount(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        singular.likeCount += 1
        return singularRepository.save(singular)
    }

    // remove singular like count
    @PostMapping("/singular/unlike/{singularId}")
    fun removeSingularLikeCount(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        singular.likeCount -= 1
        return singularRepository.save(singular)
    }

    // add subscribe user to current user
    @PostMapping("/singular/subscribe/{userId}")
    fun addSubscribe(authentication: Authentication,@PathVariable userId: Long): String{
        if (!userRepository.existsById(userId)){
            throw IllegalArgumentException("User not found")
        }
        val user = userRepository.findById(userId).get()
        val owner = userRepository.findByUsername(authentication.name)!!
        if (user.userId == owner.userId) throw IllegalArgumentException("Can't subscribe yourself")
        if (owner.subscriptionList.contains(user)) throw IllegalArgumentException("You cannot re-subscribe")
        owner.subscriptionList.add(user)
        userRepository.save(owner)
        return "Subscribe Success"
    }

    // remove subscribe user from current user
    @PostMapping("/singular/unsubscribe/{userId}")
    fun removeSubscribe(authentication: Authentication,@PathVariable userId: Long): String{
        if (!userRepository.existsById(userId)){
            throw IllegalArgumentException("User not found")
        }
        val user = userRepository.findById(userId).get()
        val owner = userRepository.findByUsername(authentication.name)
        owner!!.subscriptionList.remove(user)
        userRepository.save(owner)
        return "Unsubscribe Success"
    }

    // check if user has been subscribed
    @GetMapping("/singular/hasSubscribed/{userId}")
    fun checkHasSubscribedUser(authentication: Authentication, @PathVariable userId: Long): Boolean{
        if (!userRepository.existsById(userId)){
            throw IllegalArgumentException("User not found")
        }
        val user = userRepository.findById(userId).get()
        val owner = userRepository.findByUsername(authentication.name)!!
        if (owner.subscriptionList.contains(user)) return true
        return false
    }

    // get subscribe user list of current user
    @GetMapping("/singular/subscribe/list/{page}/{size}")
    fun getSubscribeUserList(
        authentication: Authentication,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<User>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val owner = userRepository.findByUsername(authentication.name)
        val res = owner?.subscriptionList ?: listOf()
        if ((page - 1) * size > res.size) {
            throw IllegalArgumentException("Page not found")
        }
        return listToPageDTO(res,page,size)
    }

    @GetMapping("/singular/subscribe/{userId}/{page}/{size}")
    fun getSubscribeSingularListByUserId(
        authentication: Authentication,
        @PathVariable(name = "userId") userId: Long,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val owner = userRepository.findByUsername(authentication.name)
        var res: List<Singular>
        owner!!.subscriptionList.forEach {user ->
            if (user.userId == userId){
                singularRepository.findBySingularStatusAndUser(SingularConstant.SHARED.name, user).let { res = it }
                return listToPageDTO(res,page,size)
            }
        }
        throw IllegalArgumentException("You have not subscribed this user")
    }

    // get singular list of subscribe users
    @GetMapping("/singular/subscribe/all/{page}/{size}")
    fun getSubscribeSingularList(
        authentication: Authentication,
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "size") size: Int
    ): PagesDTO<Singular>{
        if (page <= 0 || size <= 0)  throw Exception("Page or size is Invalid")
        val owner = userRepository.findByUsername(authentication.name)
        val singularList = mutableListOf<Singular>()
        owner!!.subscriptionList.forEach { user ->
            singularRepository.findBySingularStatusAndUser(SingularConstant.SAVED.name, user).forEach {
                singularList.add(it)
            }
        }
        if ((page - 1) * size > singularList.size) {
            throw IllegalArgumentException("Page not found")
        }
        return listToPageDTO(singularList,page,size)
    }

    // plus read count of singular
    @PostMapping("/singular/read/{singularId}")
    fun updateReadCount(authentication: Authentication,@PathVariable singularId: Long): Singular {
        if (!singularRepository.existsById(singularId)){
            throw IllegalArgumentException("Singular not found")
        }
        val singular = singularRepository.findById(singularId).get()
        singular.readCount += 1
        return singularRepository.save(singular)
    }
}