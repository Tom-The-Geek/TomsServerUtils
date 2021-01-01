package me.geek.tom.serverutils.crashreports

import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import net.minecraft.util.crash.CrashReport
import java.io.File
import java.time.Instant

interface CrashReportHelper {
    fun handleCrashReport(report: CrashReport, saved: Boolean, file: File)

    class Impl(
            private val webhook: String,
            private val webhookName: String,
            private val webhookIcon: String,
            private val serverName: String
    ) : CrashReportHelper {
        override fun handleCrashReport(report: CrashReport, saved: Boolean, file: File) {
            WebhookClientBuilder(webhook).build().use { whClient ->

                val savedMessage = when (saved) {
                    true -> "the crash report has been saved as ${file.name}."
                    false -> "the crash report failed to save!"
                }

                val message = if (report.causeAsString.length > 1500)
                    report.causeAsString.substring(0 until 1497) + "..."
                else report.causeAsString

                val builder = WebhookMessageBuilder()
                        .addEmbeds(WebhookEmbedBuilder()
                                .setTitle(WebhookEmbed.EmbedTitle("$serverName crashed!", null))
                                .setDescription("$serverName appears to have crashed and $savedMessage\n" +
                                        "Message:```\n" +
                                        message +
                                        "```\n" +
                                        "The full report is attached.")
                                .setColor(0xFF0000)
                                .setTimestamp(Instant.now())
                                .build())
                        .setUsername(webhookName)
                        .setAvatarUrl(webhookIcon)

                if (saved) {
                    builder.addFile("crash-report.txt", file)
                } else {
                    builder.addFile("crash-report.txt", report.asString().byteInputStream())
                }

                whClient.send(builder.build()).join()
            }
        }
    }

    class Noop : CrashReportHelper { override fun handleCrashReport(report: CrashReport, saved: Boolean, file: File) { } }
}
