package com.phaserush.gallerybot.data.exceptions

import discord4j.core.`object`.util.Permission
import java.lang.RuntimeException

class BotPermissionException(permissions: Set<Permission>): RuntimeException(permissions.joinToString())