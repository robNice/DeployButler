package de.robnice.deploybutler.notify

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object DeployNotifications {
    private fun notify(project: Project, type: NotificationType, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("DeployButler")
            .createNotification(content, type)
            .notify(project)
    }

    fun info(project: Project, content: String) = notify(project, NotificationType.INFORMATION, content)
    fun warning(project: Project, content: String) = notify(project, NotificationType.WARNING, content)
    fun error(project: Project, content: String) = notify(project, NotificationType.ERROR, content)
}
