package com.example.picsingularcore.controller

import com.example.picsingularcore.common.constant.FilePathConstant
import com.example.picsingularcore.common.constant.SingularConstant
import com.example.picsingularcore.dao.CategoryRepository
import com.example.picsingularcore.dao.SingularRepository
import com.example.picsingularcore.dao.UserRepository
import com.example.picsingularcore.pojo.ImageUrl
import com.example.picsingularcore.pojo.Singular
import com.example.picsingularcore.pojo.SingularCategory
import com.example.picsingularcore.pojo.User
import com.example.picsingularcore.pojo.dto.SingularDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.UUID
import javax.servlet.http.HttpServletResponse

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

    // upload a singular, and return the singular entity with the id
    @PostMapping("/singular/create")
    fun uploadSingular(authentication: Authentication,@RequestBody singularDTO: SingularDTO): Singular {
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
        val singular = Singular(description = singularDTO.content, singularStatus = singularDTO.status, user = user, categoryList = singularCategoryList, imageList = imageUrlList)
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
    @GetMapping("/singular/image/{singularId}/{url}")
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
    @GetMapping("/singular/list/saved")
    fun getSavedSingularList(authentication: Authentication): List<Singular> {
        return singularRepository.findBySingularStatusAndUser(SingularConstant.SAVED.name, userRepository.findByUsername(authentication.name))
    }

    // get shared singular list
    @GetMapping("/singular/list/shared")
    fun getSharedSingularList(authentication: Authentication): List<Singular> {
        return singularRepository.findBySingularStatusAndUser(SingularConstant.SHARED.name, userRepository.findByUsername(authentication.name))
    }

    // get all shared singular list
    @GetMapping("/singular/list/all")
    fun getAllSingularList(): List<Singular> {
        return singularRepository.findBySingularStatus(SingularConstant.SHARED.name)
    }

    // change singular status to shared
    @PostMapping("/singular/change/{singularId}")
    fun changeSingularStatus(authentication: Authentication,@PathVariable singularId: Long): Singular {
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
        singular.likeCount += 1
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
        singular.likeCount -= 1
        userRepository.save(user)
        return singularRepository.save(singular)
    }

    // get favorite singular list of current user
    @GetMapping("/singular/favorite/list")
    fun getFavoriteSingularList(authentication: Authentication): List<Singular> {
        val user = userRepository.findByUsername(authentication.name)
        return user?.favoriteList ?: mutableListOf()
    }

    // add subscribe user to current user
    @PostMapping("/singular/subscribe/{userId}")
    fun addSubscribe(authentication: Authentication,@PathVariable userId: Long){
        if (!userRepository.existsById(userId)){
            throw IllegalArgumentException("User not found")
        }
        val user = userRepository.findById(userId).get()
        val owner = userRepository.findByUsername(authentication.name)!!
        owner.subscriptionList.add(user)
        userRepository.save(owner)
    }

    // remove subscribe user from current user
    @PostMapping("/singular/unsubscribe/{userId}")
    fun removeSubscribe(authentication: Authentication,@PathVariable userId: Long){
        if (!userRepository.existsById(userId)){
            throw IllegalArgumentException("User not found")
        }
        val user = userRepository.findById(userId).get()
        val owner = userRepository.findByUsername(authentication.name)
        owner!!.subscriptionList.remove(user)
        userRepository.save(owner)
    }

    // get subscribe user list of current user
    @GetMapping("/singular/subscribe/list")
    fun getSubscribeList(authentication: Authentication): List<User>{
        val owner = userRepository.findByUsername(authentication.name)!!
        return owner.subscriptionList
    }

    // get singular list of subscribe user
    @GetMapping("/singular/subscribe")
    fun getSubscribeSingularList(authentication: Authentication): List<Singular>{
        val owner = userRepository.findByUsername(authentication.name)
        val singularList = mutableListOf<Singular>()
        owner!!.subscriptionList.forEach { user ->
            singularRepository.findBySingularStatusAndUser(SingularConstant.SAVED.name, user).forEach {
                singularList.add(it)
            }
        }
        return singularList
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