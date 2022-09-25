package com.example.picsingularcore.dao

import com.example.picsingularcore.pojo.Banner
import org.springframework.data.jpa.repository.JpaRepository

interface BannerRepository: JpaRepository<Banner, Long> {

}