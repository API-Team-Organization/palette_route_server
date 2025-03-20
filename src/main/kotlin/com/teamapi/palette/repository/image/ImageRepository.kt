package com.teamapi.palette.repository.image

import com.teamapi.palette.entity.image.Image
import com.teamapi.palette.repository.mongo.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : MongoRepository<Image>
