package com.example.picsingularcore.controller

import com.example.picsingularcore.common.constant.FilePathConstant
import com.example.picsingularcore.dao.BannerRepository
import com.example.picsingularcore.pojo.Banner
import org.apache.tomcat.util.net.openssl.ciphers.Authentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

@RestController
class BannerController {
    @Autowired
    lateinit var bannerRepository: BannerRepository

    @PostMapping("/admin/banner/upload")
    fun uploadBanner(@RequestBody multipartFileList: List<MultipartFile>): MutableList<String> {
        if (multipartFileList.isEmpty()) {
            throw IllegalArgumentException("Image list is empty")
        }
        val filePath = FilePathConstant.BANNER_PATH
        File(filePath).mkdirs()
        val bannerUrlList = mutableListOf<String>()
        multipartFileList.forEach {
            val fileName = UUID.randomUUID().toString() + "-" + it.originalFilename
            val file = File(filePath + fileName)
            // write multipartFile to file
            file.writeBytes(it.bytes)
            // 保存到数据库
            val banner = Banner(bannerUrl = fileName)
            bannerRepository.save(banner)
            bannerUrlList.add(fileName)
        }
        return bannerUrlList
    }

    // get size of the latest banner
    @GetMapping("/banner/list/{size}")
    fun getBannerList(@PathVariable("size") size: Int): MutableList<Banner> {
        val bannerUrlList = bannerRepository.findAll(Sort.by("pushDate").descending())
        if (size > bannerUrlList.size) return bannerUrlList
        return bannerUrlList.subList(0,size - 1)
    }
}