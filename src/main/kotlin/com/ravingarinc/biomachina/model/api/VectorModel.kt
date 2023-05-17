package com.ravingarinc.biomachina.model.api

import org.joml.Quaternionf
import org.joml.Vector3f

interface VectorModel {
    val origin: Vector3f
    val leftRotation: Quaternionf
    val rightRotation: Quaternionf
    val scale: Vector3f
}