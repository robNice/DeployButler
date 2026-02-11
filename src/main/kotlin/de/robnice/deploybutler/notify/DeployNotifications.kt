package de.robnice.deploybutler.notify

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object DeployNotifications {

    private const val GROUP_ID = "DeployButler"

    fun info(project: Project, message: String) {
        notify(project, message, NotificationType.INFORMATION)
    }

    fun warning(project: Project, message: String) {
        notify(project, message, NotificationType.WARNING)
    }

    fun error(project: Project, message: String) {
        notify(project, message, NotificationType.ERROR)
    }

    private fun notify(
        project: Project,
        message: String,
        type: NotificationType
    ) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(message, type)
            .notify(project)
    }
}
