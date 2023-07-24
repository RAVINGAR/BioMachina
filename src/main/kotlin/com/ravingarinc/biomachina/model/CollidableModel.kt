package com.ravingarinc.biomachina.model

import com.ravingarinc.biomachina.data.CollisionBox

interface CollidableModel : VectorModel {
    val collisionBox: CollisionBox
    val isCollisionEnabled: Boolean
}